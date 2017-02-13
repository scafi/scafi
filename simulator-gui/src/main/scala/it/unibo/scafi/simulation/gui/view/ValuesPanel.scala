package it.unibo.scafi.simulation.gui.view

import java.awt._
import javax.swing._

import it.unibo.scafi.simulation.gui.controller.Controller
import it.unibo.scafi.simulation.gui.utility.Utils
import it.unibo.scafi.simulation.gui.Settings
import it.unibo.scafi.simulation.gui.model.implementation.SensorEnum

import scala.util.Try

/**
  * This is the panel where are represents
  * the connection of neighbors.
  * Created by Varini.
  * Converted/refactored to Scala by Casadei on 3/02/17
  */
class ValuesPanel private[view]() extends JPanel {
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
      val color = if (n.getSensorValue(SensorEnum.SENS1.name)==true) Settings.Color_device1 else
                  if (n.getSensorValue(SensorEnum.SENS2.name)==true) Settings.Color_device2 else
                  if (n.getSensorValue(SensorEnum.SENS3.name)==true) Settings.Color_device3 else Settings.Color_device
      var dim = (getWidth/Settings.Size_Device_Relative).min(getHeight/Settings.Size_Device_Relative)
      g.fillOval(p1x.toInt-dim/2,p1y.toInt-dim/2,dim,dim)
      println(n.export)
      if (Try(Settings.Led_Activator(n.export).asInstanceOf[Boolean]) getOrElse false) {
        println("in!!")
        g.setColor(Settings.Color_actuator);
        g.fillOval(p1x.toInt-dim,p1y.toInt-dim,dim*2,dim*2)
      }
      g.setColor(color)
      g.fillOval(p1x.toInt-dim/2,p1y.toInt-dim/2,dim,dim)
      if (gn!=null && gn.getValueToShow()!=null) {
        val toShow = if (Settings.To_String!=null) Settings.To_String(n.export) else gn.getValueToShow()
        println()
        g.drawChars(toShow.toCharArray, 0, toShow.length, p1x.toInt, p1y.toInt - 10)
      }
    })
  }
}