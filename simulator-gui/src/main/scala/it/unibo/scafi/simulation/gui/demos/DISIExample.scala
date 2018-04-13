package it.unibo.scafi.simulation.gui.demos

import it.unibo.scafi.incarnations.BasicSimulationIncarnation.{AggregateProgram, _}
import it.unibo.scafi.simulation.gui.launcher.scalaFX.Launcher
import it.unibo.scafi.simulation.gui.incarnation.scafi.Actions._
import it.unibo.scafi.simulation.gui.model.graphics2D.BasicShape2D.Rectangle
import it.unibo.scafi.simulation.gui.view.scalaFX.drawer.{GradientFXDrawer, StandardFXDrawer}
object DISIExample extends App {
  import Launcher._
  program = classOf[Main]
  drawer = GradientFXDrawer
  nodes = 100000
  boundary = None
  //GradientFXDrawer.maxValue = boundary.get.w.toInt
  radius = 2
  actions = generalaction :: actions
  neighbourRender = false
  launch()
}

abstract class DISIDemoAggregateProgram extends AggregateProgram {
  import it.unibo.scafi.simulation.gui.launcher.SensorName._
  def sense1 = sense[Boolean](sens1.name)
  def sense2 = sense[Boolean](sens2.name)
  def sense3 = sense[Boolean](sens3.name)
  def boolToInt(b: Boolean) = mux(b){1}{0}
  def nbrRange = nbrvar[Double]("nbrRange")
}

class Main extends DISIDemoAggregateProgram {
  def inc(x:Double):Double = x+10.0
  override def main() = rep(init = 0.0)(fun = inc)
}

class Main1 extends DISIDemoAggregateProgram {
  override def main() = 1
}

class Main2 extends DISIDemoAggregateProgram {
  override def main() = 2+3
}

class Main3 extends DISIDemoAggregateProgram {
  override def main() = (10,20)
}

class Main4 extends DISIDemoAggregateProgram {
  override def main() = Math.random()
}

class Main5 extends DISIDemoAggregateProgram {
  override def main() = sense1
}

class Main6 extends DISIDemoAggregateProgram {
  override def main() = if (sense1) 10 else 20
}

class Main7 extends DISIDemoAggregateProgram {
  override def main() = mid()
}

class Main8 extends DISIDemoAggregateProgram {
  override def main() = minHoodPlus(nbrRange)
}

class Main9 extends DISIDemoAggregateProgram {
  override def main() = rep(0){_+1}
}

class Main10 extends DISIDemoAggregateProgram {
  override def main() = rep(Math.random()){x=>x}
}

class Main11 extends DISIDemoAggregateProgram {
  override def main() = rep[Double](0.0){x => x + rep(Math.random()){y=>y}}
}

class Main12 extends DISIDemoAggregateProgram {
  override def main() = maxHoodPlus(boolToInt(nbr{sense1}))
}

class Main13 extends DISIDemoAggregateProgram {
  override def main() = foldhoodPlus(0)(_+_){nbr{1}}
}
class Main14 extends DISIDemoAggregateProgram {
  override def main() = rep(0){ x => boolToInt(sense1) max maxHoodPlus( nbr{x}) }
}

class Main15 extends DISIDemoAggregateProgram {
  override def main() = rep(Double.MaxValue){ d => mux[Double](sense1){0.0}{minHoodPlus(nbr{d}+1.0)} }
}

class Main16 extends DISIDemoAggregateProgram {
  override def main() = rep(Double.MaxValue){ d => mux[Double](sense1){0.0}{minHoodPlus(nbr{d}+nbrRange)} }
}