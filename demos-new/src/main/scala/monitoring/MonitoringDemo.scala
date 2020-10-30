/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package monitoring

import it.unibo.scafi.distrib.actor.p2p.SpatialPlatform
import it.unibo.scafi.incarnations.BasicAbstractActorIncarnation
import it.unibo.scafi.lib.StandardLibrary
import it.unibo.scafi.simulation.s2.frontend.configuration.SensorName
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.monitoring.MonitoringInitializer.RadiusSimulation
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.SimulationInfo
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.configuration.{ScafiProgramBuilder, ScafiWorldInformation}
import it.unibo.scafi.simulation.s2.frontend.view.scalaFX.drawer.StandardFXOutput
import it.unibo.scafi.space.Point2D
import it.unibo.scafi.space.graphics2D.BasicShape2D.Circle
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.world.ScafiWorldInitializer._

object MonitoringDemoPlatform extends SpatialPlatform with BasicAbstractActorIncarnation with StandardLibrary {
  override val LocationSensorName: LSensorName = "LocationSensor"
  override implicit val idBounded: MonitoringDemoPlatform.Builtins.Bounded[Int] = Builtins.Bounded.of_i
}
import monitoring.{MonitoringDemoPlatform => Platform}

case class Node(host: String, port: Int, devices: Map[Platform.ID, (Point2D, Set[Platform.ID])])

object MonitoringDemo_Inputs {
  val nodes: List[Node] = List(
    Node("127.0.0.1", 9000, Map(
      1 -> (Point2D(250, 120), Set(2)),
      2 -> (Point2D(260, 180), Set(3, 4)),
      3 -> (Point2D(290, 260), Set(2, 5, 6)),
      4 -> (Point2D(350, 160), Set(2, 6, 7)),
      5 -> (Point2D(340, 330), Set(3, 8, 9))
    )),
    Node("127.0.0.1", 9100, Map(
      6 -> (Point2D(375, 200), Set(7, 9, 10)),
      7 -> (Point2D(440, 170), Set(10)),
      8 -> (Point2D(420, 370), Set(9)),
      9 -> (Point2D(420, 280), Set(10)),
      10 -> (Point2D(470, 230), Set[Platform.ID]())
    ))
  )
  val platformName: String = "MonitoringDemo"

  trait MonitoringSensorDefinitions extends Platform.StandardSensors { self: Platform.AggregateProgram =>
    def sense1: Boolean = sense[Boolean](SensorName.sensor1)
    def sense2: Boolean = sense[Boolean](SensorName.sensor2)
    def sense3: Boolean = sense[Boolean](SensorName.sensor3)
  }

  class MonitoringDemoProgram extends Platform.AggregateProgram with Platform.BlockG with MonitoringSensorDefinitions {
    def channel1(source: Boolean, target: Boolean, width: Double): Boolean =
      distanceTo(source) + distanceTo(target) <= distanceBetween(source, target) + width

    override def main(): Boolean = branch(sense3){false}{channel1(sense1, sense2, 1)}
  }

  val aggregateAppSettings = Platform.AggregateApplicationSettings(
    name = platformName,
    program = () => Some(new MonitoringDemoProgram())
  )

  val deploymentSubsys1 = Platform.DeploymentSettings(nodes(0).host, nodes(0).port)
  val deploymentSubsys2 = Platform.DeploymentSettings(nodes(1).host, nodes(1).port)

  val settings1: Platform.Settings = Platform.settingsFactory.defaultSettings().copy(
    aggregate = aggregateAppSettings,
    platform = Platform.PlatformSettings(
      subsystemDeployment = deploymentSubsys1,
      otherSubsystems = Set(Platform.SubsystemSettings(
        subsystemDeployment = deploymentSubsys2,
        ids = nodes(1).devices.keySet
      ))
    ),
    deviceConfig = Platform.DeviceConfigurationSettings(
      ids = nodes(0).devices.keySet,
      nbs = nodes(0).devices.map(d => d._1 -> d._2._2))
  )
  val settings2: Platform.Settings = settings1.copy(
    platform = Platform.PlatformSettings(
      subsystemDeployment = deploymentSubsys2
    ),
    deviceConfig = Platform.DeviceConfigurationSettings(
      ids = nodes(1).devices.keySet,
      nbs = nodes(1).devices.map(d => d._1 -> d._2._2)
    )
  )

  class MonitoringDemoMain(override val settings: Platform.Settings) extends Platform.BasicMain(settings)
   with Platform.StandardSensorNames {
    override def onDeviceStarted(dm: Platform.DeviceManager, sys: Platform.SystemFacade): Unit = {
      dm.addSensorValue(SensorName.sensor1, false)
      dm.addSensorValue(SensorName.sensor2, false)
      dm.addSensorValue(SensorName.sensor3, false)
      val devPosition = nodes.filter(n => n.devices.contains(dm.selfId)).head.devices(dm.selfId)._1
      dm.addSensorValue(Platform.LocationSensorName, devPosition)
      dm.actorRef ! Platform.MsgNbrSensorValue(NBR_RANGE,
        nodes.flatMap(n => n.devices).map(n => n._1 -> devPosition.distance(n._2._1)).toMap)
      dm.start
    }
  }
}

import monitoring.MonitoringDemo_Inputs._
object MonitoringDemoMain1 extends MonitoringDemoMain(settings1)
object MonitoringDemoMain2 extends MonitoringDemoMain(settings2)

object MonitoringDemoMonitor extends App {
  val initializer = RadiusSimulation(
    radius = 100,
    platformName = platformName,
    platformNodes = nodes.flatMap(n => n.devices.map(_._1 -> (n.host, n.port))).toMap,
    platform = Platform)
  ScafiProgramBuilder(
    worldInitializer = Fixed(nodes.flatMap(n => n.devices.map(d => d._1 -> d._2._1)).toSet),
    scafiSimulationInfo = SimulationInfo(program = classOf[MonitoringDemoProgram]),
    simulationInitializer = initializer,
    neighbourRender = true,
    outputPolicy = StandardFXOutput,
    scafiWorldInfo = ScafiWorldInformation(shape = Some(Circle(5)))
  ).launch()
}
