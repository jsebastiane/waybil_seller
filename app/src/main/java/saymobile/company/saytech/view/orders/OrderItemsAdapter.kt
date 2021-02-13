package saymobile.company.saytech.view.orders

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_order_details.view.*
import saymobile.company.saytech.R
import saymobile.company.saytech.model.orders.OrderItem
import saymobile.company.saytech.util.formatCurrency
import kotlin.math.roundToInt

class OrderItemsAdapter (val orderItemsList: ArrayList<OrderItem>) : RecyclerView.Adapter<OrderItemsAdapter.OrderItemViewHolder>(){

    fun submitOrderItems(newOrderItems: List<OrderItem>){
        orderItemsList.clear()
        orderItemsList.addAll(newOrderItems)
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_order_details, parent, false)
        return OrderItemViewHolder(view)
    }

    override fun getItemCount() = orderItemsList.size

    override fun onBindViewHolder(holder: OrderItemViewHolder, position: Int) {
        val productSum =
            "${orderItemsList[position].product.productName} \n${orderItemsList[position].product.details}"
        val subtotal = formatCurrency(orderItemsList[position].subTotal)

        holder.view.order_item_name.text = productSum
        holder.view.order_item_quantity.text = orderItemsList[position].quantity.toString()
        holder.view.order_item_subtotal.text = subtotal
    }

    class OrderItemViewHolder(var view: View) : RecyclerView.ViewHolder(view)
}