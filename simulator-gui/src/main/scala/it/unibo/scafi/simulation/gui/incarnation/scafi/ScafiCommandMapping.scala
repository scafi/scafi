package it.unibo.scafi.simulation.gui.incarnation.scafi

import it.unibo.scafi.simulation.gui.configuration.CommandMapping
import it.unibo.scafi.simulation.gui.configuration.SensorName.{sensor1, sensor2, sensor3}
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.ScafiBridge
import it.unibo.scafi.simulation.gui.incarnation.scafi.world.scafiWorldCommandSpace
import it.unibo.scafi.simulation.gui.model.space.Point3D
import it.unibo.scafi.simulation.gui.view.AbstractKeyboardManager._
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
      keyboard.addCommand(Code1, (ids : Set[Any]) => scafiWorldCommandSpace.ToggleDeviceCommand(ids,sensor1.name))
      keyboard.addCommand(Code2, (ids : Set[Any]) => scafiWorldCommandSpace.ToggleDeviceCommand(ids,sensor2.name))
      keyboard.addCommand(Code3, (ids : Set[Any]) => scafiWorldCommandSpace.ToggleDeviceCommand(ids,sensor3.name))
      keyboard.addCommand(Code4, () => ScafiBridge.scafiSimulationCommandSpace.StopSimulation)
      keyboard.addCommand(Code5, () => ScafiBridge.scafiSimulationCommandSpace.ContinueSimulation)
      selection.foreach {
        x => {
          x.addMovementAction((ids : Map[Any,Point3D]) => scafiWorldCommandSpace.MoveCommand(ids))
        }
      }
    }

  }
}
