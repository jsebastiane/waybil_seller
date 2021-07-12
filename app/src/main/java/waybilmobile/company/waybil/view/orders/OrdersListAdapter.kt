package waybilmobile.company.waybil.view.orders

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
//import androidx.room.FtsOptions
import kotlinx.android.synthetic.main.item_order.view.*
import waybilmobile.company.waybil.R
import waybilmobile.company.waybil.model.orders.Order
import waybilmobile.company.waybil.util.formatCurrency
import java.text.SimpleDateFormat

class OrdersListAdapter (val ordersList: ArrayList<Order>) : RecyclerView.Adapter<OrdersListAdapter.OrderViewHolder>() {


    fun updateOrdersList(newOrdersList: List<Order>){
        ordersList.clear()
        ordersList.addAll(newOrdersList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun getItemCount() = ordersList.size


    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {

        /**
         * remove comment from code below once dates have been implemented on customer side
         */
        val orderDate = ordersList[position].orderTimestampReceived?.toDate()
        orderDate?.let {
            val formattedDate = SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(it)
            holder.view.time_order.text = formattedDate

        }

        val deliveryTrue = "Entrega a negocio"
        val deliveryFalse = "Para recolectar"

//        val deliveryTrue = "Entrega a negocio"
//        val deliveryFalse = "Para recoger"

        when(ordersList[position].orderStatus){
            2 -> holder.view.upper_container_order.setBackgroundResource(R.drawable.confirmed_bg)
            3 -> holder.view.upper_container_order.setBackgroundResource(R.drawable.transaction_bg)
            9 -> holder.view.upper_container_order.setBackgroundResource(R.drawable.rejected_bg)
            else -> holder.view.upper_container_order.setBackgroundResource(R.drawable.pending_bg)
        }

        if(ordersList[position].forDelivery == true){
            holder.view.delivery_details_order.text = deliveryTrue
        }else{
            holder.view.delivery_details_order.text = deliveryFalse
        }
        holder.view.store_name_order.text = ordersList[position].customer.businessName
        holder.view.address_order.text = ordersList[position].customer.businessAddress
        holder.view.total_order_view.text = formatCurrency(ordersList[position].orderTotal)
        holder.view.distributor_details_order.text = ordersList[position].seller?.userName


        holder.view.setOnClickListener {
            val action = OrdersFragmentDirections.actionOrdersFragmentToOrderDetailsFragment()
            action.orderId = ordersList[position].orderId
            Navigation.findNavController(it).navigate(action)
        }




    }

    class OrderViewHolder(var view: View): RecyclerView.ViewHolder(view)


}