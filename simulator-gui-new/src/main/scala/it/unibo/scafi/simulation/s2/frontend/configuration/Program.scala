package it.unibo.scafi.simulation.s2.frontend.configuration

import it.unibo.scafi.simulation.s2.frontend.configuration.command.CommandBinding
import it.unibo.scafi.simulation.s2.frontend.configuration.environment.{ProgramEnvironment, ViewEnvironment}
import it.unibo.scafi.simulation.s2.frontend.controller.synchronization.Scheduler.scheduler
import it.unibo.scafi.simulation.s2.frontend.model.aggregate.AggregateWorld
import it.unibo.scafi.simulation.s2.frontend.view.View

/**
  * a program that can be launched
  * @param programEnv the program environment
  * @param viewEnv the view environment
  * @param commandBinding a strategy to map command
  * @tparam W world type
  * @tparam V view type
  */
class Program[W <: AggregateWorld, V <: View](programEnv : ProgramEnvironment[W,V],
                                              viewEnv : Option[ViewEnvironment[V]],
                                              commandBinding : CommandBinding) {

  private var _launched : Boolean = false
  def launched : Boolean = _launched
  def launch(): Unit = {
    //initialize view, map keyboard with command and render application
    viewEnv match {
      case Some(view) =>
        view.init()
        commandBinding(view.keyboard, view.selection)
        view.container.render()
      case _ =>
    }
    //init log
    programEnv.logConfiguration()
    //initialized the simulation
    programEnv.simulation.init()
    //start simulation
    programEnv.simulation.start()
    //init scheduler
    scheduler.attach(programEnv.input)
    scheduler.attach(programEnv.simulation)
    //put output into the presenter
    viewEnv match {
      case Some(view) =>
        programEnv.presenter.output(view.container.output)
        scheduler.attach(programEnv.presenter)
      case _ =>
    }
    scheduler.delta = programEnv.policy.tick
    scheduler.start()
    _launched = true
  }
}
