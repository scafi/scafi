package it.unibo.scafi.simulation.gui.view

import java.awt.Toolkit

import it.unibo.scafi.space.graphics2D.BasicShape2D.Rectangle
import it.unibo.scafi.simulation.gui.view.WindowConfiguration.Size

/**
  * describe a window configuration used to initialize a screen
  */
trait WindowConfiguration {
  /**
    * @return window size
    */
  def size : Size
  /**
    * @return window name
    */
  def name : String

  /**
    * @return None if the application hasn't a logo path Some(path) otherwise
    */
  def logoPath : Option[String]

  /**
    * @return None if the application hasn't an icon Some(path) otherwise
    */
  def iconPath : Option[String]

  override def toString: String = s"[name = $name]; [size = $size]"
}

object WindowConfiguration {
  // standard window value
  private val windowName = "application"

  //standard log path
  private val logoPath : Option[String] = None

  //standard icon path
  private val iconPath : Option[String] = None

  private class WindowConfigurationImpl(override val size : Size,
                                        override val name : String,
                                        override val logoPath : Option[String],
                                        override val iconPath : Option[String]) extends WindowConfiguration

  /**
    * used to add clone method to WindowConfiguration
    * @param windowConfiguration the window configuration to clone
    */
  implicit class RichWindowConfiguration(windowConfiguration: WindowConfiguration) {
    def clone(name : String = windowConfiguration.name,
              logoPath : Option[String] = windowConfiguration.logoPath,
              iconPath : Option[String] = windowConfiguration.iconPath,
              size : Size = windowConfiguration.size
             ) : WindowConfiguration = new WindowConfigurationImpl(size,name,logoPath,iconPath)
  }

  /**
    * root window size trait
    */
  sealed trait Size

  /**
    * a fullscreen application
    */
  case object FullScreen extends Size

  /**
    * a windowed application
    * @param width the window width
    * @param height thw window height
    */
  case class Window(width : Int, height : Int)  extends Size

  /**
    * create a window configuration
    * @param width the window width
    * @param height the window height
    * @return the window created
    */
  def apply (width : Int,
             height : Int) : WindowConfiguration =
    new WindowConfigurationImpl(Window(width,height),windowName,logoPath,iconPath)

  /**
    * @return a fullscreen window
    */
  def apply() : WindowConfiguration =
    new WindowConfigurationImpl(FullScreen,windowName,logoPath,iconPath)

  /**
    * create a rectangle with the size specified
    * @param windowConfiguration the window configuration
    * @return a rectangle with the width and height passed
    */
  implicit def toWindowRect(windowConfiguration: WindowConfiguration) : Rectangle = windowConfiguration.size match {
    case FullScreen => Rectangle(Toolkit.getDefaultToolkit.getScreenSize.width, Toolkit.getDefaultToolkit.getScreenSize.height)
    case Window(w,h) => Rectangle(w,h)
  }
}