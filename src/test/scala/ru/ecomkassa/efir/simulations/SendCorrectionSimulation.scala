package ru.ecomkassa.efir.simulations

import io.gatling.core.Predef._
import ru.ecomkassa.efir.scenarios.SendCorrectionScenario

class SendCorrectionSimulation extends Simulation {
  private val sendOrdersExec = SendCorrectionScenario.sendCorrectionScenario.inject(atOnceUsers(1))
  setUp(sendOrdersExec)
}
