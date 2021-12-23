package ru.ecomkassa.efir.requests

import io.gatling.core.Predef._
import io.gatling.core.feeder.Feeder
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef._
import ru.ecomkassa.efir.config.Config

object CorrectionRequest {

  val sentHeaders = Map("Accept" -> "application/json", "Content-Type" -> "application/json")

  def sendCorrection(feeder: Feeder[_]): ChainBuilder = {
    val builder = http("Do a correction")
      .post(s"${Config.rootUrl}/kkt/cloud/receipt")
      .queryParam("AuthToken", "${token}")
      .headers(sentHeaders)
      .body(PebbleFileBody("templates/correction.json"))
      .asJson
      .check(status is 200)
      .check(jsonPath("$.Data.ReceiptId").notNull)

    feed(feeder).exec(builder)
  }

}
