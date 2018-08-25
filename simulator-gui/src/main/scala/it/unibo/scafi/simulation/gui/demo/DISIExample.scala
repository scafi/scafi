package it.unibo.scafi.simulation.gui.demo

import it.unibo.scafi.incarnations.BasicSimulationIncarnation.{AggregateProgram, _}
import it.unibo.scafi.simulation.gui.configuration.environment.ProgramEnvironment.FastPerformancePolicy
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.ScafiSimulationInitializer.RadiusSimulationInitializer
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.ScafiSimulationSeed
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.reflection.Demo
import it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.ScafiProgramBuilder
import it.unibo.scafi.simulation.gui.incarnation.scafi.world.ScafiWorldInitializer.Grid
import it.unibo.scafi.simulation.gui.view.scalaFX.drawer.GradientFXOutputPolicy
object DISIExample extends App {
  val demoInfo = classOf[Main].getAnnotation(classOf[Demo])
  ScafiProgramBuilder (
    worldInitializer = Grid(5,5,5),
    scafiSimulationSeed = ScafiSimulationSeed(classOf[Main]),
    simulationInitializer = RadiusSimulationInitializer(radius = 7),
    outputPolicy = GradientFXOutputPolicy,
    perfomance = FastPerformancePolicy,
    neighbourRender = true
  ).launch()
}

abstract class DISIDemoAggregateProgram extends AggregateProgram {
  import it.unibo.scafi.simulation.gui.configuration.SensorName._
  def sense1 = sense[Boolean](sensor1)
  def sense2 = sense[Boolean](sensor2)
  def sense3 = sense[Boolean](sensor3)
  def boolToInt(b: Boolean) = mux(b){1}{0}
  def nbrRange = nbrvar[Double]("nbrRange")
}

@Demo
class Main extends DISIDemoAggregateProgram {
  def inc(x:Double):Double = x+10.0
  override def main() = rep(init = 0.0)(fun = inc)
}

@Demo
class Main1 extends DISIDemoAggregateProgram {
  override def main() = 1
}
@Demo
class Main2 extends DISIDemoAggregateProgram {
  override def main() = 2+3
}
@Demo
class Main3 extends DISIDemoAggregateProgram {
  override def main() = (10,20)
}
@Demo
class Main4 extends DISIDemoAggregateProgram {
  override def main() = Math.random()
}
@Demo
class Main5 extends DISIDemoAggregateProgram {
  override def main() = sense1
}
@Demo
class Main6 extends DISIDemoAggregateProgram {
  override def main() = if (sense1) 10 else 20
}
@Demo
class Main7 extends DISIDemoAggregateProgram {
  override def main() = mid()
}
@Demo
class Main8 extends DISIDemoAggregateProgram {
  override def main() = minHoodPlus(nbrRange)
}
@Demo
class Main9 extends DISIDemoAggregateProgram {
  override def main() = rep(0){_+1}
}
@Demo
class Main10 extends DISIDemoAggregateProgram {
  override def main() = rep(Math.random()){x=>x}
}
@Demo
class Main11 extends DISIDemoAggregateProgram {
  override def main() = rep[Double](0.0){x => x + rep(Math.random()){y=>y}}
}
@Demo
class Main12 extends DISIDemoAggregateProgram {
  override def main() = maxHoodPlus(boolToInt(nbr{sense1}))
}
@Demo
class Main13 extends DISIDemoAggregateProgram {
  override def main() = foldhoodPlus(0)(_+_){nbr{1}}
}
@Demo
class Main14 extends DISIDemoAggregateProgram {
  override def main() = rep(0){ x => boolToInt(sense1) max maxHoodPlus( nbr{x}) }
}
@Demo
class Main15 extends DISIDemoAggregateProgram {
  override def main() = rep(Double.MaxValue){ d => mux[Double](sense1){0.0}{minHoodPlus(nbr{d}+1.0)} }
}
@Demo
class Main16 extends DISIDemoAggregateProgram {
  override def main() = rep(Double.MaxValue){ d => mux[Double](sense1){0.0}{minHoodPlus(nbr{d}+nbrRange)} }
}