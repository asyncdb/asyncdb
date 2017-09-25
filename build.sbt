name := "asyncdb"

scalaVersion := "2.12.3"

val fs2Version = "0.10.0-M2"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-effect" % "0.4",
  "org.scalatest" %% "scalatest" % "3.0.3" % "test"
)

scalacOptions ++= Seq(
  "-feature",
  "-deprecation"
)
