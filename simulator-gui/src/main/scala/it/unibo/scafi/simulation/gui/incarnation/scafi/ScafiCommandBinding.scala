package it.unibo.scafi.simulation.gui.incarnation.scafi

import it.unibo.scafi.simulation.gui.configuration.SensorName
import it.unibo.scafi.simulation.gui.configuration.command.CommandBinding
import it.unibo.scafi.simulation.gui.configuration.command.factory.AbstractMoveCommandFactory.MultiMoveCommandFactory
import it.unibo.scafi.simulation.gui.configuration.command.factory.AbstractToggleCommandFactory.MultiToggleCommandFactory
import it.unibo.scafi.simulation.gui.configuration.command.factory.{AbstractToggleCommandFactory, SimulationCommandFactory}
import it.unibo.scafi.simulation.gui.controller.input.InputCommandController
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.scafiSimulationExecutor
import it.unibo.scafi.simulation.gui.incarnation.scafi.world.scafiWorld
import it.unibo.scafi.simulation.gui.view.AbstractKeyboardManager._
import it.unibo.scafi.simulation.gui.view.{AbstractKeyboardManager, AbstractSelectionArea}

/**
  * describe a set of command mapping
  */
object ScafiCommandBinding {
  import it.unibo.scafi.simulation.gui.incarnation.scafi.world.ScafiLikeWorld.analyzer
  private val moveFactory = new MultiMoveCommandFactory(scafiWorld)
  private val toggleFactory = new MultiToggleCommandFactory(scafiWorld)
  private val simulationFactory = new SimulationCommandFactory(scafiSimulationExecutor)
  /**
    * base mapping
    */

  object BaseBinding extends CommandBinding {
    override def apply(keyboard: AbstractKeyboardManager, selection: Option[AbstractSelectionArea]): Unit = {
      keyboard.addCommand(Code4, Map(SimulationCommandFactory.Action -> SimulationCommandFactory.Stop), simulationFactory)
      keyboard.addCommand(Code5, Map(SimulationCommandFactory.Action -> SimulationCommandFactory.Continue), simulationFactory)
      keyboard.addCommand(Code6, Map(SimulationCommandFactory.Action -> SimulationCommandFactory.Restart),simulationFactory)
      keyboard.addCommand(Plus, Map(SimulationCommandFactory.Action -> SimulationCommandFactory.Fast),simulationFactory)
      keyboard.addCommand(Minus, Map(SimulationCommandFactory.Action -> SimulationCommandFactory.Slow),simulationFactory)
      keyboard.addCommand(Undo, Map(), InputCommandController.UndoCommandFactory)
      selection match {
        case Some(selectionArea) => selectionArea.addMovementFactory(moveFactory,"map")
        case _ =>
      }
    }
  }

  /**
    * stardard mapping to scafi application
    */
  object StandardBinding extends CommandBinding {
    override def apply(keyboard: AbstractKeyboardManager, selection: Option[AbstractSelectionArea]): Unit = {
      BaseBinding(keyboard,selection)
      keyboard.addCommand(Code1, Map(AbstractToggleCommandFactory.Name-> SensorName.sensor1),toggleFactory,AbstractToggleCommandFactory.Ids)
      keyboard.addCommand(Code2, Map(AbstractToggleCommandFactory.Name -> SensorName.sensor2),toggleFactory,AbstractToggleCommandFactory.Ids)
      keyboard.addCommand(Code3, Map(AbstractToggleCommandFactory.Name -> SensorName.sensor3),toggleFactory,AbstractToggleCommandFactory.Ids)
    }
  }

  /**
    * ad hoc mapping used to ad hoc application
    *
    * @param mapping the mapping
    */
  case class AdHocToggleBinding(mapping : Map[AbstractKeyCode, Any]) extends CommandBinding {

    override def apply(keyboard: AbstractKeyboardManager, selection: Option[AbstractSelectionArea]): Unit = {
      BaseBinding(keyboard,selection)
      mapping foreach {x => keyboard.addCommand(x._1, Map(AbstractToggleCommandFactory.Name -> x._2),toggleFactory,AbstractToggleCommandFactory.Ids)}
    }
  }
}
