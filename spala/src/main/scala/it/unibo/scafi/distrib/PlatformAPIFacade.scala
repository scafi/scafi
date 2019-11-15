/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
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
