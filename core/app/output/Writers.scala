package output
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.ss.usermodel.Sheet
import java.io.OutputStream
import org.apache.poi.ss.usermodel.Workbook
import model._
import util.FileManager
import libt._
import libt.spreadsheet._
import libt.spreadsheet.reader._
import model.ExecutivesBod._
import model.mapping.bod._
import output.mapping._

trait OutputWriter {
  val schema: TModel
  val fileName: String
  def write(out: Workbook, models: Seq[Model], yearRange: Int): Unit

  def outputArea(
    layout: FlattedAreaLayout,
    outputMapping: Seq[Strip],
    flatteningPK: Path,
    flatteningPath: Path,
    writeStrategy: WriteStrategy) =
      FlattedArea(
        PK(Path('ticker), Path('name), Path('disclosureFiscalYear)),
        PK(flatteningPK),
        flatteningPath,
        schema,
        layout,
        outputMapping,
        writeStrategy)

  def write(out: OutputStream, companies: Seq[Model], range: Int): Unit = {
    FileManager.loadResource(fileName) {
      x =>
      {
        val wb = WorkbookFactory.create(x)
        write(wb, companies, range)
        wb.write(out)
      }
    }
  }
}

object BodWriter extends OutputWriter {
  val schema = TModel(
    TBod.elementTypes ++
    TModel('ticker -> TString, 'name -> TString, 'disclosureFiscalYear -> TInt).elementTypes : _*)

  val fileName = "EmptyBodOutputTemplate.xls"

  def bodArea(range: Int) =
    Area(schema= schema,
      offset= Offset(4,0),
      limit= None,
      orientation= ColumnOrientedLayout(RawValueReader),
      columns= Seq[Strip](Path('ticker),
                          Path('name),
                          Path('disclosureFiscalYear),
                          Gap, Gap, Gap, Gap, Gap) ++ bodMapping)

  def metadataArea(range: Int) =
    outputArea(
      MetadataAreaLayout(Offset(1, 0)),
      execDbOutputMapping.filter(_ match {
        case Gap => false
        case _ => true
      }),
      Path(),
      Path('bod, *),
      FullWriteStrategy)

  def write(out: Workbook, models: Seq[Model], yearRange: Int): Unit =
    WorkbookMapping(Seq(bodArea(yearRange))).write(
      Model.flattenWith(
        models,
        PK(Path('ticker), Path('name), Path('disclosureFiscalYear)),
        Path('bod, *)), out)
}


object StandardWriter extends OutputWriter {
  val schema = TCompanyFiscalYear
  val fileName = "EmptyStandardOutputTemplate.xls"

  def execDBArea(range: Int, yearOffset: Option[Int]) =
    outputArea(
      ValueAreaLayout(Offset(6, 2)),
      execDbOutputMapping,
      Path('lastName),
      Path('executives, *),
      ExecutivesWriteStrategy(range, yearOffset))

  def stBonusPlanArea =
    outputArea(
      ValueAreaLayout(Offset(6, 2)),
      stBonusPlanOutputMapping,
      Path('lastName),
      Path('stBonusPlan, *),
      LastYearWriteStrategy)

  def executiveOwnershipArea =
    outputArea(
      ValueAreaLayout(Offset(6, 2)),
      executiveOwnershipMapping,
      Path('lastName),
      Path('guidelines, *),
      LastYearWriteStrategy)


  def companyDBArea =
    outputArea(
      ValueAreaLayout(Offset(6, 2)),
      Seq(Gap) ++
      usageAndSVTDataMapping ++
      Seq(Gap) ++
      bsInputsMapping ++
      Seq(Gap) ++
      dilutionMapping,
      Path(),
      Path('companyDB, *),
      LastYearWriteStrategy)

  def companyDBMetadataArea =
    outputArea(
      MetadataAreaLayout(Offset(1, 0)),
      usageAndSVTDataMapping ++
      bsInputsMapping ++
      dilutionMapping
        .filter(_ match {
        case Gap => false
        case _ => true
      }),
      Path(),
      Path('companyDB, *),
      LastYearWriteStrategy)

  def grantTypesArea =
    outputArea(
      ValueAreaLayout(Offset(7, 2)),
      grantTypesMapping,
      Path(),
      Path('grantTypes, *),
      LastYearWriteStrategy)

  def execMetadataArea(range: Int) =
    outputArea(
      MetadataAreaLayout(Offset(1, 0)),
      execDbOutputMapping
        .filter(_ match {
        case Gap => false
        case _ => true
      }),
      Path('lastName),
      Path('executives, *),
      ExecutivesWriteStrategy(range, None))

  def stBonusMetadataArea =
    outputArea(
      MetadataAreaLayout(Offset(1, 0)),
      stBonusPlanOutputMapping
        .filter(_ match {
        case Gap => false
        case _ => true
      }),
      Path('lastName),
      Path('stBonusPlan, *),
      LastYearWriteStrategy)

  def executiveOwnershipMetadataArea =
    outputArea(
      MetadataAreaLayout(Offset(1, 0)),
      executiveOwnershipMapping
        .filter(_ match {
        case Gap => false
        case _ => true
      }),
      Path('lastName),
      Path('guidelines, *),
      LastYearWriteStrategy)

  def grantTypesMetadataArea =
    outputArea(
      MetadataAreaLayout(Offset(1, 0)),
      grantTypesMapping
        .filter(_ match {
        case Gap => false
        case _ => true
      }),
      Path(),
      Path('grantTypes, *),
      LastYearWriteStrategy)


  def write(out: Workbook, companies: Seq[Model], executivesRange: Int): Unit = {
    WorkbookMapping(
      Seq(
        execDBArea(executivesRange - 1, Some(0)), //ExecDB
        execDBArea(executivesRange - 2, Some(1)), //ExecDB -1 
        execDBArea(executivesRange - 3, Some(2)), //ExecDB -2
        execMetadataArea(executivesRange),
        stBonusPlanArea,
        stBonusMetadataArea,
        executiveOwnershipArea,
        executiveOwnershipMetadataArea,
        companyDBArea,
        companyDBMetadataArea,
        grantTypesArea,
        grantTypesMetadataArea)).write(companies, out)
  }

  def loadTemplateInto(out: OutputStream) =
    FileManager.loadResource("EmptyStandardOutputTemplate.xls") {
      x => WorkbookFactory.create(x).write(out)
    }

  /**
    * [[output.WriteStrategy]] that writes TCompanyFiscalYears for a given yearOffset
    * @param range
    * @param yearOffset the negative year offset (0 is last year, 1 is previous year, and so on)
    */
  case class ExecutivesWriteStrategy(/*TODO remove*/range: Int, yearOffset: Option[Int]) extends WriteStrategy {
    override def write(models: Seq[Model], area: FlattedArea, sheet: Sheet) = {
      if (range >= 0) {
        val validModels = ModelGrouper(models).map(_._2)
        yearOffset match {
        	case Some(p) => area.layout.write(validModels.map(_(p)), sheet, area)
        	case None => area.layout.write(validModels.flatten, sheet, area)
        }		
      }
    }
  }

  /**
    * [[output.WriteStrategy]] that writes only TCompanyFiscalYears for the last year
    */
  object LastYearWriteStrategy extends WriteStrategy {
    override def write(models: Seq[Model], area: FlattedArea, sheet: Sheet) {
      val validModels = ModelGrouper(models).map(_._2.head)
      area.layout.write(validModels, sheet, area)
    }
  }

  /**
   * Group models by cusip and these are ordered by disclosureFiscalYear (descending)
   */
  object ModelGrouper {
    def apply(models: Seq[Model]) : Seq[(String, Seq[Model])] = {
      def Desc[T: Ordering] = implicitly[Ordering[T]].reverse
      models
        .groupBy(_ /!/ 'cusip)
        .map { case (ticker, ms) => ticker -> ms.sortBy(_ /#/ 'disclosureFiscalYear)(Desc) }
        .toSeq
    }
  }

}