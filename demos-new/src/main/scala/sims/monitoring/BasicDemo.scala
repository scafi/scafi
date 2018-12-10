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

package sims.monitoring

import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.SimulationInfo
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.monitoring.MonitoringInitializer.RadiusSimulation
import it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.ScafiProgramBuilder
import it.unibo.scafi.simulation.gui.incarnation.scafi.world.ScafiWorldInitializer.Fixed
import it.unibo.scafi.simulation.gui.view.scalaFX.drawer.GradientFXOutput
import it.unibo.scafi.distrib.actor.p2p.{SpatialPlatform => SpatialP2PActorPlatform}
import it.unibo.scafi.incarnations.BasicAbstractActorIncarnation
import it.unibo.scafi.simulation.gui.configuration.SensorName
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.ScafiWorldIncarnation._
import it.unibo.scafi.space.Point2D

object DistributedBasicDemoPlatform extends SpatialP2PActorPlatform with BasicAbstractActorIncarnation {
  override val LocationSensorName: LSensorName = "LocationSensor"
}
import sims.monitoring.{ DistributedBasicDemoPlatform => Platform }

case class Node(host: String, port: Int, devices: Map[ID, (Point2D, Set[ID])])

object DistributedBasicDemo_Inputs {
  val nodes: List[Node] = List(
    Node("127.0.0.1", 9000, Map(1 -> (Point2D(50, 4), Set(2)))),
    Node("127.0.0.1", 9100, Map(2 -> (Point2D(50, 8), Set(3)))),
    Node("127.0.0.1", 9200, Map(3 -> (Point2D(50, 12), Set(4)))),
    Node("127.0.0.1", 9300, Map(4 -> (Point2D(50, 16), Set(5)))),
    Node("127.0.0.1", 9400, Map(5 -> (Point2D(50, 20), Set[ID]())))
  )
  val platformName: String = "DistributedBasicDemo"

  class DistributedBasicDemo_AggregateProgram extends Platform.AggregateProgram {
    override def main(): Int = foldhoodPlus(0){_ + _}(150)
  }

  val aggregateAppSettings = Platform.AggregateApplicationSettings(
    name = platformName,
    program = () => Some(new DistributedBasicDemo_AggregateProgram())
  )

  val deploymentSubsys1 = Platform.DeploymentSettings(nodes(0).host, nodes(0).port)
  val deploymentSubsys2 = Platform.DeploymentSettings(nodes(1).host, nodes(1).port)
  val deploymentSubsys3 = Platform.DeploymentSettings(nodes(2).host, nodes(2).port)
  val deploymentSubsys4 = Platform.DeploymentSettings(nodes(3).host, nodes(3).port)
  val deploymentSubsys5 = Platform.DeploymentSettings(nodes(4).host, nodes(4).port)

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
      subsystemDeployment = deploymentSubsys2,
      otherSubsystems = Set(Platform.SubsystemSettings(
        subsystemDeployment = deploymentSubsys3,
        ids = nodes(2).devices.keySet
      ))
    ),
    deviceConfig = Platform.DeviceConfigurationSettings(
      ids = nodes(1).devices.keySet,
      nbs = nodes(1).devices.map(d => d._1 -> d._2._2)
    )
  )
  val settings3: Platform.Settings = settings1.copy(
    platform = Platform.PlatformSettings(
      subsystemDeployment = deploymentSubsys3,
      otherSubsystems = Set(Platform.SubsystemSettings(
        subsystemDeployment = deploymentSubsys4,
        ids = nodes(3).devices.keySet
      ))
    ),
    deviceConfig = Platform.DeviceConfigurationSettings(
      ids = nodes(2).devices.keySet,
      nbs = nodes(2).devices.map(d => d._1 -> d._2._2))
  )
  val settings4: Platform.Settings = settings1.copy(
    platform = Platform.PlatformSettings(
      subsystemDeployment = deploymentSubsys4,
      otherSubsystems = Set(Platform.SubsystemSettings(
        subsystemDeployment = deploymentSubsys5,
        ids = nodes(4).devices.keySet
      ))
    ),
    deviceConfig = Platform.DeviceConfigurationSettings(
      ids = nodes(3).devices.keySet,
      nbs = nodes(3).devices.map(d => d._1 -> d._2._2))
  )
  val settings5: Platform.Settings = settings1.copy(
    platform = Platform.PlatformSettings(
      subsystemDeployment = deploymentSubsys5
    ),
    deviceConfig = Platform.DeviceConfigurationSettings(
      ids = nodes(4).devices.keySet,
      nbs = nodes(4).devices.map(d => d._1 -> d._2._2))
  )

  class DistributedBasicDemoMain(override val settings: Platform.Settings) extends Platform.BasicMain(settings) {
    override def onDeviceStarted(dm: Platform.DeviceManager, sys: Platform.SystemFacade): Unit = {
      dm.addSensorValue(SensorName.sensor1, false)
      dm.addSensorValue(Platform.LocationSensorName,
        nodes.filter(n => n.devices.contains(dm.selfId)).head.devices(dm.selfId)._1)
      dm.start
    }
  }
}

object DistributedBasicDemo_MainProgram_1 extends DistributedBasicDemo_Inputs.DistributedBasicDemoMain(DistributedBasicDemo_Inputs.settings1)
object DistributedBasicDemo_MainProgram_2 extends DistributedBasicDemo_Inputs.DistributedBasicDemoMain(DistributedBasicDemo_Inputs.settings2)
object DistributedBasicDemo_MainProgram_3 extends DistributedBasicDemo_Inputs.DistributedBasicDemoMain(DistributedBasicDemo_Inputs.settings3)
object DistributedBasicDemo_MainProgram_4 extends DistributedBasicDemo_Inputs.DistributedBasicDemoMain(DistributedBasicDemo_Inputs.settings4)
object DistributedBasicDemo_MainProgram_5 extends DistributedBasicDemo_Inputs.DistributedBasicDemoMain(DistributedBasicDemo_Inputs.settings5)


object BasicMonitorDemo extends App {
  val nodes = DistributedBasicDemo_Inputs.nodes
  val initializer = RadiusSimulation(
    radius = 5,
    platformName = DistributedBasicDemo_Inputs.platformName,
    platformNodes = DistributedBasicDemo_Inputs.nodes.flatMap(n => n.devices.map(_._1 -> (n.host, n.port))).toMap,
    platform = DistributedBasicDemoPlatform)
  ScafiProgramBuilder (
    worldInitializer = Fixed(nodes.flatMap(n => n.devices.map(d => d._1 -> d._2._1)).toSet),
    scafiSimulationInfo = SimulationInfo(program = classOf[DistributedBasicDemo_Inputs.DistributedBasicDemo_AggregateProgram]),
    simulationInitializer = initializer,
    outputPolicy = GradientFXOutput
  ).launch()
}
