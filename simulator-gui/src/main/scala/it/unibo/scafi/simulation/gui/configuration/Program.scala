package it.unibo.scafi.simulation.gui.configuration

import it.unibo.scafi.simulation.gui.configuration.command.CommandBinding
import it.unibo.scafi.simulation.gui.configuration.environment.{ProgramEnvironment, ViewEnvironment}
import it.unibo.scafi.simulation.gui.controller.synchronization.Scheduler.scheduler
import it.unibo.scafi.simulation.gui.model.aggregate.AggregateWorld
import it.unibo.scafi.simulation.gui.view.View

/**
  * a program that can be launched
  * @param programEnv the program environment
  * @param viewEnv the view environment
  * @param commandBinding a strategy to map command
  * @tparam W world type
  * @tparam V view type
  */
class Program[W <: AggregateWorld, V <: View](programEnv : ProgramEnvironment[W,V],
                                              viewEnv : ViewEnvironment[V],
                                              commandBinding : CommandBinding) {

  private var _launched : Boolean = false
  def launched : Boolean = _launched
  def launch(): Unit = {
    //initialize the view
    viewEnv.init()
    //map command
    commandBinding(viewEnv.keyboard, viewEnv.selection)
    //initialized the simulation
    programEnv.simulation.init()
    //put output into the presenter
    programEnv.presenter.output(viewEnv.container.output)
    //render the output
    viewEnv.container.render
    //start simulation
    programEnv.simulation.start()
    //start all logic controller
    programEnv.controller.foreach( _.start)
    //init log
    programEnv.logConfiguration()
    //init scheduler
    scheduler.attach(programEnv.input)
    programEnv.controller.foreach{scheduler.attach(_)}
    scheduler.attach(programEnv.simulation)
    scheduler.attach(programEnv.presenter)
    scheduler.delta = programEnv.policy.tick
    scheduler.start()
    _launched = true
  }
}
