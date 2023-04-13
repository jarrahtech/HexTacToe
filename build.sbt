import org.scalajs.linker.interface.ModuleSplitStyle

ThisBuild / scalaVersion := "3.2.2"
ThisBuild / organization := "com.jarrahtechnology"
ThisBuild / versionScheme := Some("early-semver")

Global / stQuiet := true

lazy val hextactoe = project.in(file("."))
  .enablePlugins(ScalaJSPlugin) 
  .enablePlugins(ScalablyTypedConverterExternalNpmPlugin)
  .settings(
    name := "hextactoe",
    version := "0.1.0",

    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= {
      _.withModuleKind(ModuleKind.ESModule)
        .withModuleSplitStyle(
          ModuleSplitStyle.SmallModulesFor(List("hextactoe")))
    },
    externalNpm := baseDirectory.value,

    githubOwner := "jarrahtech",
    githubRepository := "hex",

    resolvers ++= Resolver.sonatypeOssRepos("public"),
    resolvers += Resolver.githubPackages("jarrahtech"),

    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "2.4.0",
    libraryDependencies += "com.raquo" %%% "laminar" % "15.0.1",
    //libraryDependencies += ("com.lihaoyi" %%% "scalatags" % "0.12.0"),
  )