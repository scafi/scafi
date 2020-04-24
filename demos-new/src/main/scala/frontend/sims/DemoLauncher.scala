/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package frontend.sims

import frontend.sims.standard.Main
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.ScafiSimulationInitializer.RadiusSimulation
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.bridge.SimulationInfo
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.configuration.{ScafiProgramBuilder, ScafiWorldInformation}
import it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.world.ScafiWorldInitializer.Random
import it.unibo.scafi.simulation.s2.frontend.launcher.scafi.{ListDemo, Console => ScafiConsole, GraphicsLauncher => ScafiGraphicsLauncher, StringLauncher => ScafiStringLauncher}
import it.unibo.scafi.simulation.s2.frontend.view.scalaFX.drawer.FastFXOutput
import it.unibo.scafi.space.graphics2D.BasicShape2D.Circle


object DemoLauncher extends App {
  /**
    * Full screen simulation
    * ViewSetting.windowConfiguration = WindowConfiguration()
    * Windowed simulation
    * ViewSetting.windowConfiguration = WindowConfiguration(1200,700) // standard value 800 x 600
    */
  ScafiProgramBuilder (
    Random(5000,1920,1080),
    SimulationInfo(program = classOf[Main]),
    RadiusSimulation(radius = 40),
    neighbourRender = true,
    scafiWorldInfo = ScafiWorldInformation(shape = Some(Circle(4))),
    outputPolicy = FastFXOutput
  ).launch()
}

object ConsoleLauncher extends App {
  ListDemo.packageName = "frontend.sims"
  ScafiConsole.main(args)
}

object GraphicsLauncher extends App {
  ListDemo.packageName = "frontend.sims"
  ScafiGraphicsLauncher.main(args)
}

object StringLauncher extends App {
  ListDemo.packageName = "frontend.sims"
  ScafiStringLauncher("radius-simulation SupplyRescueDemo 50;random-world 500 1000 1000")
}
