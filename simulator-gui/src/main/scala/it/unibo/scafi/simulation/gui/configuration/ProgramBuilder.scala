package it.unibo.scafi.simulation.gui.configuration

/**
  * a program builder used to create a program launcher
  * @tparam C the type of configuration
  */
trait ProgramBuilder[C <: Configuration] {

  /**
    * the configuration used to create the program
    * @return the configuration
    */
  def configuration : C

  /**
    * try to create a program launcher
    * @return None if the configuration is not legit false otherwise
    */
  def create : Program[_,_]
}
