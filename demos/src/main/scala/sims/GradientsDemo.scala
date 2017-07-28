/*
 * Copyright (C) 2016-2017, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENCE.txt file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package sims

import it.unibo.scafi.incarnations.BasicSimulationIncarnation.{AggregateProgram, BlockG, Builtins, ID, BoundedTypeClasses}
import it.unibo.scafi.simulation.gui.{Launcher, Settings}

import scala.concurrent.duration.FiniteDuration

object GradientsDemo extends Launcher {
  // Configuring simulation
  Settings.Sim_ProgramClass = "sims.CrfGradient" // starting class, via Reflection
  Settings.ShowConfigPanel = false // show a configuration panel at startup
  Settings.Sim_NbrRadius = 0.2 // neighbourhood radius
  Settings.Sim_NumNodes = 100 // number of nodes
  launch()
}

class FlexGradient extends AggregateProgram with Gradients with SensorDefinitions {
  override def main() = flex(sense1)
}

class CrfGradient extends AggregateProgram with Gradients with SensorDefinitions {
  override def main() = crf(sense1)
}

class ClassicGradient extends AggregateProgram with Gradients with SensorDefinitions {
  override def main() = classic(sense1)
}

class ClassicGradientWithG extends AggregateProgram with Gradients with SensorDefinitions {
  override def main() = classicWithG(sense1)
}

class ClassicGradientWithUnboundedG extends AggregateProgram with Gradients with SensorDefinitions {
  override def main() = classicWithUnboundedG(sense1)
}

trait Gradients extends BlockG { self: AggregateProgram with SensorDefinitions =>

  def crf(source: Boolean, raisingSpeed: Double = 2): Double = rep((Double.PositiveInfinity, 0.0)){ case (g, speed) =>
    mux(source){ (0.0, 0.0) }{
      implicit def durationToDouble(fd: FiniteDuration): Double = fd.toSeconds
      case class Constraint(nbr: ID, gradient: Double, nbrDistance: Double)

      val constraints = foldhoodPlus[List[Constraint]](List.empty)(_ ++ _){
        val (nbrg, d) = (nbr{g}, nbrRange)
        mux(nbrg + d + speed * (nbrLag() + deltaTime()) < g){ List(Constraint(nbr{mid()}, nbrg, d)) }{ List() }
      }

      if(constraints.isEmpty){
        (g + raisingSpeed * deltaTime(), raisingSpeed)
      } else {
        (constraints.map(c => c.gradient + c.nbrDistance).min, 0.0)
      }
    }
  }._1

  /**
    * Idea: a device should change its estimate only for significant errors.
    * Useful when devices far from the source need only coarse estimates.
    * Flex gradient provides tunable trade-off between precision and communication cost.
    *
    * @param source Source fields of devices from which the gradient is calculated
    * @param epsilon Parameter expressing tolerance wrt changes
    * @param delta Distortion into the distance measure, such that neighbor distance is
    *              never considered to be less than delta * communicationRadius.
    * @param communicationRadius
    * @return
    */
  def flex(source: Boolean,
           epsilon: Double = 0.5,
           delta: Double = 1.0,
           communicationRadius: Double = 1.0
          ): Double =
    rep(Double.PositiveInfinity){ g =>
      def distance = Math.max(nbrRange(), delta * communicationRadius)

      import BoundedTypeClasses._; import Builtins.Bounded._ // for min/maximizing over tuples
      val maxLocalSlope = maxHood {
        ((g - nbr{g})/distance, nbr{mid}, nbr{g}, nbrRange())
      }
      val constraint = minHoodPlus{ (nbr{g} + distance) }

      mux(source){ 0.0 }{
        if(Math.max(communicationRadius, 2*constraint) < g) {
          constraint
        }
        else if(maxLocalSlope._1 > 1 + epsilon) {
          maxLocalSlope._3 + (1 + epsilon)*Math.max(delta * communicationRadius, maxLocalSlope._4)
        }
        else if(maxLocalSlope._1 < 1 - epsilon){
          maxLocalSlope._3 + (1 - epsilon)*Math.max(delta * communicationRadius, maxLocalSlope._4)
        } else {
          g
        }
      }
    }

  def classic(source: Boolean): Double = rep(Double.PositiveInfinity){ distance =>
    mux(source){ 0.0 }{
      // NB: must be minHoodPlus (i.e., not the minHood which includes the device itself)
      //     otherwise a source which stops being a source will continue to count as 0 because of self-messages.
      minHoodPlus(nbr{distance} + nbrRange)
    }
  }

  def classicWithG(source: Boolean): Double = G(source, 0.0, (_:Double) + nbrRange, nbrRange)

  def classicWithUnboundedG(source: Boolean): Double = {
    implicit val defValue = Builtins.Defaultable.apply(Double.PositiveInfinity)
    unboundedG(source, 0.0, (_:Double) + nbrRange, nbrRange)
  }

  def unboundedG[V : Builtins.Defaultable](source: Boolean, field: V, acc: V => V, metric: => Double): V =
    rep((Double.MaxValue, field)) { case (dist, value) =>
      mux(source) {
        (0.0, field)
      } {
        import Builtins.Bounded.tupleOnFirstBounded
        minHoodPlus { (nbr {dist} + metric, acc(nbr {value})) }
      }
    }._2
}