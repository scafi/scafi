# scafi (**sca**la **fi**elds) #

**scafi** is a Scala-based library and framework for Aggregate Programming. 
It implements a variant of the Higher-Order Field Calculus (HOFC) operational semantics,
 which is made available as a usable domain-specific language (DSL),
and provides a platform and API for simulating and executing Aggregate Computing systems and applications.

Please refer to [the scafi main website](https://scafi.github.io/) for further information.

## Overview

### Status Badges

#### Stable branch

[![Build Status](https://travis-ci.org/scafi/scafi.svg?branch=master)](https://travis-ci.org/scafi/scafi)

#### Development branch

[![Build Status](https://travis-ci.org/scafi/scafi.svg?branch=develop)](https://travis-ci.org/scafi/scafi)

## Notes for developers

### Usage ###

Steps

* Add the dependency to scafi in your project (e.g., via sbt)


```scala
val scafi_core  = "it.unibo.apice.scafiteam" %% "scafi-core"  % "0.2.0"
val scafi_simulator  = "it.unibo.apice.scafiteam" %% "scafi-simulator"  % "0.2.0"
val scafi_simulator_gui  = "it.unibo.apice.scafiteam" %% "scafi-simulator-gui"  % "0.2.0"
val scafi_platform = "it.unibo.apice.scafiteam" %% "scafi-distributed"  % "0.2.0"

libraryDependencies ++= Seq(scafi_core, scafi_simulator, scafi_platform)
```

* Use the API (e.g., to set up a simple simulation)


```scala
package experiments

import it.unibo.scafi.incarnations.BasicSimulationIncarnation.AggregateProgram

object MyAggregateProgram extends AggregateProgram {
  
  override def main() = gradient(isSource)

  def gradient(source: Boolean): Double =
    rep(Double.PositiveInfinity){ distance => 
      mux(source) { 0.0 } {
        foldhood(Double.PositiveInfinity)(Math.min)(nbr{distance}+nbrRange)
      }
    }

  def isSource = sense[Boolean]("source")
  def nbrRange = nbrvar[Double](NBR_RANGE_NAME)
}

import it.unibo.scafi.simulation.gui.{Launcher, Settings}

object SimulationRunner extends Launcher {
  Settings.Sim_ProgramClass = "experiments.MyAggregateProgram")
  Settings.ShowConfigPanel = true
  launch()
}
```

### Release notes ###

**0.2.0** (2017-06-28)

* several important adjustments to the core operational semantics (and more tests)
* refactoring of the field calculus interpreter
* a basic graphical simulator has been added as a separate module `simulator-gui`
* cross compilation for Scala 2.11 and 2.12

## References ##

* Towards Aggregate Programming in Scala. Roberto Casadei and Mirko Viroli. 
  In First Workshop on Programming Models and Languages for Distributed Computing (PMLDC), 2016.
* Aggregate Programming for the Internet of Things. Jacob Beal, Danilo Pianini, and Mirko Viroli. IEEE Computer, 2015

## Contacts ##

* roby [dot] casadei [at] unibo [dot] it
* mirko [dot] viroli [at] unibo [dot] it

## License ##

scafi is Open Source and available under the Apache 2 License.