package saymobile.company.saytech.model.user

import android.net.Uri
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.IgnoreExtraProperties


@IgnoreExtraProperties
data class User (
    val id: String ="",
    val businessName: String = "",
    val userAddress: String = "",
    val userMobileNumber: String = "",
    val businessEmail: String? = "",
    val accountManager: String? = "",
    val dateJoined: Timestamp? = null,
    val geoLocation: GeoPoint? = null,

    //settings variables
    val deliveryCost: String? = null,
    val operatingRadius: String? = null,
    val sellerVisibility: Boolean = false,
    val paymentOwed: Double? = null,
    val sellerPaid: Boolean? = null

)