package waybilmobile.company.waybil.view.orders

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.item_order_details.view.*
import waybilmobile.company.waybil.R
import waybilmobile.company.waybil.model.orders.OrderItem
import waybilmobile.company.waybil.util.formatCurrency
import waybilmobile.company.waybil.util.getProgressDrawable
import waybilmobile.company.waybil.util.loadImage

class OrderItemsAdapter (val orderItemsList: ArrayList<OrderItem>) : RecyclerView.Adapter<OrderItemsAdapter.OrderItemViewHolder>(){

    var focusedSeller: String? = ""


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

        val item = orderItemsList[position]

        val gsReference =
            FirebaseStorage.getInstance().reference.child("/$focusedSeller" +
                    "/inventory//${item.product.skuNumber}/${item.product.imageRef}.jpeg")

        val productSum =
            "${item.product.productName} \n${item.product.details}"
        val subtotal = formatCurrency(item.subTotal)

        holder.view.order_item_name.text = productSum
        holder.view.order_item_quantity.text = item.quantity.toString()
        holder.view.order_item_subtotal.text = subtotal

        holder.view.product_image_listPage.apply {
            clipToOutline = true
        }

        holder.view.product_image_listPage.loadImage(gsReference, getProgressDrawable(holder.view
            .product_image_listPage.context)
        )

    }

    class OrderItemViewHolder(var view: View) : RecyclerView.ViewHolder(view)
}