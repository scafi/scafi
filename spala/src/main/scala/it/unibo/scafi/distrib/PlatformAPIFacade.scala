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

package it.unibo.scafi.distrib

trait PlatformAPIFacade { self: Platform.Subcomponent =>

  trait DistributedPlatformFactory {
    def buildPlatformConfigurator(): PlatformConfigurator
  }
  val platformFactory: DistributedPlatformFactory

  type PlatformFacade <: AbstractPlatformFacade
  type SystemFacade <: AbstractSystemFacade
  type DeviceManager <: AbstractDeviceManager

  trait SystemMain extends App with Serializable {
    def programBuilder: Option[ProgramContract] = None

    def setupSystem(settings: Settings): Unit = {
      val s = refineSettings(settings)
      val pc = self.platformFactory.buildPlatformConfigurator()
      val platform = pc.setupPlatform(s.platform, s.profile)
      onPlatformReady(platform)

      val sys = platform.newAggregateApplication(s.aggregate, s.profile, s.execution.scope)
      onReady(sys)

      if(s.start) sys.start()

      s.deviceConfig.ids.foreach { id =>
        val dm = sys.newDevice(id, programBuilder.orElse(s.aggregate.program()), s.deviceConfig.nbs.getOrElse(id,Set()))
        onDeviceStarted(dm, sys)
      }
    }

    def onPlatformReady(platform: PlatformFacade): Unit = { }
    def onReady(app: SystemFacade): Unit = { }
    def onDeviceStarted(dm: DeviceManager, sys: SystemFacade): Unit = { }
    def refineSettings(s: Settings): Settings = { s }
  }

  class CmdLineMain extends SystemMain {
    override def main(args: Array[String]): Unit = {
      cmdLineParser.parse(args, Settings()) foreach (s => setupSystem(s))
    }
  }

  class BasicMain(val settings: Settings) extends SystemMain {
    override def main(args: Array[String]): Unit = {
      setupSystem(settings)
    }
  }

  class FileMain(val configFile: String) extends SystemMain {
    override def main(args: Array[String]): Unit = {
      setupSystem(Settings.fromConfig(configFile))
    }
  }

  trait AbstractDeviceManager {
    def addSensorValue[T](name: LSensorName, value: T): Unit
    def addSensor[T](name: LSensorName, provider: ()=>T): Unit
    def addActuator(name: LSensorName, consumer: Any=>Unit): Unit
  }

  trait AbstractSystemFacade {
    def newDevice(id: UID,
                  program: Option[ProgramContract] = None,
                  neighbors: Set[UID] = Set()): DeviceManager
    def addNeighbor(id: UID, idn: UID): Unit
    def start(): Unit
  }

  trait AbstractPlatformFacade {
    def newAggregateApplication(appSettings: AggregateApplicationSettings,
                                profileSettings: ProfileSettings,
                                execScope: ExecScope): SystemFacade
  }

  trait PlatformConfigurator {
    def setupPlatform(s: PlatformSettings,
                      p: ProfileSettings): PlatformFacade
    def allInOneSetup(s: Settings): SystemFacade = {
      val platform = setupPlatform(s.platform, s.profile)
      val aggregateApp = platform.newAggregateApplication(s.aggregate, s.profile, s.execution.scope)
      s.deviceConfig.ids.foreach{ devId =>
        aggregateApp.newDevice(devId, s.aggregate.program(), s.deviceConfig.nbs.getOrElse(devId,Set()))
      }
      s.deviceConfig.nbs.keys.foreach(devId =>
        s.deviceConfig.nbs(devId).foreach(nbrId => aggregateApp.addNeighbor(devId, nbrId))
      )
      aggregateApp
    }

    def deviceSetup(s: Settings, devId: UID): (PlatformFacade, SystemFacade, DeviceManager) = {
      val platform = setupPlatform(s.platform, s.profile)
      val aggregateApp = platform.newAggregateApplication(s.aggregate, s.profile, s.execution.scope)

      val dm = aggregateApp.newDevice(devId, s.aggregate.program(), s.deviceConfig.nbs.getOrElse(devId,Set()))

      (platform, aggregateApp, dm)
    }
  }

}
