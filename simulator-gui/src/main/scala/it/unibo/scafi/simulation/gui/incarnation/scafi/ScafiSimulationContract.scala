package it.unibo.scafi.simulation.gui.incarnation.scafi

import it.unibo.scafi.incarnations.{BasicAbstractSpatialSimulationIncarnation => ExternSimulation}
import it.unibo.scafi.simulation.gui.controller.SimulationContract

class ScafiSimulationContract[W <: ScafiLikeWorld, PROTO <: ScafiPrototype]
  extends SimulationContract[ExternSimulation#SpaceAwareSimulator,W,PROTO] {
  private var currentSimulation : Option[ExternSimulation#SpaceAwareSimulator] = None
  override def getSimulation: Option[ExternSimulation#SpaceAwareSimulator] = this.currentSimulation

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

  private def createSimulation(w : W, p : PROTO) : ExternSimulation#SpaceAwareSimulator = {
    import it.unibo.scafi.space.{Point3D => ExternalPoint}
    val abstraction = new ExternSimulation {
      override type P = ExternalPoint
    }
    val nodes: Map[abstraction.ID, abstraction.P] = w.nodes map {n => n.id -> new abstraction.P(n.position.x,n.position.y,n.position.z)} toMap
    val res : ExternSimulation#SpaceAwareSimulator = new abstraction.SpaceAwareSimulator(simulationSeed = p.randomSeed,randomSensorSeed = p.randomDeviceSeed,
      space = new abstraction.Basic3DSpace(nodes,
      proximityThreshold = p.radius),
      devs = nodes.map { case (d, p) => d -> new abstraction.DevInfo(d, p,
        lsns => if (lsns == "sensor") 1 else 0,
        nsns => nbr => null)
      })
    res
  }
}
trait ScafiPrototype {
  def randomSeed : Long
  def randomDeviceSeed : Long
  def radius : Double
}
