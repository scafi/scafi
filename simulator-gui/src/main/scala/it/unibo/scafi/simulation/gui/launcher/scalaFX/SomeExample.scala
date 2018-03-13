package it.unibo.scafi.simulation.gui.launcher.scalaFX


import it.unibo.scafi.incarnations.BasicSimulationIncarnation.{ AggregateProgram, Builtins }
import it.unibo.scafi.incarnations.BasicSimulationIncarnation._
object SomeExample extends App {
  Launcher.program = classOf[Main13]
  Launcher.nodes = 100
  Launcher.maxPoint = 1000
  Launcher.radius = 200
  Launcher.launch()
}

abstract class DISIDemoAggregateProgram extends AggregateProgram {
  import WorldConfig._
  def sense1 = sense[Boolean](source.name)
  def sense2 = sense[Boolean](destination.name)
  def sense3 = sense[Boolean](obstacle.name)
  def boolToInt(b: Boolean) = mux(b){1}{0}
  def nbrRange = nbrvar[Double]("nbrRange")*100
}

class Main extends DISIDemoAggregateProgram {
  def inc(x:Int):Int = x+1
  override def main() = rep(init = 0)(fun = inc)
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
/*
class Main14 extends DISIDemoAggregateProgram {
  import Builtins.Bounded.of_i

  override def main() = rep(0){ x => boolToInt(sense1) max maxHoodPlus( nbr{x}) }
}

class Main15 extends DISIDemoAggregateProgram {
  override def main() = rep(Double.MaxValue){ d => mux[Double](sense1){0.0}{minHoodPlus(nbr{d}+1.0)} }
}

class Main16 extends DISIDemoAggregateProgram {
  override def main() = rep(Double.MaxValue){ d => mux[Double](sense1){0.0}{minHoodPlus(nbr{d}+nbrRange)} }
}*/
