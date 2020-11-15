/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package demos

import it.unibo.scafi.distrib.actor.{Platform => ActorPlatform}
import it.unibo.scafi.incarnations.BasicAbstractActorIncarnation

trait Demo6_Platform extends ActorPlatform with BasicAbstractActorIncarnation {
  val SourceSensorName: String = "source"

  trait Demo6DeviceActor extends WeakCodeMobilityDeviceActor {
    override def updateProgram(program: () => Any): Unit = program() match {
      case ap: ScafiStandardAggregateProgram => super.updateProgram(() => ap: ProgramContract)
    }
  }

  val IdleAggregateProgram = () => new ScafiStandardAggregateProgram {
    override def main(): String = "IDLE"
  }
  val StillFieldAggregateProgram = () => new ScafiStandardAggregateProgram {
    override def main(): Int = 1
  }
  val SourceDetectorAggregateProgram = () => new ScafiStandardAggregateProgram {
    override def main(): Boolean = sense(SourceSensorName)
  }
  val RandomFieldAggregateProgram = () => new ScafiStandardAggregateProgram {
    override def main(): Double = Math.random()
  }
  val ConstantRandomFieldAggregateProgram = () => new ScafiStandardAggregateProgram {
    override def main(): Double = rep(Math.random()){x => x}
  }
  val RoundCounterAggregateProgram = () => new ScafiStandardAggregateProgram {
    override def main(): Int = rep(0) { _ + 1 }
  }
  val RandomIncreasingFieldAggregateProgram = () => new ScafiStandardAggregateProgram {
    override def main(): Double = rep(0.0){x => x + rep(Math.random()){y=>y} }
  }
  val NeighborsCountAggregateProgram = () => new ScafiStandardAggregateProgram {
    override def main(): Int = foldhoodPlus(0)(_ + _)(1)
  }
  val BooleanGossipAggregateProgram = () => new ScafiStandardAggregateProgram {
    override def main(): Boolean = rep(false)(x =>
      sense[Boolean](SourceSensorName) | foldhoodPlus(false)(_|_)(nbr(x)))
  }
  val HopGradientAggregateProgram = () => new ScafiStandardAggregateProgram {
    override def main(): Double = rep(Double.PositiveInfinity) {
      hops => {
        mux(sense(SourceSensorName)) { 0.0 } { 1 + minHood(nbr { hops }) }
      }
    }
  }
}
