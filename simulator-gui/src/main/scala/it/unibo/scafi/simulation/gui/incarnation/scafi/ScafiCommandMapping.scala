package it.unibo.scafi.simulation.gui.incarnation.scafi

import it.unibo.scafi.simulation.gui.configuration.CommandMapping
import it.unibo.scafi.simulation.gui.configuration.SensorName.{sens1, sens2, sens3}
import it.unibo.scafi.simulation.gui.launcher.scalaFX.Launcher.commandWorldSpace
import it.unibo.scafi.simulation.gui.model.space.Point3D
import it.unibo.scafi.simulation.gui.view.AbstractKeyboardManager.{Code1, Code2, Code3}
import it.unibo.scafi.simulation.gui.view.{AbstractKeyboardManager, AbstractSelectionArea}

object ScafiCommandMapping {

  object standardMapping extends CommandMapping {
    /**
      * map the command with the main input policy
      *
      * @param keyboard  keyboard manager
      * @param selection selection manager
      */
    override def map(keyboard: AbstractKeyboardManager, selection: Option[AbstractSelectionArea]): Unit = {
      keyboard.addCommand(Code1, (ids : Set[Any]) => commandWorldSpace.ToggleDeviceCommand(ids,sens1.name))
      keyboard.addCommand(Code2, (ids : Set[Any]) => commandWorldSpace.ToggleDeviceCommand(ids,sens2.name))
      keyboard.addCommand(Code3, (ids : Set[Any]) => commandWorldSpace.ToggleDeviceCommand(ids,sens3.name))
      selection.foreach {
        x => {
          x.addMovementAction((ids : Map[Any,Point3D]) => commandWorldSpace.MoveCommand(ids))
        }
      }
    }

  }
}
