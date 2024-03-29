package model

import libt.error._
import libt._

package object validation {

  //TODO move out
  implicit class ValidableModel(val model:Model) {
    def validate(route: Symbol)(validation: => Validated[Model]): Validated[Model] =
      if (!model.contains(route)) Valid(model) else validation
  }

  implicit class Validable[A](val any:A) {
    def validateWhen(condition: => Boolean)(validation: => Validated[A]) : Validated[A] =
      if (!condition) Valid(any) else validation
    def validateUnless(condition: => Boolean)(validation: => Validated[A]) : Validated[A] =
      validateWhen(!condition)(validation)
  }
  
  implicit def validatedModels2reducedValidatedModel(models: Seq[Validated[Model]]) = new {
    def reduceValidations(default: Model): Validated[Model] = 
      if(models.isEmpty) Valid(default)
      else models.reduce( (a, b) => a andThen b)
  }

  def execMsg(year: Int, m: Model) =
    year + " - " + (m /!/ 'firstName) + (m /!/ 'lastName) + " - "

  def nonEmptyExecutive(exec: Element) = Seq('firstName, 'lastName).forall(exec nonEmpty _)

  def reduceExecutiveValidations(path: Path, model: Model)(action: (Element) => Validated[Model]) = {
    val results: Seq[Validated[Model]] = model.applySeq(path).map { m => action(m) }
    results.reduceValidations(model)
  }
  
  def threeDigitValidation(basePath: Path, valuePaths: Seq[Path], model: Model) = 
    digitValidation(basePath, valuePaths, model)(_ >= 100)

  def digitValidation(basePath: Path, valuePaths: Seq[Path], model: Model)(digitValidation: (BigDecimal => Boolean)) =
    reduceExecutiveValidations(basePath, model) {
      m =>
        {
          val results: Seq[Validated[Model]] =
            valuePaths.map(path => m(path).rawValue[BigDecimal] match {
              case Some(salary) if !digitValidation(salary) =>
                Doubtful(model,
                  "Warning on ExecDb " + execMsg(model /#/ 'disclosureFiscalYear, m.asModel) +
                    ": " + valuePaths.map(_.titles).mkString(" - ") + " should be 3 digits or more")
              case _ => Valid(model)
            })
          results.reduceValidations(model)
        }
    }
  def warning(id: String, msg: String) = s"Warning on $id: $msg"
  def err(id: String, msg: String) = s"Error on $id: $msg"
}