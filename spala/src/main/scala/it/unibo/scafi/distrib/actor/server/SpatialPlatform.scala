/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.distrib.actor.server

import akka.actor.{ActorRef, Props}
import it.unibo.scafi.distrib.actor.server.{Platform => BasePlatform}
import it.unibo.scafi.space.MetricSpatialAbstraction

/**
 * Specializes an [[it.unibo.scafi.distrib.actor.Platform]] into a "centralized platform" where
 *   - There is a central component in the system to which all the devices
 *     have to register and communicate in order to get info such as
 *     neighborhood state.
 */

trait SpatialPlatform extends BasePlatform {
  thisVery: MetricSpatialAbstraction =>

  val LocationSensorName: LSensorName

  case class MsgWithPosition(id: UID, pos: P)

  class SettingsFactorySpatial extends SettingsFactoryServer {
    override def defaultProfileSettings(): ProfileSettings =
      super.defaultProfileSettings().copy(serverActorProps = SpatialServerActor.props(_))
  }

  @transient override val settingsFactory = new SettingsFactorySpatial

  class SpatialServerActor(val space: MutableMetricSpace[UID], override val scheduler: Option[ActorRef])
    extends ServerActor(scheduler) {

    override def neighborhood(id: UID): Set[UID] = {
      if(space.contains(id)) space.getNeighbors(id).toSet else Set()
    }

    override def setSensorValue(id: UID, name: LSensorName, value: Any): Unit = {
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
