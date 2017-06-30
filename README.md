# scafi (**sca**la **fi**elds) #


### Introduction ###

**scafi** is an Scala-based aggregate-programming framework which implements the Field Calculus semantics and provides an API for simulation and execution of aggregate programming applications

### Usage ###

Steps

* Add the dependency to scafi in your project (e.g., via sbt)


```
#!scala

val scafi_core  = "it.unibo.apice.scafiteam" %% "scafi-core"  % "0.2.0"
val scafi_simulator  = "it.unibo.apice.scafiteam" %% "scafi-simulator"  % "0.2.0"
val scafi_simulator_gui  = "it.unibo.apice.scafiteam" %% "scafi-simulator-gui"  % "0.2.0"
val scafi_platform = "it.unibo.apice.scafiteam" %% "scafi-distributed"  % "0.2.0"

libraryDependencies ++= Seq(scafi_core, scafi_simulator, scafi_platform)
```

* Use the API (e.g., to set up a simple simulation)


```
#!scala
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

### References ###

* Towards Aggregate Programming in Scala. Roberto Casadei and Mirko Viroli. In First Workshop on Programming Models and Languages for Distributed Computing (PMLDC), 2016.
* Aggregate Programming for the Internet of Things. Jacob Beal, Danilo Pianini, and Mirko Viroli. IEEE Computer, 2015

### Contacts ###

* roby.casadei@unibo.it
* mirko.viroli@unibo.it