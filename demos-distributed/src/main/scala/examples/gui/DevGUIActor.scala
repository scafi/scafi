/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package examples.gui

import java.awt._
import java.awt.event._

import javax.swing._
import it.unibo.scafi.incarnations.BasicAbstractActorIncarnation
import it.unibo.scafi.space.Point2D

import scala.concurrent.duration._
import scala.collection.mutable.{Map => MMap}
import akka.actor.{Actor, ActorRef, Props}
import akka.util.Timeout
import it.unibo.scafi.distrib.actor._

import scala.util.Success
import akka.event.LoggingAdapter

class DevGUIActor(val I: BasicAbstractActorIncarnation,
                  private var dev: ActorRef) extends Actor with ActionListener {
  val width = 750
  val height = 400

  type ID = I.ID
  type LSNS = I.CNAME
  type EXPORT = I.EXPORT

  val interopId = I.interopID
  val interopLsns = I.interopCNAME

  dev ! MsgAddObserver(self)

  /* Local imports and variables */

  import scala.collection._
  import context.dispatcher

  // Provides the ExecutionContext
  protected val Log: LoggingAdapter = akka.event.Logging(context.system, this)

  var registry: ActorRef = _

  /* GUI-related members */
  var frame: JFrame = _
  var bAddNbr, bSetSensor: JButton = _
  var lId, lExport, lRounds: JTextField = _
  var localSensors, neighbors, exps: DefaultListModel[String] = _

  BuildFrame()

  /* Behavior */

  def invokeLater(body: =>Any): Unit = {
    SwingUtilities.invokeLater(new Runnable(){
      override def run(): Unit = {
        body
        frame.repaint(); frame.revalidate()
      }
    })
  }

  def receive: Receive = {
    //case GoOn => { frame.repaint(); frame.revalidate() }
    case m:I.MyNameIs=> invokeLater {
      lId.setText("Id: " + m.id)
      this.frame.setTitle(s"ID = ${m.id}")
    }
    case MsgWithInput("registry", ref: ActorRef) =>
      registry = ref
    case m:I.MsgNeighborhood => invokeLater {
      neighbors.removeAllElements()
      m.nbrs.foreach { n => neighbors.addElement(n.toString)}
    }
    case m:I.MsgLocalSensorValue[_] => invokeLater {
      val index = localSensors.toArray.indexWhere{
        case s:String => s.startsWith(m.name.toString)
      }
      if(index >= 0) localSensors.remove(index)
      localSensors.addElement(m.name.toString + ":" + m.value)

      if(m.name=="LOCATION_SENSOR") {
        val pos = m.value.asInstanceOf[Point2D]
        this.frame.setBounds(pos.x.toInt*250,pos.y.toInt*200,240,150)
      }

      if(m.name.toString=="source" && m.value==true) {
        this.frame.setBackground(Color.ORANGE)
      }

    }
    case m:I.MsgNbrSensorValue[_] => { } // TODO
    case m:I.MsgExports => invokeLater {
      exps.removeAllElements()
      m.exports.keySet.foreach {
        k => exps.addElement(k + " -> " +
          m.exports(k).get(I.factory.emptyPath()))
      }
    }
    case p:I.MsgExport => invokeLater {
      lExport.setText(s"${p.export.root[Double]().toInt}")
    }
    case p:I.MsgRound => invokeLater {
      lRounds.setText(s"Rounds: ${p.n}")
    }
  }

  var toPause: Boolean = true
  override def actionPerformed(e: ActionEvent): Unit = {
    val s = e.getSource
    if(s == bAddNbr){
      val nbrId = interopId.fromString(JOptionPane.showInputDialog(
        frame,
        "Enter neighbor's ID",
        "Add NBR",
        JOptionPane.PLAIN_MESSAGE));
      //dev ! MsgWithExport(nbrId, factory.emptyExport())
      import akka.pattern.ask
      implicit val timeout: Timeout = 1.second

      if(registry!=null){
        (registry ? I.MsgLookup(nbrId)).onComplete {
          case Success(m:I.MsgDeviceLocation) =>
            dev ! I.MsgDeviceLocation(m.id, m.ref)
          case _ => throw new IllegalArgumentException(s"Remote registry ${registry} not found")
        }
      }
    }
    if(s == bSetSensor){
      val sv = JOptionPane.showInputDialog(
        frame,
        "sensorName=value",
        "Set Sensor Value",
        JOptionPane.PLAIN_MESSAGE).split('=')

      if(sv.length==2) {
        val k = interopLsns.fromString(sv(0).trim)
        var v = sv(1).trim
        val posPattern = """\((\d+(?:\.\d+)?);(\d+(?:\.\d+)?)\)""".r
        val posMatch = posPattern.findFirstMatchIn(v)
        dev ! I.MsgLocalSensorValue(k, v match {
          case _ if posMatch.isDefined => new Point2D(posMatch.get.group(1).toDouble, posMatch.get.group(2).toDouble)
          case "true" | "false" => v.toBoolean
          case _ if v.forall(_.isDigit) => v.toInt
          case _ if v.forall(c => c == '.' || c.isDigit) => v.toDouble
          case _ => v
        })
      }
    }
  }

  def BuildFrame(): Unit = {
    frame = new javax.swing.JFrame("GUI: " + self.path.name)
    frame.setSize(width, height)
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)

    val panel = new JPanel()
    frame.setContentPane(panel)
    panel.setLayout(new BorderLayout())

    val topPanel = new JPanel()

    lId = new JTextField("Id: ")
    lId.setEnabled(false)
    lId.setFont(new Font("Arial", Font.BOLD, 25))

    lExport = new JTextField("")
    lExport.setEnabled(false)
    lExport.setFont(new Font("Arial", Font.BOLD, 25))

    lRounds = new JTextField("Rounds: ")
    lRounds.setEnabled(false)
    lRounds.setFont(new Font("Arial", Font.BOLD, 18))

    //topPanel.add(lId)
    topPanel.add(lExport)
    //topPanel.add(lRounds)

    panel.add(topPanel, BorderLayout.NORTH)

    localSensors = new DefaultListModel[String]()
    val sensorList = new JList[String](localSensors)
    sensorList.setLayoutOrientation(JList.VERTICAL)
    sensorList.setVisibleRowCount(-1)
    val sensorListWrapper = new JScrollPane()
    sensorListWrapper.setViewportView(sensorList)
    sensorListWrapper.setBounds(0,0,200,400)
    panel.add(sensorListWrapper, BorderLayout.WEST)

    exps = new DefaultListModel[String]()
    val expsList= new JList[String](exps)
    expsList.setLayoutOrientation(JList.VERTICAL)
    expsList.setVisibleRowCount(-1)
    val expsListWrapper = new JScrollPane()
    expsListWrapper.setViewportView(expsList)
    expsListWrapper.setBounds(0,0,200,400)
    panel.add(expsListWrapper, BorderLayout.CENTER)

    neighbors = new DefaultListModel[String]()
    val nbrList= new JList[String](neighbors)
    nbrList.setLayoutOrientation(JList.VERTICAL)
    nbrList.setVisibleRowCount(-1)
    val nbrListWrapper = new JScrollPane()
    nbrListWrapper.setViewportView(nbrList)
    nbrListWrapper.setBounds(0,0,200,400)
    panel.add(nbrListWrapper, BorderLayout.EAST)

    val cmdPanel = new JPanel()
    cmdPanel.setLayout(new FlowLayout())
    bAddNbr = new JButton("AddNBR")
    bSetSensor = new JButton("SetSensor")

    bAddNbr.addActionListener(this)
    bSetSensor.addActionListener(this)

    cmdPanel.add(bAddNbr)
    cmdPanel.add(bSetSensor)
    //panel.add(cmdPanel, BorderLayout.SOUTH)

    //frame.pack()
    frame.setVisible(true)

    // GUI will update at 500 ms interval
    //context.system.scheduler.schedule(1 second, 100 millis) { self ! GoOn }

    context.system.scheduler.scheduleOnce(5.seconds) {
      frame.addComponentListener(new ComponentListener {
        override def componentShown(e: ComponentEvent): Unit = {}

        override def componentHidden(e: ComponentEvent): Unit = {}

        override def componentMoved(e: ComponentEvent): Unit = {
          val c = e.getSource().asInstanceOf[Component]
          val loc = c.getLocationOnScreen()

          println(s"Moved to $loc")

          val pos = Point2D((loc.getX / 250).round, (loc.getY / 200).round)
          //self ! I.MsgLocalSensorValue("LOCATION_SENSOR", pos)
          dev ! I.MsgLocalSensorValue("LOCATION_SENSOR", pos)
        }

        override def componentResized(e: ComponentEvent): Unit = {}
      })
    }
  }
}

object DevGUIActor {
  def props(inc: BasicAbstractActorIncarnation, devActorRef: ActorRef): Props =
    Props(classOf[DevGUIActor], inc, devActorRef)
}
