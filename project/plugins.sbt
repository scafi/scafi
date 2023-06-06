logLevel := Level.Warn

// Repo management

addSbtPlugin("uk.co.randomcoding" % "sbt-git-hooks" % "0.2.0")

// Integrations with IDEs

addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "5.2.4")

// Code quality

addSbtPlugin("com.sksamuel.scapegoat" %% "sbt-scapegoat" % "1.2.1")

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0")

addSbtPlugin("com.github.sbt" % "sbt-cpd" % "2.0.0")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.0.0")

addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.11.0")

// Project documentation

addSbtPlugin("com.eed3si9n" % "sbt-unidoc" % "0.4.3")

addSbtPlugin("com.github.xuwei-k" % "sbt-class-diagram" % "0.2.1") // Note: requires Graphviz to be installed

addSbtPlugin("com.typesafe.sbt" % "sbt-site" % "1.4.1")

addSbtPlugin("com.typesafe.sbt" % "sbt-ghpages" % "0.6.3")

// Packaging

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "2.1.1")

// Release process

addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.9.21")

addSbtPlugin("com.github.sbt" % "sbt-ci-release" % "1.5.10")

// Scala.js plugins

addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.10.1")

addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.3.1")

addSbtPlugin("ch.epfl.scala" % "sbt-scalajs-bundler" % "0.21.1")
