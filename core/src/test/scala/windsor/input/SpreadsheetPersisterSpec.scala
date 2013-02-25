package windsor.input
import org.scalatest.path.FunSpec
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import java.io.ByteArrayOutputStream

@RunWith(classOf[JUnitRunner])
class SpreadsheetPersisterSpec extends FunSpec {
  describe("SpreadsheetPersister") {
    
    it("should output companies") {
      import Closeables._
      new ByteArrayOutputStream().processWith { out =>
      	SpreadsheetPersister.persist(Seq(Company("Z", 2.0, 2009)), out)
      	assert(out.size > 0)
      }
    }
    
  } 
}