package it.unibo.scafi.simulation.gui.incarnation.scafi.bridge


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
}
