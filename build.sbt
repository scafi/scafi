import ReleaseTransformations._
import sbtrelease.ReleasePlugin.autoImport.releaseProcess

// Resolvers
resolvers += Resolver.sonatypeRepo("snapshots")
resolvers += Resolver.typesafeRepo("releases")

// Constants
val akkaVersion = "2.5.0" // NOTE: Akka 2.4.0 REQUIRES Java 8!

// Managed dependencies
val akkaActor  = "com.typesafe.akka" %% "akka-actor"  % akkaVersion
val akkaRemote = "com.typesafe.akka" %% "akka-remote" % akkaVersion
val bcel       = "org.apache.bcel"   % "bcel"         % "5.2"
val scalatest  = "org.scalatest"     %% "scalatest"   % "3.0.0"     % "test"
val scopt      = "com.github.scopt"  %% "scopt"       % "3.5.0"
val shapeless  = "com.chuusai"       %% "shapeless"   % "2.3.2"

lazy val sharedPublishSettings = Seq(
  sonatypeProfileName := "it.unibo.apice.scafiteam", // Your profile name of the sonatype account
  publishMavenStyle := true, // ensure POMs are generated and pushed
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false }, // no repositories show up in the POM file
  licenses := Seq("Apache 2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0")),
  homepage := Some(url("https://scafi.github.io/")),
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/scafi/scafi"),
      "scm:git:git@github.org:scafi/scafi.git"
    )
  ),
  developers := List(
    Developer(id="metaphori", name="Roberto Casadei", email="roby.casadei@unibo.it", url=url("http://robertocasadei.apice.unibo.it")),
    Developer(id="mviroli", name="Mirko Viroli", email="mirko.viroli@unibo.it", url=url("http://mirkoviroli.apice.unibo.it"))
  ),
  // Add sonatype repository settings
  publishTo := Some(
    if (isSnapshot.value)
      Opts.resolver.sonatypeSnapshots
    else
      Opts.resolver.sonatypeStaging
  ),
  // Enable cross release
  releaseCrossBuild := true,
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    runTest,
    setReleaseVersion,
    //commitReleaseVersion,
    //tagRelease,
    ReleaseStep(action = Command.process("publishSigned", _), enableCrossBuild = true)
    //setNextVersion,
    //commitNextVersion,
    //ReleaseStep(action = Command.process("sonatypeReleaseAll", _), enableCrossBuild = true),
    //pushChanges
  )
)

lazy val compileScalastyle = taskKey[Unit]("compileScalastyle")

lazy val commonSettings = Seq(
  organization := "it.unibo.apice.scafiteam",
  scalaVersion := "2.11.8",
  version := "0.3.0",
  compileScalastyle := org.scalastyle.sbt.ScalastylePlugin.scalastyle.in(Compile).toTask("").value,
  (compile in Compile) <<= (compile in Compile) dependsOn compileScalastyle,
  // Cross-Building
  crossScalaVersions := Seq("2.11.8","2.12.2") // "2.13.0-M1"
)

lazy val scafi = project.in(file(".")).
  aggregate(core, distributed, simulator, `simulator-gui`, `stdlib-ext`, `tests`, `demos`).
  settings(commonSettings:_*).
  settings(sharedPublishSettings:_*).
  settings(
    // Prevents aggregated project (root) to be published
    packagedArtifacts in file(".") := Map.empty
  )

lazy val commons = project.
  settings(commonSettings: _*).
  settings(sharedPublishSettings: _*).
  settings(name := "commons")

lazy val core = project.
  dependsOn(commons).
  settings(commonSettings: _*).
  settings(sharedPublishSettings: _*).
  settings(
    name := "scafi-core",
    libraryDependencies += scalatest
  )

lazy val `stdlib-ext` = project.
  dependsOn(core).
  settings(commonSettings: _*).
  settings(sharedPublishSettings: _*).
  settings(
    name := "scafi-lib-ext",
    libraryDependencies ++= Seq(scalatest, shapeless)
  )

lazy val simulator = project.
  dependsOn(core).
  settings(commonSettings: _*).
  settings(sharedPublishSettings: _*).
  settings(
    name := "scafi-simulator"
  )

lazy val `simulator-gui` = project.
  dependsOn(core,simulator).
  settings(commonSettings: _*).
  settings(sharedPublishSettings: _*).
  settings(
    name := "scafi-simulator-gui",
    libraryDependencies ++= Seq(scopt)
  )

lazy val spala = project.
  dependsOn(commons).
  settings(commonSettings: _*).
  settings(sharedPublishSettings: _*).
  settings(
    name := "spala",
    libraryDependencies ++= Seq(akkaActor, akkaRemote, bcel, scopt)
  )

// 'distributed' project definition
lazy val distributed = project.
  dependsOn(core, spala).
  settings(commonSettings: _*).
  settings(sharedPublishSettings: _*).
  settings(name := "scafi-distributed")

// 'tests' project definition
lazy val tests = project.
  dependsOn(core, simulator).
  settings(commonSettings: _*).
  settings(
    name := "scafi-tests",
    libraryDependencies += scalatest
  )

// 'demos' project definition
lazy val demos = project.
  dependsOn(core, `stdlib-ext`, distributed, simulator, `simulator-gui`).
  settings(commonSettings: _*).
  settings(
    name := "scafi-demos"
  )
