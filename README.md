# ScaFi (**Sca**la **Fi**elds) #

![workflow-master badge](https://github.com/scafi/scafi/actions/workflows/build-and-deploy.yml/badge.svg?branch=master)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![codecov master](https://codecov.io/gh/scafi/scafi/branch/master/graph/badge.svg?token=RONGUW08K1)](https://codecov.io/gh/scafi/scafi)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/it.unibo.scafi/scafi-core_2.13/badge.svg)](https://maven-badges.herokuapp.com/maven-central/it.unibo.scafi/scafi-core_2.13/badge.svg)

**ScaFi** is a Scala-based library and framework for Aggregate Programming.
It implements a variant of the Higher-Order Field Calculus (HOFC) operational semantics,
 which is made available as a usable domain-specific language (DSL),
and provides a platform and API for simulating and executing Aggregate Computing systems and applications.

Please refer to [the ScaFi main website](https://scafi.github.io/) 
 and [ScaFi Documentation](https://scafi.github.io/docs) the for further information.

## Overview

## Notes for application developers

- NOTE: `scafi-core` and `scafi-simulator` cross-target both the **JVM** and the **JavaScript Platform** (via Scala.js)

### Import via Maven/sbt/Gradle

Add the dependency to scafi in your project, e.g., via sbt

```scala
// build.sbt
val scafi_version = "1.1.5"

val scafi_core  = "it.unibo.scafi" %% "scafi-core"  % scafi_version
val scafi_simulator  = "it.unibo.scafi" %% "scafi-simulator"  % scafi_version
val scafi_simulator_gui  = "it.unibo.scafi" %% "scafi-simulator-gui"  % scafi_version
val scafi_platform = "it.unibo.scafi" %% "scafi-distributed"  % scafi_version

libraryDependencies ++= Seq(scafi_core, scafi_simulator, scafi_platform)
```

or Gradle

```kotlin
// build.gradle.kts
dependencies {
    implementation("it.unibo.scafi:scafi-core_2.13:1.1.5")
}
```

### Hello, ScaFi

* Consider the following repository: [https://github.com/scafi/hello-scafi](https://github.com/scafi/hello-scafi)

As another example, consider the following steps.

**Step 1:** Import or define an **incarnation** (a family of types),
from which you can import types like `AggregateProgram`
```scala
package experiments

// Method #1: Use an incarnation which is already defined
// (Note: BasicSimulationIncarnation is defined in module 'scafi-simulator')
import it.unibo.scafi.incarnations.BasicSimulationIncarnation.AggregateProgram

// Method #2: Define a custom incarnation and import stuff from it
object MyIncarnation extends it.unibo.scafi.incarnations.BasicAbstractIncarnation
import MyIncarnation._
```

**Step 2:** Define an `AggregateProgram` which expresses the global behaviour of an ensemble.

```scala
// An "aggregate program" can be seen as a function from a Context to an Export
// The Context is the input for a local computation: includes state 
//  from previous computations, sensor data, and exports from neighbours.
// The export is a tree-like data structure that contains all the information needed
//  for coordinating with neighbours. It also contains the output of the computation.
object MyAggregateProgram extends AggregateProgram with StandardSensorNames{
  // Main program expression driving the ensemble
  // This is run in a loop for each agent
  // According to this expression, coordination messages are automatically generated
  // The platform/middleware/simulator is responsible for coordination
  override def main() = gradient(isSource)

  // The gradient is the (self-adaptive) field of the minimum distances from source nodes
  // `rep` is the construct for state transformation (remember the round-by-round loop behaviour)
  // `mux` is a purely functional multiplexer (selects the first or second branch according to condition)
  // `foldhoodPlus` folds over the neighbourhood (think like Scala's fold)
  // (`Plus` means "without self"--with plain `foldhood`, the device itself is folded)
  // `nbr(e)` denotes the values to be locally computed and shared with neighbours
  // `nbrRange` is a sensor that, when folding, returns the distance wrt each neighbour
  def gradient(source: Boolean): Double =
    rep(Double.PositiveInfinity){ distance =>
      mux(source) { 0.0 } {
        foldhoodPlus(Double.PositiveInfinity)(Math.min)(nbr{distance}+nbrRange)
      }
    }

  // A custom local sensor
  def isSource = sense[Boolean]("source")
  // A custom "neighbouring sensor"
  def nbrRange = nbrvar[Double](NBR_RANGE)
}
```

**Step 3:** Use ScaFi's internal simulator and GUI 
(modules **`scafi-simulator`** and **`scafi-simulator-gui`**, respectively) 
to run the program on a predefined network of devices.

```scala
import it.unibo.scafi.simulation.gui.{Launcher, Settings}

object SimulationRunner extends Launcher {
  Settings.Sim_ProgramClass = "experiments.MyAggregateProgram"
  Settings.ShowConfigPanel = true
  launch()
}
```

Alternatively, you can (a) implement your integration/middleware layer,
or (b) leverages integration with the Alchemist simulator, for more
sophisticated simulations.
**This and much more is described in the [ScaFi Documentation Page](https://scafi.github.io/docs).**

## ScaFi Architecture

From a deployment perspective, ScaFi consists of the following modules:

* **`scafi-commons`**: provides basic entities (e.g., spatial and temporal abstractions)
* **`scafi-core`**: represents the core of the project and provides an implementation of the ScaFi aggregate programming DSL,
  together with its standard library
* **`scafi-simulator`**: provides a basic support for simulating aggregate systems
* **`scafi-simulator-gui`**: provides a GUI for visualising simulations of aggregate systems
* **`spala`**: provides an actor-based aggregate computing middleware
* **`scafi-distributed`**: ScaFi integration-layer for `spala`

The modules to be imported (e.g., via sbt or Gradle) depend on the use case:

* _Development of a real-world aggregate application_.
  Bring `scafi-core` in for a fine-grained integration. For more straightforward distributed system setup, take a look at `scafi-distributed`.
* _Play, exercise, and experiment with aggregate programming_.
  Bring `scafi-core` in for writing aggregate programs as well as `scafi-simulator-gui` to quickly render an executing system.
* _Set up sophisticated simulations_
  Bring `scafi-core` in for writing aggregate programs
  and either (A) leverage the basic machinery provided by `scafi-simulator`,
  or (B) leverage the ScaFi support provided by Alchemist.

## People

### Main Researchers and Authors

* Roberto Casadei
* Gianluca Aguzzi
* Mirko Viroli

### Research Collaborators

* Ferruccio Damiani
* Giorgio Audrito
* Danilo Pianini

## Contributing

Please refer to the [ScaFi Developer Manual](https://scafi.github.io/docs/#scafi-developer-manual).

## License ##

scafi is Open Source and available under the Apache 2 License.
