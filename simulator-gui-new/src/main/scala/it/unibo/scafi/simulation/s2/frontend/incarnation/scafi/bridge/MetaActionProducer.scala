package it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge

import it.unibo.scafi.simulation.MetaActionManager
import it.unibo.scafi.simulation.MetaActionManager.MetaAction
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.ScafiWorldIncarnation.EXPORT
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.ScafiWorldIncarnation._
import it.unibo.scafi.space.Point2D
import it.unibo.scafi.space.Point3D
/**
 * describe a meta action producer used to create a meta action by some value
 * @tparam O
 *   the type of value used to create meta action
 */
trait MetaActionProducer[O] {
  /**
   * convert any val into output value
   * @return
   *   none if the actuator can convert a specific value some of option otherwise
   */
  def valueParser: (Any) => (Option[O])

  /**
   * put the action used to parse any val
   * @param function
   *   the function used to parse any vale
   */
  def valueParser_=(function: (Any) => (Option[O])): Unit

  def apply(id: ID, export: EXPORT): MetaAction = if (valueParser(export.root()).isDefined) {
    this.apply(id, valueParser(export.root()).get)
  } else {
    MetaActionManager.EmptyAction
  }

  def apply(id: ID, argument: O): MetaAction
}
/**
 * describe action to actuate to the world, by export produced
 */
object MetaActionProducer {
  implicit private def bridgeToSimulator(bridge: ScafiBridge): SpaceAwareSimulator = bridge.contract.simulation.get
  /**
   * a meta action producer that create an action used to move node by a delta movement
   */
  val movementDtActionProducer: MetaActionProducer[(Double, Double)] = new MetaActionProducer[(Double, Double)] {
    private var action: (Any) => (Option[(Double, Double)]) = {
      case (x: Double, y: Double) => Some((x, y))
      case _ => None
    }
    override def valueParser_=(action: Any => Option[(Double, Double)]): Unit = this.action = action
    override def valueParser: Any => Option[(Double, Double)] = action

    override def apply(id: Int, dt: (Double, Double)): MetaAction = if (dt != (0.0, 0.0)) {
      ScafiSimulationExecutor.NodeDtMovement(id, dt)
    } else {
      MetaActionManager.EmptyAction
    }

    override def toString: String = "dt-meta-action"
  }
}
