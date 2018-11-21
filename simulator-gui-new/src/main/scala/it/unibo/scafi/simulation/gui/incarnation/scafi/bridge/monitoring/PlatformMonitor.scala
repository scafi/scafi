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

package it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.monitoring

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import it.unibo.scafi.distrib.actor.MsgAddObserver
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.ScafiWorldIncarnation
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.ScafiWorldIncarnation._
import it.unibo.scafi.incarnations.{BasicAbstractActorIncarnation => Platform}
import it.unibo.scafi.simulation.SimulationObserver.{MovementEvent, SensorChangedEvent}

class PlatformMonitor(override val space: SPACE[ID],
                      override val devs: Map[ID, ScafiWorldIncarnation.DevInfo],
                      val platformName: String,
                      val platformNodes: Map[ID, (String, Int)],
                      val I: Platform)
  extends SpaceAwareSimulator(space = space, devs = devs, simulationSeed = 0, randomSensorSeed = 0) {

  var devicesLocation: Map[ID, Option[ActorRef]] = devs.map(_._1 -> None)

  ActorSystem().actorOf(Props(classOf[PlatformObserverActor], this, I))

  private def setExport(id: ID, export: EXPORT): Unit = {
    eMap.put(id, export)
  }

  override def setPosition(id: ID, newPos: P): Unit = {
    val previousNbrs = neighbourhood(id) - id
    devs(id).pos = newPos
    space.setLocation(id, newPos)
    val nextNbrs = neighbourhood(id) - id
    updateNbrs(previousNbrs + id ++ nextNbrs)
    updateNbrSensors(previousNbrs + id ++ nextNbrs)
    this.notify(MovementEvent(id))
  }

  override def chgSensorValue[A](name: String, ids: Set[Int], value: A): Unit = {
    ids.foreach(id => {
      devs(id).lsns += name -> value
      devicesLocation(id).foreach(ref => ref ! I.MsgLocalSensorValue(name, value))
      this.notify(SensorChangedEvent(id, name))
    })
  }

  private def updateNbrs(ids: Set[ID]): Unit = ids.foreach { id =>
    devicesLocation(id).foreach(ref => {
      ref ! I.MsgNeighborhoodLocations(id,
        neighbourhood(id).filter(nbr => devicesLocation(nbr).isDefined).map(nbr => nbr -> devicesLocation(nbr).get.path.toString).toMap)
    })
  }

  private def updateNbrSensors(ids: Set[ID]): Unit = ids.foreach { id =>
    devicesLocation(id).foreach(ref => {
      ref ! I.MsgNbrSensorValue(NBR_RANGE_NAME,
        neighbourhood(id).map(nid => nid -> space.getDistance(space.getLocation(id), space.getLocation(nid))).toMap)
    })
  }

  class PlatformObserverActor(platform: Platform) extends Actor {
    import scala.util.{Failure, Success}
    import scala.concurrent.duration._
    import context.dispatcher

    override def preStart(): Unit = {
      super.preStart()

      val LOOKUP_TIMEOUT = 2.seconds
      devs.foreach(dev => {
        val path = self.path.address.copy(
          protocol = "akka.tcp",
          system = "scafi",
          host = Some(platformNodes(dev._1)._1),
          port = Some(platformNodes(dev._1)._2)
        ).toString + "/user/" + platformName + "/dev-" + dev._1
        context.system.actorSelection(path).resolveOne(LOOKUP_TIMEOUT).onComplete {
          case Success(ref) => devicesLocation += dev._1 -> Some(ref); ref ! MsgAddObserver(self)
          case Failure(e) =>
        }
      })

    }

    override def receive: Receive = {
      case platform.MsgExport(id, export) => setExport(id, export.asInstanceOf[EXPORT])
      case _ =>
    }
  }
}
