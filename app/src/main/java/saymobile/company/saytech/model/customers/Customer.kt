package saymobile.company.saytech.model.customers

import com.google.firebase.firestore.GeoPoint
import saymobile.company.saytech.model.user.User

//test number3 uu

data class Customer(
    //There will be more -- just testing
    val customer: User = User(),
    val lifeTimeOrderQuantity: Int = 0,
    val lifeTimeOrderValue: Double = 0.0

)
