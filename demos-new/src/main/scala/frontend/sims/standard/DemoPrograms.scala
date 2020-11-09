/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package frontend.sims.standard

import java.util.concurrent.TimeUnit

import frontend.sims.SensorDefinitions
import it.unibo.scafi.incarnations.BasicSimulationIncarnation._
import ScafiStandardLibraries._
import it.unibo.scafi.simulation.s2.frontend.configuration.SensorName
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.reflection.Demo

import scala.concurrent.duration.Duration
import scala.util.Random

@Demo
class Mid extends ScafiStandardAggregateProgram {
  override def main() = mid()
}

@Demo
class Types extends ScafiStandardAggregateProgram {
  override def main() = ("a", 1, List(10,20))
}

@Demo
class CountRounds extends ScafiStandardAggregateProgram {
  override def main() = rep(0)(x => x + 1)
}

@Demo
class CountNeighbours extends ScafiStandardAggregateProgram {
  override def main() = foldhood(0)(_ + _) { nbr { 1 } }
}

@Demo
class CountNeighboursExceptMyself extends ScafiStandardAggregateProgram {
  override def main() = foldhood(0)(_ + _) {
    if (nbr { mid() } == mid()) 0 else 1
  }
}

@Demo
class MaxId extends ScafiStandardAggregateProgram {
  override def main() = {
    val maxId = foldhood(Int.MinValue)(Math.max(_, _)) { nbr(mid()) }
    (mid(), maxId)
  }
}

@Demo
class Gradient extends ScafiStandardAggregateProgram with StandardSensors {
  def isSource = sense[Boolean](SensorName.sensor1)
  def isObstacle = sense[Boolean](SensorName.sensor2)

  override def main(): Double =
    branch (isObstacle) { Double.MaxValue } {
      rep(Double.MaxValue) {
        distance => mux(isSource) { 0.0 } {
          minHoodPlus { nbr { distance } + nbrRange }
        }
      }
    }
}

@Demo
class GradientHop extends ScafiStandardAggregateProgram with SensorDefinitions with BlockG  {
  def isSource = sense[Boolean](SensorName.sensor1)

  def hopGradientByG(src: Boolean): Double = Gcurried(src)(0)(_ + 1)(() =>1)

  override def main(): Int = hopGradientByG(isSource).toInt
}

@Demo
class RouteChannel extends ScafiStandardAggregateProgram with SensorDefinitions with BlockG {
  override def main() = channel1(sense1, sense2, 0.05)

  def channel2(source: Boolean, target: Boolean, width: Double): (String, String, String) =
    (distanceTo(source).formatted("%.2f"), distanceTo(target).formatted("%.2f"), distanceBetween(source, target).formatted("%.2f"))

  def channel1(source: Boolean, target: Boolean, width: Double): Boolean =
    distanceTo(source) + distanceTo(target) <= distanceBetween(source, target) + width
}

@Demo
class Timer extends ScafiStandardAggregateProgram with StandardSensors with TimeUtils {
  override def main() = Duration(
    branch(!sense[Boolean](SensorName.sensor1)){ timerLocalTime(Duration(30, TimeUnit.SECONDS)) } { 0 },
    TimeUnit.NANOSECONDS
  ).toMillis + "ms"
}

@Demo
class SparseChoice extends ScafiStandardAggregateProgram with SensorDefinitions with BlockG with BlockS {
  override def main() = S(20, nbrRange) //if(channel(isSource, isDest, 0)) 1 else 0
}

@Demo
class SensorNbrRange extends ScafiStandardAggregateProgram with StandardSensors with FieldUtils {
  import excludingSelf.reifyField
  override def main() = mid() + " => " + reifyField("%.2f".format(nbrRange()))
}

@Demo
class SensorCurrTime extends ScafiStandardAggregateProgram with StandardSensors with FieldUtils {
  override def main() = currentTime().toString
}

@Demo
class SensorTimestamp extends ScafiStandardAggregateProgram with StandardSensors with FieldUtils {
  override def main() = timestamp() + "ms"
}

@Demo
class SensorCurrPos extends ScafiStandardAggregateProgram with StandardSensors with FieldUtils {
  override def main() = currentPosition()
}

@Demo
class SensorNbrVector extends ScafiStandardAggregateProgram with StandardSensors with FieldUtils {
  import excludingSelf.reifyField
  override def main() = mid() + " => " + reifyField(nbrVector())
}

@Demo
class SensorDeltaTime extends ScafiStandardAggregateProgram with StandardSensors with FieldUtils {
  override def main() = deltaTime().toMillis + "ms"
}

@Demo
class SensorNbrDelay extends ScafiStandardAggregateProgram with StandardSensors with FieldUtils {
  import excludingSelf.reifyField
  override def main() = mid() + " => " + reifyField(nbrDelay().toMillis+"ms")
}

@Demo
class SensorNbrLag extends ScafiStandardAggregateProgram with StandardSensors with FieldUtils {
  import excludingSelf.reifyField
  override def main() = deltaTime().toMillis  + "ms -- " + mid() + " => " + reifyField(nbrLag().toMillis+"ms")
}

@Demo
class SensorNbrDelayLag extends ScafiStandardAggregateProgram with StandardSensors with FieldUtils {
  import excludingSelf.reifyField
  override def main() = mid() + " => " + reifyField(s"${nbrDelay().toMillis}ms; ${nbrLag().toMillis}ms")
}

@Demo
class CollectNbrsIncludingMyself extends ScafiStandardAggregateProgram with StandardSensors with FieldUtils {
  override def main() = includingSelf.unionHood(nbr{mid})
}

@Demo
class CollectNbrsExcludingMyself extends ScafiStandardAggregateProgram with StandardSensors with FieldUtils {
  override def main() = excludingSelf.unionHood(nbr{mid})
}

@Demo
class DemoMeanCounter extends ScafiStandardAggregateProgram with StandardSensors with GenericUtils {
  override def main() = meanCounter(if(sense[Random](LSNS_RANDOM).nextDouble() > 0.5) 1 else -1, 50000)
}