import ReleaseTransformations._

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

// Cross-Building
crossScalaVersions := Seq("2.11.8","2.12.2","2.13.0-M1")

// Prevents aggregated project (root) to be published
packagedArtifacts in file(".") := Map.empty

lazy val sharedPublishSettings = Seq(
  sonatypeProfileName := "it.unibo.apice.scafiteam", // Your profile name of the sonatype account
  publishMavenStyle := true, // ensure POMs are generated and pushed
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false }, // no repositories show up in the POM file
  licenses := Seq("Apache 2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0")),
  homepage := Some(url("http://scafi.apice.unibo.it")),
  scmInfo := Some(
    ScmInfo(
      url("https://bitbucket.org/scafiteam/scafi"),
      "scm:git:git@bitbucket.org:scafiteam/scafi.git"
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
  )
)

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  ReleaseStep(action = Command.process("publishSigned", _), enableCrossBuild = true)
  //setNextVersion,
  //commitNextVersion,
  //ReleaseStep(action = Command.process("sonatypeReleaseAll", _), enableCrossBuild = true),
  //pushChanges
)

// Common settings across projects
lazy val commonSettings = Seq(
  organization := "it.unibo.apice.scafiteam",
  scalaVersion := "2.11.8"
) ++ sharedPublishSettings

// 'core' project definition
lazy val core = project.
  settings(commonSettings: _*).
  settings(
    name := "scafi-core",
    version := "0.1.0",
    libraryDependencies += scalatest
  )

// 'simulator' project definition
lazy val simulator = project.
  dependsOn(core).
  settings(commonSettings: _*).
  settings(
    version := "0.1.0",
    name := "scafi-simulator"
  )

// 'distributed' project definition
lazy val distributed = project.
  dependsOn(core).
  settings(commonSettings: _*).
  settings(
    version := "0.1.0",
    name := "scafi-distributed",
    libraryDependencies ++= Seq(akkaActor, akkaRemote, bcel, scopt)
  )

// 'tests' project definition
lazy val tests = project.
  dependsOn(core, simulator).
  settings(commonSettings: _*).
  settings(
    version := "0.1.0",
    name := "scafi-tests",
    libraryDependencies += scalatest,
    packagedArtifacts := Map.empty
  )

// 'demos' project definition
lazy val demos = project.
  dependsOn(core, distributed, simulator).
  settings(commonSettings: _*).
  settings(
    version := "0.1.0",
    name := "scafi-demos"
  )
