/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package sims

import java.util.concurrent.TimeUnit

import it.unibo.scafi.incarnations.BasicSimulationIncarnation._
import it.unibo.scafi.simulation.frontend.model.implementation.SensorEnum

import scala.concurrent.duration.Duration
import scala.util.Random

class Mid extends AggregateProgram {
  override def main(): ID = mid()
}

class Types extends AggregateProgram {
  override def main(): (String, Int, List[Int]) = ("a", 1, List(10,20))
}

class CountRounds extends AggregateProgram {
  override def main(): Int = rep(0)(x => x + 1)
}

class CountNeighbours extends AggregateProgram {
  override def main(): Int = foldhood(0)(_ + _) { nbr { 1 } }
}

class CountNeighboursExceptMyself extends AggregateProgram {
  override def main(): Int = foldhood(0)(_ + _) {
    if (nbr { mid() } == mid()) 0 else 1
  }
}

class MaxId extends AggregateProgram {
  override def main(): (ID, Int) = {
    val maxId = foldhood(Int.MinValue)(Math.max(_, _)) { nbr(mid()) }
    (mid(), maxId)
  }
}

class Gradient extends AggregateProgram with StandardSensors {
  def isSource: Boolean = sense[Boolean](SensorEnum.SENS1.name)
  def isObstacle: Boolean = sense[Boolean](SensorEnum.SENS2.name)

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
  def isSource: Boolean = sense[Boolean](SensorEnum.SENS1.name)

  def hopGradientByG(src: Boolean): Double = Gcurried(src)(0)(_ + 1)(() =>1)

  override def main(): Int = hopGradientByG(isSource).toInt
}

class RouteChannel extends AggregateProgram with SensorDefinitions with BlockG {
  override def main(): Boolean = channel(sense1, sense2, 0.05)

  def channel2(source: Boolean, target: Boolean, width: Double): (String, String, String) =
    (distanceTo(source).formatted("%.2f"), distanceTo(target).formatted("%.2f"), distanceBetween(source, target).formatted("%.2f"))
}

class Timer extends AggregateProgram with StandardSensors with TimeUtils {
  override def main(): String = Duration(
    branch(!sense[Boolean](SensorEnum.SENS1.name)){ timerLocalTime(Duration(30, TimeUnit.SECONDS)) } { 0 },
    TimeUnit.NANOSECONDS
  ).toMillis + "ms"
}

class SparseChoice extends AggregateProgram with SensorDefinitions with BlockG with BlockS {
  override def main(): Boolean = S(20, nbrRange) //if(channel(isSource, isDest, 0)) 1 else 0
}

class SensorNbrRange extends AggregateProgram with StandardSensors with FieldUtils {
  import excludingSelf.reifyField
  override def main(): String = mid() + " => " + reifyField("%.2f".format(nbrRange()))
}

class SensorCurrTime extends AggregateProgram with StandardSensors with FieldUtils {
  override def main(): String = currentTime().toString
}

class SensorTimestamp extends AggregateProgram with StandardSensors with FieldUtils {
  override def main(): String = timestamp() + "ms"
}

class SensorCurrPos extends AggregateProgram with StandardSensors with FieldUtils {
  override def main(): P = currentPosition()
}

class SensorNbrVector extends AggregateProgram with StandardSensors with FieldUtils {
  import excludingSelf.reifyField
  override def main(): String = mid() + " => " + reifyField(nbrVector())
}

class SensorDeltaTime extends AggregateProgram with StandardSensors with FieldUtils {
  override def main(): String = deltaTime().toMillis + "ms"
}

class SensorNbrDelay extends AggregateProgram with StandardSensors with FieldUtils {
  import excludingSelf.reifyField
  override def main(): String = mid() + " => " + reifyField(nbrDelay().toMillis + "ms")
}

class SensorNbrLag extends AggregateProgram with StandardSensors with FieldUtils {
  import excludingSelf.reifyField
  override def main(): String = deltaTime().toMillis  + "ms -- " + mid() + " => " + reifyField(nbrLag().toMillis + "ms")
}

class SensorNbrDelayLag extends AggregateProgram with StandardSensors with FieldUtils {
  import excludingSelf.reifyField
  override def main(): String = mid() + " => " + reifyField(s"${nbrDelay().toMillis}ms; ${nbrLag().toMillis}ms")
}

class CollectNbrsIncludingMyself extends AggregateProgram with StandardSensors with FieldUtils {
  override def main(): Set[ID] = includingSelf.unionHood(nbr{mid})
}

class CollectNbrsExcludingMyself extends AggregateProgram with StandardSensors with FieldUtils {
  override def main(): Set[ID] = excludingSelf.unionHood(nbr{mid})
}

class DemoMeanCounter extends AggregateProgram with StandardSensors with GenericUtils {
  override def main(): Double = meanCounter(if(sense[Random](LSNS_RANDOM).nextDouble() > 0.5) 1 else -1, 50000)
}


