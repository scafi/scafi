package it.unibo.scafi.simulation.gui.model.implementation

import it.unibo.scafi.simulation.gui.Simulation
import it.unibo.scafi.simulation.gui.controller.Controller
import it.unibo.scafi.simulation.gui.model.{EuclideanDistanceNbr, Network, Node, Sensor}
import it.unibo.scafi.space.Point2D
import it.unibo.scafi.simulation.gui.BasicSpatialIncarnation._

/**
  * Created by Varini on 14/11/16.
  * Converted/refactored to Scala by Casadei on 3/02/17
  */
class SimulationImpl() extends Simulation {
  //private Thread runProgram;  //should implements runnable
  private var net: SpaceAwareSimulator = null
  var network: Network = null
  var runProgram: Function0[(Int,Export)] = null
  var deltaRound: Double = .0
  var strategy: Any = null
  final private val controller: Controller = Controller.getIstance

  this.deltaRound = 0.00
  this.strategy = null

  def setRunProgram(program: Any): Unit = {

    val devsToPos: Map[Int, Point2D] = network.nodes.mapValues(n => new Point2D (n.position.getX, n.position.getY)) // Map id->position
    net = new SpaceAwareSimulator(
      space = new Basic3DSpace(devsToPos,
        proximityThreshold = this.network.neighbourhoodPolicy match {
          case EuclideanDistanceNbr(radius) => radius
        }),
        devs = devsToPos.map { case (d, p) => d -> new DevInfo(d, p,
          lsns => if (lsns == "sensor" && d == 3) 1 else 0,
          nsns => nbr => null)
        }
    )

    SensorEnum.sensors.foreach(se => { println(se); net.addSensor(se.name, se.value) })

    val ap = Class.forName(program.toString).newInstance().asInstanceOf[CONTEXT=>EXPORT]
    this.runProgram = () => net.exec(ap)
  }

  def setDeltaRound(deltaRound: Double) {
    this.deltaRound = deltaRound
  }

  def setStrategy(strategy: Any) {
    this.strategy = strategy
  }

  def getRunProgram: ()=>(Int,Export) = this.runProgram

  private var sensors = Map[String,Any]()

  override def setSensor(sensor: String, value: Any, nodes: Set[Node] = Set()): Unit = {
    val idSet: Set[Int] = nodes.map(_.id)
    if(nodes.size==0) {
      net.addSensor(sensor, value)
      sensors += sensor -> value
    }
    else
      net.chgSensorValue(sensor, idSet, value)
  }

  def getSensorValue(sensorName: String): Option[Any] = {
    net.getSensor(sensorName)
  }

  override def setPosition(n: Node): Unit ={
    net.setPosition(n.id, new Point2D(n.position.getX, n.position.getY))
  }
}
