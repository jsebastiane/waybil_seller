package waybilmobile.company.waybil.view.customers

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.fragment_customer_details.*

import waybilmobile.company.waybil.R
import waybilmobile.company.waybil.util.CustomSupportMapFragment
import waybilmobile.company.waybil.util.formatCurrency
import waybilmobile.company.waybil.viewmodel.customers.CustomerDetailsViewModel
import java.text.SimpleDateFormat


class CustomerDetailsFragment : Fragment(), OnMapReadyCallback {

    private var customerTransactionsAdapter = CustomerTransactionsAdapter(arrayListOf())
    private lateinit var viewModel: CustomerDetailsViewModel
    private lateinit var mMap: GoogleMap
    private var customerId = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_customer_details, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        customerDetails_scrollView.requestDisallowInterceptTouchEvent(true)

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.customerDetails_map) as CustomSupportMapFragment
        mapFragment.getMapAsync(this)
        mapFragment.setListener(object: CustomSupportMapFragment.OnTouchListener {
            override fun onTouch() {
                customerDetails_scrollView.requestDisallowInterceptTouchEvent(true)
            }
        })

        viewModel = ViewModelProviders.of(this).get(CustomerDetailsViewModel::class.java)


        transactionsRecycler_CustomerDetails.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = customerTransactionsAdapter
        }


        arguments?.let {
            customerId = CustomerDetailsFragmentArgs.fromBundle(it).customerId
        }

        //Child fragment manager is used as we are operating in a fragment activity with a map fragment


        close_fragment_customerDetails.setOnClickListener {
            findNavController().navigateUp()
        }

        radioGroup_customerDetails.setOnCheckedChangeListener { _, checkedId ->
            if(checkedId == R.id.radioSummary_customerDetails){
                summary_customerDetails.visibility = View.VISIBLE
                transactions_CustomerDetails.visibility = View.GONE
            }else if(checkedId == R.id.radioTransactions_customerDetails){
                summary_customerDetails.visibility = View.GONE
                transactions_CustomerDetails.visibility = View.VISIBLE
            }
        }

        radioSummary_customerDetails.isChecked = true

        observeViewModels()
        viewModel.refresh(customerId)

    }


    private fun observeViewModels(){
        viewModel.focusedCustomer.observe(viewLifecycleOwner, Observer { focusedCustomer ->
            focusedCustomer?.let {
                customerName_Detailsfragment.text = focusedCustomer.customer.businessName
                customerDetails_phoneNumber.text = focusedCustomer.customer.businessPhoneNumber
                customerDetails_address.text = focusedCustomer.customer.businessAddress
                ordersMade_customerDetails.text = focusedCustomer.lifeTimeOrderQuantity.toString()
                moneySpent_customerDetails.text = formatCurrency(focusedCustomer.lifeTimeOrderValue)

                val formattedDate = SimpleDateFormat("dd/MM/yyyy").format(it.lastOrder!!.orderTimestampDelivered!!.toDate())
                lastOrder_customerDetails.text = formattedDate


                val longitude = focusedCustomer.customer.businessLocation?.longitude
                val latitude = focusedCustomer.customer.businessLocation?.latitude
                val customerLocation = LatLng(latitude!!, longitude!!)
                mMap.addMarker(MarkerOptions().position(customerLocation).title(focusedCustomer.customer.businessName))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(customerLocation, 15f))

            }
        })

        viewModel.loading.observe(viewLifecycleOwner, Observer { loading ->
            loading?.let {
                if(it){
                    progressBar_customerDetails.visibility = View.VISIBLE
                }else{
                    progressBar_customerDetails.visibility = View.GONE

                }
            }
        })

        viewModel.recentTransactions.observe(viewLifecycleOwner, Observer { recentTransactions ->
            recentTransactions?.let{
                customerTransactionsAdapter.updateRecentTransactions(it)
            }
        })


    }


    override fun onMapReady(googleMap: GoogleMap?) {
        if (googleMap != null) {
            mMap = googleMap
        }

    }


}
