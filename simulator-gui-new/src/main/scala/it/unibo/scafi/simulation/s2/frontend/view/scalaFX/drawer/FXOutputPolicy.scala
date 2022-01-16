package it.unibo.scafi.simulation.s2.frontend.view.scalaFX.drawer

import it.unibo.scafi.simulation.s2.frontend.configuration.environment.ViewEnvironment
import it.unibo.scafi.simulation.s2.frontend.view.scalaFX.ScalaFXEnvironment
import it.unibo.scafi.simulation.s2.frontend.view.OutputPolicy
import it.unibo.scafi.simulation.s2.frontend.view.SimulationView

/**
 * standard fx output policy
 */
trait FXOutputPolicy extends OutputPolicy {
  type OUTPUT_NODE <: javafx.scene.Node

  override def getViewEnvAndAttach(): Option[ViewEnvironment[SimulationView]] = {
    ScalaFXEnvironment.drawer = this
    Some(ScalaFXEnvironment)
  }
}
