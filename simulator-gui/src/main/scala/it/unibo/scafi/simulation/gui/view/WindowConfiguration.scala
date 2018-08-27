package it.unibo.scafi.simulation.gui.view

import java.awt.Toolkit

import it.unibo.scafi.simulation.gui.model.graphics2D.BasicShape2D.Rectangle
import it.unibo.scafi.simulation.gui.view.WindowConfiguration.Size

/**
  * describe a window configuration used to initialize a screen
  */
trait WindowConfiguration {
  def size : Size
  /**
    * @return window name
    */
  def name : String

  def logoPath : Option[String]

  def iconPath : Option[String]

  override def toString: String = s"[name = $name]; [size = $size]"
}

object WindowConfiguration {
  val windowName = "application"

  val logoPath : Option[String] = None

  val iconPath : Option[String] = None

  private class WindowConfigurationImpl(override val size : Size,
                                        override val name : String,
                                        override val logoPath : Option[String],
                                        override val iconPath : Option[String]) extends WindowConfiguration

  implicit class RichWindowConfiguration(windowConfiguration: WindowConfiguration) {
    def clone(name : String = windowConfiguration.name,
              logoPath : Option[String] = windowConfiguration.logoPath,
              iconPath : Option[String] = windowConfiguration.iconPath,
              size : Size = windowConfiguration.size
             ) : WindowConfiguration = new WindowConfigurationImpl(size,name,logoPath,iconPath)
  }
  sealed trait Size

  case object FullScreen extends Size

  case class Window(width : Int, height : Int)  extends Size

  def apply (width : Int,
             height : Int) : WindowConfiguration =
    new WindowConfigurationImpl(Window(width,height),windowName,logoPath,iconPath)

  def apply() : WindowConfiguration =
    new WindowConfigurationImpl(FullScreen,windowName,logoPath,iconPath)

  implicit def toWindowRect(windowConfiguration: WindowConfiguration) : Rectangle = windowConfiguration.size match {
    case FullScreen => Rectangle(Toolkit.getDefaultToolkit.getScreenSize.width, Toolkit.getDefaultToolkit.getScreenSize.height)
    case Window(w,h) => Rectangle(w,h)
  }
}