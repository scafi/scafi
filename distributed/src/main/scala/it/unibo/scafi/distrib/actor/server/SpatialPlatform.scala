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

package it.unibo.scafi.distrib.actor.server

import akka.actor.{ActorRef, Props}
import it.unibo.scafi.distrib.actor.server.{Platform => BasePlatform}
import it.unibo.scafi.platform.Platform.PlatformDependency
import it.unibo.scafi.space.{MetricSpatialAbstraction, SpatialAbstraction}

/**
 * Specializes an [[it.unibo.scafi.distrib.actor.Platform]] into a "centralized platform" where
 *   - There is a central component in the system to which all the devices
 *     have to register and communicate in order to get info such as
 *     neighborhood state.
 */

trait SpatialPlatform extends BasePlatform {
  thisVery: PlatformDependency with MetricSpatialAbstraction =>

  val LocationSensorName: LSNS

  case class MsgWithPosition(id: ID, pos: P)

  class SettingsFactorySpatial extends SettingsFactoryServer {
    override def defaultProfileSettings(): ProfileSettings =
      super.defaultProfileSettings().copy(serverActorProps = SpatialServerActor.props(_))
  }

  @transient override val settingsFactory = new SettingsFactorySpatial

  class SpatialServerActor(val space: MutableMetricSpace[ID],
                           val scheduler: Option[ActorRef])
    extends AbstractServerActor
    with ObservableServerActor {

    override def neighborhood(id: ID): Set[ID] = {
      if(space.contains(id)) space.getNeighbors(id).toSet else Set()
    }

    override def setSensorValue(id: ID, name: LSNS, value: Any): Unit = {
      super.setSensorValue(id, name, value)
      if(name == LocationSensorName) {
        space.setLocation(id, value.asInstanceOf[P])

        // Notify observers about change of neighborhoods
        // TODO: it is not very efficient (especially in dynamic nets)...
        this.space.getAll().foreach(id => {
          val nbs = this.space.getNeighbors(id)
          notifyObservers(MsgNeighborhood(id,nbs.toSet))
        })
      }
    }
  }

  object SpatialServerActor extends Serializable {
    def props(sched: Option[ActorRef] = None): Props =
      Props(classOf[SpatialServerActor], thisVery, buildNewSpace(Seq()), sched)
  }
}
