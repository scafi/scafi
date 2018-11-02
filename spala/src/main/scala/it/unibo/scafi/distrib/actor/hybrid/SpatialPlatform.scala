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

package it.unibo.scafi.distrib.actor.hybrid

import akka.actor.{ActorRef, Props}
import akka.util.Timeout
import akka.pattern.ask
import it.unibo.scafi.distrib.actor.hybrid.{Platform => BasePlatform}
import it.unibo.scafi.space.MetricSpatialAbstraction

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Specializes an [[it.unibo.scafi.distrib.actor.Platform]] into a "centralized platform" where
  *   - There is a central component in the system to which all the devices
  *     have to register and communicate in order to get info such as
  *     neighborhood state.
  */

trait SpatialPlatform extends BasePlatform {
  thisVery: MetricSpatialAbstraction =>

  val LocationSensorName: LSensorName

  class SettingsFactorySpatial extends SettingsFactoryHybrid {
    override def defaultProfileSettings(): ProfileSettings =
      super.defaultProfileSettings().copy(
        devActorProps = (id, program, scope, server) => Some(SpatialDeviceActor.props(id, program, scope, server)),
        serverActorProps = SpatialServerActor.props())
  }

  @transient override val settingsFactory = new SettingsFactorySpatial

  class SpatialDeviceActor(override val selfId: UID,
                           _aggregateExecutor: Option[ProgramContract],
                           _execScope: ExecScope,
                           override val server: ActorRef)
    extends DeviceActor(selfId, _aggregateExecutor, _execScope, server) {

    import context.dispatcher
    context.system.scheduler.schedule(
      initialDelay = 0 seconds,
      interval = 1 second,
      receiver = server,
      message = MsgGetNeighborhoodLocations(selfId))

    override def setLocalSensorValue(name: LSensorName, value: Any): Unit = {
      super.setLocalSensorValue(name, value)
      if (name == LocationSensorName) {
        server ! MsgPosition(selfId, value)
      }
    }
  }

  object SpatialDeviceActor {
    def props(selfId: UID, program: Option[ProgramContract], execStrategy: ExecScope, serverActor: ActorRef): Props =
      Props(classOf[SpatialDeviceActor], thisVery, selfId, program, execStrategy, serverActor)
  }

  class SpatialServerActor(val space: MutableMetricSpace[UID]) extends AbstractServerActor with ObservableServerActor {

    override def neighborhood(id: UID): Set[UID] = {
      if(space.contains(id)) space.getNeighbors(id).toSet else Set()
    }

    override def inputManagementBehavior: Receive = super.inputManagementBehavior.orElse {
      case MsgPosition(id, pos) =>
        space.setLocation(id, pos.asInstanceOf[P])
        this.space.getAll().foreach(id => {
          val nbs = this.space.getNeighbors(id)
          notifyObservers(MsgNeighborhood(id,nbs.toSet))
        })
    }

    override def queryManagementBehavior: Receive = super.queryManagementBehavior.orElse {
      case MsgGetNeighborhoodLocations(id) =>
        val locs = neighborhood(id)
          .filter(idn => lookupActor(idn).isDefined)
          .map(idn => idn -> lookupActor(idn).get.path.toString)
          .toMap
        sender ! MsgNeighborhoodLocations(id, locs)
    }
  }

  object SpatialServerActor {
    def props(): Props =
      Props(classOf[SpatialServerActor], thisVery, buildNewSpace(Seq()))
  }
}

