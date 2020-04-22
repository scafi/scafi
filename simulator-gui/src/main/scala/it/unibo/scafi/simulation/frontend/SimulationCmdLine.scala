/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.simulation.frontend

import it.unibo.scafi.simulation.frontend.Settings._
import it.unibo.scafi.simulation.frontend.SettingsSpace.Topologies._

object SimulationCmdLine extends scopt.OptionParser[Settings.type]("<scafi graphical simulator>") {

  opt[Unit]('G', "no-config-panel").action { (_, s) =>
    s.ShowConfigPanel = false
    s
  } text("When provided, no config panel is shown.")

  opt[String]('p',"program").action { (v, s) =>
    s.Sim_ProgramClass = v
    s
  } text(s"Fully-qualified path to program to run. Default: '$Sim_ProgramClass'.")

  opt[String]('t',"topology").action { (v, s) =>
    s.Sim_Topology = v
    s
  } text(s"The network topology: '$Random','$Grid','$Grid_LoVar','$Grid_MedVar','$Grid_HighVar'. Default: '$Sim_Topology'.")

  opt[Int]('S',"sleep").action { (v, s) =>
    s.Sim_DeltaRound = v
    s
  } text(s"Time to sleep between a round execution. Default: $Sim_DeltaRound.")

  opt[Int]('N',"num-nodes").action { (v, s) =>
    s.Sim_NumNodes = v
    s
  } text(s"Number of nodes in the network. Default: $Sim_NumNodes.")

  opt[Double]('r',"radius").action { (v, s) =>
    s.Sim_NbrRadius = v
    s
  } text(s"Radius of neighbourhood (when policy is euclidean distance). Default: $Sim_NbrRadius.")

  opt[String]('s',"sensors").action { (v, s) =>
    s.Sim_Sensors += "\n"+v.replace(';','\n') // Appends to predefined sensors
    s
  } text(s"Sensors to be appended to default ones: '${Sim_Sensors.replace('\n',';')}'")

  opt[Long]("configuration-seed").action { (v, s) =>
    s.ConfigurationSeed = v
    s
  } text(s"Configuration seed. Defaults to System.nanoTime().")

  opt[Long]("simulation-seed").action { (v, s) =>
    s.SimulationSeed = v
    s
  } text(s"Simulation seed. Defaults to System.nanoTime().")

  opt[Long]("random-sensor-seed").action { (v, s) =>
    s.RandomSensorSeed = v
    s
  } text(s"Random sensor seed. Defaults to System.nanoTime().")

  help("help") text ("prints this usage text")
}
