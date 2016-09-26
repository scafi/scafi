logLevel := Level.Warn

// Create a fat JAR of a project with all of its dependencies
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.3")

// Provide PGP signing (publishSigned)
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.0.0")

// Create Eclipse project definitions
addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "4.0.0")

// Provide support for Scalastyle
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.8.0")

// Publish projects to the Maven Central Repository (sonatypeRelease)
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "1.1")
