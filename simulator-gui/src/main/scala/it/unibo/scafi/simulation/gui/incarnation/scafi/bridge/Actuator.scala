package it.unibo.scafi.simulation.gui.incarnation.scafi.bridge

import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.ScafiWorldIncarnation.{EXPORT, _}
import it.unibo.scafi.space.{Point2D, Point3D}
/**
  * describe a scafi actuator used to change world state
  * by export produced
  * @tparam A the type of value read by actuator
  */
trait Actuator[A] {
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

  def apply(id : ID, export : EXPORT, net : SpaceAwareSimulator) : Unit = if(valueParser(export.root()).isDefined) this(id,valueParser(export.root()).get,net)
  def apply(id : ID, export : A, net : SpaceAwareSimulator) : Unit
}
/**
  * describe action to actuate to the world, by export produced
  */
object Actuator {
  /**
    * an actuator that move node by a delta movement
    */
  val movementDtActuator : Actuator[(Double,Double)] = new Actuator[(Double, Double)] {
    private var action : (Any) => (Option[(Double,Double)]) = v => Some(v.asInstanceOf[(Double,Double)])
    override def valueParser_=(function: Any => Option[(Double, Double)]): Unit = this.action = action
    override def valueParser: Any => Option[(Double, Double)] = action

    override def apply(id: Int, dt: (Double, Double), net: ScafiWorldIncarnation.SpaceAwareSimulator): Unit = {
      if(dt == (0.0,0.0)) return
      val oldPos = net.space.getLocation(id)
      val (dtx,dty) = dt
      val point = Point3D(oldPos.x + dtx,oldPos.y + dty,oldPos.z)
      net.setPosition(id,point)
    }
  }
  /**
    * an actuator that move node into position specified
    */
  val movementActuator : Actuator[(Double,Double)] = new Actuator[(Double, Double)] {
    private var action : (Any) => (Option[(Double,Double)]) = v => Some(v.asInstanceOf[(Double,Double)])
    override def valueParser: Any => Option[(Double, Double)] = action
    override def valueParser_=(function: Any => Option[(Double, Double)]): Unit = this.action = action

    override def apply(id: Int, position: (Double, Double), net: ScafiWorldIncarnation.SpaceAwareSimulator): Unit = {
      net.setPosition(id,Point2D(position._1,position._2))
    }
  }

}