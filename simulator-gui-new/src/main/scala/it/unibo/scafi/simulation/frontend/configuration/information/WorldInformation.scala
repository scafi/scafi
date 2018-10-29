package it.unibo.scafi.simulation.frontend.configuration.information

import it.unibo.scafi.simulation.frontend.model.aggregate.AggregateConcept
import it.unibo.scafi.simulation.frontend.model.core.World
import it.unibo.scafi.space.Shape
import it.unibo.scafi.space.SpatialAbstraction.Bound

/**
  * world information used to initialize a world
  * @tparam D the type of device producer
  * @tparam S the shape type
  */
trait WorldInformation[D <: AggregateConcept#DeviceProducer,
                S <: Shape] {
  /**
    * @return the node shape
    */
  def shape : Option[S]

  /**
    * @return world boundary
    */
  def boundary : Option[Bound]

  /**
    * @return device producers that describe device associated with node
    */
  def deviceProducers : Iterable[D]
}
