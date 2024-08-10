import sbt.Def

// Resolvers
resolvers += Resolver.sonatypeRepo("snapshots")
resolvers += Resolver.typesafeRepo("releases")

// Constants
val defaultScalaVersion = "2.13.6"
val scalaVersionsForCrossCompilation = Seq("2.11.12", "2.12.14", defaultScalaVersion)
val akkaVersion = "2.5.32" // NOTE: Akka 2.4.0 REQUIRES Java 8! NOTE: Akka 2.6.x drops Scala 2.11

// Managed dependencies
val akkaActor = "com.typesafe.akka" %% "akka-actor" % akkaVersion
val akkaRemote = "com.typesafe.akka" %% "akka-remote" % akkaVersion
val bcel = "org.apache.bcel" % "bcel" % "6.10.0"
val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5"
val scalatest = Def.setting("org.scalatest" %%% "scalatest" % "3.2.19" % "test")
val scopt = "com.github.scopt" %% "scopt" % "4.1.0"
val shapeless = "com.chuusai" %% "shapeless" % "2.3.11"
val playJson = "com.typesafe.play" %% "play-json" % "2.9.4"
val slf4jlog4 = "org.slf4j" % "slf4j-log4j12" % "2.0.16"
val log4 = "org.apache.logging.log4j" % "log4j-core" % "2.23.1"
val apacheCommonsMath = "org.apache.commons" % "commons-math3" % "3.6.1"
// ScalaFX dependency management
val javaFXVersion = Def.setting(if (scalaVersion.value == "2.11.12") "15.0.1" else "18.0.1")
val scalaFXVersion = Def.setting {
  if (javaFXVersion.value == "18.0.1") { "18.0.1-R27" }
  else { "15.0.1-R21" }
}
lazy val javaFXModules = "base" :: "controls" :: "graphics" :: "media" :: "swing" :: "web" :: Nil
lazy val platforms = "linux" :: "mac" :: "win" :: Nil
val scalaFX = Def.setting("org.scalafx" %% "scalafx" % scalaFXVersion.value)
val javaFXBinary = Def.setting {
  for {
    fxModule <- javaFXModules
    platform <- platforms
  } yield "org.openjfx" % s"javafx-$fxModule" % javaFXVersion.value classifier platform
}
inThisBuild(
  List(
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
      Developer(
        id = "metaphori",
        name = "Roberto Casadei",
        email = "roby.casadei@unibo.it",
        url = url("https://robertocasadei.github.io")
      ),
      Developer(
        id = "cric96",
        name = "Gianluca Aguzzi",
        email = "gianluca.aguzzi@unibo.it",
        url = url("https://cric96.github.io/")
      ),
      Developer(
        id = "mviroli",
        name = "Mirko Viroli",
        email = "mirko.viroli@unibo.it",
        url = url("http://mirkoviroli.apice.unibo.it")
      )
    ),
    scalaVersion := defaultScalaVersion,
    scalafixScalaBinaryVersion := "2.12",
    scapegoatVersion := "1.4.11",
    coverageExcludedPackages := "<empty>;demos.*;examples.*;.*frontend.*;sims.*;monitoring.*;plainSim.*;lib.*;.*renderer3d.*"
  )
)

lazy val scalacProjectOption = Def.setting {
  val message = "The outer reference in this type test cannot be checked at run time." // see https://github.com/scala/bug/issues/4440
  scalaBinaryVersion.value match {
    case "2.13" | "2.12" => Seq(s"-Wconf:msg=${message}:s")
    case "2.11" => Seq("-nowarn") // avoid warning for 2.11 since warnings could conflict with 2.12 and 2.13
  }
}
lazy val compileScalastyle = taskKey[Unit]("compileScalastyle")

lazy val commonSettings = Seq(
  organization := "it.unibo.scafi",
  Test / scalastyleConfig := file("./scalastyle-test-config.xml"),
  compileScalastyle := (Test / scalastyle).toTask("").value,
  crossScalaVersions := scalaVersionsForCrossCompilation,
  Test / test := ((Test / test) dependsOn compileScalastyle).value,
  scalacOptions ++= scalacProjectOption.value,
  (assembly / assemblyJarName) := s"${name.value}_${CrossVersion.binaryScalaVersion(scalaVersion.value)}-${version.value}-assembly.jar",
  (assembly / assemblyMergeStrategy) := { x =>
    val oldStrategy = (assembly / assemblyMergeStrategy).value
    oldStrategy(x)
  }
)

lazy val noPublishSettings = Seq(
  publishArtifact := false,
  publish := {},
  publishLocal := {}
)

lazy val noPublishSettingsRoot = noPublishSettings ++ Seq(crossScalaVersions := Seq.empty)

lazy val scafiJVM: Seq[ProjectReference] = Seq(core, commons, spala, distributed, simulator, `simulator-gui`,
  `renderer-3d`, `stdlib-ext`, `tests`, `demos`, `simulator-gui-new`, `demos-new`, `demos-distributed`)

lazy val scafiJS: Seq[ProjectReference] = Seq(coreCross.js, commonsCross.js, simulatorCross.js, testsCross.js)

lazy val scafi = project
  .in(file("."))
  .aggregate(scafiJVM ++ scafiJS: _*)
  .enablePlugins(ScalaUnidocPlugin, ClassDiagramPlugin, GhpagesPlugin)
  .settings(commonSettings: _*)
  .settings(noPublishSettings: _*)
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

lazy val commonsCross = crossProject(JSPlatform, JVMPlatform)
  .in(file("commons"))
  .settings(commonSettings: _*)
  .settings(
    name := "scafi-commons"
  )
  .jsSettings(
    libraryDependencies += "org.scala-js" %%% "scalajs-java-time" % "1.0.0"
  )

lazy val commons = commonsCross.jvm

lazy val coreCross = crossProject(JSPlatform, JVMPlatform)
  .in(file("core"))
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

lazy val simulatorCross = crossProject(JSPlatform, JVMPlatform)
  .in(file("simulator"))
  .dependsOn(coreCross)
  .settings(commonSettings: _*)
  .settings(
    name := "scafi-simulator"
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
    libraryDependencies ++= Seq(scopt, scalaLogging),
    compileScalastyle := {}
  )

lazy val `renderer-3d` = project
  .dependsOn()
  .settings(commonSettings: _*)
  .settings(
    name := "scafi-3d-renderer",
    libraryDependencies ++= Seq(scalaFX.value, scalaLogging) ++ javaFXBinary.value
  )

lazy val spala = project
  .dependsOn(commons)
  .settings(commonSettings: _*)
  .settings(
    name := "spala",
    libraryDependencies ++= Seq(
      akkaActor,
      akkaRemote,
      bcel,
      scopt,
      scalaBinaryVersion.value match {
        case "2.11" => "com.typesafe.play" %% "play-json" % "2.6.9"
        case "2.12" | "2.13" => "com.typesafe.play" %% "play-json" % "2.8.2"
      },
      slf4jlog4,
      log4
    )
  )

lazy val distributed = project
  .dependsOn(core, spala)
  .settings(commonSettings: _*)
  .settings(
    name := "scafi-distributed",
    libraryDependencies += scalatest.value
  )

lazy val testsCross = crossProject(JSPlatform, JVMPlatform)
  .in(file("tests"))
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
    compileScalastyle := {}
  )

lazy val `demos-distributed` = project
  .dependsOn(core, `stdlib-ext`, distributed)
  .enablePlugins(ClassDiagramPlugin)
  .settings(commonSettings: _*)
  .settings(noPublishSettings: _*)
  .settings(
    name := "scafi-demos-distributed",
    compileScalastyle := {}
  )

lazy val `simulator-gui-new` = project
  .dependsOn(core, simulator, distributed)
  .settings(commonSettings: _*)
  .settings(
    name := "simulator-gui-new",
    libraryDependencies ++= Seq(scopt, scalatest.value, scalaFX.value) ++ javaFXBinary.value,
    compileScalastyle := {}
  )

lazy val `demos-new` = project
  .dependsOn(core, `stdlib-ext`, distributed, simulator, `simulator-gui-new`)
  .enablePlugins(ClassDiagramPlugin)
  .settings(commonSettings: _*)
  .settings(noPublishSettings: _*)
  .settings(
    name := "scafi-demos-new",
    compileScalastyle := {}
  )
