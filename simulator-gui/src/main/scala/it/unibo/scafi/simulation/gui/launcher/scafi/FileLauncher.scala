package it.unibo.scafi.simulation.gui.launcher.scafi

import scala.io.Source

object FileLauncher {
  def apply(path : String): Unit = {
    StringLauncher.apply(Source.fromFile(path).getLines().toList.reduce((a,b) => a+b))
  }
}
