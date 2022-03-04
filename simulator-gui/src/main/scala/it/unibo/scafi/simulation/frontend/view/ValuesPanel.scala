/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package it.unibo.scafi.simulation.frontend.view

import java.awt._
import javax.swing._

import it.unibo.scafi.simulation.frontend.Settings
import it.unibo.scafi.simulation.frontend.controller.Controller
import it.unibo.scafi.simulation.frontend.model.implementation.SensorEnum
import it.unibo.scafi.simulation.frontend.utility.Utils
import it.unibo.scafi.space.Point3D.toPoint2D

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

  override protected def paintComponent(g: Graphics): Unit = {
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

      if(controller.getObservation.apply(n.export)) color = Settings.Color_observation

      var dim = (getWidth/Settings.Size_Device_Relative).min(getHeight/Settings.Size_Device_Relative)

      if(Settings.Sim_Draw_Sensor_Radius){
        g.setColor(color)
        g.drawOval(p1x.toInt- (Settings.Sim_Sensor_Radius * Utils.getFrameDimension().getWidth()).toInt,
          p1y.toInt-(Settings.Sim_Sensor_Radius * Utils.getFrameDimension().getHeight).toInt,
          (Settings.Sim_Sensor_Radius * Utils.getFrameDimension().getWidth() * 2).toInt,
          (Settings.Sim_Sensor_Radius * Utils.getFrameDimension().getHeight() * 2).toInt)
      }

      var vector: (Double,Double) = Try(Settings.Movement_Activator(n.export).asInstanceOf[(Double,Double)]) getOrElse((0.0,0.0))
      if(vector._1 != 0.0 || vector._2 != 0.0){
        var angle = math.atan2(vector._2, vector._1) * 180.0 / math.Pi

        g.setColor(color)

        drawDrone(g, angle, p1x.toInt, p1y.toInt)

      } else {
        if (Try(Settings.Led_Activator(n.export).asInstanceOf[Boolean]) getOrElse false) {
          g.setColor(Settings.Color_actuator)
          g.fillOval(p1x.toInt-dim*10/16,p1y.toInt-dim*10/16,dim*10/8,dim*10/8)
          g.setColor(color)
          g.drawOval(p1x.toInt-dim*10/16,p1y.toInt-dim*10/16,dim*10/8,dim*10/8)
          g.fillOval(p1x.toInt-dim*6/16,p1y.toInt-dim*6/16,dim*6/8,dim*6/8)
        } else {
          g.setColor(color)
          g.fillOval(p1x.toInt - dim / 2, p1y.toInt - dim / 2, dim, dim)
        }
      }

      if (gn!=null && gn.getValueToShow()!=null) {
        val toShow = if (Settings.To_String!=null) Settings.To_String(n.export) else gn.getValueToShow()
        g.drawChars(toShow.toCharArray, 0, toShow.length, p1x.toInt, p1y.toInt - 10)
      }
    })
  }

  def drawDrone(g: Graphics, angle: Double, x: Int, y: Int): Unit = {
    var dist = 9
    var newX1 = x + dist * (math.cos(angle * math.Pi / 180.0))
    var newY1 = y + dist * (math.sin(angle * math.Pi / 180.0))

    var newX2 = x + dist * (math.cos((angle - 150) * math.Pi / 180.0))
    var newY2 = y + dist * (math.sin((angle - 150) * math.Pi / 180.0))

    var newX3 = x + dist/3 * (math.cos((angle + 180) * math.Pi / 180.0))
    var newY3 = y + dist/3 * (math.sin((angle + 180) * math.Pi / 180.0))

    var newX4 = x + dist * (math.cos((angle + 150) * math.Pi / 180.0))
    var newY4 = y + dist * (math.sin((angle + 150) * math.Pi / 180.0))

    var xx = Array[Int](newX1.toInt, newX2.toInt, newX3.toInt, newX4.toInt)
    var yy = Array[Int](newY1.toInt, newY2.toInt, newY3.toInt, newY4.toInt)

    g.fillPolygon(xx, yy, 4)
  }

  def increaseFontSize(): Unit = {
    this.nodeLabelFont = nodeLabelFont.deriveFont(nodeLabelFont.getSize2D+1)
    updateFont(this.nodeLabelFont)
  }

  def decreaseFontSize(): Unit = {
    this.nodeLabelFont = nodeLabelFont.deriveFont(nodeLabelFont.getSize2D-1)
    updateFont(this.nodeLabelFont)
  }

  private def updateFont(font: Font): Unit ={
    this.setFont(font)
  }
}
