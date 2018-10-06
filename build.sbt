lazy val scrapper = (project in file("."))
  .disablePlugins(plugins.JUnitXmlReportPlugin)
  .settings(
    name := "scrapper",

    version := "1.5",
    scalaVersion := "2.12.3",

    scalaSource in Compile := baseDirectory.value / "src",
    scalacOptions ++= Seq("-feature"),

    scalaSource in Test := baseDirectory.value / "test" / "scala",
    parallelExecution in Test := false,
    libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test",
    libraryDependencies += "net.ruippeixotog" %% "scala-scraper" % "2.1.0",
    testOptions += Tests.Argument(TestFrameworks.JUnit, "-v")
  )
