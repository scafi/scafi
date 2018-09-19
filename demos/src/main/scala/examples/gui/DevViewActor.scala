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

class DevViewActor(val I: BasicAbstractActorIncarnation, private var dev: ActorRef) extends Actor {
  protected val Log = akka.event.Logging(context.system, this)
  private val TextSize: Int = 11
  private val CustomFont: Font = new Font("Arial", Font.BOLD, TextSize)

  private var devComponent, componentSpot: JComponent = _
  private var lId, lExport: JLabel = _

  dev ! MsgAddObserver(self)
  buildComponent()

  override def receive: Receive = {
    case gui: ActorRef => gui ! devComponent
    case m: I.MyNameIs => invokeLater { lId.setText("id: " + m.id.toString) }
    case m: I.MsgLocalSensorValue[_] => invokeLater {
      if (m.name == "LOCATION_SENSOR") {
        val pos = m.value.asInstanceOf[Point2D]
        this.devComponent.setBounds(pos.x.toInt * 83, pos.y.toInt * 100,80,75)
      } else if (m.name.toString == "source" && m.value == true) {
        componentSpot.asInstanceOf[CircularPanel].circleColor = Color.RED
      }
    }
    case p: I.MsgExport => invokeLater { lExport.setText("ex: " + s"${p.export.root[Double]().toInt}") }
    case msg => Log.debug("[DevGUIActor_id=+" + lId.getText + "] Message unhandled: " + msg); unhandled(msg)
  }

  private def buildComponent(): Unit = {
    devComponent = new JPanel() with DraggableComponent {
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

  private def invokeLater(body: =>Any): Unit = {
    SwingUtilities.invokeLater(() => {
      body
      devComponent.revalidate(); devComponent.repaint()
    })
  }
}

object DevViewActor {
  def props(inc: BasicAbstractActorIncarnation, devActorRef: ActorRef): Props = Props(classOf[DevViewActor], inc, devActorRef)
}
