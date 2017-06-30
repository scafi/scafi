# scafi #


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

import it.unibo.scafi.incarnations.BasicSimulationIncarnation._

object MyAggregateProgram extends AggregateProgram {
  def gradient(source: Boolean): Double =
    rep(Double.MaxValue){
      distance => mux(source) { 0.0 } {
        foldhood(Double.MaxValue)((x,y)=>if (x<y) x else y)(nbr{distance}+nbrvar[Double](NBR_RANGE_NAME))
      }
    }

  def isSource = sense[Boolean]("source")

  override def main() = gradient(isSource)
}


object MyTest extends App {

  val net = simulatorFactory.gridLike(
    n = 10,
    m = 10,
    stepx = 1,
    stepy = 1,
    eps = 0.0,
    rng = 1.1)

  net.addSensor(name = "source", value = false)
  net.chgSensorValue(name = "source", ids = Set(3), value = true)

  var v = java.lang.System.currentTimeMillis()

  net.executeMany(
    node = MyAggregateProgram,
    size = 100000,
    action = (n,i) => {
      if (i % 1000 == 0) {
        println(net)
        val newv = java.lang.System.currentTimeMillis()
        println(newv-v)
        println(net.context(4))
        v=newv
      }
    })
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