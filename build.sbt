name := "asyncdb"

scalaVersion := "2.12.7"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-effect" % "1.1.0",
  "org.scalatest" %% "scalatest"   % "3.0.5" % Test,
  "org.typelevel" %% "cats-core"   % "1.5.0",
  "org.typelevel" %% "cats-free"   % "1.5.0"
)

scalacOptions ++= Seq(
  "-language:higherKinds",
  "-feature",
  "-deprecation"
)

fork in Test := true

scalafmtOnCompile in ThisBuild := true
