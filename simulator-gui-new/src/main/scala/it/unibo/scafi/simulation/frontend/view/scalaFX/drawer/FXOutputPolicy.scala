package it.unibo.scafi.simulation.frontend.view.scalaFX.drawer

import it.unibo.scafi.simulation.frontend.configuration.environment.ViewEnvironment
import it.unibo.scafi.simulation.frontend.view.scalaFX.ScalaFXEnvironment
import it.unibo.scafi.simulation.frontend.view.{OutputPolicy, SimulationView}

/**
  * standard fx output policy
  */
trait FXOutputPolicy extends OutputPolicy{
  type OUTPUT_NODE <: javafx.scene.Node

  override def getViewEnvAndAttach() : Option[ViewEnvironment[SimulationView]] = {
    ScalaFXEnvironment.drawer = this
    Some(ScalaFXEnvironment)
  }
}
