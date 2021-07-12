package waybilmobile.company.waybil.model.customers

import waybilmobile.company.waybil.model.orders.Order
import waybilmobile.company.waybil.model.user.UserBusiness

data class Customer(
    //There will be more -- just testing
    val customer: UserBusiness = UserBusiness(),
    val lifeTimeOrderQuantity: Int = 0,
    val lifeTimeOrderValue: Double = 0.0,
    val lastOrder: Order? = null

)
