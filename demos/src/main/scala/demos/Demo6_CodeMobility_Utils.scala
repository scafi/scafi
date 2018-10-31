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

package demos

import it.unibo.scafi.distrib.actor.{Platform => ActorPlatform}
import it.unibo.scafi.incarnations.BasicAbstractActorIncarnation

trait Demo6_Platform extends ActorPlatform with BasicAbstractActorIncarnation {
  val SourceSensorName: String = "source"

  trait Demo6DeviceActor extends CodeMobilityDeviceActor {
    override def updateProgram(nid: UID, program: ()=>Any): Unit = program() match {
      case ap: AggregateProgram => aggregateExecutor = Some(ap); lastExport = None
    }
  }

  val IdleAggregateProgram = () => new AggregateProgram {
    override def main(): String = "IDLE"
  }
  val StillFieldAggregateProgram = () => new AggregateProgram {
    override def main(): Int = 1
  }
  val SourceDetectorAggregateProgram = () => new AggregateProgram {
    override def main(): Boolean = sense(SourceSensorName)
  }
  val RandomFieldAggregateProgram = () => new AggregateProgram {
    override def main(): Double = Math.random()
  }
  val ConstantRandomFieldAggregateProgram = () => new AggregateProgram {
    override def main(): Double = rep(Math.random()){x => x}
  }
  val RoundCounterAggregateProgram = () => new AggregateProgram {
    override def main(): Int = rep(0) { _ + 1 }
  }
  val RandomIncreasingFieldAggregateProgram = () => new AggregateProgram {
    override def main(): Double = rep(0.0){x => x + rep(Math.random()){y=>y} }
  }
  val NeighborsCountAggregateProgram = () => new AggregateProgram {
    override def main(): Int = foldhoodPlus(0)(_ + _)(1)
  }
  val BooleanGossipAggregateProgram = () => new AggregateProgram {
    override def main(): Boolean = rep(false)(x =>
      sense[Boolean](SourceSensorName) | foldhoodPlus(false)(_|_)(nbr(x)))
  }
  val HopGradientAggregateProgram = () => new AggregateProgram {
    override def main(): Double = rep(Double.PositiveInfinity) {
      hops => {
        mux(sense(SourceSensorName)) { 0.0 } { 1 + minHood(nbr { hops }) }
      }
    }
  }
}
