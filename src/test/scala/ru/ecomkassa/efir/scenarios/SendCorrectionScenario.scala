package ru.ecomkassa.efir.scenarios

import io.gatling.core.Predef._
import io.gatling.core.feeder.Feeder
import io.gatling.core.structure.ScenarioBuilder
import ru.ecomkassa.efir.{ExcelCorrectionFeeder, Order}
import ru.ecomkassa.efir.config.Config
import ru.ecomkassa.efir.requests.{CorrectionRequest, SignInRequest}

object SendCorrectionScenario {
  val feeder: Feeder[Order] = new ExcelCorrectionFeeder(
    Config.dataFile, Config.inn, Config.sno
  )

  val sendCorrectionScenario: ScenarioBuilder = scenario("Send corrections")
    .exec(SignInRequest.getToken)
    .exec(
      doWhile(feeder.hasNext) {
        CorrectionRequest.sendCorrection(feeder)
      }
    )
}
