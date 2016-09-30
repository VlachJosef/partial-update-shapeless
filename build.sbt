import play.sbt.routes.RoutesKeys._

lazy val commonSettings = Seq(
  version := "1.0",
  scalaVersion := "2.11.8",
  libraryDependencies ++= Seq(
    "com.chuusai" %% "shapeless" % "2.3.2",
    "org.typelevel" %% "cats" % "0.7.2",
    "org.scalacheck" %% "scalacheck" % "1.13.2",
    "org.scalatest" %% "scalatest" % "3.0.0" % "test"
    )
)

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(commonSettings: _*)
  .settings(
    name := "partial-update-shapeless",
    routesImport ++= Seq(
      "tags._",
      "binders.IdBinder._"
    )
    //scalacOptions += "-Xlog-implicits"
    //scalacOptions in Compile += "-Xprint:typer"
  )
