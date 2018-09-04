package it.unibo.scafi.simulation.gui.configuration.seed

import it.unibo.scafi.simulation.gui.model.aggregate.AggregateConcept
import it.unibo.scafi.simulation.gui.model.core.{Shape, World}

/**
  * world information used to initialize a world
  * @tparam D the type of device producer
  * @tparam B the type of boundary
  * @tparam S the shape type
  */
trait WorldInformation[D <: AggregateConcept#DeviceProducer,
                B <: World#Boundary,
                S <: Shape] {
  /**
    * @return the node shape
    */
  def shape : Option[S]

  /**
    * @return world boundary
    */
  def boundary : Option[B]

  /**
    * @return device producers that describe device associated with node
    */
  def deviceProducers : Iterable[D]
}
