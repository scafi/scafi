package it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge

import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.ScafiSimulationExecutor.world
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.world.scafiWorld
import it.unibo.scafi.simulation.s2.frontend.model.sensor.SensorConcept

import scala.util.Random

/**
 * describe a scafi simulation initializer, it is used to initialize the external simulation by a simulation information
 * the difference between scafiSimulationInformation is that this has the target to create a bridge with the seed passed
 * the seed instead has the target to describe a scafi simulation skeleton
 */
trait ScafiSimulationInitializer {

  /**
   * create the simulation
   * @return
   */
  def create(scafiSimulationSeed: SimulationInfo): ScafiBridge
}

object ScafiSimulationInitializer {
  import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.ScafiWorldIncarnation._

  /**
   * standard scafi simulation
   * @param radius
   *   the radius of neighbour
   */
  case class RadiusSimulation(radius: Double = 0.0) extends ScafiSimulationInitializer {

    override def create(scafiSimulationSeed: SimulationInfo): ScafiBridge = {
      val bridge = ScafiSimulationExecutor
      val proto = () => {
        val w = bridge.world
        val nodes: Map[ID, P] = w.nodes.map(n => n.id -> new P(n.position.x, n.position.y, n.position.z)).toMap
        val createdSpace = new QuadTreeSpace(nodes, radius, scafiWorld.boundary)
        val createdDevs = nodes.map { case (d, p) => d -> new DevInfo(d, p, nsns = nsns => nbr => null) }
        val res: SpaceAwareSimulator = new SpaceAwareSimulator(
          simulationSeed = System.nanoTime(),
          randomSensorSeed = System.nanoTime(),
          space = createdSpace,
          devs = createdDevs
        )
        w.nodes foreach { x =>
          x.devices filter { device => device.stream == SensorConcept.sensorInput } foreach { y =>
            res.chgSensorValue(y.name, Set(x.id), y.value)
          }
        }
        res.getAllNeighbours().foreach(x => world.network.setNeighbours(x._1, x._2.toSet))
        res
      }
      bridge.simulationPrototype = Some(proto)
      bridge.simulationInfo = scafiSimulationSeed
      bridge
    }
  }
}
