/*
 * Copyright (C) 2016-2019, Roberto Casadei, Mirko Viroli, and contributors.
 * See the LICENSE file distributed with this work for additional information regarding copyright ownership.
*/

package examples

import java.awt.{Color, Component, Graphics, Point}
import java.awt.event.{ActionEvent, ActionListener, MouseEvent}

import javax.swing._
import javax.swing.event.MouseInputAdapter

package object gui {
  trait DraggableComponent extends JComponent {
    protected var anchorPoint: Point = _
    private val mouseLis = new MouseInputAdapter {
      override def mousePressed(e: MouseEvent): Unit = {
        if (SwingUtilities.isLeftMouseButton(e)) anchorPoint = e.getPoint
      }
      override def mouseDragged(e: MouseEvent): Unit = {
        if (SwingUtilities.isLeftMouseButton(e)) onDragging(e.getLocationOnScreen)
      }
      override def mouseReleased(e: MouseEvent): Unit = {
        if (SwingUtilities.isLeftMouseButton(e)) afterDragging(e.getSource.asInstanceOf[Component].getLocationOnScreen)
      }
    }
    addMouseListener(mouseLis)
    addMouseMotionListener(mouseLis)

    protected def onDragging(location: Point): Unit = {
      val parentLocation = getParent.getLocationOnScreen
      setLocation(new Point(location.x - parentLocation.x - anchorPoint.x, location.y - parentLocation.y - anchorPoint.y))
      getParent.setComponentZOrder(DraggableComponent.this, 0)
    }
    protected def afterDragging(location: Point): Unit = {}
  }

  trait RightClickMenuComponent extends JComponent {
    val menu = new JPopupMenu("Programs")

    def addItems(entries: Map[String, ()=>Unit]): Unit = {
      entries.foreach(entry => {
        val mItem = new JMenuItem(entry._1)
        mItem.addActionListener(new ActionListener {
          override def actionPerformed(e: ActionEvent): Unit = entries(e.getActionCommand)()
        })
        menu.add(mItem)
      })
    }

    def addTwoLevelItems(commonLabel: String, entries: Map[String, ()=>Unit]): Unit = {
      val mainItem = new JMenu(commonLabel)
      entries.foreach(entry => {
        val subItem = new JMenuItem(entry._1)
        subItem.addActionListener(new ActionListener {
          override def actionPerformed(e: ActionEvent): Unit = entries(e.getActionCommand)()
        })
        mainItem.add(subItem)
      })
      menu.add(mainItem)
    }

    private val mouseLis = new MouseInputAdapter {
      override def mousePressed(e: MouseEvent): Unit = {
        if (SwingUtilities.isRightMouseButton(e)) { menu.show(e.getComponent, e.getX, e.getY) }
      }
    }
    addMouseListener(mouseLis)
    addMouseMotionListener(mouseLis)
  }

  class CircularPanel(var circleColor: Color = Color.WHITE) extends JPanel {
    override def paintComponent(g: Graphics): Unit = {
      super.paintComponent(g)
      g.setColor(circleColor)
      g.fillOval((getWidth - getHeight) / 2, 0, getHeight, getHeight)
    }
  }
}
