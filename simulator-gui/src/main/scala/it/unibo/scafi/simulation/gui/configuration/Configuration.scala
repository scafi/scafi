package it.unibo.scafi.simulation.gui.configuration

/**
  * a configuratio places where link the seed to initialize a program
  * @tparam S the seed type
  */
trait Configuration [S <: WorldSeed[_,_,_], I <: WorldInitializer[S]]{
  /**
    * @return a world initializer
    */
  def worldInitializer : Option[I]

  /**
    * @return a world seed
    */
  def worldSeed : Option[S]

  /**
    * @return true if the program is launched false otherwise
    */
  def launched : Boolean
}
