package it.unibo.scafi.simulation.gui.configuration.command.factory

/**
  * a strategy used to check the correctness of type at runtime
  */
trait WorldTypeAnalyzer {
  /**
    * verify if the id is correct
    * @param id the current id
    * @return true if is is correct false otherwise
    */
  def acceptId(id : Any) : Boolean

  /**
    * verify if the name is correct
    * @param name the name
    * @return true if is correct false otherwise
    */
  def acceptName(name : Any) : Boolean
}