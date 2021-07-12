package waybilmobile.company.waybil.view.orders

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_orders.*

import waybilmobile.company.waybil.R
import waybilmobile.company.waybil.viewmodel.orders.OrdersViewModel


class OrdersFragment : Fragment() {

    private lateinit var viewModel: OrdersViewModel
    private var orderListAdapter = OrdersListAdapter(arrayListOf())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_orders, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        close_fragment_orders.setOnClickListener {
            findNavController().navigateUp()
        }

        viewModel = ViewModelProviders.of(this).get(OrdersViewModel::class.java)

        orders_recyclerview.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = orderListAdapter

        }


        //Setting listeners for radio group of filters
        orders_radio_filters.setOnCheckedChangeListener { _, checkedId ->
            viewModel.refresh(
                when(checkedId){
                    R.id.radio_pending_orders -> 1
                    R.id.radio_confirmed_orders -> 2
                    else -> 3

                }
            )
        }
        radio_pending_orders.isChecked = true
        viewModel.refresh(1)
        observeOrdersViewModel()

    }

    fun observeOrdersViewModel(){
        viewModel.listOfOrders.observe(viewLifecycleOwner, Observer { orders ->
            if(orders.isEmpty()){
                noOrders_message.visibility = View.VISIBLE
                orders_recyclerview.visibility = View.GONE
            }else{
                orders?.let {
                    orders_recyclerview.visibility = View.VISIBLE
                    noOrders_message.visibility = View.GONE
                    orders_recycler_error.visibility = View.GONE
                    orderListAdapter.updateOrdersList(orders)
                }
            }

        })

        viewModel.ordersLoadError.observe(viewLifecycleOwner, Observer { isError ->
            isError?.let{
                orders_recycler_error.visibility = if(it) View.VISIBLE else View.GONE
            }
        })

        viewModel.loading.observe(viewLifecycleOwner, Observer { isLoading ->
            isLoading?.let {
                orders_progress_bar.visibility = if(it) View.VISIBLE else View.GONE
            }
        })

    }

}
