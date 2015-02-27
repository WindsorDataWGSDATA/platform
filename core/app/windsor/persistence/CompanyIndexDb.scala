package windsor.persistence

import windsor.generic.persistence.Database
import libt.TModel
import libt.Path
import com.mongodb.casbah.MongoDB
import model.CompanyIndex
import windsor.generic.persistence.DBO
import com.mongodb.casbah.commons.MongoDBObject

case class CompanyIndexDb(db: MongoDB) extends Database {

  val TDBSchema: TModel = CompanyIndex.TCompanyIndex
  protected val colName: String = "companyIndex"
  protected val pk: Seq[Path] = Seq(Path('ticker))

  collection.ensureIndex("ticker.value")

  def nameForTicker(ticker: String) =
    find(MongoDBObject("ticker.value" -> ticker))

}