package waybilmobile.company.waybil.model.orders

import com.google.firebase.Timestamp
import waybilmobile.company.waybil.model.user.User
import waybilmobile.company.waybil.model.user.UserBusiness

data class Order (
    val orderId: String = "",
    val seller: User? = null,
    //Add user object in here
    val productsOrderList: ArrayList<OrderItem> = arrayListOf(),
    var orderStatus: Int? = 0,
    val orderTimestampReceived: Timestamp? = null,
    val orderTimestampConfirmed: Timestamp? = null,
    var orderTimestampDelivered: Timestamp? = null,
    //Total of subtotal of all orderItems
    val orderTotal: Double = 0.0,
    val week: String? = "",
    val customer: UserBusiness = UserBusiness(),
    val forDelivery: Boolean? = null,
    val orderPin: String = "",
    val sellerOffersDelivery: Boolean? = null,
    val invoiceNumber: String = "",
    val productCost: Double? = null
)
