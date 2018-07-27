package it.unibo.scafi.simulation.gui.incarnation.scafi

import it.unibo.scafi.simulation.gui.configuration.SensorName.{sensor1, sensor2, sensor3}
import it.unibo.scafi.simulation.gui.configuration.command.CommandMapping
import it.unibo.scafi.simulation.gui.controller.input.Command
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.ScafiBridge
import it.unibo.scafi.simulation.gui.incarnation.scafi.world.ScafiLikeWorld.scafiWorldCommandSpace
import it.unibo.scafi.simulation.gui.model.space.Point3D
import it.unibo.scafi.simulation.gui.view.AbstractKeyboardManager._
import it.unibo.scafi.simulation.gui.view.{AbstractKeyboardManager, AbstractSelectionArea}

object ScafiCommandMapping {

  /**
    * base mapping used internal to not repeat the same mapping
    */
  private object baseMapping extends CommandMapping {

    override def map(keyboard: AbstractKeyboardManager, selection: Option[AbstractSelectionArea]): Unit = {
      keyboard.addCommand(Code4, () => ScafiBridge.scafiSimulationCommandSpace.StopSimulation)
      keyboard.addCommand(Code5, () => ScafiBridge.scafiSimulationCommandSpace.ContinueSimulation)
      selection.foreach {
        x => {
          x.addMovementAction((ids : Map[Any,Point3D]) => scafiWorldCommandSpace.MoveCommand(ids))
        }
      }
    }
  }

  /**
    * stardard mapping to scafi application
    */
  object standardMapping extends CommandMapping {

    override def map(keyboard: AbstractKeyboardManager, selection: Option[AbstractSelectionArea]): Unit = {
      baseMapping.map(keyboard,selection)
      keyboard.addCommand(Code1, (ids : Set[Any]) => scafiWorldCommandSpace.ToggleDeviceCommand(ids,sensor1))
      keyboard.addCommand(Code2, (ids : Set[Any]) => scafiWorldCommandSpace.ToggleDeviceCommand(ids,sensor2))
      keyboard.addCommand(Code3, (ids : Set[Any]) => scafiWorldCommandSpace.ToggleDeviceCommand(ids,sensor3))

    }
  }

  /**
    * ad hoc mapping used to ad hoc application
    * @param mapping the mapping
    */
  case class AdHocMapping(mapping : Map[AbstractKeyCode, Set[Any] => Command]) extends CommandMapping {

    override def map(keyboard: AbstractKeyboardManager, selection: Option[AbstractSelectionArea]): Unit = {
      baseMapping.map(keyboard,selection)
      mapping foreach {x => keyboard.addCommand(x._1,x._2)}
    }
  }
}
