/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package frontend.sims.standard

import it.unibo.scafi.incarnations.BasicSimulationIncarnation.ScafiStandardAggregateProgram
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.ScafiSimulationInitializer.RadiusSimulation
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.SimulationInfo
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.reflection.Demo
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.configuration.ScafiProgramBuilder
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.world.ScafiWorldInitializer.Grid

object DISIDemo extends App {
  ScafiProgramBuilder (
    Grid(50,10,10),
    SimulationInfo(program = classOf[Main6]),
    RadiusSimulation(radius = 50),
    neighbourRender = true
  ).launch()
}

abstract class DISIDemoAggregateProgram extends ScafiStandardAggregateProgram {
  def sense1 = sense[Boolean]("sens1")
  def sense2 = sense[Boolean]("sens2")
  def sense3 = sense[Boolean]("sens3")
  def boolToInt(b: Boolean) = mux(b){1}{0}
  def nbrRange = nbrvar[Double]("nbrRange")*100
}

@Demo
class Main extends DISIDemoAggregateProgram {
  def inc(x:Int):Int = x+1
  override def main() = rep(init = 0)(fun = inc)
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
  import it.unibo.scafi.languages.TypesInfo.Bounded.of_i

  override def main() = maxHoodPlus(boolToInt(nbr{sense1}))
}

@Demo
class Main13 extends DISIDemoAggregateProgram {
  override def main() = foldhoodPlus(0)(_+_){nbr{1}}
}

@Demo
class Main14 extends DISIDemoAggregateProgram {
  import it.unibo.scafi.languages.TypesInfo.Bounded.of_i

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
