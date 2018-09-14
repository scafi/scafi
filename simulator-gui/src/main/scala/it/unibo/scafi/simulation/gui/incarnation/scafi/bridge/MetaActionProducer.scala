package it.unibo.scafi.simulation.gui.incarnation.scafi.bridge

import it.unibo.scafi.simulation.MetaActionManager
import it.unibo.scafi.simulation.MetaActionManager.MetaAction
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.ScafiWorldIncarnation.{EXPORT, _}
import it.unibo.scafi.space.{Point2D, Point3D}
/**
  * describe a meta action producer used to create a meta action
  * by some value
  * @tparam A the type of value used to create meta action
  */
trait MetaActionProducer[A] {
  /**
    * convert any val in the output value
    * @return none if the actuator can convert a specific value some of option otherwise
    */
  def valueParser : (Any) => (Option[A])

  /**
    * put the action used to parse any val
    * @param function the function used to parse any vale
    */
  def valueParser_= (function : (Any) => (Option[A]))

  def apply(id : ID, export : EXPORT, net : SpaceAwareSimulator) : MetaAction = if(valueParser(export.root()).isDefined) {
    this.apply(id,valueParser(export.root()).get,net)
  } else {
    MetaActionManager.EmptyAction
  }

  def apply(id : ID, argument : A, net : SpaceAwareSimulator) : MetaAction
}
/**
  * describe action to actuate to the world, by export produced
  */
object MetaActionProducer {
  /**
    * a meta action producer that create an action used to move node by a delta movement
    */
  val movementDtActionProducer : MetaActionProducer[(Double,Double)] = new MetaActionProducer[(Double, Double)] {
    private var action : (Any) => (Option[(Double,Double)]) = v => Some(v.asInstanceOf[(Double,Double)])
    override def valueParser_=(function: Any => Option[(Double, Double)]): Unit = this.action = action
    override def valueParser: Any => Option[(Double, Double)] = action

    override def apply(id: Int, dt: (Double, Double), net: ScafiWorldIncarnation.SpaceAwareSimulator): MetaAction = if (dt != (0.0, 0.0)) {
      net.NodeDtMovement(id,dt)
    } else {
      MetaActionManager.EmptyAction
    }
  }
}