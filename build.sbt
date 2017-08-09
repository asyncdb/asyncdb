name := "fs2-db"

scalaVersion := "2.11.11"

val fs2Version = "0.10.0-M2"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-effect" % "0.4",
  "org.scalatest" %% "scalatest" % "3.0.3" % "test"
)

scalacOptions ++= Seq(
  "-feature",
  "-deprecation"
)
