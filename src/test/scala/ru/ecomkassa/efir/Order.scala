package ru.ecomkassa.efir

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

case class Order(
                  id: String
                  , dateTime: LocalDateTime
                  , payments: java.util.List[Payment]
                  , items: java.util.List[Item]
                  , place: String
                  , inn: String = ""
                  , sno: String = ""
                ) {

  val date: String = dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))

  def uuid: String = UUID.randomUUID().toString
}
