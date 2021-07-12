package waybilmobile.company.waybil.view.inventory

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.item_product.view.*
import waybilmobile.company.waybil.R
import waybilmobile.company.waybil.model.products.Product
import waybilmobile.company.waybil.util.formatCurrency
import waybilmobile.company.waybil.util.getProgressDrawable
import waybilmobile.company.waybil.util.loadImage

class InventoryListAdapter(val inventoryList: ArrayList<Product>) :
    RecyclerView.Adapter<InventoryListAdapter.ProductListViewHolder>() {

    private var currentUser = FirebaseAuth.getInstance().currentUser
    private var userId = currentUser!!.uid

    fun updateProductList(newProductInventoryList: List<Product>) {
        inventoryList.clear()
        inventoryList.addAll(newProductInventoryList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_product, parent, false)
        return ProductListViewHolder(view)
    }

    override fun getItemCount() = inventoryList.size

    override fun onBindViewHolder(holder: ProductListViewHolder, position: Int) {

        val inventoryItem = inventoryList[position]
        val gsReference =
            FirebaseStorage.getInstance().reference.child("/$userId" +
                    "/inventory//${inventoryItem.skuNumber}/${inventoryItem.imageRef}.jpeg")

        holder.view.product_name_listPage.text = inventoryItem.productName
        holder.view.product_details_listPage.text = inventoryItem.details
        holder.view.product_price_listPage.text = formatCurrency(inventoryItem.pricePerUnit)

        holder.view.product_image_listPage.apply {
            clipToOutline = true
        }

        holder.view.product_image_listPage.loadImage(gsReference, getProgressDrawable(holder.view
            .product_image_listPage.context))

        holder.view.setOnClickListener {
            val action = InventoryFragmentDirections.actionInventoryFragmentToProductFragment()
            action.productId = inventoryItem.skuNumber
            Navigation.findNavController(it).navigate(action)

        }




    }

    class ProductListViewHolder(var view: View) : RecyclerView.ViewHolder(view)
}