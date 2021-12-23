package ru.ecomkassa.efir.scenarios

import io.gatling.core.Predef.scenario
import io.gatling.core.structure.ScenarioBuilder
import ru.ecomkassa.efir.config.Config
import ru.ecomkassa.efir.requests.{CorrectionRequest, SignInRequest}

object SendCorrectionScenario {
  val sendCorrectionScenario: ScenarioBuilder = scenario("Send corrections")
    .exec(SignInRequest.getToken)
    .exec((1 to Config.iterations).map(_ => CorrectionRequest.sendCorrection))

}
