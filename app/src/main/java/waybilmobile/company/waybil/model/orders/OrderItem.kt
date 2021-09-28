package waybilmobile.company.waybil.model.orders

import com.google.firebase.firestore.IgnoreExtraProperties
import waybilmobile.company.waybil.model.products.Product

@IgnoreExtraProperties
data class OrderItem (
    val product: Product = Product(),
    var quantity: Int = 0,
    var subTotal: Double = product.pricePerUnit * quantity

)