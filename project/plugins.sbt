logLevel := Level.Warn

// Create Eclipse project definitions
addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "5.2.4")

// Provide support for Scalastyle
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.1")

// Provide PGP signing (publishSigned)
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.1.2")

// Publish projects to the Maven Central Repository (sonatypeRelease)
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "2.3")

// Provides a customizable release process
addSbtPlugin("ch.epfl.scala" % "sbt-release-early" % "2.1.0")

// Create a fat JAR of a project with all of its dependencies
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.7")

addSbtPlugin("com.eed3si9n" % "sbt-unidoc" % "0.4.1")

