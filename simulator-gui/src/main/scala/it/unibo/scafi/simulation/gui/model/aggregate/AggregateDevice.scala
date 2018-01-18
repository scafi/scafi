package it.unibo.scafi.simulation.gui.model.aggregate

import it.unibo.scafi.simulation.gui.model.core.Device

/***
  * an immutable device,
  * there are some method used to
  * alter the state(activation of device)
  */
trait AggregateDevice extends Device {
  private var parent : Option[NODE] = None
  protected def createDevice(state: Boolean,parent : Option[NODE] = this.node) : this.type

  /**
    * create a new on device
    * @return the new device
    */
  def switchOn : this.type = createDevice(true)

  /**
    * create a new off device
    * @return the new device
    */
  def switchOff : this.type = createDevice(false)

  /**
    * create a device with a node attached
    * @param n
    */
  def node_=(n : NODE) : this.type = {
    require(parent.isEmpty)
    createDevice(this.state,Some(n))
  }

  override def node: Option[NODE] = this.parent
}

