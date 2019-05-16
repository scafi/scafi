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

import scala.language.postfixOps
import com.typesafe.config.{Config, ConfigFactory, ConfigObject}

import scala.concurrent.duration._
import java.io.File
import java.util.concurrent.TimeUnit

import it.unibo.utils.Interop

trait PlatformSettings { self: Platform.Subcomponent =>

  type ProfileSettings <: ConfigurableSettings[ProfileSettings]
  val settingsFactory: SettingsFactory

  trait SettingsFactory extends Serializable {
    def defaultProfileSettings(): ProfileSettings
    def defaultSettings(): Settings = Settings()
  }

  trait ConfigurableSettings[S] { def fromConfig(c: Config): S }

  case class Settings(aggregate: AggregateApplicationSettings = AggregateApplicationSettings(),
                      platform: PlatformSettings = PlatformSettings(),
                      profile: ProfileSettings = settingsFactory.defaultProfileSettings(),
                      deviceConfig: DeviceConfigurationSettings = DeviceConfigurationSettings(),
                      execution: ExecutionSettings = ExecutionSettings(),
                      start: Boolean = true)

  case class AggregateApplicationSettings(name: String = "untitled",
                                          program: () => Option[ProgramContract] = () => None)
  case class DeviceConfigurationSettings(ids: Set[UID] = Set(),
                                         nbs: Map[UID,Set[UID]] = Map())
  case class DeploymentSettings(host: String = "127.0.0.1",
                                port: Int = 9000)
  case class PlatformSettings(subsystemDeployment: DeploymentSettings = DeploymentSettings(),
                              otherSubsystems: Set[SubsystemSettings] = Set(),
                              loglevel: String = LogLevels.Debug,
                              codeMobilitySupport: Boolean = true)
  case class SubsystemSettings(subsystemDeployment: DeploymentSettings = DeploymentSettings(),
                               ids: Set[UID] = Set())
  case class EmptyProfileSettings() extends ConfigurableSettings[EmptyProfileSettings] {
    override def fromConfig(c: Config): EmptyProfileSettings = EmptyProfileSettings()
  }
  case class ExecutionSettings(scope: ExecScope = DeviceDelegated(DelayedDeviceExecStrategy()))

  sealed trait ExecScope
  case class Global(strategy: ExecStrategy) extends ExecScope
  case class SubsystemDelegated(strategy: ExecStrategy) extends ExecScope
  case class DeviceDelegated(strategy: DeviceExecStrategy) extends ExecScope

  sealed trait ExecStrategy
  sealed trait AsyncExecStrategy extends ExecStrategy
  case class RandomExecStrategy(seed: Int) extends AsyncExecStrategy
  case class OrderedExecStrategy(nextToRun: ()=>Option[UID]) extends AsyncExecStrategy
  case object RoundRobinStrategy extends AsyncExecStrategy

  sealed trait DeviceExecStrategy
  case class DelayedDeviceExecStrategy(initial: Option[FiniteDuration] = None,
                                       delay: FiniteDuration = 1 second) extends DeviceExecStrategy
  case class PeriodicDeviceExecStrategy(initial: Option[FiniteDuration] = None,
                                        interval: FiniteDuration = 1 second) extends DeviceExecStrategy
  object ReactiveDeviceExecStrategy extends DeviceExecStrategy

  object LogLevels extends Serializable {
    val Off = "OFF"
    val Info = "INFO"
    val Warning = "WARNING"
    val Debug = "DEBUG"
  }

  import scala.collection.JavaConversions._

  object Settings {
    def fromConfig(path: String): Settings = {
      val config = ConfigFactory.parseFile(new File(path)).withFallback(ConfigFactory.load("aggregate_application_reference.conf"))
      println("Configuration: " + config.getObject("aggregate"))
      Settings.fromConfig(config.resolve())
    }

    def fromConfig(c: Config, base: Settings = Settings()): Settings = {
      var s = base
      s = s.copy(
        aggregate = AggregateApplicationSettings.fromConfig(c.getObject("aggregate.application").toConfig, s.aggregate),
        execution = ExecutionSettings.fromConfig(c.getObject("aggregate.execution").toConfig, s.execution),
        platform = PlatformSettings.fromConfig(c.getObject("aggregate").toConfig, s.platform),
        deviceConfig = DeviceConfigurationSettings.fromConfig(c.getObject("aggregate.devices").toConfig, s.deviceConfig),
        profile =
          if(c.hasPath("aggregate.profile")) {
            settingsFactory.defaultProfileSettings().fromConfig(c.getObject("aggregate.profile").toConfig)
          } else s.profile
      )
      s
    }
  }
  object AggregateApplicationSettings {
    def fromConfig(c: Config, base: AggregateApplicationSettings = AggregateApplicationSettings()): AggregateApplicationSettings = {
      val programClass = if(c.hasPath("program-class")) Some(c.getString("program-class")) else None
      var aas = base.copy(name = c.getString("name"))
      programClass.foreach { programClassName =>
        val klass = Class.forName(programClassName)
        aas = base.copy(program = () => Some(klass.newInstance().asInstanceOf[ProgramContract]))
      }
      aas
    }
  }
  object ExecutionSettings {
    def fromConfig(c: Config, base: ExecutionSettings = ExecutionSettings()): ExecutionSettings = {
      val scopeConf = c.getObject("scope").toConfig

      val seed = if(scopeConf.hasPath("seed")) scopeConf.getInt("seed") else System.currentTimeMillis().toInt
      val initialDelay = if(scopeConf.hasPath("initial-delay")) Some(FiniteDuration(scopeConf.getInt("initial-delay"), TimeUnit.MILLISECONDS)) else None

      var scope: ExecScope = null
      (scopeConf.getString("type"), scopeConf.getString("strategy")) match {
        case ("global","round-robin") => scope = Global(RoundRobinStrategy)
        case ("global","random") => scope = Global(RandomExecStrategy(seed))
        case ("subsystem","round-robin") => scope = SubsystemDelegated(RoundRobinStrategy)
        case ("subsystem","random") => scope = SubsystemDelegated(RandomExecStrategy(seed))
        case ("device","delayed") => scope = DeviceDelegated(DelayedDeviceExecStrategy(initialDelay,
          FiniteDuration(scopeConf.getInt("interval"), TimeUnit.MILLISECONDS)))
        case ("device","periodic") => scope = DeviceDelegated(PeriodicDeviceExecStrategy(initialDelay,
          FiniteDuration(scopeConf.getInt("interval"), TimeUnit.MILLISECONDS)))
      }

      var es = base.copy(scope = scope)
      es
    }
  }
  object PlatformSettings {
    def fromConfig(c: Config, base: PlatformSettings = PlatformSettings()): PlatformSettings = {
      val deploy = c.getObject("deployment").toConfig
      val subsystems = if(c.hasPath("subsystems")) c.getObjectList("subsystems").toSet else Set()

      var ps = base.copy(
        subsystemDeployment = DeploymentSettings(host = deploy.getString("host"), port = deploy.getInt("port")),
        otherSubsystems = subsystems.map(subsysConf => SubsystemSettings.fromConfig(subsysConf.toConfig))
      )
      ps
    }
  }
  object SubsystemSettings {
    def fromConfig(c: Config, base: SubsystemSettings = SubsystemSettings())
                  (implicit ev: Interop[UID]): SubsystemSettings = {
      val deploy = c.getObject("deployment").toConfig
      val ids = if(c.hasPath("ids")) c.getStringList("ids").toSet else Set()

      var ss = base.copy(
        subsystemDeployment = DeploymentSettings(host = deploy.getString("host"), port = deploy.getInt("port")),
        ids = ids.map(ev.fromString(_))
      )
      ss
    }
  }
  object DeviceConfigurationSettings {
    def fromConfig(c: Config, base: DeviceConfigurationSettings = DeviceConfigurationSettings())
                  (implicit ev: Interop[UID]): DeviceConfigurationSettings = {
      val ids = if(c.hasPath("ids")) c.getStringList("ids").toSet else Set()
      val idsWithNbs = if(c.hasPath("nbrs")) c.getObject("nbrs").keys else Set[String]()

      var ds = base.copy(
        ids = ids.map(ev.fromString(_)).toSet,
        nbs = idsWithNbs.map { id =>
          val nbrs = c.getStringList(s"nbrs.${id}").toSet[String].map(ev.fromString(_))
          ev.fromString(id) -> nbrs
        }.toMap
      )
      ds
    }
  }

  /*********************************/
  /******** CMD-LINE PARSER ********/
  /*********************************/

  /**
   * Template method for extending the parser in specialized platform components.
   */
  def extendParser(p: scopt.OptionParser[Settings]): Unit = { }

  def cmdLineParser: scopt.OptionParser[Settings] =
    new ScafiCmdLineParser

  class ScafiCmdLineParser extends scopt.OptionParser[Settings]("<scafi distributed system>") {
      extendParser(this)

      opt[String]("loglevel") valueName ("<off|info|warning|debug>") action { (x, c) =>
        c.copy(platform = c.platform.copy(loglevel = x))
      } text ("Log level") validate(x => x.toLowerCase match {
        case "off" | "info" | "warning" | "debug" => success
        case _ => failure("Admitted values are: 'off', 'info', 'warning', 'debug'")
      })

      opt[String]("program") valueName ("<FULLY QUALIFIED CLASS NAME>") action { (x, c) =>
        val klass = Class.forName(x)
        c.copy(aggregate = c.aggregate.copy(program = () => Some(klass.newInstance().asInstanceOf[ProgramContract])))
      } text ("Aggregate program")

      opt[String]('h', "host") valueName ("<HOST>") action { (x, c) =>
        c.copy(platform = c.platform.copy(subsystemDeployment = c.platform.subsystemDeployment.copy(host = x)))
      } text ("Host of deployment of the subsystem node")

      opt[Int]('p', "port") valueName ("<PORT>") action { (x, c) =>
        c.copy(platform = c.platform.copy(subsystemDeployment = c.platform.subsystemDeployment.copy(port = x)))
      } text ("Port of deployment of the subsystem node")

      opt[String]("sched-global") action { (x,c) =>
        val strategy = x match {
          case "random" => RandomExecStrategy(System.currentTimeMillis().toInt)
          case "rr" => RoundRobinStrategy
        }
        c.copy(execution = c.execution.copy(scope = Global(strategy)))
      } text("Global scheduling")

      opt[String]("sched-sub") action { (x,c) =>
        val strategy = x match {
          case "random" => RandomExecStrategy(System.currentTimeMillis().toInt)
          case "rr" => RoundRobinStrategy
        }
        c.copy(execution = c.execution.copy(scope = SubsystemDelegated(strategy)))
      } text("Subsystem-delegated scheduling")

      opt[String]("sched-dev") action { (x,c) =>
        val parts = x.split(":")
        val np = parts.size

        val strategy = parts(0) match {
          case "delayed" if np==1 => DelayedDeviceExecStrategy()
          case "delayed" if np==2 => DelayedDeviceExecStrategy(delay = parts(1).toInt millis)
          case "delayed" => DelayedDeviceExecStrategy(initial = Some(parts(1).toInt millis), delay = parts(2).toInt millis)

          case "periodic" if np==1 => PeriodicDeviceExecStrategy()
          case "periodic" if np==2 => PeriodicDeviceExecStrategy(interval = parts(1).toInt millis)
          case "periodic" => PeriodicDeviceExecStrategy(initial = Some(parts(1).toInt millis), interval = parts(2).toInt millis)
        }
        c.copy(execution = c.execution.copy(scope = DeviceDelegated(strategy)))
      } text("Subsystem-delegated scheduling")

      opt[Boolean]("start") valueName ("start") action { (x, c) =>
        c.copy(start = x)
      } text("Start the system")

      opt[String]('S', "subsystems") valueName
        ("<HOST1>:<PORT1>:<ID11>:<ID12>:...;<HOST2>:<PORT2>:<ID21>:...") action { (x, c) => {
        var ss = Set[SubsystemSettings]()
        x.split(';').foreach { subsysDesc =>
          val xs = subsysDesc.split(":")
          val host = xs(0)
          val port = xs(1).toInt
          val ids = xs.drop(2).map(interopUID.fromString(_)).toSet

          ss += SubsystemSettings(
            ids = ids,
            subsystemDeployment = new DeploymentSettings(host,port))
        }
        c.copy(platform = c.platform.copy(otherSubsystems = c.platform.otherSubsystems ++ ss ))
      } } text ("Subsystems")

      opt[String]('e', "elements") valueName
        ("<ID1>:<NBR11>,<NBR12>;<ID2>:<NBR21>,<NBR22>;...") action { (x, c) => {
        val elems = x.split(';')
        var parsedIds = Set[String]()
        var parserNbs = Map[String, Set[String]]()

        for (e <- elems) {
          val eld = e.split(':')
          val el = eld.head
          parsedIds = parsedIds + el

          if(!eld.tail.isEmpty) {
            val elnbslist = eld.tail.head
            val elnbs = elnbslist.split(',')
            parserNbs += (el -> elnbs.toSet)
          }
        }

        c.copy(deviceConfig = c.deviceConfig.copy(
          ids = parsedIds.map(interopUID.fromString(_)),
          nbs = parserNbs.map {
            case (k, v) => interopUID.fromString(k) -> v.map(interopUID.fromString(_))
          }))
      } } text ("Neighbors")

      //note("some notes.\n")
      help("help") text ("prints this usage text")
    }

}
