package it.unibo.scafi.simulation.gui.incarnation.scafi.configuration

import it.unibo.scafi.simulation.gui.view.WindowConfiguration

object ScafiWindowInfo {
  val name = "Scafi"

  val logoPath = "loadingLogo.png"

  val iconPath = "icon.png"
  def apply (windowConfiguration: WindowConfiguration) : WindowConfiguration = {
    windowConfiguration.clone(ScafiWindowInfo.name,Some(ScafiWindowInfo.logoPath),Some(ScafiWindowInfo.iconPath))
  }
}
