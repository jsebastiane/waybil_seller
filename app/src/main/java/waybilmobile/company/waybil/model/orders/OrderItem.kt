package waybilmobile.company.waybil.model.orders

import waybilmobile.company.waybil.model.products.Product

data class OrderItem (
    val product: Product = Product(),
    var quantity: Int = 0,
    var subTotal: Double = product.pricePerUnit * quantity

)