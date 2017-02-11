package it.unibo.scafi.simulation.gui.view

import java.awt._
import javax.swing._

import it.unibo.scafi.simulation.gui.controller.Controller
import it.unibo.scafi.simulation.gui.utility.Utils
import it.unibo.scafi.simulation.gui.Settings
import it.unibo.scafi.simulation.gui.model.implementation.SensorEnum

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
  private[view] val controller: Controller = Controller.getIstance

  override protected def paintComponent(g: Graphics) {
    super.paintComponent(g)
    this.removeAll()
    //g.setColor(Settings.Color_device)
    //call the neighborhood to the network object

    controller.getNodes.foreach(ng => {
      val (n,gn) = ng
      val p1 = Utils.calculatedGuiNodePosition(n.position)
      val p1x = (p1.x + (Utils.getSizeGuiNode().getWidth() / 2))
      val p1y = (p1.y + (Utils.getSizeGuiNode().getHeight() / 160 * 71 ))
      //println(n.id,n.getSensorValue(SensorEnum.SENS1.name).asInstanceOf[Tuple2[Any,Boolean]]._2)
      //controller.getSensorValueForNode(SensorEnum.SENS3.name, n).map(_==true).getOrElse(false) // too expensive
      val color = if (n.getSensorValue(SensorEnum.SENS1.name)==true) Settings.Color_device1 else
                  if (n.getSensorValue(SensorEnum.SENS2.name)==true) Settings.Color_device2 else
                  if (n.getSensorValue(SensorEnum.SENS3.name)==true) Settings.Color_device3 else Settings.Color_device
      g.setColor(color)
      g.fillOval(p1x.toInt-5,p1y.toInt-5,10,10)
      if (gn!=null && gn.getValueToShow()!=null)
        g.drawChars(gn.getValueToShow().toCharArray,0,gn.getValueToShow().length,p1x.toInt,p1y.toInt-10)
    })
  }
}