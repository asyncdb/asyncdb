name := "asyncdb"

scalaVersion := "2.12.6"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-effect" % "1.0.0-RC3",
  "com.chuusai"   %% "shapeless"   % "2.3.3",
  "org.scalatest" %% "scalatest"   % "3.0.5" % Test
)

scalacOptions ++= Seq(
  "-language:higherKinds",
  "-feature",
  "-deprecation"
)

fork in Test := true

scalafmtOnCompile in ThisBuild := true
