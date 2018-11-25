package frontend.sims.advanced

import javafx.scene.shape.Circle

import frontend.lib.{FlockingLib, Movement2DSupport}
import frontend.sims.{SensorDefinitions, SizeConversion}
import it.unibo.scafi.incarnations.BasicSimulationIncarnation.{AggregateProgram, BlockG}
import it.unibo.scafi.simulation.MetaActionManager
import it.unibo.scafi.simulation.frontend.configuration.SensorName
import it.unibo.scafi.simulation.frontend.configuration.command.{Command, CommandBinding, CommandFactory}
import it.unibo.scafi.simulation.frontend.controller.logger.LogManager
import it.unibo.scafi.simulation.frontend.controller.logger.LogManager.IntLog
import it.unibo.scafi.simulation.frontend.incarnation.scafi.bridge.ScafiSimulationInitializer.RadiusSimulation
import it.unibo.scafi.simulation.frontend.incarnation.scafi.bridge.{MetaActionProducer, SimulationInfo, scafiSimulationExecutor}
import it.unibo.scafi.simulation.frontend.incarnation.scafi.configuration.{ScafiProgramBuilder, ScafiWorldInformation}
import it.unibo.scafi.simulation.frontend.incarnation.scafi.world.ScafiWorldInitializer.Random
import it.unibo.scafi.simulation.frontend.incarnation.scafi.world.scafiWorld
import it.unibo.scafi.simulation.frontend.model.sensor.SensorConcept.{SensorDevice, sensorInput}
import it.unibo.scafi.simulation.frontend.util.Result
import it.unibo.scafi.simulation.frontend.view.scalaFX.drawer.{FXOutputPolicy, StandardFXOutput}
import it.unibo.scafi.simulation.frontend.view.{AbstractKeyboardManager, AbstractSelectionArea, GraphicsLogger}
import it.unibo.scafi.space.SpatialAbstraction.Bound
import it.unibo.scafi.space.graphics2D.BasicShape2D.Rectangle

import scalafx.geometry.Point2D
import scalafx.scene.control.Label
import scalafx.scene.paint.Color

/**
  * a special use of the front-end.
  * with redefinition of main strategy, you can
  * create something very different from the standard context.
  * In this case I create a very simple videogame.
  * the player is a red circle.
  * you can move it with:
  * - 1 to go right
  * - 2 to go left
  * - 3 to go up
  * - 4 to go down
  * when player is next to an enemy, enemy fades.
  * the score is showed in log section over the score (to see log section you can use CTRL + L)
  */
object SimpleGame extends App {
  val gameId = 0
  private val commandArgName = "direction"
  //export type
  type E = ((Double,Double),Boolean)
  //the boundary of game scene
  private val boundary = Bound(Rectangle(500,500))
  //direction of play
  object Direction extends Enumeration {
    val R,L,U,D,N = Value
  }
  //factory used to change the player direciton
  private val specialFactory = new CommandFactory {
    import CommandFactory._
    override protected def createPolicy(args: CommandArg): (Result, Option[Command]) = {
      CommandFactory.easyResultCreation(()=> {
        //in this way i can take current scafi simulation
        val bridge = scafiSimulationExecutor.contract.simulation.get
        val sensor : Direction.Value = args(commandArgName).asInstanceOf[Direction.Value]
        bridge.chgSensorValue("direction",Set(gameId),sensor)
      })
    }
    //not usefull in this context
    override def commandArgsDescription: Seq[CommandArgDescription] = List(CommandArgDescription(commandArgName,LimitedValueType(SensorName.inputSensor:_*)))
    override def name: String = "special"
    override def description: String = ""
  }
  //i can change the command binding strategy, in this case i bind 1,..,4 with some direction
  private val specialMapping = new CommandBinding {
    override def apply(keyboard: AbstractKeyboardManager, selection: Option[AbstractSelectionArea]): Unit = {
      keyboard.linkCommandCreation(AbstractKeyboardManager.Code1,Map(commandArgName -> Direction.R),specialFactory)
      keyboard.linkCommandCreation(AbstractKeyboardManager.Code2,Map(commandArgName -> Direction.L),specialFactory)
      keyboard.linkCommandCreation(AbstractKeyboardManager.Code3,Map(commandArgName -> Direction.U),specialFactory)
      keyboard.linkCommandCreation(AbstractKeyboardManager.Code4,Map(commandArgName -> Direction.D),specialFactory)
    }
  }

  //i can create my sensor.
  private val specialDeviceProducer : Iterable[scafiWorld.DeviceProducer] = List (
      scafiWorld.GeneralSensorProducer("direction",Direction.N,sensorInput),
      scafiWorld.LedProducer("player",value = false,sensorInput),
      scafiWorld.LedProducer("other",value = true,sensorInput),
      scafiWorld.LedProducer("visible",value = true,sensorInput))

  MetaActionProducer.movementDtActionProducer.valueParser = (e : Any) => Some(e.asInstanceOf[E]._1)
  //i can add a meta action producer (in this case, with the export produced i can change the state of a sensor (visible)
  private val toggleAction = new MetaActionProducer[Boolean] {
    private val action : Any => Option[Boolean] = (e : Any) => Some(e.asInstanceOf[E]._2)
    override def valueParser: Any => Option[Boolean] = action

    override def valueParser_=(function: Any => Option[Boolean]): Unit = {}

    override def apply(id: Int, argument: Boolean): MetaActionManager.MetaAction = {
      val bridge = scafiSimulationExecutor.contract.simulation.get
      if(!argument) {
        bridge.NodeChangeSensor(id,"visible",false)
      } else {
        MetaActionManager.EmptyAction
      }
    }
  }

  //i can describe a special output strategy
  private val specialOutput = new FXOutputPolicy {
    import scalafx.Includes._
    private var score = 0
    private val offsetScore = -10
    private lazy val label = new Label("0")
    private val radiusPlayerSize = 5
    private val colorPlayer = Color.OrangeRed
    override type OUTPUT_NODE = StandardFXOutput.OUTPUT_NODE
    //this method describe how to render the model node passed
    override def nodeGraphicsNode(node: NODE): OUTPUT_NODE = {
      if(node.id == gameId) {
        val p : Point2D = new Point2D(node.position.x,node.position.y)
        val circle = new Circle {
          this.centerX = p.x
          this.centerY = p.y
          this.radius = radiusPlayerSize
          this.smooth = false
          this.fill = colorPlayer
        }
        circle
      } else {
        StandardFXOutput.nodeGraphicsNode(node)
      }
    }
    //used to draw device (in this case i draw only the score
    override def deviceToGraphicsNode (node: OUTPUT_NODE, dev: DEVICE): Option[OUTPUT_NODE] = None

    //used to update the state of device (when it change its value)
    override def updateDevice(node : OUTPUT_NODE, dev: DEVICE, graphicsDevice: Option[OUTPUT_NODE]): Unit = {
      if(dev.name == "visible") {
        dev match {
          case SensorDevice(sens) => {
            if(node.isVisible && !sens.value[Boolean]) {
              node.setVisible(sens.value[Boolean])
              score += 1
            }
            LogManager.notify(IntLog("score","score",score))
          }
        }
      }
    }
  }
  GraphicsLogger.addStrategy("score",GraphicsLogger.textual)
  ScafiProgramBuilder (
    Random(100,500,500),
    SimulationInfo(program = classOf[Game],
      metaActions = List(MetaActionProducer.movementDtActionProducer,toggleAction),
      exportEvaluations = List.empty),
    RadiusSimulation(100),
    scafiWorldInfo = ScafiWorldInformation(boundary = Some(boundary), deviceSeed = specialDeviceProducer),
    neighbourRender = false,
    outputPolicy = specialOutput,
    commandMapping = specialMapping
  ).launch()
  //change the state of player node
  private val simulation = scafiSimulationExecutor.contract.simulation.get
  simulation.chgSensorValue("player",Set(gameId),true)
  simulation.chgSensorValue("other",Set(gameId),false)
}

class Game extends AggregateProgram with SensorDefinitions with BlockG with FlockingLib with Movement2DSupport {
  import SimpleGame._
  //velocity of player
  private val dt = 0.4
  //range of eating
  private val eatDistance = 5

  private def senseDirection : Direction.Value = sense[Direction.Value]("direction")
  private def visibleSense : Boolean = sense[Boolean]("visible")
  private def other : Boolean = sense[Boolean]("other")
  private def player : Boolean = sense[Boolean]("player")

  private def control : ((Double,Double),Boolean) = (senseDirection match {
    case Direction.R => (-dt,.0)
    case Direction.L => (dt,.0)
    case Direction.U => (.0,dt)
    case Direction.D => (.0,-dt)
    case _ => (.0,.0)
  },true)

  override def main(): ((Double,Double),Boolean) =
    branch(visibleSense) {
      mux(player) {
        control
      } {
        mux(distanceTo(player) + distanceTo(other) < eatDistance) {
          ((.0, .0), false)
        } ((SizeConversion.normalSizeToWorldSize(randomMovement()),true))
      }
    } {((.0,.0),false)}
}
