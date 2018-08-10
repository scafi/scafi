package it.unibo.scafi.simulation.gui.launcher.scafi

import it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.ScafiBridge.scafiSimulationCommandSpace
import it.unibo.scafi.simulation.gui.incarnation.scafi.world.ScafiLikeWorld.scafiWorldCommandSpace
import it.unibo.scafi.simulation.gui.launcher.MetaConsoleApplication

object ConsoleLauncher extends App {
  new MetaConsoleApplication(scafiConsoleConfigurator,scafiWorldCommandSpace,scafiSimulationCommandSpace)
}
