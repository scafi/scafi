package it.unibo.scafi.simulation.gui.configuration.seed

/**
  * a strategy interface used to initialize a world with a world seed.
  * the difference with world seed is that the initializer has the target
  * to initialize the world with some seed passed, the world seed describe a
  * part of world that is necessary to initialize it
  *
  * @tparam S the seed of world
  */
trait WorldInitializer[S <: WorldSeed[_,_,_]] {
  /**
    * initialize the world with the seed passed
    * @param seed the world seed
    */
  def init(seed : S)
}
