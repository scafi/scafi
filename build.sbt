import sbt.Def

// Resolvers
resolvers += Resolver.sonatypeRepo("snapshots")
resolvers += Resolver.typesafeRepo("releases")

// Constants
val defaultScalaVersion = "2.12.14"
val scalaVersionsForCrossCompilation = Seq("2.11.12","2.12.14","2.13.6")
val akkaVersion = "2.5.32" // NOTE: Akka 2.4.0 REQUIRES Java 8! NOTE: Akka 2.6.x drops Scala 2.11

// Managed dependencies
val akkaActor  = "com.typesafe.akka" %% "akka-actor"  % akkaVersion
val akkaRemote = "com.typesafe.akka" %% "akka-remote" % akkaVersion
val bcel       = "org.apache.bcel"   % "bcel"         % "6.5.0"
val scalaLogging  = "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5"
val scalatest  = Def.setting { "org.scalatest"     %%% "scalatest"   % "3.2.10"     % "test" }
val scopt      = "com.github.scopt"  %% "scopt"       % "4.0.1"
val shapeless  = "com.chuusai"       %% "shapeless"   % "2.3.9"
val playJson   = "com.typesafe.play" %% "play-json"   % "2.9.2"
val scalafx = "org.scalafx" %% "scalafx" % "18.0.1-R27"
val slf4jlog4  = "org.slf4j" % "slf4j-log4j12" % "1.7.36"
val log4 = "org.apache.logging.log4j" % "log4j-core" % "2.17.2"
val apacheCommonsMath = "org.apache.commons" % "commons-math3" % "3.6.1"

// Determine OS version of JavaFX binaries
lazy val osName = System.getProperty("os.name") match {
  case n if n.startsWith("Linux") => "linux"
  case n if n.startsWith("Mac") => "mac"
  case n if n.startsWith("Windows") => "win"
  case _ => throw new Exception("Unknown platform!")
}

// JavaFX dependencies (Java 11)
lazy val javaFXModules = Seq("base", "controls", "graphics", "media", "swing", "web")
lazy val javaFX = if(scala.util.Try(jdkVersion.toInt).getOrElse(0) >= 11) {
  javaFXModules.map(m => "org.openjfx" % s"javafx-$m" % (11 + "+") classifier osName)
} else {
  Seq()
}

lazy val javaVersion = System.getProperty("java.version").stripPrefix("openjdk")
lazy val jdkVersion = javaVersion.split('.').headOption.getOrElse(if(javaVersion.isEmpty) "11" else javaVersion)

inThisBuild(List(
  sonatypeProfileName := "it.unibo.scafi", // Your profile name of the sonatype account
  Test / publishArtifact := false,
  pomIncludeRepository := { _ => false }, // no repositories show up in the POM file
  licenses := Seq("Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0")),
  homepage := Some(url("https://scafi.github.io/")),
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/scafi/scafi"),
      "scm:git:git@github.org:scafi/scafi.git"
    )
  ),
  developers := List(
    Developer(id="metaphori", name="Roberto Casadei", email="roby.casadei@unibo.it", url=url("http://robertocasadei.apice.unibo.it")),
    Developer(id="cric96", name="Gianluca Aguzzi", email="gianluca.aguzzi@unibo.it", url=url("https://cric96.github.io/")),
    Developer(id="mviroli", name="Mirko Viroli", email="mirko.viroli@unibo.it", url=url("http://mirkoviroli.apice.unibo.it"))
  ),
  scalaVersion :=  defaultScalaVersion,
  scalafixScalaBinaryVersion := "2.12",
  scapegoatVersion := "1.4.11",
  coverageExcludedPackages := "<empty>;demos.*;examples.*;.*frontend.*;sims.*;monitoring.*;plainSim.*;lib.*;.*renderer3d.*"
))

lazy val compileScalastyle = taskKey[Unit]("compileScalastyle")

lazy val commonSettings = Seq(
  organization := "it.unibo.scafi",
  Test / scalastyleConfig := file("./scalastyle-test-config.xml"),
  compileScalastyle := (Test / scalastyle).toTask("").value,
  crossScalaVersions := scalaVersionsForCrossCompilation, // "2.13.0-M1"
  Test / test := ((Test / test) dependsOn compileScalastyle).value,
  (assembly / assemblyJarName) := s"${name.value}_${CrossVersion.binaryScalaVersion(scalaVersion.value)}-${version.value}-assembly.jar",
  (assembly / assemblyMergeStrategy) := {
    case x =>
      val oldStrategy = (assembly / assemblyMergeStrategy).value
      oldStrategy(x)
  }
)

lazy val noPublishSettings = Seq(
  publishArtifact := false,
  publish := { },
  publishLocal := { },
  crossScalaVersions := Seq()
)

lazy val scafi = project.in(file("."))
  .aggregate(core, commons, spala, distributed, simulator, `simulator-gui`, `renderer-3d`, `stdlib-ext`, `tests`, `demos`,
   `simulator-gui-new`, `demos-new`, `demos-distributed`, coreCross.js, commonsCross.js, simulatorCross.js, testsCross.js)
  .enablePlugins(ScalaUnidocPlugin, ClassDiagramPlugin, GhpagesPlugin)
  .settings(commonSettings:_*)
  .settings(noPublishSettings:_*)
  .settings(
    // Prevents aggregated project (root) to be published
    packagedArtifacts := Map.empty,
    crossScalaVersions := Nil, // NB: Nil to prevent double publishing!
    ScalaUnidoc / unidoc / unidocProjectFilter := inProjects(commons, core, simulator, spala, `simulator-gui`,
      `stdlib-ext`, `renderer-3d`, distributed),
    git.remoteRepo := "git@github.com:scafi/doc.git",
    ScalaUnidoc / siteSubdirName := "latest/api",
    addMappingsToSiteDir(ScalaUnidoc / packageDoc / mappings, ScalaUnidoc / siteSubdirName)
  )

lazy val commonsCross = crossProject(JSPlatform, JVMPlatform).in(file("commons"))
  .settings(commonSettings: _*)
  .settings(
    name := "scafi-commons"
  )
  .jsSettings(
    libraryDependencies += "org.scala-js" %%% "scalajs-java-time" % "1.0.0"
  )

lazy val commons = commonsCross.jvm

lazy val coreCross = crossProject(JSPlatform, JVMPlatform).in(file("core"))
  .dependsOn(commonsCross)
  .settings(commonSettings: _*)
  .settings(
    name := "scafi-core",
    libraryDependencies += scalatest.value
  )

lazy val core = coreCross.jvm

lazy val `stdlib-ext` = project
  .dependsOn(core)
  .settings(commonSettings: _*)
  .settings(
    name := "scafi-lib-ext",
    libraryDependencies ++= Seq(scalatest.value, shapeless)
  )

lazy val simulatorCross = crossProject(JSPlatform, JVMPlatform).in(file("simulator"))
  .dependsOn(coreCross)
  .settings(commonSettings: _*)
  .settings(
    name := "scafi-simulator",
  )
  .jsSettings(
    libraryDependencies += "org.scala-js" %%% "scalajs-java-time" % "1.0.0"
  )

lazy val simulator = simulatorCross.jvm

lazy val `simulator-gui` = project
  .dependsOn(core, simulator, `renderer-3d`)
  .settings(commonSettings: _*)
  .settings(
    name := "scafi-simulator-gui",
    libraryDependencies ++= Seq(scopt),
    compileScalastyle := { }
  )

lazy val `renderer-3d` = project
  .dependsOn()
  .settings(commonSettings: _*)
  .settings(
    name := "scafi-3d-renderer",
    libraryDependencies ++= Seq(
      scalaBinaryVersion.value match {
        case "2.13" => "org.scalafx" %% "scalafx" % "12.0.2-R18"
        case _ => "org.scalafx" %% "scalafx" % "8.0.144-R12"
      },
      scalaLogging) ++ javaFX
  )

lazy val spala = project
  .dependsOn(commons)
  .settings(commonSettings: _*)
  .settings(
    name := "spala",
    //crossScalaVersions := scalaVersionsForCrossCompilation.filter(!_.startsWith("2.13")),
    libraryDependencies ++= Seq(akkaActor, akkaRemote, bcel, scopt,
      scalaBinaryVersion.value match {
        case "2.11" => "com.typesafe.play" %% "play-json"   % "2.6.9"
        case "2.12" => "com.typesafe.play" %% "play-json"   % "2.8.1"
        case "2.13" => "com.typesafe.play" %% "play-json"   % "2.8.1"
      }
      , slf4jlog4, log4)
  )

lazy val distributed = project
  .dependsOn(core, spala)
  .settings(commonSettings: _*)
  .settings(
    name := "scafi-distributed",
    //crossScalaVersions := scalaVersionsForCrossCompilation.filter(!_.startsWith("2.13")),
    libraryDependencies += scalatest.value
  )

lazy val testsCross = crossProject(JSPlatform, JVMPlatform).in(file("tests"))
  .dependsOn(coreCross, simulatorCross)
  .settings(commonSettings: _*)
  .settings(noPublishSettings: _*)
  .settings(
    name := "scafi-tests",
    libraryDependencies ++= Seq(scalatest.value)
  )

lazy val tests = testsCross.jvm

lazy val demos = project
  .dependsOn(core, `stdlib-ext`, simulator, `simulator-gui`)
  .enablePlugins(ClassDiagramPlugin)
  .settings(commonSettings: _*)
  .settings(noPublishSettings: _*)
  .settings(
    name := "scafi-demos",
    compileScalastyle := { }
  )

lazy val `demos-distributed` = project
  .dependsOn(core, `stdlib-ext`, distributed)
  .enablePlugins(ClassDiagramPlugin)
  .settings(commonSettings: _*)
  .settings(noPublishSettings: _*)
  .settings(
    name := "scafi-demos-distributed",
    //crossScalaVersions := scalaVersionsForCrossCompilation.filter(!_.startsWith("2.13")),
    compileScalastyle := { }
  )

lazy val `simulator-gui-new` = project
  .dependsOn(core,simulator,distributed)
  .settings(commonSettings: _*)
  .settings(
    name := "simulator-gui-new",
    //crossScalaVersions := scalaVersionsForCrossCompilation.filter(!_.startsWith("2.13")),
    libraryDependencies ++= Seq(scopt,scalatest.value,
      scalaBinaryVersion.value match {
        case "2.13" => "org.scalafx" %% "scalafx" % "12.0.2-R18"
        case _ => "org.scalafx" %% "scalafx" % "8.0.144-R12"
      }
    ) ++ javaFX,
    compileScalastyle := { }
  )

lazy val `demos-new` = project
  .dependsOn(core, `stdlib-ext`, distributed, simulator, `simulator-gui-new`)
  .enablePlugins(ClassDiagramPlugin)
  .settings(commonSettings: _*)
  .settings(noPublishSettings: _*)
  .settings(
    name := "scafi-demos-new",
    //crossScalaVersions := scalaVersionsForCrossCompilation.filter(!_.startsWith("2.13")),
    compileScalastyle := { }
  )
