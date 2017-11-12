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

package it.unibo.scafi.distrib.actor.p2p

import akka.actor.{ActorSystem, ActorRef, Props}
import com.typesafe.config.Config
import it.unibo.scafi.distrib.actor.{GoOn, MsgPropagate}

trait PlatformAPIFacade { self: Platform.Subcomponent =>

  /**************************/
  /******** SETTINGS ********/
  /**************************/

  type ProfileSettings = P2PActorSystemSettings
  case class P2PActorSystemSettings(deviceGui: Boolean = false,
                                    devActorProps: ID => Option[Props] = _ => None,
                                    devGuiActorProps: ActorRef => Option[Props] = _ => None,
                                    tmGuiActorProps: ActorRef => Option[Props] = _ => None)
    extends ConfigurableSettings[P2PActorSystemSettings] {
    override def fromConfig(c: Config): P2PActorSystemSettings = {
      this.copy(deviceGui = c.getBoolean("device-gui"))
    }
  }

  /****************************/
  /******** API FACADE ********/
  /****************************/

  type SystemFacade = BasicSystemFacade

  class BasicSystemFacade(actorSys: ActorSystem,
                          appRef: ActorRef,
                          appSettings: AggregateApplicationSettings,
                          profSettings: ProfileSettings,
                          execScope: ExecScope,
                          val otherSubsystems: Set[SubsystemSettings] = Set())
    extends AbstractActorSystemFacade(actorSys, appRef, appSettings, profSettings, execScope) {

    override val deviceGui: Boolean = profSettings.deviceGui

    override def deviceGuiProps(dev: ActorRef): Props = profSettings.devGuiActorProps(dev).get

    override def deviceProps(id: ID, program: Option[ExecutionTemplate]): Props =
      DeviceActor.props(id, program, execScope)

    override def start(): Unit = {
      execScope match {
        case DeviceDelegated(DelayedDeviceExecStrategy(None,_))
             |  DeviceDelegated(PeriodicDeviceExecStrategy(None,_)) =>
          appRef ! MsgPropagate(GoOn)
        case _ => // Do nothing
      }
    }

    override def addNeighbor(id: ID, idn: ID): Unit = {
      // NOTE: In the decentralized case, if we have no information,
      //  then this operation is local to the subsystem
      otherSubsystems.find(_.ids.contains(idn)) match {
        case Some(osys) => {
          // TODO: factor out constants
          val path = appRef.path.address.copy(
            protocol = "akka.tcp",
            host = Some(osys.subsystemDeployment.host),
            port = Some(osys.subsystemDeployment.port)
          ).toString + "/user/" + appRef.path.name + "/dev-" + idn
          appRef ! MsgDeliverTo(id, NbrInfo(idn,None,None,Some(path)))
        }
        case None => appRef ! MsgNeighbor(id, idn)
      }
    }
  }

  class PlatformFacade(val actorSys: ActorSystem,
                       val otherSubsystems: Set[SubsystemSettings])
    extends AbstractPlatformFacade with Serializable {
    override def newAggregateApplication(appSettings: AggregateApplicationSettings,
                                         profileSettings: ProfileSettings,
                                         execScope: ExecScope): SystemFacade = {
      val appRef = actorSys.actorOf(AggregateApplicationActor.props(appSettings), appSettings.name)
      new BasicSystemFacade(actorSys, appRef, appSettings, profileSettings, execScope, otherSubsystems)
    }
  }

  object PlatformConfigurator extends ActorPlatformConfigurator with Serializable {
    override def buildPlatformFacade(sys: ActorSystem, s: PlatformSettings, p: P2PActorSystemSettings): PlatformFacade =
      new PlatformFacade(sys, s.otherSubsystems)
  }
}
