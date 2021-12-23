package ru.ecomkassa.efir

import org.scalatest.flatspec._
import org.scalatest.matchers._

import java.io.File


class ExcelCorrectionFeederSuite extends AnyFlatSpec with should.Matchers {

  "A feeder" should "read values from file" in {
    val excel = new File(getClass.getClassLoader.getResource("data_sample.xlsx").toURI)

    val feeder = new ExcelCorrectionFeeder(excel, "", "")

    feeder.hasNext should be(true)
    val all = (1 to 4).map(_ => feeder.next()).fold(Map.empty)((a, b) => a ++ b)
    feeder.hasNext should be(false)
  }
}
