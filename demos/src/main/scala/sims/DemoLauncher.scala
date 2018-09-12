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

package sims

import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.ScafiSimulationInitializer.RadiusSimulation
import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.SimulationInfo
import it.unibo.scafi.simulation.gui.incarnation.scafi.configuration.ScafiProgramBuilder
import it.unibo.scafi.simulation.gui.incarnation.scafi.world.ScafiWorldInitializer.Random
import it.unibo.scafi.simulation.gui.launcher.scafi.{ListDemo, Console => ScafiConsole, GraphicsLauncher => ScafiGraphicsLauncher}


object DemoLauncher extends App {
  ScafiProgramBuilder (
    Random(500,500,500),
    SimulationInfo(program = classOf[Main]),
    RadiusSimulation(radius = 40),
    neighbourRender = true
  ).launch()
}

object ConsoleLauncher extends App {
  ListDemo.packageName = "sims"
  ScafiConsole.main(args)
}

object GraphicsLauncher extends App {
  ListDemo.packageName = "sims"
  ScafiGraphicsLauncher.main(args)
}
