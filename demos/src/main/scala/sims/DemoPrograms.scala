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

import it.unibo.scafi.incarnations.BasicSimulationIncarnation._
import it.unibo.scafi.simulation.gui.model.implementation.SensorEnum
import it.unibo.scafi.incarnations.BasicSimulationIncarnation.NBR_RANGE

import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit

import scala.util.Random

class Mid extends AggregateProgram {
  override def main() = mid()
}

class Types extends AggregateProgram {
  override def main() = ("a", 1, List(10,20))
}

class CountRounds extends AggregateProgram {
  override def main() = rep(0)(x => x + 1)
}

class CountNeighbours extends AggregateProgram {
  override def main() = foldhood(0)(_ + _) { nbr { 1 } }
}

class CountNeighboursExceptMyself extends AggregateProgram {
  override def main() = foldhood(0)(_ + _) {
    if (nbr { mid() } == mid()) 0 else 1
  }
}

class MaxId extends AggregateProgram {
  override def main() = {
    val maxId = foldhood(Int.MinValue)(Math.max(_, _)) { nbr(mid()) }
    (mid(), maxId)
  }
}

class Gradient extends AggregateProgram {
  def isSource = sense[Boolean](SensorEnum.SENS1.name)
  def isObstacle = sense[Boolean](SensorEnum.SENS2.name)
  def nbrRange = nbrvar[Double](NBR_RANGE)

  override def main(): Double =
    branch (isObstacle) { Double.MaxValue } {
      rep(Double.MaxValue) {
        distance => mux(isSource) { 0.0 } {
          minHoodPlus { nbr { distance } + nbrRange }
        }
      }
    }
}

class GradientHop extends AggregateProgram with SensorDefinitions with BlockG  {
  def isSource = sense[Boolean](SensorEnum.SENS1.name)

  def hopGradientByG(src: Boolean): Double = G2(src)(0)(_ + 1)(() =>1)

  override def main(): Int = hopGradientByG(isSource).toInt
}

class RouteChannel extends AggregateProgram with SensorDefinitions with BlockG {
  override def main() = channel(sense1, sense2, 0.05)

  def channel2(source: Boolean, target: Boolean, width: Double): (String, String, String) =
    (distanceTo(source).formatted("%.2f"), distanceTo(target).formatted("%.2f"), distanceBetween(source, target).formatted("%.2f"))

  def channel(source: Boolean, target: Boolean, width: Double): Boolean =
    distanceTo(source) + distanceTo(target) <= distanceBetween(source, target) + width
}

class Timer extends AggregateProgram with StandardSensors with TimeUtils {
  override def main() = Duration(
    branch(!sense[Boolean](SensorEnum.SENS1.name)){ timerLocalTime(Duration(30, TimeUnit.SECONDS)) } { 0 },
    TimeUnit.NANOSECONDS
  ).toMillis + "ms"
}

class SparseChoice extends AggregateProgram with SensorDefinitions with BlockG with BlockS {
  override def main() = S(20, nbrRange) //if(channel(isSource, isDest, 0)) 1 else 0
}

class SensorNbrRange extends AggregateProgram with StandardSensors with FieldUtils {
  import excludingSelf.reifyField
  override def main() = mid() + " => " + reifyField("%.2f".format(nbrRange()))
}

class SensorCurrTime extends AggregateProgram with StandardSensors with FieldUtils {
  override def main() = currentTime().toString
}

class SensorTimestamp extends AggregateProgram with StandardSensors with FieldUtils {
  override def main() = timestamp() + "ms"
}

class SensorCurrPos extends AggregateProgram with StandardSensors with FieldUtils {
  override def main() = currentPosition()
}

class SensorNbrVector extends AggregateProgram with StandardSensors with FieldUtils {
  import excludingSelf.reifyField
  override def main() = mid() + " => " + reifyField(nbrVector())
}

class SensorDeltaTime extends AggregateProgram with StandardSensors with FieldUtils {
  override def main() = deltaTime().toMillis + "ms"
}

class SensorNbrDelay extends AggregateProgram with StandardSensors with FieldUtils {
  import excludingSelf.reifyField
  override def main() = mid() + " => " + reifyField(nbrDelay().toMillis+"ms")
}

class SensorNbrLag extends AggregateProgram with StandardSensors with FieldUtils {
  import excludingSelf.reifyField
  override def main() = deltaTime().toMillis  + "ms -- " + mid() + " => " + reifyField(nbrLag().toMillis+"ms")
}

class SensorNbrDelayLag extends AggregateProgram with StandardSensors with FieldUtils {
  import excludingSelf.reifyField
  override def main() = mid() + " => " + reifyField(s"${nbrDelay().toMillis}ms; ${nbrLag().toMillis}ms")
}

class CollectNbrsIncludingMyself extends AggregateProgram with StandardSensors with FieldUtils {
  override def main() = includingSelf.unionHood(nbr{mid})
}

class CollectNbrsExcludingMyself extends AggregateProgram with StandardSensors with FieldUtils {
  override def main() = excludingSelf.unionHood(nbr{mid})
}

class DemoMeanCounter extends AggregateProgram with StandardSensors with GenericUtils {
  override def main() = meanCounter(if(sense[Random](LSNS_RANDOM).nextDouble() > 0.5) 1 else -1, 50000)
}


