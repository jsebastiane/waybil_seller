package waybilmobile.company.waybil.view.orders

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.fragment_order_details.*
import kotlinx.android.synthetic.main.order_pin_dialog.view.*

import waybilmobile.company.waybil.R
import waybilmobile.company.waybil.model.orders.Order
import waybilmobile.company.waybil.util.CustomSupportMapFragment
import waybilmobile.company.waybil.util.formatCurrency
import waybilmobile.company.waybil.viewmodel.orders.OrderDetailsViewModel
import java.text.SimpleDateFormat
import java.util.*


class OrderDetailsFragment : Fragment(), OnMapReadyCallback {

    private lateinit var viewModel : OrderDetailsViewModel
    private var orderItemsAdapter = OrderItemsAdapter(arrayListOf())
    private lateinit var mMap: GoogleMap
    private var orderId = ""



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_order_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        orderDetails_scrollView.requestDisallowInterceptTouchEvent(true)

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.orderDetails_map) as CustomSupportMapFragment
        mapFragment.getMapAsync(this)
        mapFragment.setListener(object: CustomSupportMapFragment.OnTouchListener {
            override fun onTouch() {
                orderDetails_scrollView.requestDisallowInterceptTouchEvent(true)
            }
        })

        arguments?.let {
            orderId = OrderDetailsFragmentArgs.fromBundle(it).orderId
        }

        close_fragment_orderDetails.setOnClickListener {
            findNavController().navigateUp()
        }

        viewModel = ViewModelProviders.of(this).get(OrderDetailsViewModel::class.java)

        order_details_recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = orderItemsAdapter
        }

        accept_order_button.setOnClickListener {
            confirmOrderPin()
        }

        decline_order_button.setOnClickListener {
            viewModel.declineOrder()
        }



        observeViewModels()
        viewModel.retrieveOrderItems(orderId)

    }

    private fun observeViewModels(){
        viewModel.focusedOrder.observe(viewLifecycleOwner, Observer { focusedOrder ->
            focusedOrder?.let {
                setFragmentData(focusedOrder)
                orderItemsAdapter.focusedSeller = it.seller!!.id
                orderItemsAdapter.submitOrderItems(focusedOrder.productsOrderList)
                if(focusedOrder.forDelivery == true){
                    deliveryFeeLayout_orderDets.visibility = View.VISIBLE
                    deliveryFee_orderDets.text = formatCurrency(focusedOrder.seller?.deliveryCost!!.toDouble())
                }
                if(focusedOrder.forDelivery == true && focusedOrder.orderStatus == 2){
                    dummyMap_frameLayout.visibility = View.VISIBLE
                    destination_info_button.visibility = View.VISIBLE
                }else{
                    dummyMap_frameLayout.visibility = View.GONE
                    destination_info_button.visibility = View.GONE
                }
            }
        })

        viewModel.updatingOrder.observe(viewLifecycleOwner, Observer { updatingOrder->
            updatingOrder?.let {
                if(updatingOrder == true){
                    progressBar_orderDetails.visibility = View.VISIBLE
                }else{
                    progressBar_orderDetails.visibility = View.GONE
                    findNavController().navigateUp()

                }
            }
        })

    }

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    fun setFragmentData(focusedOrder: Order){
        val latitude = focusedOrder.customer.businessLocation?.latitude
        val longitude = focusedOrder.customer.businessLocation?.longitude

        when(focusedOrder.orderStatus){
            1 -> upper_container_orderDets.setBackgroundResource(R.drawable.pending_bg)
            2 -> {upper_container_orderDets.setBackgroundResource(R.drawable.confirmed_bg)
            orderDetails_fragment_header.text = "#${focusedOrder.invoiceNumber}"
            accept_order_button.text = getString(R.string.deliver)}
            3 -> {upper_container_orderDets.setBackgroundResource(R.drawable.transaction_bg)
                orderDetails_fragment_header.text = "#${focusedOrder.invoiceNumber}" }
            9 -> upper_container_orderDets.setBackgroundResource(R.drawable.rejected_bg)
        }
        val orderDate = focusedOrder.orderTimestampReceived?.toDate()
        orderDate?.let {
            val formattedDate = SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(it)
            time_orderContextDets.text = formattedDate

        }

        val deliveryTrue = getString(R.string.delivery)
        val deliveryFalse = getString(R.string.for_pickup)


        //Setting total and delivery information
        val price = formatCurrency(focusedOrder.orderTotal)
        if (focusedOrder.forDelivery == true){
            delivery_details_orderContextDets.text = deliveryTrue
            if(focusedOrder.orderTimestampConfirmed != null && focusedOrder.orderStatus == 2){
                estimatedDelivery_layout.visibility = View.VISIBLE
                //if timestamp confirmed is not null than orderDate should not be null
                val estimatedDeliveryTime = getEstimatedDeliveryTime(focusedOrder.orderTimestampConfirmed.toDate(), focusedOrder.seller!!.deliveryTime)
                estimatedDelivery_orderContextDets.text = estimatedDeliveryTime
            }else{
                estimatedDelivery_layout.visibility = View.GONE
            }
        }else{
            delivery_details_orderContextDets.text = deliveryFalse
        }
        distributor_details_orderContextDets.text = focusedOrder.seller?.userName
        client_phoneNumber.text = focusedOrder.customer.businessPhoneNumber
        store_name_orderContextDets.text = focusedOrder.customer.businessName
        address_orderContextDets.text = focusedOrder.customer.businessAddress
        total_order_viewDets.text = price
        linearLayout_orderResponse.visibility = if(focusedOrder.orderStatus == 3 || focusedOrder.orderStatus == 9) View.GONE else View.VISIBLE


        //Adding google route intent and button OnClickListener
        destination_info_button.setOnClickListener {
            println("$latitude, $longitude")

//            val gmmIntentUri = Uri.parse("geo:0,0?q=$latitude, $longitude")
//            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
//            mapIntent.setPackage("com.google.android.apps.maps")
//            startActivity(mapIntent)

//            Option 2
            val gmmIntentUri =
                Uri.parse("google.navigation:q=$latitude,$longitude")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            startActivity(mapIntent)
        }

        val customerLocation = LatLng(latitude!!, longitude!!)
        mMap.addMarker(MarkerOptions().position(customerLocation).title(focusedOrder.customer.businessName))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(customerLocation, 15f))

        setDialButton(focusedOrder.customer.businessPhoneNumber)
    }






    private fun confirmOrderPin(){
        println("CLICK")
        val currentOrder = viewModel.focusedOrder.value
        val pinDialogView = LayoutInflater.from(activity).inflate(R.layout.order_pin_dialog, null)
        activity?.let {
            println("activity")
            val builder = AlertDialog.Builder(it)
            builder.setView(pinDialogView)
            val dialog = builder.create()

            pinDialogView.ok_pinDialog.setOnClickListener {
                val pin = pinDialogView.orderPin_editText.text.toString()
                if(pin == currentOrder!!.orderPin){
                    Toast.makeText(activity, R.string.success, Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    viewModel.updateOrderStatus()

                }else{
                    Toast.makeText(activity, R.string.incorrect_pin, Toast.LENGTH_SHORT).show()
                }
            }

            pinDialogView.cancel_pinDialog.setOnClickListener {
                dialog.dismiss()
            }

            if(currentOrder?.orderStatus == 2){
                dialog.show()

            }else{
                viewModel.updateOrderStatus()
            }

        }

    }

    private fun getEstimatedDeliveryTime(dateConfirmed: Date, daysToDeliver: Int): String{
        val cal = Calendar.getInstance()
        cal.time = dateConfirmed
        cal.add(Calendar.DATE, 1)
        val newDate = cal.time

        return SimpleDateFormat("dd/MM/yyyy").format(newDate)
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        if (googleMap != null) {
            mMap = googleMap
        }
    }

    private fun setDialButton(number: String){
        client_phoneNumber.setOnClickListener {
            activity?.let {
                try {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel: $number"))
                    it.startActivity(intent)
                }catch (e: Exception){
                    Log.d("ACTION_DIAL", e.toString())
                }

            }
        }
    }


}


