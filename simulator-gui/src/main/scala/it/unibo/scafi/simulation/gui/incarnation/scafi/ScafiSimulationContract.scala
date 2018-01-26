package it.unibo.scafi.simulation.gui.incarnation.scafi

import it.unibo.scafi.incarnations.{BasicAbstractSpatialSimulationIncarnation => ExternSimulation}
import it.unibo.scafi.simulation.gui.controller.logical.SimulationContract
import it.unibo.scafi.simulation.gui.incarnation.scafi.ScafiWorldIncarnation._
//SCAFI CONTRACT DON'T SUPPORT REMOVING
class ScafiSimulationContract[W <: ScafiLikeWorld, PROTO <: ScafiPrototype]
  extends SimulationContract[SpaceAwareSimulator,W,PROTO] {
  private var currentSimulation : Option[SpaceAwareSimulator] = None
  override def getSimulation: Option[SpaceAwareSimulator] = this.currentSimulation

  override def initialize(world: W, prototype: PROTO): Unit = {
    require(currentSimulation.isEmpty)
    this.currentSimulation = Some(createSimulation(world,prototype))
  }
  /**
    * restart the external simulation
    *
    * @param world the internal representation of the world
    */
  override def restart(world: W, prototype: PROTO): Unit = {
    require(currentSimulation.isDefined)
    this.currentSimulation = Some(createSimulation(world,prototype))
  }

  private def createSimulation(w : W, p : PROTO) : SpaceAwareSimulator = {

    import it.unibo.scafi.space.{Point3D => ExternalPoint}

    val nodes: Map[ID, P] = w.nodes map {n => n.id -> new P(n.position.x,n.position.y,n.position.z)} toMap
    val res : SpaceAwareSimulator = new SpaceAwareSimulator(simulationSeed = p.randomSeed,randomSensorSeed = p.randomDeviceSeed,
      space = new Basic3DSpace(nodes,
      proximityThreshold = p.radius),
      devs = nodes.map { case (d, p) => d -> new DevInfo(d, p,
        lsns => if (lsns == "sensor") 1 else 0,
        nsns => nbr => null)
      })
    nodes map {x => w(x._1).get} foreach {x => x.devices.foreach(y => res.chgSensorValue(y.name,Set(x.id),y.value))}
    w.nodes  foreach { x =>
      x.devices foreach {y => res.chgSensorValue(y.name,Set(x.id),y.value)}
    }
    res
  }
}
trait ScafiPrototype {
  def randomSeed : Long
  def randomDeviceSeed : Long
  def radius : Double
}
