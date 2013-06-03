package output
import org.junit.runner.RunWith
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Sheet
import libt.spreadsheet.reader._
import libt.spreadsheet._
import libt.spreadsheet.util._
import libt._
import org.scalatest.BeforeAndAfter
import output._
import org.scalatest.FlatSpec
import libt.spreadsheet.Offset
import scala.math.BigDecimal.int2bigDecimal
import org.scalatest.junit.JUnitRunner
import java.io.FileOutputStream

@RunWith(classOf[JUnitRunner])
class WorkbookWriterWithFlattedAreaSpec extends FlatSpec with BeforeAndAfter {

  val TSimpleSchema = TModel(
    'key1 -> TNumber,
    'key2 -> TString,
    'values -> TCol(
      TModel(
        'c -> TString,
        'd -> TStringEnum("foo", "bar"),
        'e -> TXBool)))

  val models = Seq(
    Model(
      'key1 -> Value(1000: BigDecimal),
      'key2 -> Value("value1" ),
      'values -> Col(
        Model(
          'c -> Value(Some("someValue"),Some("value1"),Some("value2"),Some("value3"),Some("value4")),
          'd -> Value("foo"),
          'e -> Value(true)),
        Model(
          'c -> Value("otherValue"),
          'd -> Value(),
          'e -> Value()))),
    Model(
        'key1 -> Value(2000: BigDecimal),
        'key2 -> Value("value2"),
        'values -> Col(
        Model(
          'c -> Value("someValue2"),
          'd -> Value("bar"),
          'e -> Value(false)),
        Model(
          'c -> Value("otherValue2"),
          'd -> Value("bar"),
          'e -> Value(true)))))

  val wb = new HSSFWorkbook
  var sheet: Sheet = _
  var offset: Offset = _

  def writeModel = WorkbookMapping(
    Seq(
      FlattedArea(
        PK(Path('key1), Path('key2)),
        PK(Path('c)),
        Path('values),
        TSimpleSchema,
        ValueAreaLayout(offset),
        Seq(
          Feature('c),
          Feature('d),
          Feature('e)))))
    .write(models, wb)

  behavior of "workbook writer using a flatted area"

  before {
    wb.createSheet()
    sheet = wb.getSheetAt(0)
    offset = Offset(0,0)
    writeModel
  }

  after {
    wb.removeSheetAt(0)
  }

  it should "write keys" in {
    assert(sheet.cellAt(0, 0).getNumericCellValue() === 1000)
    assert(sheet.cellAt(0, 1).getStringCellValue() === "value1")
  }

  it should "write values of flattened models" in {
    assert(sheet.cellAt(0, 2).getStringCellValue() === "someValue")
    assert(sheet.cellAt(0, 3).getStringCellValue() === "foo")
    assert(sheet.cellAt(0, 4).getStringCellValue() === "X")
  }

  it should "write a row for each flatted model" in {
    assert(sheet.cellAt(0, 0).getNumericCellValue() === 1000)
    assert(sheet.cellAt(0, 2).getStringCellValue() === "someValue")
    assert(sheet.cellAt(1, 0).getNumericCellValue() === 1000)
    assert(sheet.cellAt(1, 2).getStringCellValue() === "otherValue")
  }

  it should "write a row for each root model and flattened model" in {
    //First root model - First flattened model
    assert(sheet.cellAt(0, 0).getNumericCellValue() === 1000)
    assert(sheet.cellAt(0, 1).getStringCellValue() === "value1")
    assert(sheet.cellAt(0, 2).getStringCellValue() === "someValue")
    //First root model - Second flattened model
    assert(sheet.cellAt(1, 0).getNumericCellValue() === 1000)
    assert(sheet.cellAt(1, 1).getStringCellValue() === "value1")
    assert(sheet.cellAt(1, 2).getStringCellValue() === "otherValue")
    
    //Second root model - First flattened model
    assert(sheet.cellAt(2, 0).getNumericCellValue() === 2000)
    assert(sheet.cellAt(2, 1).getStringCellValue() === "value2")
    assert(sheet.cellAt(2, 2).getStringCellValue() === "someValue2")
    
    //Second root model - First flattened model
    assert(sheet.cellAt(3, 0).getNumericCellValue() === 2000)
    assert(sheet.cellAt(3, 1).getStringCellValue() === "value2")
    assert(sheet.cellAt(3, 2).getStringCellValue() === "otherValue2")
  }

  it should "leave empty rows for the headers" in {
    offset = Offset(3,0)
    writeModel
    assert(sheet.cellAt(3, 0).getNumericCellValue() === 1000)
    assert(sheet.cellAt(5, 0).getNumericCellValue() === 2000)
    assert(sheet.cellAt(6, 0).getNumericCellValue() === 2000)
  }
  
  it should "leave empty columns" in {
    offset = Offset(0,2)
    writeModel
    assert(sheet.cellAt(0, 2).getNumericCellValue() === 1000)
    assert(sheet.cellAt(2, 2).getNumericCellValue() === 2000)
    assert(sheet.cellAt(3, 2).getNumericCellValue() === 2000)
  }
}
