package it.unibo.scafi.simulation.s2.frontend.incarnation.scafi

import it.unibo.scafi.simulation.s2.frontend.configuration.SensorName
import it.unibo.scafi.simulation.s2.frontend.configuration.command.CommandBinding
import it.unibo.scafi.simulation.s2.frontend.configuration.command.factory.SimulationCommandFactory
import it.unibo.scafi.simulation.s2.frontend.controller.input.InputCommandController
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.ScafiSimulationExecutor
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.configuration.command.AbstractMoveCommandFactory.MultiMoveCommandFactory
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.configuration.command.AbstractMoveCommandFactory
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.configuration.command.AbstractToggleCommandFactory
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.configuration.command.AbstractToggleCommandFactory.MultiToggleCommandFactory
import it.unibo.scafi.simulation.s2.frontend.view.AbstractKeyboardManager._
import it.unibo.scafi.simulation.s2.frontend.view.AbstractKeyboardManager
import it.unibo.scafi.simulation.s2.frontend.view.AbstractSelectionArea

/**
 * describe a set of command mapping in scafi context
 */
object ScafiCommandBinding {
  private val moveFactory = new MultiMoveCommandFactory
  private val toggleFactory = new MultiToggleCommandFactory
  private val simulationFactory = new SimulationCommandFactory(ScafiSimulationExecutor)
  /**
   * base mapping, allow to modify the state of simulation (start, stop, reset simulation, increase or decrease
   * simulation velocity), use undo command and move the node with selection
   */
  object BaseBinding extends CommandBinding {
    override def apply(keyboard: AbstractKeyboardManager, selection: Option[AbstractSelectionArea]): Unit = {
      keyboard.linkCommandCreation(
        Code5,
        Map(SimulationCommandFactory.Action -> SimulationCommandFactory.Stop),
        simulationFactory
      )
      keyboard.linkCommandCreation(
        Code6,
        Map(SimulationCommandFactory.Action -> SimulationCommandFactory.Continue),
        simulationFactory
      )
      keyboard.linkCommandCreation(
        Code7,
        Map(SimulationCommandFactory.Action -> SimulationCommandFactory.Restart),
        simulationFactory
      )
      keyboard.linkCommandCreation(
        Plus,
        Map(SimulationCommandFactory.Action -> SimulationCommandFactory.Fast),
        simulationFactory
      )
      keyboard.linkCommandCreation(
        Minus,
        Map(SimulationCommandFactory.Action -> SimulationCommandFactory.Slow),
        simulationFactory
      )
      keyboard.linkCommandCreation(Undo, Map(), InputCommandController.UndoCommandFactory)
      selection match {
        case Some(selectionArea) => selectionArea.addMovementFactory(moveFactory, AbstractMoveCommandFactory.MoveMap)
        case _ =>
      }
    }
  }

  /**
   * standard mapping used in scafi aggregate command, allow to change the state of sensor with actuation (you can
   * change the state of led with select node and click a number). It use {@see
   * it.unibo.scafi.simulation.gui.incarnation.scafi.BaseBinding}
   */
  object StandardBinding extends CommandBinding {
    override def apply(keyboard: AbstractKeyboardManager, selection: Option[AbstractSelectionArea]): Unit = {
      BaseBinding(keyboard, selection)
      keyboard.linkCommandCreation(
        Code1,
        Map(AbstractToggleCommandFactory.Name -> SensorName.sensor1),
        toggleFactory,
        AbstractToggleCommandFactory.Ids
      )
      keyboard.linkCommandCreation(
        Code2,
        Map(AbstractToggleCommandFactory.Name -> SensorName.sensor2),
        toggleFactory,
        AbstractToggleCommandFactory.Ids
      )
      keyboard.linkCommandCreation(
        Code3,
        Map(AbstractToggleCommandFactory.Name -> SensorName.sensor3),
        toggleFactory,
        AbstractToggleCommandFactory.Ids
      )
      keyboard.linkCommandCreation(
        Code4,
        Map(AbstractToggleCommandFactory.Name -> SensorName.sensor4),
        toggleFactory,
        AbstractToggleCommandFactory.Ids
      )

    }
  }

  /**
   * ad hoc mapping used to ad hoc application, you can select the name of sensor that you want change at run time. It
   * use {@see it.unibo.scafi.simulation.gui.incarnation.scafi.BaseBinding}
   * @param mapping
   *   map keyboard code to sensor name
   */
  case class AdHocToggleBinding(mapping: Map[AbstractKeyCode, Any]) extends CommandBinding {

    override def apply(keyboard: AbstractKeyboardManager, selection: Option[AbstractSelectionArea]): Unit = {
      BaseBinding(keyboard, selection)
      mapping foreach { x =>
        keyboard.linkCommandCreation(
          x._1,
          Map(AbstractToggleCommandFactory.Name -> x._2),
          toggleFactory,
          AbstractToggleCommandFactory.Ids
        )
      }
    }
  }
}
