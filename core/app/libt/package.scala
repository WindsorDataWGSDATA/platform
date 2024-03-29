import libt.spreadsheet.Feature
import play.api.libs.json._
import play.api.libs.json.Json._

package object libt {

  /**
   * A path is a pointer to an element inside another element.
   */
  type Path = List[PathPart]

  /* Small DSL for declaring paths  */

  implicit def symbol2Route(s: Symbol) = Route(s)

  implicit def int2Index(i: Int) = Index(i)

  implicit def path2RichPath(path: Path) = new {
    def titles = path.map(_.name)
    def joinWithDots = path.map(_.routeValue.name).mkString(".")
  }

  object Path {
    def apply(parts: PathPart*): Path = List(parts: _*)
  }

  def Relative(base: Path, relativePaths: Path*) = relativePaths.map {base ++ _}

  def RelativeTo(base:Symbol)(ps:Seq[PathPart]) : Path = (base :: Path(ps:_*))

  /**
   * A PK is just a sequence of Paths that represent
   * the primary identifier an element
   */
  type PK = Seq[Path]

  implicit class RichPK(val self:PK) {
    def writeOps(schema: TElement, element: Element) = self.map {
      Feature(_).writeOps(schema, element).value
    }
  }

  object PK {
    def apply(elements: Path*) : PK = elements
  }
}