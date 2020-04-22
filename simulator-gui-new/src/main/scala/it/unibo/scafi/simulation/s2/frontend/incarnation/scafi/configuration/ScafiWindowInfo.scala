package it.unibo.scafi.simulation.s2.frontend.incarnation.scafi.configuration

import it.unibo.scafi.simulation.s2.frontend.view.WindowConfiguration

/**
  * a windows information for scafi application
  */
object ScafiWindowInfo {
  val name = "Scafi"

  val logoPath = "loadingLogo.png"

  val iconPath = "icon.png"

  /**
    * put scafi information in a generic window configuration
    * @param windowConfiguration the window configuration
    * @return the window configuration with scafi setting
    */
  def apply (windowConfiguration: WindowConfiguration) : WindowConfiguration = {
    windowConfiguration.clone(ScafiWindowInfo.name,Some(ScafiWindowInfo.logoPath),Some(ScafiWindowInfo.iconPath))
  }
}
