package ru.ecomkassa.efir.scenarios

import io.gatling.core.Predef._
import io.gatling.core.feeder.Feeder
import io.gatling.core.structure.ScenarioBuilder
import ru.ecomkassa.efir.{ExcelCorrectionFeeder, Order}
import ru.ecomkassa.efir.config.Config
import ru.ecomkassa.efir.requests.{CorrectionRequest, SignInRequest, StatusRequest}
import scala.concurrent.duration._

object SendCorrectionScenario {
  val feeder: Feeder[Order] = new ExcelCorrectionFeeder(
    Config.dataFile, Config.inn, Config.sno
  )

  val sendCorrectionScenario: ScenarioBuilder = scenario("Send corrections")
    .exec(SignInRequest.getToken)
    .exec(
      (1 to Config.iterations)
        .map(
          _ => CorrectionRequest.sendCorrection(feeder)
            .exec(pause(2.seconds))
            .exec(StatusRequest.status)
        )
    )
}
