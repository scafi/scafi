package it.unibo.scafi.simulation.gui.configuration.command.factory

import it.unibo.scafi.simulation.gui.configuration.command.Command.onlyMakeCommand
import it.unibo.scafi.simulation.gui.configuration.command.{Command, CommandFactory}
import it.unibo.scafi.simulation.gui.configuration.environment.ViewEnvironment
import it.unibo.scafi.simulation.gui.model.graphics2D.BasicShape2D.Rectangle
import it.unibo.scafi.simulation.gui.util.Result
import it.unibo.scafi.simulation.gui.util.Result.Fail
import it.unibo.scafi.simulation.gui.view.WindowConfiguration

class WindowConfigurationCommandFactory(viewEnv : ViewEnvironment[_]) extends CommandFactory{
  import CommandFactory._
  import WindowConfigurationCommandFactory._
  import it.unibo.scafi.simulation.gui.configuration.launguage.ResourceBundleManager._
  private val rect : Rectangle = viewEnv.windowConfiguration
  override val name: String = "window-configuration"

  override def commandArgsDescription: Seq[CommandArgDescription] =
    List(CommandArgDescription(Width,IntType,true,international(name,Width),defaultValue = Some(rect.w.toInt)),
      (CommandArgDescription(Height,IntType,true,international(name,Height),defaultValue = Some(rect.h.toInt))),
        (CommandArgDescription(FullScreen,BooleanType,true,international(name,FullScreen))))

  /**
    * a strategy defined by command factory implementatoin
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
      easyResultCreation(() => viewEnv.windowConfiguration = WindowConfiguration.apply)
    } else if(width.isDefined && height.isDefined) {
      easyResultCreation(() => viewEnv.windowConfiguration = WindowConfiguration(width.get,height.get))
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
