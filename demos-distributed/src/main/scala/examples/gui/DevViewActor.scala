/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package examples.gui

import akka.actor.{Actor, ActorRef}
import it.unibo.scafi.distrib.actor.MsgAddObserver
import it.unibo.scafi.incarnations.BasicAbstractActorIncarnation
import it.unibo.scafi.space.Point2D
import javax.swing._
import java.awt._

trait DevViewActor extends Actor {
  val I: BasicAbstractActorIncarnation
  var dev: ActorRef
  protected var id: I.UID = _
  protected var devsGUIActor: ActorRef = _
  protected val Log = akka.event.Logging(context.system, this)
  protected var devComponent, componentSpot: JComponent = _
  protected var lId, lExport: JLabel = _

  val programs: Map[String, () => I.ScafiStandardAggregateProgram] = I.getClass
    .getDeclaredFields
    .map(f => { f.setAccessible(true); f.getName -> f.get(I) })
    .filter(f => f._2.isInstanceOf[() => I.ScafiStandardAggregateProgram])
    .map(f => f._1 -> f._2.asInstanceOf[() => I.ScafiStandardAggregateProgram])
    .toMap

  dev ! MsgAddObserver(self)
  buildComponent()

  override def receive: Receive = {
    case g: I.MsgDevsGUIActor => updateGuiRef(g.devsGuiActor)
    case m: I.MyNameIs => updateId(m.id)
    case m: I.MsgLocalSensorValue[_] => updateSensor(m.name, m.value)
    case p: I.MsgExport => updateExport(p.export)
    case msg => Log.debug("[DevGUIActor_id=" + id + "] Message unhandled: " + msg); unhandled(msg)
  }

  protected def buildComponent(): Unit = {
    devComponent = new JPanel with DraggableComponent with RightClickMenuComponent {
      override protected def afterDragging(location: Point): Unit = {
        val pos = Point2D((location.getX / DevViewActor.Width).round,
          (location.getY / DevViewActor.Height).round)
        dev ! I.MsgLocalSensorValue("LOCATION_SENSOR", pos)
      }
    }
    devComponent.setLayout(new GridBagLayout())
    devComponent.setVisible(true)

    val customFont: Font = new Font("Arial", Font.BOLD, DevViewActor.TextSize)

    val exportPanel = new JPanel()
    lExport = new JLabel("export")
    lExport.setFont(customFont)
    exportPanel.add(lExport)

    componentSpot = new CircularPanel()
    componentSpot.setLayout(new GridBagLayout())
    lId = new JLabel("id")
    lId.setFont(customFont)
    componentSpot.add(lId)

    val cbc = new GridBagConstraints()
    cbc.weightx = 1
    cbc.fill = GridBagConstraints.HORIZONTAL
    devComponent.add(exportPanel, cbc)
    cbc.gridy = 1
    cbc.ipady = DevViewActor.Width / 3
    devComponent.add(componentSpot, cbc)

    devComponent.asInstanceOf[RightClickMenuComponent].addTwoLevelItems("programs", programs.map(p => {
      p._1 -> (() => dev ! I.MsgUpdateProgram(id, p._2))
    }))
    devComponent.asInstanceOf[RightClickMenuComponent].addTwoLevelItems("source", Map(
      "true" -> (() => dev ! I.MsgLocalSensorValue("source", true)),
      "false" -> (() => dev ! I.MsgLocalSensorValue("source", false))))
  }

  protected def updateGuiRef(ref: ActorRef): Unit = {
    devsGUIActor = ref
    devsGUIActor ! I.MsgAddDevComponent(dev, devComponent)
  }

  protected def updateId(id: I.ID): Unit = invokeLater {
    this.id = id
    lId.setText("id=" + id.toString)
    devsGUIActor ! I.MsgDevName(dev, id)
  }

  protected def updateExport(export: I.EXPORT): Unit = invokeLater {
    if (export.root().isInstanceOf[Double]) {
      val exp = export.root().asInstanceOf[Double]
      lExport.setText("ex:" + (
        if (exp > Int.MaxValue) {
          Double.PositiveInfinity
        } else {
          BigDecimal(exp).setScale(2, BigDecimal.RoundingMode.HALF_EVEN).toDouble
        }))
    } else {
      lExport.setText("ex:" + export.root().toString)
    }
  }

  protected def updateSensor(sensorName: I.LSensorName, sensorValue: Any): Unit = invokeLater {
    if (sensorName == "LOCATION_SENSOR") {
      val pos = sensorValue.asInstanceOf[Point2D]
      lExport.setToolTipText("pos:(" + pos.x.toInt + "," + pos.y.toInt + ")")
      devComponent.setBounds(pos.x.toInt * DevViewActor.Width, pos.y.toInt * DevViewActor.Height,
        DevViewActor.Width, DevViewActor.Height)
      devsGUIActor ! I.MsgDevPosition(dev, pos)
    } else if (sensorName.toString == "source") {
      componentSpot.asInstanceOf[CircularPanel].circleColor =
        if (sensorValue.asInstanceOf[Boolean]) { Color.RED } else { Color.ORANGE }
    }
  }

  protected def invokeLater(body: =>Any): Unit = {
    SwingUtilities.invokeLater(new Runnable {
      override def run(): Unit = {
        body
        devComponent.revalidate()
        devComponent.repaint()
      }
    })
  }
}

object DevViewActor {
  val Width: Int = 70
  val Height: Int = 70
  private val TextSize: Int = 11
  val DevicesInRow: Int = (Toolkit.getDefaultToolkit.getScreenSize.getWidth / Width).toInt
}
