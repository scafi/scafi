/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package sims

import it.unibo.scafi.incarnations.BasicSimulationIncarnation.{AggregateProgram, Builtins}
import it.unibo.scafi.simulation.frontend.{Launcher, Settings}

object DISIDemo extends Launcher {
  Settings.Sim_ProgramClass = "sims.Main" + (if(args.length == 0) "" else args(0))
  Settings.ShowConfigPanel = false
  Settings.Sim_NbrRadius = 0.15
  Settings.Sim_NumNodes = 100
  launch()
}

abstract class DISIDemoAggregateProgram extends AggregateProgram {
  def sense1: Boolean = sense[Boolean]("sens1")
  def sense2: Boolean = sense[Boolean]("sens2")
  def sense3: Boolean = sense[Boolean]("sens3")
  def boolToInt(b: Boolean): Int = mux(b){1}{0}
  def nbrRange: Double = nbrvar[Double]("nbrRange")*100
}

class Main extends DISIDemoAggregateProgram {
  def inc(x:Int):Int = x + 1
  override def main(): Int = rep(init = 0)(fun = inc)
}

class Main1 extends DISIDemoAggregateProgram {
  override def main() = 1
}

class Main2 extends DISIDemoAggregateProgram {
  override def main(): Int = 2 + 3
}

class Main3 extends DISIDemoAggregateProgram {
  override def main(): (Int, Int) = (10,20)
}

class Main4 extends DISIDemoAggregateProgram {
  override def main(): Double = Math.random()
}

class Main5 extends DISIDemoAggregateProgram {
  override def main(): Boolean = sense1
}

class Main6 extends DISIDemoAggregateProgram {
  override def main(): MainResult = if (sense1) 10 else 20
}

class Main7 extends DISIDemoAggregateProgram {
  override def main(): Int = mid()
}

class Main8 extends DISIDemoAggregateProgram {
  override def main(): Double = minHoodPlus(nbrRange)
}

class Main9 extends DISIDemoAggregateProgram {
  override def main(): Int = rep(0){_ + 1}
}

class Main10 extends DISIDemoAggregateProgram {
  override def main(): Double = rep(Math.random()){x=>x}
}

class Main11 extends DISIDemoAggregateProgram {
  override def main(): Double = rep[Double](0.0){x => x + rep(Math.random()){y=>y}}
}

class Main12 extends DISIDemoAggregateProgram {
  import Builtins.Bounded.of_i

  override def main(): Int = maxHoodPlus(boolToInt(nbr{sense1}))
}

class Main13 extends DISIDemoAggregateProgram {
  override def main(): Int = foldhoodPlus(0)(_ + _){nbr{1}}
}

class Main14 extends DISIDemoAggregateProgram {
  import Builtins.Bounded.of_i

  override def main(): Int = rep(0){ x => boolToInt(sense1) max maxHoodPlus( nbr{x}) }
}

class Main15 extends DISIDemoAggregateProgram {
  override def main(): Double = rep(Double.MaxValue){ d => mux[Double](sense1){0.0}{minHoodPlus(nbr{d} + 1.0)} }
}

class Main16 extends DISIDemoAggregateProgram {
  override def main(): Double = rep(Double.MaxValue){ d => mux[Double](sense1){0.0}{minHoodPlus(nbr{d} + nbrRange)} }
}
