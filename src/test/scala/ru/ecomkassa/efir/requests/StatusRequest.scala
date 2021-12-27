package ru.ecomkassa.efir.requests

import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef._
import ru.ecomkassa.efir.config.Config

object StatusRequest {
  val sentHeaders = Map("Accept" -> "application/json", "Content-Type" -> "application/json")

  val status: ChainBuilder = exec(
    http("Check status")
      .post(s"${Config.rootUrl}/kkt/cloud/status")
      .queryParam("AuthToken", "${token}")
      .headers(sentHeaders)
      .body(PebbleFileBody("templates/status.json"))
      .asJson
  )
}
