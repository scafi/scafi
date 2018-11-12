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

package it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.actor

import akka.actor.{Actor, Props}
import it.unibo.scafi.distrib.actor.MsgAddObserver
import it.unibo.scafi.simulation.SimulationObserver
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.actor.PlatformSimulatorActor._
import it.unibo.utils.observer.SimpleSource
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.ScafiWorldIncarnation._

class PlatformSimulatorActor(val I: SimulationActorPlatform) extends Actor with SimpleSource {
  override type O = SimulationObserver[_,_]
  var exports: Map[ID, EXPORT] = Map()

  override def receive: Receive = {
    case MsgAttachObserver(obs) => attach(obs)
    case MsgDetachObserver(obs) => detach(obs)
    case MsgObserveDevices() => I.devices.values.foreach(dev => dev ! MsgAddObserver(self))
    case I.MsgExport(id, export) => exports += id -> export.asInstanceOf[EXPORT]
    case MsgNeighborhood(id, nbrs) =>
      I.devices(id) ! I.MsgNeighborhoodUpdate(id, nbrs.map(nbr => nbr -> I.devices(nbr)).toMap)
    case MsgGetExports() => sender ! MsgExports(exports)
    case msg => unhandled(msg)
  }
}

object PlatformSimulatorActor {
  def props(inc: SimulationActorPlatform): Props = Props(classOf[PlatformSimulatorActor], inc)
  case class MsgAttachObserver(observer: SimulationObserver[_,_])
  case class MsgDetachObserver(observer: SimulationObserver[_,_])
  case class MsgObserveDevices()
  case class MsgGetExports()
  case class MsgExports(exps: Map[ID, EXPORT])
  case class MsgNeighborhood(id: ID, nbrs: Set[ID])
}
