package input

import java.io.InputStream
import scala.collection.JavaConversions._
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import model.CarriedInterest
import model.Executive
import model.Company
import util.poi.Cells._
import model.CarriedInterest
import model.EquityCompanyValue
import scala.collection.immutable.Map
import model.Input
import model.AnualCashCompensation
import model.New8KData
object SpreadsheetLoader {

  def load(in: InputStream): Company = {
    val wb = WorkbookFactory.create(in)

    val executivesSheet: Sheet = wb.getSheetAt(1)
    val executives = rows(executivesSheet).drop(3).grouped(6).map(toExecutive).toSeq

    val companiesSheet = wb.getSheetAt(0)
    toCompany(executives)(rows(companiesSheet).drop(1))
  }

  def toCompany(executives: Seq[Executive])(rows: Seq[Row]) = {
    Company(
      ticker = None,
      name = None,
      disclosureFiscalYear = None,
      gicsIndustry = None,
      annualRevenue = None,
      marketCapital = None,
      proxyShares = None,
      executives = executives)
  }

  def toExecutive(rows: Seq[Row]) = {
    val reader = new ColumnOrientedReader(rows)
    import reader._

    Executive(
      name = { skip(1); string },
      title = string,
      shortTitle = string,
      functionalMatch = string,
      functionalMatch1 = string,
      functionalMatch2 = string,
      founder = string,
      cashCompensations = Seq(
        AnualCashCompensation(
          baseSalary = numeric,
          actualBonus = numeric,
          targetBonus = numeric,
          thresholdBonus = numeric,
          maxBonus = numeric,
          new8KData = New8KData(
            baseSalary = numeric,
            targetBonus = numeric))),
      equityCompanyValue = EquityCompanyValue(
        optionsValue = numeric,
        options = numeric,
        exPrice = numeric,
        bsPercentage = numeric,
        timeVest = numeric,
        rsValue = numeric,
        shares = numeric,
        price = numeric,
        perfRSValue = numeric,
        shares2 = numeric,
        price2 = numeric,
        perfCash = numeric),
      carriedInterest = CarriedInterest(
        ownedShares = numeric,
        vestedOptions = numeric,
        unvestedOptions = numeric,
        tineVest = numeric,
        perfVest = numeric))
  }

}
