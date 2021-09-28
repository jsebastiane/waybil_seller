package waybilmobile.company.waybil.model.products

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class ProductMetaData (
    val productDetails: Product = Product(),
    val quantitySold: Int = 0,
    val revenueEarned: Double = 0.0,
    val costOfGoodSold: Double = 0.0
)