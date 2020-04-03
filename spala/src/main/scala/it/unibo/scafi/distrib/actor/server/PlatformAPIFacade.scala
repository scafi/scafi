/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.distrib.actor.server

import akka.actor.{ActorSystem, ActorRef, Props}
import com.typesafe.config.Config
import it.unibo.scafi.distrib.actor._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Success

trait PlatformAPIFacade { self: Platform.Subcomponent =>

  /**************************/
  /******** SETTINGS ********/
  /**************************/

  type ProfileSettings = ServerBasedActorSystemSettings
  case class ServerBasedActorSystemSettings(serverHost: String = "127.0.0.1",
                                            serverPort: Int = 9000,
                                            startServer: Boolean = false,
                                            deviceGui: Boolean = false,
                                            serverGui: Boolean = false,
                                            devActorProps: (UID, Option[ProgramContract], ExecScope, ActorRef) => Option[Props] = (_,_,_,_) => None,
                                            serverActorProps: Option[ActorRef]=>Props = ServerActor.props(_),
                                            devGuiActorProps: ActorRef => Option[Props] = _ => None,
                                            serverGuiActorProps: ActorRef => Option[Props] = _ => None)
    extends ConfigurableSettings[ServerBasedActorSystemSettings]{
    override def fromConfig(c: Config): ServerBasedActorSystemSettings = {
      this.copy(
        serverHost = c.getString("server-host"),
        serverPort = c.getInt("server-port"),
        startServer = c.getBoolean("server-start"),
        deviceGui = c.getBoolean("device-gui"),
        serverGui = c.getBoolean("server-gui")
      )
    }
  }

  /****************************/
  /******** API FACADE ********/
  /****************************/

  trait ServerMain extends App with Serializable {
    def setupServer(settings: Settings): Unit = {
      var s = refineSettings(settings)
      s = s.copy(profile = s.profile.copy(startServer = true))

      val pc = self.platformFactory.buildPlatformConfigurator()
      val platform = pc.setupPlatform(s.platform, s.profile)
      onPlatformReady(platform)

      val sys = platform.newAggregateApplication(s.aggregate, s.profile, s.execution.scope)
      onServerReady(sys.server)

      if(s.start) sys.start()
    }

    def onPlatformReady(platform: PlatformFacade): Unit = { }
    def onServerReady(app: ActorRef): Unit = { }
    def refineSettings(s: Settings): Settings = { s }
  }

  class ServerCmdLineMain extends ServerMain {
    override def main(args: Array[String]): Unit = {
      cmdLineParser.parse(args, Settings()) foreach (s => setupServer(s))
    }
  }

  class ServerBasicMain(val deployment: DeploymentSettings,
                        val aggregate: AggregateApplicationSettings,
                        val gui: Boolean = true)
    extends ServerMain {
    override def main(args: Array[String]): Unit = {
      var s = Settings()
      s = s.copy(profile = s.profile.copy(
        startServer = true,
        serverHost = deployment.host,
        serverPort = deployment.port,
        serverGui = true
      ),
        aggregate = aggregate
      )
      setupServer(s)
    }
  }

  type SystemFacade = BasicSystemFacade

  class BasicSystemFacade(actorSys: ActorSystem,
                          appRef: ActorRef,
                          appSettings: AggregateApplicationSettings,
                          profSettings: ProfileSettings,
                          execScope: ExecScope,
                          val server: ActorRef,
                          var scheduler: Option[ActorRef])
    extends AbstractActorSystemFacade(actorSys, appRef, appSettings, profSettings, execScope) {

    override val deviceGui: Boolean = profSettings.deviceGui

    override def deviceGuiProps(dev: ActorRef): Props = profSettings.devGuiActorProps(dev).get

    override def deviceProps(id: UID, program: Option[ProgramContract]): Props =
      profSettings.devActorProps(id, program, execScope, server)
        .getOrElse(DeviceActor.props(id, program, execScope, server))

    override def addNeighbor(id: UID, idn: UID): Unit = {
      server ! MsgNeighbor(id, idn)
    }

    override def start(): Unit = execScope match {
      case Global(_) => server ! MsgStart
      case SubsystemDelegated(_) => startScheduling
      case DeviceDelegated(_) => appRef ! MsgPropagate(GoOn)
    }

    def startScheduling: Unit = scheduler.foreach(_ ! GoOn)
  }

  class PlatformFacade(val actorSys: ActorSystem)
    extends AbstractPlatformFacade with Serializable {
    import akka.pattern.ask

    import scala.concurrent.ExecutionContext.Implicits.global
    implicit val timeout: akka.util.Timeout = 2.seconds
    val logger = actorSys.log

    override def newAggregateApplication(appSettings: AggregateApplicationSettings,
                                         profileSettings: ProfileSettings,
                                         execScope: ExecScope): SystemFacade = {
      val appRef = actorSys.actorOf(AggregateApplicationActor.props(appSettings), appSettings.name)

      var schedulerOpt: Option[ActorRef] = None
      execScope match {
        case Global(strategy) if profileSettings.startServer => {
          // Start global scheduler
          val sched = actorSys.actorOf(AutonomousScheduler.props(strategy), "scheduler")
          schedulerOpt = Some(sched)
        }
        case SubsystemDelegated(strategy) => {
          // Start global scheduler
          val sched = actorSys.actorOf(AutonomousScheduler.props(strategy), "scheduler")
          schedulerOpt = Some(sched)
        }
        case _ => // Do nothing
      }

      var sa: ActorRef = null // server actor
      if(profileSettings.startServer) {
        sa = startServer(appRef, profileSettings.serverActorProps(schedulerOpt))
      } else {
        sa = lookupServer(appRef, profileSettings)
      }

      if(profileSettings.serverGui) {
        profileSettings.serverGuiActorProps(sa).foreach(actorSys.actorOf(_))
      }

      new BasicSystemFacade(actorSys, appRef, appSettings, profileSettings, execScope, sa, schedulerOpt)
    }

    private def startServer(appRef: ActorRef, serverProps: Props): ActorRef = {
      var sa: ActorRef = null
      val thisTime = System.currentTimeMillis()
      val serverCreation = (appRef ? MsgCreateActor(serverProps, Some(PlatformFacade.ServerActorName), Some(thisTime))).andThen {
        case Success(MsgCreationAck(ref, _, Some(thisTime))) => {
          logger.info(s"\n*** Successfully created TM for current app: ${ref}\n")
          sa = ref
        }
        case e => { logger.error(s"\n${e}"); System.exit(0) }
      }
      Await.ready(serverCreation, Duration.Inf)
      sa
    }

    private def lookupServer(appRef: ActorRef, p: ProfileSettings): ActorRef = {
      val (serverHost,serverPort) = (p.serverHost, p.serverPort)

      var sa: ActorRef = null
      val selection = s"akka.tcp://${actorSys.name}@$serverHost:$serverPort/user/${appRef.path.name}/${PlatformFacade.ServerActorName}"

      logger.info(s"\n*** Trying SERVER lookup at ${selection}\n")

      val serverLookup = actorSys.actorSelection(selection).resolveOne.andThen {
        case Success(ref) => {
          logger.info(s"\n*** Successfully resolved SERVER @ $ref\n")
          sa = ref
        }
        case e => { logger.error(s"\n${e}"); System.exit(0) }
      }
      Await.ready(serverLookup, Duration.Inf)
      sa
    }
  }

  object PlatformFacade {
    val ServerActorName = "server"
  }

  object PlatformConfigurator extends ActorPlatformConfigurator with Serializable {
    override def buildPlatformFacade(sys: ActorSystem, s: PlatformSettings, p: ProfileSettings): PlatformFacade =
      new PlatformFacade(sys)
  }
}
