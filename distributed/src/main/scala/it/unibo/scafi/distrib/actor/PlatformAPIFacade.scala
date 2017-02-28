package it.unibo.scafi.distrib.actor

import it.unibo.scafi.distrib.CustomClassLoader

import akka.actor.{Props, ActorRef, ActorSystem}
import com.typesafe.config.ConfigFactory
import it.unibo.scafi.distrib.actor.extensions.CodeMobilityExtension

import scala.concurrent.duration._
import scala.concurrent.{Await}
import scala.util.Success

/**
 * @author Roberto Casadei
 *
 */

trait PlatformAPIFacade { self: Platform.Subcomponent =>

  type DeviceManager = BasicDeviceManager

  /**
   * Represents the façade towards the logical (sub)system.
   * Responsibilities
   *   - Creation of device
   * @param actorSys The ActorSystem instance for this subsystem
   * @param appRef The reference to the local top-level actor for this aggregate app
   * @param appSettings The settings for this aggregate application
   * @param profileSettings The settings for the chosen platform profile
   * @param execScope The setttings related to execution
   */
  abstract class AbstractActorSystemFacade(val actorSys: ActorSystem,
                                           val appRef: ActorRef,
                                           val appSettings: AggregateApplicationSettings,
                                           val profileSettings: ProfileSettings,
                                           val execScope: ExecScope)
    extends AbstractSystemFacade {
    /* Note: a refactoring may consist in removing 'deviceGui', 'deviceGuiProps',
     *  and 'deviceProps' method and fixing ProfileSettings at this level
     *  as a case class with an additional field 'subprofile' of a type
     *  SubprofileSettings which will be realized in p2p/server specializations.
     */
    val deviceGui: Boolean
    def deviceGuiProps(dev: ActorRef): Props
    def deviceProps(id: ID, program: Option[ExecutionTemplate]): Props

    var devices: Map[ID, DeviceManager] = Map()

    override def newDevice(id: ID, program: Option[ExecutionTemplate] = None, nbs: Set[ID] = Set()): DeviceManager = {
      import akka.pattern._
      import scala.concurrent.ExecutionContext.Implicits.global
      implicit val to: akka.util.Timeout = 10.seconds
      var dm: DeviceManager = null

      val dmf = (appRef ? MsgAddDevice(id, deviceProps(id, program.orElse(appSettings.program())))).andThen {
        case Success(MsgDeviceLocation(id,devRef)) => dm = new BasicDeviceManager(id, devRef, appRef)
        case x => println("\n\nError: "+x) // TODO: handle failure
      }
      Await.ready(dmf, Duration.Inf)

      // Add neighbors to device via its device manager
      nbs.foreach(nbrId => this.addNeighbor(id,nbrId))

      devices += id -> dm

      if(deviceGui){
        val devGuiActor = actorSys.actorOf(deviceGuiProps(dm.actorRef))
        dm.actorRef ! MsgAddObserver(devGuiActor)
      }

      dm
    }
  }

  /**
   * Represents the management façade for a single device.
   * Responsibilities
   *   - Sensor/actuator attachment
   * @param selfId The device's identifier
   * @param actorRef The device's actor reference
   * @param appRef The actor reference for this very aggregate application
   */
  class BasicDeviceManager(val selfId: ID,
                           val actorRef: ActorRef,
                           val appRef: ActorRef) extends AbstractDeviceManager
    with Serializable {
    def addPushSensor(sensorRef: ActorRef) = {
      actorRef ! MsgAddPushSensor(sensorRef)
    }

    def start = actorRef ! GoOn

    override def addSensorValue[T](name: LSNS, value: T): Unit = {
      actorRef ! MsgLocalSensorValue(name, value)
    }

    override def addSensor[T](name: LSNS, provider: () => T): Unit = {
      actorRef ! MsgAddSensor(name, provider)
    }

    override def addActuator(name: LSNS, consumer: Any => Unit): Unit = {
      actorRef ! MsgAddActuator(name, consumer)
    }

    def addNeighbor(nbr: NbrInfo) = {
      actorRef ! nbr
    }

    def addObserver(observer: ActorRef) = {
      actorRef ! MsgAddObserver(observer)
    }
  }

  /**
   * Objects of this class are responsible for the configuration of
   *  the actor-based platform.
   * The façade interface allows the creation of a platform façade.
   */
  trait ActorPlatformConfigurator extends PlatformConfigurator {
    def buildPlatformFacade(sys: ActorSystem,
                            s: PlatformSettings,
                            p: ProfileSettings): PlatformFacade

    override def setupPlatform(s: PlatformSettings,
                               p: ProfileSettings): PlatformFacade = {
      val d = s.subsystemDeployment
      val config = buildNodeConfiguration(s)
      val ctxClassloader = Thread.currentThread().getContextClassLoader
      val actorSys = ActorSystem("scafi", config, new CustomClassLoader(ctxClassloader))
      buildPlatformFacade(actorSys, s, p)
    }

    def buildNodeConfiguration(ps: PlatformSettings) = {
      val d = ps.subsystemDeployment

      val (host,port) = (d.host, d.port)
      var extensions = List[String]()
      if(ps.codeMobilitySupport)
        extensions ::= CodeMobilityExtension.getClass.getName

      ConfigFactory.parseString(
        """
          akka.remote.netty.tcp.hostname = %s
          akka.remote.netty.tcp.port = %s
          akka.loglevel = "%s"
          akka.extensions = [%s]
        """.stripMargin.format(
          host,
          port,
          ps.loglevel.toString.toUpperCase,
          extensions.map(s => s""""$s"""").mkString(","))
      ) withFallback(ConfigFactory.load("remote_application"))
    }
  }

}
