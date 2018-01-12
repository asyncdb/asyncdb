name := "asyncdb"

scalaVersion := "2.12.4"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-effect" % "0.8",
  "org.scalatest" %% "scalatest"   % "3.0.3" % "test"
)

scalacOptions ++= Seq(
  "-language:higherKinds",
  "-feature",
  "-deprecation"
)

scalafmtOnCompile in ThisBuild := true
