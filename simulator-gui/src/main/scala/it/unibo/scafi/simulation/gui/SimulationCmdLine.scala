/*
 * Copyright (C) 2016-2017, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENCE.txt file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package it.unibo.scafi.simulation.gui

import SettingsSpace.Topologies._
import Settings._

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

  opt[Long]('R',"random-seed").action { (v, s) =>
    s.RandomSeed = v
    s
  } text(s"Random seed. Defaults to System.nanoTime().")

  help("help") text ("prints this usage text")
}
