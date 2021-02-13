package saymobile.company.saytech.view.orders

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.fragment_order_details.*
import kotlinx.android.synthetic.main.item_order.*
import kotlinx.android.synthetic.main.item_order.view.*
import kotlinx.android.synthetic.main.item_order.view.address_order

import saymobile.company.saytech.R
import saymobile.company.saytech.model.orders.Order
import saymobile.company.saytech.util.formatCurrency
import saymobile.company.saytech.viewmodel.orders.OrderDetailsViewModel
import saymobile.company.saytech.viewmodel.orders.OrdersViewModel
import java.text.SimpleDateFormat


class OrderDetailsFragment : Fragment() {

    private lateinit var viewModel : OrderDetailsViewModel
    private var orderItemsAdapter = OrderItemsAdapter(arrayListOf())
    private var invoiceId = ""


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_order_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            invoiceId = OrderDetailsFragmentArgs.fromBundle(it).orderId
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
            viewModel.updateOrderStatus()
        }

        decline_order_button.setOnClickListener {
            viewModel.declineOrder()
        }



        observeViewModels()
        viewModel.retrieveOrderItems(invoiceId)

    }

    fun observeViewModels(){
        viewModel.focusedOrder.observe(viewLifecycleOwner, Observer { focusedOrder ->
            focusedOrder?.let {
                setBannerData(focusedOrder)
                orderItemsAdapter.submitOrderItems(focusedOrder.productsOrderList!!)
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

    @SuppressLint("SimpleDateFormat")
    fun setBannerData(focusedOrder: Order){
        when(focusedOrder.orderStatus){
            1 -> upper_container_orderDets.setBackgroundResource(R.drawable.pending_bg)
            2 -> upper_container_orderDets.setBackgroundResource(R.drawable.confirmed_bg)
            3 -> upper_container_orderDets.setBackgroundResource(R.drawable.transaction_bg)
            9 -> upper_container_orderDets.setBackgroundResource(R.drawable.rejected_bg)
        }
        val orderDate = focusedOrder.orderTimestampReceived?.toDate()
        orderDate?.let {
            val formattedDate = SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(it)
            time_orderContextDets.text = formattedDate

        }

        val price = formatCurrency(focusedOrder.orderTotal)
        client_phoneNumber.text = focusedOrder.customer.userMobileNumber
        store_name_orderContextDets.text = focusedOrder.customer.businessName
        address_orderContextDets.text = focusedOrder.customer.userAddress
        total_order_viewDets.text = price
        linearLayout_orderResponse.visibility = if(focusedOrder.orderStatus == 3 || focusedOrder.orderStatus == 9) View.GONE else View.VISIBLE
    }

}
