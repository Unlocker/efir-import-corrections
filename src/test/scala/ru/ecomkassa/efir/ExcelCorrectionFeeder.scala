package ru.ecomkassa.efir

import io.gatling.core.feeder
import io.gatling.core.feeder.CloseableFeeder
import org.apache.poi.ss.usermodel.{Cell, CellType, Sheet, Workbook, WorkbookFactory}
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

  val safeNumConverter: Cell => BigDecimal = {
    case x if x.getCellType == CellType.STRING =>
      if (x.getStringCellValue.trim == "") BigDecimal(0) else BigDecimal(
        x.getStringCellValue.replace(",", ".")
      )
    case x if x.getCellType == CellType.NUMERIC => BigDecimal(x.getNumericCellValue)
    case _ => BigDecimal(0)
  }

  override def next(): feeder.Record[Order] = {
    log.debug(s"currentRow=$currentRow; lastRowNum=$lastRowNum")
    var currentCell: Int = 1
    try {
      val row = sheet.getRow(currentRow)
      val currentId: String = row.getCell(currentCell).toString


      currentCell = 0
      val date: LocalDateTime = row.getCell(currentCell) match {
        case x if x.getCellType == CellType.NUMERIC => x.getLocalDateTimeCellValue
        case x if x.getCellType == CellType.STRING => LocalDateTime.parse(x.getStringCellValue, dtf)
      }

      currentCell = 5
      val email = row.getCell(currentCell).getStringCellValue

      currentCell = 9
      val bonus = Payment(4, safeNumConverter(row.getCell(currentCell)))

      currentCell = 19
      val advanceMarker = row.getCell(currentCell)
      val isAdvance = advanceMarker != null && (advanceMarker match {
        case x if x.getCellType == CellType.STRING =>
          "Зачет аванса".toLowerCase == x.getStringCellValue.toLowerCase
        case _ => false
      })

      currentCell = 10
      val card = Payment(if (isAdvance) 2 else 1, safeNumConverter(row.getCell(currentCell)))

      currentCell = 12
      val place = row.getCell(currentCell).getStringCellValue

      currentCell = 15
      val reason = row.getCell(currentCell).getStringCellValue

      var nextId = currentId
      var size = 1
      val itemList = new ListBuffer[Item]()
      log.debug(s"Header for ORDER_ID=$currentId parsed")
      do {
        val itemRow = sheet.getRow(currentRow + size)
        currentCell = 6
        val name = itemRow.getCell(currentCell).getStringCellValue

        currentCell = 7
        val quantity = safeNumConverter(itemRow.getCell(currentCell))

        currentCell = 8
        val price = safeNumConverter(itemRow.getCell(currentCell))

        currentCell = 13
        val pObject = itemRow.getCell(currentCell).getStringCellValue match {
          case "услуга" => 4
          case "товар" => 1
          case _ =>
            log.debug(s"No payment object defined for ORDER_ID=$currentId, NUM=$size. Using the default 1")
            1
        }

        currentCell = 14
        val vat = itemRow.getCell(currentCell).getStringCellValue match {
          case "БЕЗ НДС" | "БЕЗНДС" => "VatNo"
          case _ =>
            log.debug(s"No VAT defined for ORDER_ID=$currentId, NUM=$size. Using the default VatNo")
            "VatNo"
        }

        currentCell = 18
        val pMethod = itemRow.getCell(18).getStringCellValue match {
          case "Полный расчет" => 4
          case "предоплата 100%" | "Предоплата 100%" => 1
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
    } catch {
      case e: Throwable => log.error(s"Error parsing ROW=$currentRow, CELL=$currentCell: ${e.getMessage}")
        throw e
    }
  }

  override def close(): Unit = workbook.close()
}
