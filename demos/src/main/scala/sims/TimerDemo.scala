package sims

import it.unibo.scafi.incarnations.BasicSimulationIncarnation.AggregateProgram
import it.unibo.scafi.simulation.gui.{Launcher, Settings}
import lib.{BlockT2, SensorDefinitions}

/**
  * @author Roberto Casadei
  *
  */
object TimerDemo extends Launcher {
  // Configuring simulation
  Settings.Sim_ProgramClass = "sims.SimpleTimer" // starting class, via Reflection
  Settings.ShowConfigPanel = false // show a configuration panel at startup
  Settings.Sim_NbrRadius = 0.15 // neighbourhood radius
  Settings.Sim_NumNodes = 100 // number of nodes
  //Settings.Led_Activator = (b: Any) => b.asInstanceOf[Boolean]
  //Settings.To_String = (b: Any) => ""
  launch()
}

class SimpleTimer extends AggregateProgram with SensorDefinitions with BlockT2 {
  override def main() = branch(sense1){linearFlow(100)}{0}
}