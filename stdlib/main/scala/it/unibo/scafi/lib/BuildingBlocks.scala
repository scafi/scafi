package it.unibo.scafi.lib

/**
  * @author Roberto Casadei
  *
  */
trait BuildingBlocks {
  self: StandardLibrary.Subcomponent =>

  trait BuildingBlocks extends BlockG with BlockC with BlockS with BlockT with BlocksWithGC {
    self: AggregateProgram with StandardSensors =>
  }

}