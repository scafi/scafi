package lib

import it.unibo.scafi.incarnations.BasicSimulationIncarnation._

/**
  * @author Roberto Casadei
  *
  */
trait BuildingBlocks extends BlockG with BlockC with BlockS with BlockT with BlocksWithGC { self: AggregateProgram with SensorDefinitions => }
