name := "asyncdb"

scalaVersion := "2.12.4"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-effect" % "1.0.0-RC",
  "org.scalatest" %% "scalatest"   % "3.0.3" % Test
)

scalacOptions ++= Seq(
  "-language:higherKinds",
  "-feature",
  "-deprecation"
)

fork in Test := true

scalafmtOnCompile in ThisBuild := true
