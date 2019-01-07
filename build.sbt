name := "asyncdb"

scalaVersion := "2.12.8"

libraryDependencies ++= Seq(
  "io.netty"      % "netty-transport" % "4.1.32.Final",
  "io.netty"      % "netty-handler"   % "4.1.32.Final",
  "org.typelevel" %% "cats-effect"    % "1.1.0",
  "org.typelevel" %% "cats-core"      % "1.5.0",
  "org.typelevel" %% "cats-free"      % "1.5.0",
  "com.chuusai"   %% "shapeless"      % "2.3.3",
  "org.scalatest" %% "scalatest"      % "3.0.5" % Test
)

scalacOptions ++= Seq(
  "-language:higherKinds",
  "-feature",
  "-deprecation"
)

fork in Test := true

(scalafmtOnCompile in ThisBuild) := true
(compile in Compile) := {
  (compile in Compile).dependsOn(scalafmtSbt in Compile).value
}
