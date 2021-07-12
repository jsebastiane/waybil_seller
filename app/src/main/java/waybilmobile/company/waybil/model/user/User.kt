package waybilmobile.company.waybil.model.user

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.IgnoreExtraProperties


@IgnoreExtraProperties
data class User (
    val id: String ="",
    val userName: String = "",
    val userAddress: String = "",
    val userMobileNumber: String = "",
    val businessEmail: String? = "",
    val accountManager: Name? = null,
    val dateJoined: Timestamp? = null,
    val geoLocation: GeoPoint? = null,

    //settings variables
    val deliveryCost: Int = 0,
    val operatingRadius: Int = 0,
    val sellerVisibility: Boolean = false,
    val paymentOwed: Double? = null,
    val sellerPaid: Boolean? = null,
    val clientPortfolio: ArrayList<String> = arrayListOf(),
    val connectedDevices: ArrayList<Device> = arrayListOf(),
    val devicesAuthorized: Int = 1,
    val deliveryOffered: Boolean = false,
    val deliveryTime: Int = 0,
    val currentInvoiceNumber: Int = 1,
    val profileImageRef: String? = null
)