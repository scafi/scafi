package it.unibo.scafi.simulation.gui.launcher

import it.unibo.scafi.simulation.gui.configuration.command.CommandMapping
import it.unibo.scafi.simulation.gui.configuration.{ProgramEnvironment, ViewEnvironment}
import it.unibo.scafi.simulation.gui.controller.synchronization.Scheduler.scheduler
import it.unibo.scafi.simulation.gui.model.aggregate.AggregateWorld
import it.unibo.scafi.simulation.gui.view.View

/**
  * a launcher that don't depend on graphics platform
  * @param programEnv the program environment
  * @param viewEnv the view environment
  * @param commandMapping a strategy to map command
  * @tparam W world type
  * @tparam V view type
  */
class MetaLauncher[W <: AggregateWorld, V <: View](programEnv : ProgramEnvironment[W,V],
                                                   viewEnv : ViewEnvironment[V],
                                                   commandMapping : CommandMapping) {

  def launch(): Unit = {
    //initialize the view
    viewEnv.init()
    //map command
    commandMapping.map(viewEnv.keyboard, viewEnv.selection)
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
    //init scheduler
    scheduler.attach(programEnv.input)
    programEnv.controller.foreach{scheduler.attach(_)}
    scheduler.attach(programEnv.simulation)
    scheduler.attach(programEnv.presenter)
    scheduler.delta = programEnv.policy.tick
    scheduler.start()
  }
}
