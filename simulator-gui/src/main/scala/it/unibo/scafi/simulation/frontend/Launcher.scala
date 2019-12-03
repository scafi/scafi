/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.simulation.frontend

import it.unibo.scafi.simulation.frontend.controller.Controller
import it.unibo.scafi.simulation.gui.controller.controller3d.DefaultController3D

class Launcher extends App {
  def parseCommandLine(): Unit = SimulationCmdLine.parse(args, Settings)
  def launch(): Unit =
    if (Settings.Sim_3D_Rendering) {
      DefaultController3D().startup()
    } else {
      Controller.startup
    }
}

object DefaultLauncher extends Launcher {
  parseCommandLine()
  launch()
}
