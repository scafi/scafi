// Resolvers
resolvers += Resolver.sonatypeRepo("snapshots") // Sonatype OSS Maven Repository
resolvers += Resolver.typesafeRepo("releases")  // Typesafe Repository

// Constants
val akkaVersion = "2.3.7" // NOTE: Akka 2.4.0 REQUIRES Java 8!

// Managed dependencies
val akkaActor  = "com.typesafe.akka" % "akka-actor_2.11"  % akkaVersion
val akkaRemote = "com.typesafe.akka" % "akka-remote_2.11" % akkaVersion
val bcel       = "org.apache.bcel"   % "bcel"             % "5.2"
val scalatest  = "org.scalatest"     % "scalatest_2.11"   % "2.2.4"     % "test"
val scopt      = "com.github.scopt"  % "scopt_2.11"       % "3.3.0"

// Cross-Building
crossScalaVersions := Seq("2.11.8")

// Maven publishing settings
publishArtifact in Test := false // do not publish test artifacts
publishMavenStyle := true        // ensure POMs are generated and pushed
publishTo := {                   // set up the repository
  val nexus = "https://oss.sonatype.org/" // OSSRH base URL
  if (isSnapshot.value)
    // Deploy to 'snapshots'
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    // Deploy to 'releases'
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

// POM metadata
pomIncludeRepository := { _ => false } // no repositories show up in the POM file
sonatypeProfileName := "org.example"   // profile name of the sonatype account
pomExtra := (
  <url>http://your.project.url</url>
  <licenses>
    <license>
      <name>BSD-style</name>
      <url>http://www.opensource.org/licenses/bsd-license.php</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:your-account/your-project.git</url>
    <connection>scm:git:git@github.com:your-account/your-project.git</connection>
  </scm>
  <developers>
    <developer>
      <id>you</id>
      <name>Your Name</name>
      <url>http://your.url</url>
    </developer>
  </developers>
) // TODO: ADD REAL METADATA -- ASK PROF. MIRKO VIROLI AND ROBERTO CASADEI

// Common settings across projects
lazy val commonSettings = Seq(
  organization := "it.unibo", // organization/group ID
  version := "1.1",           // the version/revision of the current module
  scalaVersion := "2.11.8"    // the version of Scala used for building
)

// 'core' project definition
lazy val core = project.
  settings(commonSettings: _*). // common settings import
  settings(                     // project specific settings
    name := "scafi-core",       // project name
    libraryDependencies += scalatest
  )

// 'simulator' project definition
lazy val simulator = project.
  dependsOn(core). // classpath dependency
  settings(commonSettings: _*).
  settings(
    name := "scafi-simulator"
  )

// 'distributed' project definition
lazy val distributed = project.
  dependsOn(core).
  settings(commonSettings: _*).
  settings(
    name := "scafi-distributed",
    libraryDependencies ++= Seq(akkaActor, akkaRemote, bcel, scopt)
  )

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
  dependsOn(core, distributed, simulator).
  settings(commonSettings: _*).
  settings(
    name := "scafi-demos"
  )
