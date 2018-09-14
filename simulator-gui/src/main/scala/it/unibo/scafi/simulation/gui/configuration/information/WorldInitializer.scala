package it.unibo.scafi.simulation.gui.configuration.information

/**
  * a strategy interface used to initialize a world with world information.
  * the difference with world information is that the initializer has the target
  * to initialize the world with some seed passed, the world information describe a
  * part of world that is necessary to initialize it
  *
  * @tparam I the information about world
  */
trait WorldInitializer[I <: WorldInformation[_,_]] {
  /**
    * initialize the world with the seed passed
    * @param worldInfo the world seed
    */
  def init(worldInfo : I)
}
