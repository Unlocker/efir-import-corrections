package ru.ecomkassa.efir.config

import java.io.File
import java.nio.file.Path

object Config {
  /*
    5.2. Тестовое API Ferma
    Для того чтобы пробить чеки на тестовой кассе Ferma, используйте домен 44) ferma-test.ofd.ru, для кассы версии ФФД 1.05 и 1.1 используйте следующие данные:
    Логин - fermatest1;
    Пароль - Hjsf3321klsadfAA;

    для кассы версии ФФД 1.2:
    Логин - fermatest2;
    Пароль - Go2999483Mb.

    Логин и пароль используются в API-запросе для получения кода авторизации (AuthToken).
   */

  //  val rootUrl = "https://ferma.ofd.ru/api"
  val rootUrl = "https://ferma-test.ofd.ru/api"


  val dataFile: File = {
    Option(System.getProperty("datafile"))
      .map(Path.of(_).toFile)
      .getOrElse(
        new File(getClass.getClassLoader.getResource("data_sample.xlsx").toURI)
      )
  }


  val login: String = System.getProperty("login", "fermatest1")
  val password: String = System.getProperty("password", "Hjsf3321klsadfAA")
  val inn: String = System.getProperty("inn", "1739284652")
  val sno: String = System.getProperty("sno", "Common")

  val iterations: Int = Integer.getInteger("iterations", 4).toInt
}
