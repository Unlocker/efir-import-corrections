package ru.ecomkassa.efir.requests

import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef._
import ru.ecomkassa.efir.config.Config

object SignInRequest {

  val sentHeaders = Map("Accept" -> "application/json", "Content-Type" -> "application/json")

  val feeder: Iterator[Map[String, String]] = Iterator.continually {
    Map("login" -> Config.login, "password" -> Config.password)
  }

  val getToken: ChainBuilder = feed(feeder).exec(
    http("Get a Token")
      .post(Config.rootUrl + "/Authorization/CreateAuthToken")
      .headers(sentHeaders)
      .body(PebbleFileBody("templates/signin.json"))
      .asJson
      .check(status is 200)
      .check(jsonPath("$.Data.AuthToken").saveAs("token"))
  )
}
