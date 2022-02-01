package ru.ecomkassa.efir

import io.gatling.core.feeder
import io.gatling.core.feeder.CloseableFeeder
import org.apache.poi.ss.usermodel.{CellType, Sheet, Workbook, WorkbookFactory}
import org.slf4j.{Logger, LoggerFactory}

import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters._

class ExcelCorrectionFeeder(
                             excel: File,
                             inn: String,
                             sno: String
                           )
  extends CloseableFeeder[Order] {

  val log: Logger = LoggerFactory.getLogger(classOf[ExcelCorrectionFeeder])

  val workbook: Workbook = WorkbookFactory.create(excel)
  private val sheet: Sheet = workbook.getSheetAt(0)
  val lastRowNum: Int = sheet.getLastRowNum

  val dtf: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")

  var currentRow: Int = 1

  override def hasNext: Boolean = currentRow <= lastRowNum

  override def next(): feeder.Record[Order] = {
    log.debug(s"currentRow=$currentRow; lastRowNum=$lastRowNum")
    val row = sheet.getRow(currentRow)
    val currentId: String = row.getCell(1).toString


    val date: LocalDateTime = row.getCell(0) match {
      case x if x.getCellType == CellType.NUMERIC => x.getLocalDateTimeCellValue
      case x if x.getCellType == CellType.STRING => LocalDateTime.parse(x.getStringCellValue, dtf)
    }

    val email = row.getCell(5).getStringCellValue
    val bonus = Payment(4, BigDecimal(row.getCell(9).getNumericCellValue))
    val isAdvance = "Зачет аванса" == row.getCell(18).getStringCellValue
    val card = Payment(if (isAdvance) 2 else 1, BigDecimal(row.getCell(10).getNumericCellValue))
    val place = row.getCell(12).getStringCellValue
    val reason = row.getCell(15).getStringCellValue

    var nextId = currentId
    var size = 1
    val itemList = new ListBuffer[Item]()
    log.debug(s"Header for ORDER_ID=$currentId parsed")
    do {
      val itemRow = sheet.getRow(currentRow + size)
      val name = itemRow.getCell(6).getStringCellValue
      val quantity = BigDecimal(itemRow.getCell(7).getNumericCellValue)
      val price = BigDecimal(itemRow.getCell(8).getNumericCellValue)
      val pObject = itemRow.getCell(13).getStringCellValue match {
        case "услуга" => 4
        case "товар" => 1
        case _ =>
          log.debug(s"No payment object defined for ORDER_ID=$currentId, NUM=$size. Using the default 1")
          1
      }
      val vat = itemRow.getCell(14).getStringCellValue match {
        case "БЕЗ НДС" | "БЕЗНДС" => "VatNo"
        case _ =>
          log.debug(s"No VAT defined for ORDER_ID=$currentId, NUM=$size. Using the default VatNo")
          "VatNo"
      }
      val pMethod = itemRow.getCell(18).getStringCellValue match {
        case "Полный расчет" => 4
        case "предоплата 100%" => 1
        case _ => 4
      }
      itemList.append(Item(name, price, quantity, pObject, pMethod, vat))
      log.debug(s"Item NUM=$size for ORDER_ID=$currentId parsed")
      size = size + 1
      val idProbeRow = sheet.getRow(currentRow + size)
      nextId = if (idProbeRow == null) "" else idProbeRow.getCell(1).toString
    } while (currentId == nextId)

    currentRow = currentRow + size
    Map(
      "order" -> Order(
        currentId
        , date
        , reason
        , List(bonus, card).filter(_.sum.>(BigDecimal(0))).asJava
        , itemList.toList.asJava
        , place
        , email
        , inn
        , sno
      )
    )
  }

  override def close(): Unit = workbook.close()
}
