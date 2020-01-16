// Resolvers
resolvers += Resolver.sonatypeRepo("snapshots")
resolvers += Resolver.typesafeRepo("releases")

// Constants
val akkaVersion = "2.5.0" // NOTE: Akka 2.4.0 REQUIRES Java 8!

// Managed dependencies
val akkaActor     = "com.typesafe.akka" %% "akka-actor"  % akkaVersion
val akkaRemote    = "com.typesafe.akka" %% "akka-remote" % akkaVersion
val bcel          = "org.apache.bcel"   % "bcel"         % "5.2"
val scalaLogging   = "com.typesafe.scala-logging" %% "scala-logging"  % "3.9.2"
val scalatest     = "org.scalatest"     %% "scalatest"   % "3.0.0"     % "test"
val scopt         = "com.github.scopt"  %% "scopt"       % "3.5.0"
val shapeless     = "com.chuusai"       %% "shapeless"   % "2.3.2"
val playJson      = "com.typesafe.play" %% "play-json"   % "2.6.9"
val scalafx       = "org.scalafx"       %% "scalafx"     % "8.0.144-R12"
val scalafx12     = "org.scalafx"       %% "scalafx"     % "12.0.2-R18"
val slf4jlog4  = "org.slf4j" % "slf4j-log4j12" % "1.7.26"
val log4 = "log4j" % "log4j" % "1.2.17"

// Determine OS version of JavaFX binaries
lazy val osName = System.getProperty("os.name") match {
  case n if n.startsWith("Linux") => "linux"
  case n if n.startsWith("Mac") => "mac"
  case n if n.startsWith("Windows") => "win"
  case _ => throw new Exception("Unknown platform!")
}

// JavaFX dependencies (Java 11)
lazy val javaFXModules = Seq("base", "controls", "graphics", "media", "swing", "web")

lazy val javaVersion = System.getProperty("java.version").stripPrefix("openjdk")
lazy val jdkVersion = javaVersion.split('.').headOption.getOrElse(if(javaVersion.isEmpty) "11" else javaVersion)

inThisBuild(List(
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
  releaseEarlyWith := SonatypePublisher,
  releaseEarlyEnableLocalReleases := true,
  publishTo := Some(
    if (isSnapshot.value)
      Opts.resolver.sonatypeSnapshots
    else
      Opts.resolver.sonatypeStaging
  ),
  pgpPublicRing := file("./.travis/local.pubring.asc"),
  pgpSecretRing := file("./.travis/local.secring.asc")
))

lazy val compileScalastyle = taskKey[Unit]("compileScalastyle")

lazy val commonSettings = Seq(
  organization := "it.unibo.apice.scafiteam",
  scalaVersion := "2.12.10",
  compileScalastyle := scalastyle.in(Compile).toTask("").value,
  (assemblyJarName in assembly) := s"${name.value}_${CrossVersion.binaryScalaVersion(scalaVersion.value)}-${version.value}-assembly.jar",
  (compile in Compile) := ((compile in Compile) dependsOn compileScalastyle).value,
  crossScalaVersions := Seq("2.11.12","2.12.10") // "2.13.0-M1"
)

lazy val noPublishSettings = Seq(
    publishArtifact := false,
    publish := (),
    publishLocal := ()
  )

lazy val scafi = project.in(file(".")).
  enablePlugins(ScalaUnidocPlugin).
  aggregate(core, commons, spala, distributed, simulator, `simulator-gui`, `renderer-3d`, `stdlib-ext`, `tests`, `demos`,
   `simulator-gui-new`, `demos-new`).
  settings(commonSettings:_*).
  settings(noPublishSettings:_*).
  settings(
    // Prevents aggregated project (root) to be published
    packagedArtifacts := Map.empty,
    unidocProjectFilter in (ScalaUnidoc, unidoc) := inAnyProject -- inProjects(tests,demos)
  )

lazy val commons = project.
  settings(commonSettings: _*).
  settings(name := "scafi-commons")

lazy val core = project.
  dependsOn(commons).
  settings(commonSettings: _*).
  settings(
    name := "scafi-core",
    libraryDependencies += scalatest
  )

lazy val `stdlib-ext` = project.
  dependsOn(core).
  settings(commonSettings: _*).
  settings(
    name := "scafi-lib-ext",
    libraryDependencies ++= Seq(scalatest, shapeless)
  )

lazy val simulator = project.
  dependsOn(core).
  settings(commonSettings: _*).
  settings(
    name := "scafi-simulator"
  )

lazy val `simulator-gui` = project.
  dependsOn(core,simulator,`renderer-3d`).
  settings(commonSettings: _*).
  settings(
    name := "scafi-simulator-gui",
    libraryDependencies ++= Seq(scopt),
    compileScalastyle := ()
  )

lazy val `renderer-3d` = project.
  dependsOn().
  settings(commonSettings: _*).
  settings(
    name := "scafi-3d-renderer",
    unmanagedJars in Compile += file("renderer-3d/lib/fxyz3d-0.5.2.jar"),
    libraryDependencies ++= Seq(scalafx12, scalaLogging) ++
      javaFXModules.map(m => "org.openjfx" % s"javafx-$m" % "12.0.2" classifier osName)
  )

lazy val spala = project.
  dependsOn(commons).
  settings(commonSettings: _*).
  settings(
    name := "spala",
    libraryDependencies ++= Seq(akkaActor, akkaRemote, bcel, scopt, playJson, slf4jlog4, log4)
  )

lazy val distributed = project.
  dependsOn(core, spala).
  settings(commonSettings: _*).
  settings(
    name := "scafi-distributed",
    libraryDependencies += scalatest
  )


lazy val tests = project.
  dependsOn(core, simulator).
  settings(commonSettings: _*).
  settings(noPublishSettings: _*).
  settings(
    name := "scafi-tests",
    libraryDependencies += scalatest
  )

lazy val demos = project.
  dependsOn(core, `stdlib-ext`, distributed, simulator, `simulator-gui`).
  settings(commonSettings: _*).
  settings(noPublishSettings: _*).
  settings(
    name := "scafi-demos",
    compileScalastyle := ()
  )

lazy val `simulator-gui-new` = project.
  dependsOn(core,simulator,distributed).
  settings(commonSettings: _*).
  settings(
    name := "simulator-gui-new",
    libraryDependencies ++= Seq(scopt,scalatest,scalafx),
    if(scala.util.Try(jdkVersion.toInt).getOrElse(0) >= 11)
      libraryDependencies ++= javaFXModules.map( m =>
        "org.openjfx" % s"javafx-$m" % jdkVersion classifier osName
      ) else libraryDependencies ++= Seq(),
    compileScalastyle := ()
  )

lazy val `demos-new` = project.
  dependsOn(core, `stdlib-ext`, distributed, simulator, `simulator-gui-new`).
  settings(commonSettings: _*).
  settings(noPublishSettings: _*).
  settings(
    name := "scafi-demos-new",
    compileScalastyle := ()
  )