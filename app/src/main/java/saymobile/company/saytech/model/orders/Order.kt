package saymobile.company.saytech.model.orders

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import saymobile.company.saytech.model.customers.Customer
import saymobile.company.saytech.model.user.User

data class Order (
    val invoiceId: String = "",
    val sellerUid: String = "",
    //Add user object in here
    val productsOrderList: ArrayList<OrderItem> = arrayListOf(),
    val orderStatus: Int? = 0,
    val orderTimestampReceived: Timestamp? = null,
    val orderTimestampConfirmed: Timestamp? = null,
    val orderTimestampDelivered: Timestamp? = null,
    //Total of subtotal of all orderItems
    val orderTotal: Double = 0.0,
    val week: String? = "",
    val customer: User = User()

)
