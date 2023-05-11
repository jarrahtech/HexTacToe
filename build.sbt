import org.scalajs.linker.interface.ModuleSplitStyle

ThisBuild / scalaVersion := "3.2.2"
ThisBuild / organization := "com.jarrahtechnology"
ThisBuild / versionScheme := Some("early-semver")

Global / stQuiet := true

lazy val hextactoe = project.in(file("."))
  .enablePlugins(BuildInfoPlugin)
  .enablePlugins(ScalaJSPlugin) 
  .enablePlugins(ScalablyTypedConverterExternalNpmPlugin)
  .settings(
    name := "hextactoe",
    version := "0.1.0",

    scalacOptions ++= Seq(
      "-encoding", "utf8",
      "-Xfatal-warnings",
      "-deprecation",
    ),

    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= {
      _.withModuleKind(ModuleKind.ESModule)
        .withModuleSplitStyle(
          ModuleSplitStyle.SmallModulesFor(List("hextactoe")))
    },
    externalNpm := baseDirectory.value,

    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion, "baseUrl" -> sys.env.getOrElse("BASE_URL", "/")),
    buildInfoOptions += BuildInfoOption.BuildTime,
    buildInfoPackage := "hextactoe",

    resolvers ++= Resolver.sonatypeOssRepos("public"),
    resolvers += Resolver.githubPackages("jarrahtech"),

    githubOwner := "jarrahtech",
    githubRepository := "hextactoe",

    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "2.4.0",
    libraryDependencies += "com.jarrahtechnology" %%% "hex" % "0.3.2",
    libraryDependencies += "com.jarrahtechnology" %%% "kassite" % "0.1.3",
  )