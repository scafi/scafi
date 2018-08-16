package it.unibo.scafi.simulation.gui.incarnation.scafi

import it.unibo.scafi.simulation.gui.configuration.SensorName.{sensor1, sensor2, sensor3}
import it.unibo.scafi.simulation.gui.configuration.command.CommandFactory.CommandArg
import it.unibo.scafi.simulation.gui.configuration.command.MoveCommandFactory.MoveArg
import it.unibo.scafi.simulation.gui.configuration.command.SimulationCommandFactory.{ContinueArg, StopArg}
import it.unibo.scafi.simulation.gui.configuration.command.ToggleCommandFactory.ToggleArg
import it.unibo.scafi.simulation.gui.configuration.command.{CommandMapping, MoveCommandFactory, SimulationCommandFactory, ToggleCommandFactory}
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.scafiSimulationObserver
import it.unibo.scafi.simulation.gui.incarnation.scafi.world.scafiWorld
import it.unibo.scafi.simulation.gui.model.space.Point3D
import it.unibo.scafi.simulation.gui.view.AbstractKeyboardManager._
import it.unibo.scafi.simulation.gui.view.{AbstractKeyboardManager, AbstractSelectionArea}

object ScafiCommandMapping {
  import it.unibo.scafi.simulation.gui.incarnation.scafi.world.ScafiLikeWorld._
  private val moveFactory = new MoveCommandFactory(scafiWorld)
  private val toggleFactory = new ToggleCommandFactory(scafiWorld)
  private val simulationFactory = new SimulationCommandFactory(scafiSimulationObserver)
  private val toToggleArg : (Any,Set[Any]) => CommandArg = (value : Any, ids : Set[Any]) => ToggleArg(value,ids)
  /**
    * base mapping used internal to not repeat the same mapping
    */
  private object baseMapping extends CommandMapping {

    override def map(keyboard: AbstractKeyboardManager, selection: Option[AbstractSelectionArea]): Unit = {
      keyboard.addCommand(Code4, (v : Any) => StopArg, simulationFactory, StopArg)
      keyboard.addCommand(Code5, (v : Any) => ContinueArg, simulationFactory, ContinueArg)
      selection.foreach {
        x => {
          x.addMovementAction((ids : Map[Any,Point3D]) => MoveArg(ids), moveFactory)
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
      keyboard.addCommand(Code1, toToggleArg,toggleFactory,sensor1)
      keyboard.addCommand(Code2, toToggleArg,toggleFactory,sensor2)
      keyboard.addCommand(Code3, toToggleArg,toggleFactory,sensor3)

    }
  }

  /**
    * ad hoc mapping used to ad hoc application
    *
    * @param mapping the mapping
    */
  case class AdHocToggleMapping(mapping : Map[AbstractKeyCode, Any]) extends CommandMapping {

    override def map(keyboard: AbstractKeyboardManager, selection: Option[AbstractSelectionArea]): Unit = {
      baseMapping.map(keyboard,selection)
      mapping foreach {x => keyboard.addCommand(x._1,toToggleArg,toggleFactory,x._2)}
    }
  }
}
