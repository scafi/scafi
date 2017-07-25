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

package it.unibo.scafi.distrib.actor

import akka.actor.{Props, Actor}

trait PlatformActors { self: Platform.Subcomponent =>

  /**
   * This is the top-level actor for a given aggregate application in the
   *  current subsystem.
   * Responsibilities
   *   - Creation of devices
   *   - Supervision of devices (as a consequence of previous point)
   * @param settings about the aggregate programming application
   */
  class AggregateApplicationActor(val settings: AggregateApplicationSettings) extends Actor {
    val logger = akka.event.Logging(context.system, this)

    def receive: Receive = {
      case MsgAddDevice(id, props) => {
        logger.info(s"\nCreating device $id")
        val dref = context.actorOf(props, deviceNameFromId(id))
        sender ! MsgDeviceLocation(id,dref)
      }
      case MsgCreateActor(props, nameOpt, corrOpt) => {
        val name = nameOpt.getOrElse("unnamed-" + System.currentTimeMillis())
        val aref = context.actorOf(props, name)
        sender ! MsgCreationAck(aref, name, corrOpt)
      }
      case MsgPropagate(msg) => {
        deviceActors.foreach(_ ! msg)
      }
      case MsgDeliverTo(id, msg) => {
        val recipient = context.children.find(_.path.name == deviceNameFromId(id))
        recipient.foreach(_ ! msg)
      }
      case MsgNeighbor(id,idn) => {
        val recipient = context.children.find(_.path.name == deviceNameFromId(id))
        val nbr = context.children.find(_.path.name == deviceNameFromId(idn))
        recipient.foreach(_ ! NbrInfo(idn,None,nbr,Some(self.path + "/" + deviceNameFromId(idn))))
      }
    }

    override def preStart() = {
      super.preStart()
      logger.info(s"\nSTARTED AGGREGATE APPLICATION '${settings.name}'")
    }

    val DEV_NAME_PREFIX = "dev-"

    private def deviceActors = context.children.filter(_.path.name.startsWith(DEV_NAME_PREFIX))

    private def deviceNameFromId(id: ID) = DEV_NAME_PREFIX+id
  }
  object AggregateApplicationActor extends Serializable {
    def props(as: AggregateApplicationSettings): Props =
      Props(classOf[AggregateApplicationActor], self, as)
  }

}
