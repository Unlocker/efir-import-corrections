package ru.ecomkassa.efir

case class Item(
                 name: String
                 , price: BigDecimal
                 , quantity: BigDecimal
                 , pObject: Int
                 , pMethod: Int
                 , vat: String
               ) {

  val total: BigDecimal = price.*(quantity).setScale(2, BigDecimal.RoundingMode.UP)

  val nameShort: String = Order.limitString(128)(name)
}
