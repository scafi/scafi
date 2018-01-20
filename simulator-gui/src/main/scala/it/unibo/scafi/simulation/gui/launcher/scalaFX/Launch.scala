package it.unibo.scafi.simulation.gui.launcher.scalaFX

import javafx.embed.swing.JFXPanel

import it.unibo.scafi.simulation.gui.model.core.World
import it.unibo.scafi.simulation.gui.view.scalaFX.SimulationWindow
import it.unibo.scafi.simulation.gui.view.{GraphicsOutput, SimulationOutput}

import scalafx.application.Platform
import scalafx.scene.Group
import scalafx.scene.control.Button
import scalafx.scene.layout.VBox

object Launch extends App {
  new JFXPanel()

  Platform.runLater {

    val dialogStage = new SimulationWindow(new VBox(){
      children = Seq(new Button("PROVA"), new Button("ANCORA"))
    }, new Group with SimulationOutput with GraphicsOutput{
      override def out[N <: World#Node](node: Set[N]): Unit = ???

      override def remove[N <: World#Node](node: Set[N]): Unit = ???

      override def outNeighbour[N <: World#Node](node: N, neighbour: Set[N]): Unit = ???
    })
    // Show dialog and wait till it is closed
    dialogStage.show

  }

}
