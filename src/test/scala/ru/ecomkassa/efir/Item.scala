package ru.ecomkassa.efir

case class Item(
                 name: String
                 , price: BigDecimal
                 , quantity: BigDecimal
                 , pObject: Int
                 , vat: String
               ) {

  val total: BigDecimal = price.*(quantity).setScale(2, BigDecimal.RoundingMode.UP)

  val nameShort: String = if (name.length <= 128) name else name.substring(0, 128)
}
