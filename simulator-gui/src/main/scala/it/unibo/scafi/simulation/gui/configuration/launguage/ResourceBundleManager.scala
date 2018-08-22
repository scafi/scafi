package it.unibo.scafi.simulation.gui.configuration.launguage

import java.util.{Locale, ResourceBundle}

object ResourceBundleManager {
  val baseFolder = "bundles//"
  var locale = Locale.ENGLISH

  def international(key : String)(implicit file : String): String = {
    val bundle = ResourceBundle.getBundle(baseFolder + file,locale)
    bundle.getString(key)
  }
}
