name := "asyncdb"

scalaVersion := "2.12.4"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-effect" % "0.4",
  "org.scalatest" %% "scalatest"   % "3.0.3" % "test"
)

scalacOptions ++= Seq(
  "-feature",
  "-deprecation"
)

scalafmtVersion in ThisBuild := "1.1.0"
scalafmtOnCompile in ThisBuild := true
