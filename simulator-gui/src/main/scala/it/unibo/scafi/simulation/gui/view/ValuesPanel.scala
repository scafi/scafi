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

package it.unibo.scafi.simulation.gui.view

import java.awt._
import javax.swing._

import it.unibo.scafi.simulation.gui.controller.Controller
import it.unibo.scafi.simulation.gui.utility.Utils
import it.unibo.scafi.simulation.gui.Settings
import it.unibo.scafi.simulation.gui.model.implementation.SensorEnum

import scala.util.Try

/**
  * This is the panel where are represents the connection of neighbors.
  */
class ValuesPanel private[view]() extends JPanel {
  private var nodeLabelFont: Font = new Font("Arial", Font.BOLD, 14)

  this.setSize(Toolkit.getDefaultToolkit.getScreenSize)
  this.setOpaque(false)
  this.setVisible(true)
  private[view] val controller: Controller = Controller.getInstance

  override protected def paintComponent(g: Graphics) {
    super.paintComponent(g)
    this.removeAll()
    //g.setColor(Settings.Color_device)
    //call the neighborhood to the network object

    controller.getNodes.foreach(ng => {
      val (n,gn) = ng
      val p1 = Utils.calculatedGuiNodePosition(n.position)
      val (dx,dy) = (Utils.getSizeGuiNode().getWidth() / 2, Utils.getSizeGuiNode().getHeight() / 160 * 71)
      val p1x = (p1.x + dx)
      val p1y = (p1.y + dy)
      //println(n.id,n.getSensorValue(SensorEnum.SENS1.name).asInstanceOf[Tuple2[Any,Boolean]]._2)
      //controller.getSensorValueForNode(SensorEnum.SENS3.name, n).map(_==true).getOrElse(false) // too expensive
      var color = if (n.getSensorValue(SensorEnum.SENS1.name)==true) Settings.Color_device1 else
                  if (n.getSensorValue(SensorEnum.SENS2.name)==true) Settings.Color_device2 else
                  if (n.getSensorValue(SensorEnum.SENS3.name)==true) Settings.Color_device3 else
                  if (n.getSensorValue(SensorEnum.SENS4.name)==true) Settings.Color_device4 else Settings.Color_device

      if(controller.getObservation()(n.export)) color = Settings.Color_observation

      var dim = (getWidth/Settings.Size_Device_Relative).min(getHeight/Settings.Size_Device_Relative)
      if (Try(Settings.Led_Activator(n.export).asInstanceOf[Boolean]) getOrElse false) {
        //println("in!!")
        g.setColor(Settings.Color_actuator)
        g.fillOval(p1x.toInt-dim*10/16,p1y.toInt-dim*10/16,dim*10/8,dim*10/8)
        g.setColor(color)
        g.drawOval(p1x.toInt-dim*10/16,p1y.toInt-dim*10/16,dim*10/8,dim*10/8)
        g.fillOval(p1x.toInt-dim*6/16,p1y.toInt-dim*6/16,dim*6/8,dim*6/8)
      } else {
        g.setColor(color)
        g.fillOval(p1x.toInt - dim / 2, p1y.toInt - dim / 2, dim, dim)
      }
      if (gn!=null && gn.getValueToShow()!=null) {
        val toShow = if (Settings.To_String!=null) Settings.To_String(n.export) else gn.getValueToShow()
        //println()
        g.drawChars(toShow.toCharArray, 0, toShow.length, p1x.toInt, p1y.toInt - 10)
      }
    })
  }

  def increaseFontSize() {
    this.nodeLabelFont = nodeLabelFont.deriveFont(nodeLabelFont.getSize2D+1)
    updateFont(this.nodeLabelFont)
  }

  def decreaseFontSize() {
    this.nodeLabelFont = nodeLabelFont.deriveFont(nodeLabelFont.getSize2D-1)
    updateFont(this.nodeLabelFont)
  }

  private def updateFont(font: Font): Unit ={
    this.setFont(font)
  }
}
