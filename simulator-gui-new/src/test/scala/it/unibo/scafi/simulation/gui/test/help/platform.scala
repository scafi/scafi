package it.unibo.scafi.simulation.gui.test.help

import it.unibo.scafi.simulation.gui.model.common.BoundaryDefinition
import it.unibo.scafi.simulation.gui.model.simulation.PlatformDefinition.SensorPlatform
import it.unibo.scafi.simulation.gui.model.simulation.implementation.StandardNetwork
import it.unibo.scafi.simulation.gui.model.simulation.implementation.mutable.{SensorDefinition, StandardNodeDefinition}

object platform extends StandardWorldDefinition with SensorPlatform
                with StandardNodeDefinition
                with SensorDefinition
                with BoundaryDefinition
                with StandardNetwork{

}
