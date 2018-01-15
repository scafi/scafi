package it.unibo.scafi.simulation.gui.model.aggregate

import it.unibo.scafi.simulation.gui.model.core.Device

/***
  * an immutable device,
  * there are some method used to
  * alter the state(activation of device)
  */
trait AggregateDevice extends Device {
  //FACTORY METHOD
  protected def createDevice(state: Boolean) : AggregateDevice

  /**
    * create a new on device
    * @return the new device
    */
  def switchOn : AggregateDevice = createDevice(true)

  /**
    * create a new off device
    * @return the new device
    */
  def switchOff : AggregateDevice = createDevice(false)
}

