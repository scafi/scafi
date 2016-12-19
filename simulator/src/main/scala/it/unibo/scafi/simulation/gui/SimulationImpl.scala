package it.unibo.scafi.simulation.gui.model.implementation

import it.unibo.scafi.simulation.gui.Simulation
import it.unibo.scafi.simulation.gui.controller.Controller
import it.unibo.scafi.simulation.gui.model.{Network, Node}
import it.unibo.scafi.space.Point2D
import it.unibo.scafi.simulation.gui.BasicSpatialIncarnation._

import scala.collection.mutable.{ArrayBuffer => MArray, Map => MMap}
import scala.collection.JavaConverters.{asScalaBufferConverter, asScalaIteratorConverter, asScalaSetConverter, mapAsScalaMapConverter}

/**
  * Created by chiara on 14/11/16.
  */
class SimulationImpl() extends Simulation {
  //private Thread runProgram;  //should implements runnable
  private var net: SpaceAwareSimulator = null
  private var network: Network = null
  private var runProgram: Function0[(Int,Export)] = null
  private var deltaRound: Double = .0
  private var strategy: Any = null
  final private val controller: Controller = Controller.getIstance

  this.deltaRound = 0.00
  this.strategy = null

  def getNetwork: Network = this.network

  def setNetwork(network: Network) {
    this.network = network
  }

  def setRunProgram(program: Any): Unit = {

    //mappa i nodi con gli id
    val mapperPos: java.util.function.Function[Node, Point2D] = new java.util.function.Function[Node, Point2D] {
      override def apply(n: Node): Point2D = new Point2D (n.getPosition.getX, n.getPosition.getY);
    }

    //mappa i nodi con le posizioni
    val mapperId : java.util.function.Function[Node, Int] = new java.util.function.Function[Node, Int] {
      override def apply(n: Node): Int = n.getId
    }

    //fai la mappa
    val idList: List[Int] = List(this.network.getNodes.stream().map[Int](mapperId).iterator().asScala.toList: _*)
    val posList: List[Point2D] = List(this.network.getNodes.stream().map[Point2D](mapperPos).iterator().asScala.toList: _*)
    val devsToPos: Map[Int, Point2D] = idList.zip(posList).toMap //mappa id->posizione
    net = new SpaceAwareSimulator(
      space = new Basic3DSpace(devsToPos, proximityThreshold = this.network.getPolicy().asInstanceOf[Double]), //valore che passo da gui
      devs = devsToPos.map { case (d, p) => d -> new DevInfo(d, p,
        lsns => if (lsns == "sensor" && d == 3) 1 else 0,
        nsns => nbr => null)
      }
    ){println(devs)}

    val sensors: List[SensorEnum] = scala.List(SensorEnum.values: _*)

    sensors.foreach(se => net.addSensor(se.getName, se.getValue)) //non gli aggiungo già qui

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

  override def setSensor(sensor: String, ids: java.util.Set[Node], value: AnyRef): Unit = {
    val idSet: Set[Int] = Set[Int](ids.asScala.map(_.getId).toSeq: _*)
    net.chgSensorValue(sensor, idSet, value)
  }

  override def setPosition(n: Node): Unit ={
    net.setPosition(n.getId, new Point2D(n.getPosition.getX, n.getPosition.getY))
  }
}
