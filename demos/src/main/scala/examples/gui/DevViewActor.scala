/*
 * Copyright (C) 2016-2017, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENCE.txt file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package examples.gui

import akka.actor.{Actor, ActorRef, Props}
import it.unibo.scafi.distrib.actor.MsgAddObserver
import it.unibo.scafi.incarnations.BasicAbstractActorIncarnation
import it.unibo.scafi.space.Point2D
import javax.swing._
import java.awt._

import it.unibo.scafi.distrib.actor.view.{DevComponent, MsgAddDevComponent, MsgDevsGUIActor}

class DevViewActor(val I: BasicAbstractActorIncarnation, private var dev: ActorRef) extends Actor {
  protected val Log = akka.event.Logging(context.system, this)
  private val TextSize: Int = 11
  private val CustomFont: Font = new Font("Arial", Font.BOLD, TextSize)

  private var devComponent: DevPanel = _
  private var componentSpot: JComponent = _
  private var lId, lExport: JLabel = _

  dev ! MsgAddObserver(self)
  buildComponent()

  override def receive: Receive = {
    case g: MsgDevsGUIActor => g.devsGuiActor ! MsgAddDevComponent(devComponent)
    case m: I.MyNameIs => updateId(m.id)
    case m: I.MsgLocalSensorValue[_] => updateSensor(m.name, m.value)
    case p: I.MsgExport => updateExport(p.export)
    case msg => Log.debug("[DevGUIActor_id=+" + lId.getText + "] Message unhandled: " + msg); unhandled(msg)
  }

  private def buildComponent(): Unit = {
    devComponent = new DevPanel with DraggableComponent {
      override protected def afterDragging(location: Point): Unit = {
        val pos = Point2D((location.getX / 83).round, (location.getY / 100).round)
        dev ! I.MsgLocalSensorValue("LOCATION_SENSOR", pos)
      }
    }
    devComponent.setLayout(new GridBagLayout())
    devComponent.setVisible(true)

    val exportPanel = new JPanel()
    lExport = new JLabel("export")
    lExport.setFont(CustomFont)
    exportPanel.add(lExport)

    componentSpot = new CircularPanel(Color.ORANGE)
    componentSpot.setLayout(new GridBagLayout())
    lId = new JLabel("id")
    lId.setFont(CustomFont)
    componentSpot.add(lId)

    val cbc = new GridBagConstraints()
    cbc.weightx = 1
    cbc.fill = GridBagConstraints.HORIZONTAL
    devComponent.add(exportPanel, cbc)
    cbc.gridy = 1
    cbc.ipady = 30
    devComponent.add(componentSpot, cbc)
  }

  private def updateId(id: I.ID): Unit = invokeLater {
    lId.setText("id=" + id.toString)
    devComponent.id = id
  }

  private def updateExport(export: I.EXPORT): Unit = invokeLater {
    lExport.setText("ex: " + s"${export.root[Double]().toInt}")
    devComponent.export = export
  }

  private def updateSensor(sensorName: I.LSensorName, sensorValue: Any): Unit = invokeLater {
    if (sensorName == "LOCATION_SENSOR") {
      val pos = sensorValue.asInstanceOf[Point2D]
      this.devComponent.setBounds(pos.x.toInt * 83, pos.y.toInt * 100,80,75)
    } else if (sensorName.toString == "source" && sensorValue == true) {
      componentSpot.asInstanceOf[CircularPanel].circleColor = Color.RED
    }
    devComponent.sensors = devComponent.sensors + (sensorName -> sensorValue)
  }

  private def invokeLater(body: =>Any): Unit = {
    SwingUtilities.invokeLater(() => {
      body
      devComponent.revalidate(); devComponent.repaint()
    })
  }

  private class DevPanel extends JPanel with DevComponent {
    override type ID = I.ID
    override type EXPORT = I.EXPORT
    override type LSNS = I.LSNS
  }
}

object DevViewActor {
  def props(inc: BasicAbstractActorIncarnation, devActorRef: ActorRef): Props = Props(classOf[DevViewActor], inc, devActorRef)
}
