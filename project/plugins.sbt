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
addSbtPlugin("ch.epfl.scala" % "sbt-release-early" % "2.1.1+4-9d76569a")

// Create a fat JAR of a project with all of its dependencies
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.7")

// Project documentation plugins

addSbtPlugin("com.eed3si9n" % "sbt-unidoc" % "0.4.2")

// Note: requires Graphviz to be installed
addSbtPlugin("com.github.xuwei-k" % "sbt-class-diagram" % "0.2.1")

// Scala.js plugins

addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.0.1")

addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.0.0")

addSbtPlugin("ch.epfl.scala" % "sbt-scalajs-bundler" % "0.17.0")

// To publish on GH Pages

addSbtPlugin("com.typesafe.sbt" % "sbt-ghpages" % "0.6.3")

// site generation for sbt

addSbtPlugin("com.typesafe.sbt" % "sbt-site" % "1.3.2")

// addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.9.18-1")
