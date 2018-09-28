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

package examples

import java.awt.{Color, Component, Graphics, Point}
import java.awt.event.MouseEvent

import javax.swing.{JComponent, JPanel}
import javax.swing.event.MouseInputAdapter

package object gui {
  trait DraggableComponent extends JComponent {
    protected var anchorPoint: Point = _
    private val mouseLis = new MouseInputAdapter {
      override def mousePressed(e: MouseEvent): Unit = anchorPoint = e.getPoint
      override def mouseDragged(e: MouseEvent): Unit = onDragging(e.getLocationOnScreen)
      override def mouseReleased(e: MouseEvent): Unit = afterDragging(e.getSource.asInstanceOf[Component].getLocationOnScreen)
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

  class CircularPanel(var circleColor: Color = Color.WHITE) extends JPanel {
    override def paintComponent(g: Graphics): Unit = {
      super.paintComponent(g)
      g.setColor(circleColor)
      g.fillOval((getWidth - getHeight) / 2, 0, getHeight, getHeight)
    }
  }
}
