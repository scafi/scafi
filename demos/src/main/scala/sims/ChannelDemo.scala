package sims

import it.unibo.scafi.incarnations.BasicSimulationIncarnation.{AggregateProgram, BlockG}
import it.unibo.scafi.simulation.gui.{Launcher, Settings}

object ChannelDemo extends Launcher {
  // Configuring simulation
  Settings.Sim_ProgramClass = "sims.Channel" // starting class, via Reflection
  Settings.ShowConfigPanel = false // show a configuration panel at startup
  Settings.Sim_NbrRadius = 0.1 // neighbourhood radius
  Settings.Sim_NumNodes = 200 // number of nodes
  Settings.Led_Activator = (b: Any) => b.asInstanceOf[Boolean]
  Settings.To_String = (b: Any) => ""
  launch()
}

class Channel extends AggregateProgram  with SensorDefinitions with BlockG {

  def channel(source: Boolean, target: Boolean, width: Double): Boolean =
    distanceTo(source) + distanceTo(target) <= distanceBetween(source, target) + width

  override def main() = branch(sense3){false}{channel(sense1, sense2, 1)}
}