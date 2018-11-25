package it.unibo.scafi.simulation.frontend.configuration.command.factory

import it.unibo.scafi.simulation.frontend.configuration.command.{Command, CommandFactory}
import it.unibo.scafi.simulation.frontend.configuration.environment.ViewEnvironment
import it.unibo.scafi.space.graphics2D.BasicShape2D.Rectangle
import it.unibo.scafi.simulation.frontend.util.Result
import it.unibo.scafi.simulation.frontend.util.Result.Fail
import it.unibo.scafi.simulation.frontend.view.{ViewSetting, WindowConfiguration}

/**
  * a factory used to create command that modify window configuration
  */
class WindowConfigurationCommandFactory extends CommandFactory{
  import CommandFactory._
  import WindowConfigurationCommandFactory._
  import it.unibo.scafi.simulation.frontend.configuration.launguage.ResourceBundleManager._
  //the standard view configuration
  private val rect : Rectangle = ViewSetting.windowConfiguration
  override val name: String = "window-configuration"

  override def commandArgsDescription: Seq[CommandArgDescription] =
    List(CommandArgDescription(Width,IntType,optional = true,international(name,Width),defaultValue = Some(rect.w.toInt)),
      CommandArgDescription(Height,IntType,optional = true,international(name,Height),defaultValue = Some(rect.h.toInt)),
      CommandArgDescription(FullScreen,BooleanType,optional = true,international(name,FullScreen)))

  /**
    * a strategy defined by command factory implementation
    *
    * @param args the command args
    * @return (Success,Some(Command)) if the arguments are accepted (Fail,None) otherwise
    */
  override protected def createPolicy(args: CommandArg): (Result, Option[Command]) = {
    var width : Option[Int] = None
    var height : Option[Int] = None
    var fullScreen : Option[Boolean] = None

    args.get(Width) match {
      case Some(widthValue : Int) => width = Some(widthValue)
      case Some(_) => return creationFailed(Fail(wrongTypeParameter(IntType,Width)))
      case _ =>
    }
    args.get(Height) match {
      case Some(heightValue : Int) => height = Some(heightValue)
      case Some(_) => return creationFailed(Fail(wrongTypeParameter(IntType,Width)))
      case _ =>
    }
    args.get(FullScreen) match {
      case Some(fullScreenValue : Boolean) => fullScreen = Some(fullScreenValue)
      case Some(_) => wrongTypeParameter(BooleanType,FullScreen)
      case _ =>
    }
    val fullscreen = fullScreen match {
      case Some(true) => true
      case _ => false
    }
    if(fullscreen) {
      easyResultCreation(() => ViewSetting.windowConfiguration = WindowConfiguration.apply())
    } else if(width.isDefined && height.isDefined) {
      easyResultCreation(() => ViewSetting.windowConfiguration = WindowConfiguration(width.get,height.get))
    } else {
      creationFailed(Fail(wrongParameterName(Width,Height)))
    }
  }
}

object WindowConfigurationCommandFactory {
  val Width = "width"
  val Height = "height"
  val FullScreen = "fullscreen"
}
