package it.unibo.scafi.simulation.gui.incarnation.scafi.bridge

import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.ScafiBridge.rand


/**
  * describe a simulation skeleton of scafi framework
  */
trait ScafiSimulation {
  /**
    * class of program
    * @return the instance of this class
    */
  def program : Class[_]

  /**
    * the action of simulation
    * @return the instance of action
    */
  def action : Actions.ACTION

  /**
    * create the simulation
    * @return
    */
  def create : ScafiBridge
}

object ScafiSimulation {
  import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.ScafiWorldIncarnation._

  /**
    * standard scafi simulation
    * @param program the program to execute
    * @param action the action on output
    * @param radius the radius of neighbour
    */
  case class RadiusSimulation(val program : Class[_], val action : Actions.ACTION = Actions.generalAction, val radius : Double = 0.0) extends ScafiSimulation {

    override def create: ScafiBridge = {
      val bridge = new ScafiSimulationObserver
      val proto = () => {
        val w = bridge.world
        val nodes: Map[ID, P] = w.nodes map {n => n.id -> new P(n.position.x,n.position.y,n.position.z)} toMap
        val createdSpace  = new QuadTreeSpace(nodes,radius)
        val createdDevs =  nodes.map { case (d, p) => d -> new DevInfo(d, p,
          lsns => if (lsns == "sensor") 1 else 0,
          nsns => nbr => null)
        }
        val res : SpaceAwareSimulator = new SpaceAwareSimulator(simulationSeed = rand.nextInt(),randomSensorSeed = rand.nextInt(),
          space = createdSpace,
          devs = createdDevs)
        nodes map {x => w(x._1).get} foreach {x => x.devices.foreach(y => res.chgSensorValue(y.name,Set(x.id),y.value))}
        w.nodes  foreach { x =>
          x.devices foreach {y => res.chgSensorValue(y.name,Set(x.id),y.value)}
        }
        res
      }
      bridge.simulationPrototype = Some(proto)
      bridge.setProgramm(program)
      bridge.setAction(action)
      bridge
    }
  }
}
