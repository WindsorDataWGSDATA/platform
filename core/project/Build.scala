import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "WindsorData"
  val appVersion      = "1.0-SNAPSHOT"

  lazy val scct_settings = Defaults.defaultSettings ++ Seq(ScctPlugin.instrumentSettings: _*)

  val appDependencies = Seq(
     "org.apache.poi" % "poi" % "3.9",
     "org.apache.poi" % "poi-ooxml" % "3.9",
     "org.apache.poi" % "poi-ooxml-schemas" % "3.9",
     "org.mongodb" %% "casbah" % "2.5.0",
     "org.scalatest" % "scalatest_2.10.0-RC5" % "2.0.M5-B1",
     "junit" % "junit" % "4.8.1" % "test",
     "org.scalamock" %% "scalamock-scalatest-support" % "3.0.1" % "test"
//     anorm,
//     jdbc
  )



  val main = play.Project(appName, appVersion, appDependencies, settings = scct_settings).settings( 
   testOptions in Test := Nil
  )

}
