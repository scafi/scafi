package it.unibo.scafi.simulation.s2.frontend.test.help

import it.unibo.scafi.simulation.s2.frontend.model.common.BoundaryDefinition
import it.unibo.scafi.simulation.s2.frontend.model.simulation.PlatformDefinition.SensorPlatform
import it.unibo.scafi.simulation.s2.frontend.model.simulation.implementation.StandardNetwork
import it.unibo.scafi.simulation.s2.frontend.model.simulation.implementation.mutable.{SensorDefinition, StandardNodeDefinition}

object platform extends StandardWorldDefinition with SensorPlatform
                with StandardNodeDefinition
                with SensorDefinition
                with BoundaryDefinition
                with StandardNetwork{

}
