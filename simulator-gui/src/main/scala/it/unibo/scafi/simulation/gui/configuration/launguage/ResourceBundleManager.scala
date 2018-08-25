package it.unibo.scafi.simulation.gui.configuration.launguage

import java.util.{Locale, ResourceBundle}

object ResourceBundleManager {
  private val Separator = "-"
  private val baseFolder = "bundles//"
  var locale = Locale.ENGLISH

  def international(key : String)(implicit file : String): String = {
    val bundle = ResourceBundle.getBundle(baseFolder + file,locale)
    bundle.getString(key)
  }

  def international(key : String *)(implicit file : String): String = {
    val bundle = ResourceBundle.getBundle(baseFolder + file,locale)
    bundle.getString(key.mkString(Separator))
  }

  object KeyFile {
    val Configuration = "configuration"
    val CommandDescription = "command-description"
    val Error = "error"
    val CommandName = "command-name"
  }

  /**
    * fast way to use resource bundle string
    * @param sc the string context
    */
  implicit class Internationalization(val sc : StringContext)(implicit val file : String) {
    def i() : String = international(sc.parts.head)
  }
}
