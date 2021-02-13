package saymobile.company.saytech.model.orders

import saymobile.company.saytech.model.products.Product

data class OrderItem (
    val product: Product = Product(),
    var quantity: Int = 0,
    var subTotal: Double = product.pricePerUnit * quantity

)