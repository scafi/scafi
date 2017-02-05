package it.unibo.scafi.simulation.gui.view

import it.unibo.scafi.simulation.gui.controller.Controller
import java.awt._
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent

/**
  * This class represent an ActionListener
  * for a GuiNode
  * Created by Varini on 03/06/16.
  * Converted/refactored to Scala by Casadei on 04/02/17
  */
class GuiNodeListeners private[view](val node: GuiNode) extends MouseAdapter {
  final private val controller: Controller = Controller.getIstance
  final private val p: Point = new Point

  //apre il pannello delle informazioni
  override def mouseClicked(e: MouseEvent) {
    super.mouseClicked(e)
    if (e.getButton == MouseEvent.BUTTON3) {
      repositionsInfoPanel()
    }
  }

  //cattura il punto da cui parte il GuiNode
  override def mousePressed(e: MouseEvent) {
    super.mousePressed(e)
    if (!e.isMetaDown) {
      p.x = e.getX
      p.y = e.getY
    }
  }

  //sposta il GuiNode
  override def mouseDragged(e: MouseEvent) {
    super.mouseDragged(e)
    if (!e.isMetaDown) {
      val pos: Point = node.getLocation
      node.setLocation(pos.x + e.getX - p.x, pos.y + e.getY - p.y)
      if (node.getInfoPanel != null) {
        repositionsInfoPanel()
      }
      controller.moveNode(node, pos)
    }
  }

  /*riposizione il pannello delle informazioni
     basandosi sulla posizione del GuiNode nell schermo*/
  private def repositionsInfoPanel() {
    controller.showInfoPanel(node, true)
    val infoP: NodeInfoPanel = node.getInfoPanel
    val screen: Dimension = Toolkit.getDefaultToolkit.getScreenSize
    if (node.getX > screen.getWidth / 2) {
      infoP.setLocation(node.getX - infoP.getSize().width, node.getY) //se è nella parte destra del monitor apro le info a sinistra
    }
    else {
      infoP.setLocation(node.getX + node.getSize().width, node.getY) //altrimenti a destra
    }
    if (node.getY > (screen.getHeight / 1.5)) {
      infoP.setLocation(node.getX, node.getY - infoP.getHeight) //se è nella parte bassa lo apro sopra
    }
  }
}