package it.unibo.scafi.simulation.gui.incarnation.scafi.bridge

import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.scafiSimulationObserver.{contract, world}

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
  def create(scafiSimulationSeed: ScafiSimulationInformation) : ScafiBridge
}

object ScafiSimulationInitializer {
  import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.ScafiWorldIncarnation._

  /**
    * standard scafi simulation
    * @param radius the radius of neighbour
    */
  case class RadiusSimulationInitializer(val radius : Double = 0.0) extends ScafiSimulationInitializer {

    override def create(scafiSimulationSeed : ScafiSimulationInformation): ScafiBridge = {
      val rand : Random = new Random()
      val bridge = scafiSimulationObserver
      val proto = () => {
        val w = bridge.world
        val nodes: Map[ID, P] = w.nodes map {n => n.id -> new P(n.position.x,n.position.y,n.position.z)} toMap
        val createdSpace  = new QuadTreeSpace(nodes,radius)
        val createdDevs =  nodes.map { case (d, p) => d -> new DevInfo(d, p,
          lsns => 0,
          nsns => nbr => null)
        }
        val res : SpaceAwareSimulator = new SpaceAwareSimulator(simulationSeed = rand.nextInt(),randomSensorSeed = rand.nextInt(),
          space = createdSpace,
          devs = createdDevs)
        w.nodes  foreach { x =>
          x.devices foreach {y => res.chgSensorValue(y.name,Set(x.id),y.value)}
        }
        res.getAllNeighbours().foreach { x => world.network.setNeighbours(x._1,x._2.toSet)}
        res
      }
      bridge.simulationPrototype = Some(proto)
      bridge.simulationSeed = scafiSimulationSeed
      bridge
    }
  }
}
