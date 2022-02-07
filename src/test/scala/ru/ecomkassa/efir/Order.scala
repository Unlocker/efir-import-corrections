package ru.ecomkassa.efir

import ru.ecomkassa.efir.Order.limitString

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

case class Order(
                  id: String
                  , dateTime: LocalDateTime
                  , reason: String
                  , payments: java.util.List[Payment]
                  , items: java.util.List[Item]
                  , rawPlace: String
                  , email: String
                  , inn: String = ""
                  , sno: String = ""
                ) {

  val date: String = dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))

  val place: String = limitString(256)(rawPlace)

  def uuid: String = UUID.randomUUID().toString
}

object Order {

  val limitString: Int => String => String = limit => input => {
    (if (input.length <= limit) input else input.substring(0, limit)).replace('"', '\'').trim
  }
}
