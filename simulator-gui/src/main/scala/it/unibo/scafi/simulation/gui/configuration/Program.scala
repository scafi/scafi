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
                                              viewEnv : Option[ViewEnvironment[V]],
                                              commandBinding : CommandBinding) {

  private var _launched : Boolean = false
  def launched : Boolean = _launched
  def launch(): Unit = {
    //initialize the view
    viewEnv.foreach {_.init()}
    //map command
    viewEnv match {
      case Some(view) => commandBinding(view.keyboard, view.selection)
      case _ =>
    }
    //render the output
    viewEnv.foreach {_.container.render}
    //start all logic controller
    programEnv.controller.foreach( _.start)
    //init log
    programEnv.logConfiguration()
    //initialized the simulation
    programEnv.simulation.init()
    //start simulation
    programEnv.simulation.start()
    //init scheduler
    scheduler.attach(programEnv.input)
    programEnv.controller.foreach{scheduler.attach(_)}
    scheduler.attach(programEnv.simulation)
    //put output into the presenter
    viewEnv match {
      case Some(view) => {
        programEnv.presenter.output(view.container.output)
        scheduler.attach(programEnv.presenter)
      }

      case _ =>
    }
    scheduler.delta = programEnv.policy.tick
    scheduler.start(

    )
    _launched = true
  }
}
