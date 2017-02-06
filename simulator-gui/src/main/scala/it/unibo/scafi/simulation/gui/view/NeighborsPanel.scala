package it.unibo.scafi.simulation.gui.view

import it.unibo.scafi.simulation.gui.controller.Controller
import it.unibo.scafi.simulation.gui.utility.Utils
import javax.swing._
import java.awt._

/**
  * This is the panel where are represents
  * the connection of neighbors.
  * Created by Varini.
  * Converted/refactored to Scala by Casadei on 3/02/17
  */
class NeighborsPanel private[view]() extends JPanel {
  this.setSize(Toolkit.getDefaultToolkit.getScreenSize)
  this.setOpaque(false)
  this.setVisible(false)
  private[view] val controller: Controller = Controller.getIstance

  override protected def paintComponent(g: Graphics) {
    super.paintComponent(g)
    this.removeAll()
    g.setColor(Color.black)
    //call the neighborhood to the network object
    controller.getNeighborhood.foreach(kv => {
      val (n, nghb) = kv
      val p1 = Utils.calculatedGuiNodePosition(n.position)
      val p1x = (p1.x + (Utils.getSizeGuiNode().getWidth() / 2))
      val p1y = (p1.y + (Utils.getSizeGuiNode().getHeight() / 3 * 2))
      nghb.foreach(ng => {
        val p2 = Utils.calculatedGuiNodePosition(ng.position)
        val p2x =  (p2.x + (Utils.getSizeGuiNode().getWidth() / 2))
        val p2y = (p2.y + (Utils.getSizeGuiNode().getHeight() / 3 * 2))
        g.drawLine(p1x.toInt, p1y.toInt, p2x.toInt, p2y.toInt)
      })
    })
  }
}