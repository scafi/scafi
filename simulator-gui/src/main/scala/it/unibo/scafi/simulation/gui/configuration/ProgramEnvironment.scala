package it.unibo.scafi.simulation.gui.configuration

import it.unibo.scafi.simulation.gui.configuration.ProgramEnvironment.PerformancePolicy
import it.unibo.scafi.simulation.gui.controller.input.InputController
import it.unibo.scafi.simulation.gui.controller.logical.{ExternalSimulation, LogicController}
import it.unibo.scafi.simulation.gui.controller.presenter.Presenter
import it.unibo.scafi.simulation.gui.model.aggregate.AggregateWorld
import it.unibo.scafi.simulation.gui.view.View

/**
  * program environment, configuration used to set up program
  */
trait ProgramEnvironment[W <: AggregateWorld, V <: View] {
  /**
    * the input controller of this program
    * @return the input controller
    */
  def input : InputController
  /**
    * the present of program
    * @return the presenter used in this program
    */
  def presenter : Presenter[W,V]

  /**
    * @return the program simulation
    */
  def simulation : ExternalSimulation[W]

  /**
    * performance policy, describe how the program try to render the changes
    * @return the policy
    */
  def policy : PerformancePolicy

  /**
    * a set of other controller attached on the program
    * @return the controller
    */
  def controller : Iterable[LogicController[W]]
}

object ProgramEnvironment {

  /**
    * a policy used to control the program performance
    */
  trait PerformancePolicy {
    /**
      * internal tick of scheduler
      */
    val tick : Int
  }

  /**
    * world changes are show are show near real time
    */
  case object NearRealTimePolicy extends PerformancePolicy {
    override val tick: Int = 16
  }

  /**
    * a compromise of performance and real time changes
    */
  case object StandardPolicy extends PerformancePolicy {
    override val tick: Int = 66
  }

  /**
    * can miss some changes, decrease fps
    */
  case object FastPerformancePolicy extends PerformancePolicy {
    override val tick: Int = 200
  }
}
