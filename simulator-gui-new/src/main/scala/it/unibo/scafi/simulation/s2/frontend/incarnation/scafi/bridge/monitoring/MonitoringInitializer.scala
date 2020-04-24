/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.monitoring

import it.unibo.scafi.incarnations.{ BasicAbstractActorIncarnation => Platform }
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.{ScafiBridge, ScafiSimulationInitializer, SimulationInfo}
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.ScafiWorldIncarnation._
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.world.scafiWorld
import it.unibo.scafi.simulation.s2.frontend.model.sensor.SensorConcept
import MonitoringExecutor.world

object MonitoringInitializer {
  case class RadiusSimulation(radius: Double = 0.0,
                              platformName: String = "untitled",
                              platformNodes: Map[ID, (String, Int)],
                              platform: Platform) extends ScafiSimulationInitializer {
    override def create(scafiSimulationSeed : SimulationInfo): ScafiBridge = {
      val bridge = MonitoringExecutor
      val proto = () => {
        val w = bridge.world
        val nodes: Map[ID, P] = w.nodes.map { n => n.id -> new P(n.position.x, n.position.y, n.position.z) }.toMap
        val createdSpace = new QuadTreeSpace(nodes, radius, scafiWorld.boundary)
        val createdDevs = nodes.map { case (d, p) => d -> new DevInfo(d, p, nsns = _ => _) }

        val res: PlatformMonitor = new PlatformMonitor(createdSpace, createdDevs, platformName, platformNodes, platform)
        w.nodes foreach { x =>
          x.devices filter { device =>
            device.stream == SensorConcept.sensorInput
          } foreach { y =>
            res.chgSensorValue(y.name, Set(x.id), y.value)
          }
        }
        res.getAllNeighbours().foreach { x => world.network.setNeighbours(x._1, x._2.toSet)}
        res
      }
      bridge.simulationPrototype = Some(proto)
      bridge.simulationInfo = scafiSimulationSeed
      bridge
    }
  }
}
