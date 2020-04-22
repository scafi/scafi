/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.simulation.frontend

import it.unibo.scafi.simulation.frontend.controller.Controller

object Main extends App {
  SimulationCmdLine.parse(args, Settings)
  Controller.startup
}
