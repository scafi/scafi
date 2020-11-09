/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package sims

import java.util.concurrent.TimeUnit

import it.unibo.scafi.incarnations.BasicSimulationIncarnation._
import ScafiStandardLibraries._
import it.unibo.scafi.simulation.frontend.model.implementation.SensorEnum

import scala.concurrent.duration.Duration
import scala.util.Random

class Mid extends ScafiStandardAggregateProgram {
  override def main() = mid()
}

class Types extends ScafiStandardAggregateProgram {
  override def main() = ("a", 1, List(10,20))
}

class CountRounds extends ScafiStandardAggregateProgram {
  override def main() = rep(0)(x => x + 1)
}

class CountNeighbours extends ScafiStandardAggregateProgram {
  override def main() = foldhood(0)(_ + _) { nbr { 1 } }
}

class CountNeighboursExceptMyself extends ScafiStandardAggregateProgram {
  override def main() = foldhood(0)(_ + _) {
    if (nbr { mid() } == mid()) 0 else 1
  }
}

class MaxId extends ScafiStandardAggregateProgram {
  override def main() = {
    val maxId = foldhood(Int.MinValue)(Math.max(_, _)) { nbr(mid()) }
    (mid(), maxId)
  }
}

class Gradient extends ScafiStandardAggregateProgram with StandardSensors {
  def isSource = sense[Boolean](SensorEnum.SENS1.name)
  def isObstacle = sense[Boolean](SensorEnum.SENS2.name)

  override def main(): Double =
    branch (isObstacle) { Double.MaxValue } {
      rep(Double.MaxValue) {
        distance => mux(isSource) { 0.0 } {
          minHoodPlus { nbr { distance } + nbrRange }
        }
      }
    }
}

class GradientHop extends ScafiStandardAggregateProgram with SensorDefinitions with BlockG  {
  def isSource = sense[Boolean](SensorEnum.SENS1.name)

  def hopGradientByG(src: Boolean): Double = Gcurried(src)(0)(_ + 1)(() =>1)

  override def main(): Int = hopGradientByG(isSource).toInt
}

class RouteChannel extends ScafiStandardAggregateProgram with SensorDefinitions with BlockG {
  override def main() = channel(sense1, sense2, 0.05)

  def channel2(source: Boolean, target: Boolean, width: Double): (String, String, String) =
    (distanceTo(source).formatted("%.2f"), distanceTo(target).formatted("%.2f"), distanceBetween(source, target).formatted("%.2f"))
}

class Timer extends ScafiStandardAggregateProgram with StandardSensors with TimeUtils {
  override def main() = Duration(
    branch(!sense[Boolean](SensorEnum.SENS1.name)){ timerLocalTime(Duration(30, TimeUnit.SECONDS)) } { 0 },
    TimeUnit.NANOSECONDS
  ).toMillis + "ms"
}

class SparseChoice extends ScafiStandardAggregateProgram with SensorDefinitions with BlockG with BlockS {
  override def main() = S(20, nbrRange) //if(channel(isSource, isDest, 0)) 1 else 0
}

class SensorNbrRange extends ScafiStandardAggregateProgram with StandardSensors with FieldUtils {
  import excludingSelf.reifyField
  override def main() = mid() + " => " + reifyField("%.2f".format(nbrRange()))
}

class SensorCurrTime extends ScafiStandardAggregateProgram with StandardSensors with FieldUtils {
  override def main() = currentTime().toString
}

class SensorTimestamp extends ScafiStandardAggregateProgram with StandardSensors with FieldUtils {
  override def main() = timestamp() + "ms"
}

class SensorCurrPos extends ScafiStandardAggregateProgram with StandardSensors with FieldUtils {
  override def main() = currentPosition()
}

class SensorNbrVector extends ScafiStandardAggregateProgram with StandardSensors with FieldUtils {
  import excludingSelf.reifyField
  override def main() = mid() + " => " + reifyField(nbrVector())
}

class SensorDeltaTime extends ScafiStandardAggregateProgram with StandardSensors with FieldUtils {
  override def main() = deltaTime().toMillis + "ms"
}

class SensorNbrDelay extends ScafiStandardAggregateProgram with StandardSensors with FieldUtils {
  import excludingSelf.reifyField
  override def main() = mid() + " => " + reifyField(nbrDelay().toMillis+"ms")
}

class SensorNbrLag extends ScafiStandardAggregateProgram with StandardSensors with FieldUtils {
  import excludingSelf.reifyField
  override def main() = deltaTime().toMillis  + "ms -- " + mid() + " => " + reifyField(nbrLag().toMillis+"ms")
}

class SensorNbrDelayLag extends ScafiStandardAggregateProgram with StandardSensors with FieldUtils {
  import excludingSelf.reifyField
  override def main() = mid() + " => " + reifyField(s"${nbrDelay().toMillis}ms; ${nbrLag().toMillis}ms")
}

class CollectNbrsIncludingMyself extends ScafiStandardAggregateProgram with StandardSensors with FieldUtils {
  override def main() = includingSelf.unionHood(nbr{mid})
}

class CollectNbrsExcludingMyself extends ScafiStandardAggregateProgram with StandardSensors with FieldUtils {
  override def main() = excludingSelf.unionHood(nbr{mid})
}

class DemoMeanCounter extends ScafiStandardAggregateProgram with StandardSensors with GenericUtils {
  override def main() = meanCounter(if(sense[Random](LSNS_RANDOM).nextDouble() > 0.5) 1 else -1, 50000)
}


