package waybilmobile.company.waybil.model.user

import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class UserBusiness (
    val businessOwnerId: String = "",
    val businessName: String = "",
    val businessId: String = "",
    val businessAddress: String = "",
    val businessType: String = "",
    val businessLocation: GeoPoint? = null,
    val businessPhoneNumber: String = ""

)