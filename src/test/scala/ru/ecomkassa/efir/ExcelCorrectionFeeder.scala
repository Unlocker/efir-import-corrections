package ru.ecomkassa.efir

import io.gatling.core.feeder
import io.gatling.core.feeder.CloseableFeeder
import org.apache.poi.ss.usermodel.{Sheet, Workbook, WorkbookFactory}

import java.io.File
import java.time.LocalDateTime
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters._

class ExcelCorrectionFeeder(
                             excel: File,
                             inn: String,
                             sno: String
                           )
  extends CloseableFeeder[Order] {

  val workbook: Workbook = WorkbookFactory.create(excel)
  private val sheet: Sheet = workbook.getSheetAt(0)
  val lastRowNum: Int = sheet.getLastRowNum

  var currentRow: Int = 1

  override def hasNext: Boolean = currentRow < lastRowNum

  override def next(): feeder.Record[Order] = {
    val row = sheet.getRow(currentRow)
    val currentId: String = row.getCell(1).toString
    val date: LocalDateTime = row.getCell(0).getLocalDateTimeCellValue

    val bonus = Payment(4, BigDecimal(row.getCell(9).getNumericCellValue))
    val card = Payment(1, BigDecimal(row.getCell(10).getNumericCellValue))
    val place = row.getCell(12).getStringCellValue

    var nextId = currentId
    var size = 1
    val itemList = new ListBuffer[Item]()
    do {
      val itemRow = sheet.getRow(currentRow + size)
      val name = itemRow.getCell(6).getStringCellValue
      val quantity = BigDecimal(itemRow.getCell(7).getNumericCellValue)
      val price = BigDecimal(itemRow.getCell(8).getNumericCellValue)
      val pObject = itemRow.getCell(13).getNumericCellValue.toInt
      val vat = itemRow.getCell(14).getStringCellValue match {
        case "ндс10" => "Vat10"
        case "ндс20" => "Vat20"
        case "ндс0" => "Vat0"
        case "ндсНет" => "VatNo"
      }
      itemList.append(Item(name, price, quantity, pObject, vat))
      size = size + 1
      val idProbeRow = sheet.getRow(currentRow + size)
      nextId = if (idProbeRow == null) "" else idProbeRow.getCell(1).toString
    } while (currentId == nextId)

    currentRow = currentRow + size
    Map(
      "order" -> Order(
        currentId, date, List(bonus, card).filter(_.sum.>(BigDecimal(0))).asJava, itemList.toList.asJava, place, inn, sno
      )
    )
  }

  override def close(): Unit = workbook.close()
}
