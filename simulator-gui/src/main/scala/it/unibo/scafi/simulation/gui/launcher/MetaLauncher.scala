package it.unibo.scafi.simulation.gui.launcher

import it.unibo.scafi.simulation.gui.configuration.{CommandMapping, ProgramEnvironment, ViewEnvironment, WorldEnvironment}
import it.unibo.scafi.simulation.gui.controller.synchronization.Scheduler.scheduler
import it.unibo.scafi.simulation.gui.model.aggregate.AggregateWorld
import it.unibo.scafi.simulation.gui.view.View

/**
  * a launcher that don't depend on graphics platform
  * @param programEnv the program environment
  * @param viewEnv the view environment
  * @param worldEnv the world environment
  * @param commandMapping a strategy to map command
  * @tparam W world type
  * @tparam V view type
  */
class MetaLauncher[W <: AggregateWorld, V <: View](programEnv : ProgramEnvironment[W,V],
                                                   viewEnv : ViewEnvironment[V],
                                                   worldEnv : WorldEnvironment[W],
                                                   commandMapping : CommandMapping) {

  def launch(): Unit = {
    commandMapping.map(viewEnv.keyboard, viewEnv.selection)
    //initialize the view environment
    viewEnv.init()
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
    scheduler.delta_=(programEnv.policy.tick)
    scheduler.start()

  }
}
