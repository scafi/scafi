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

package it.unibo.scafi.simulation.gui.utility

import javax.swing._
import java.awt._
import java.awt.geom.Point2D
import java.awt.image.BufferedImage
import java.io.File
import java.net.URL
import java.nio.file.Paths

/**
  * This class handles the images update, the component dimension and the component position.
  */
object Utils {
  private var frameDimension: Dimension = Toolkit.getDefaultToolkit.getScreenSize
  val jpeg: String = "jpeg"
  val jpg: String = "jpg"
  val gif: String = "gif"
  val tiff: String = "tiff"
  val tif: String = "tif"
  val png: String = "png"

  val klass = this.getClass

  def parseSensorValue(str: String) = {
    val sensorValueParts = str.split(" ", 2)

    var (sensorType,sensorValueStr) = if(sensorValueParts.length==1)
      ("string",sensorValueParts(0))
    else
      (sensorValueParts(0).toLowerCase, sensorValueParts(1))

    sensorType match {
      case "bool" => sensorValueStr.toBoolean
      case "int" => sensorValueStr.toInt
      case "double" => sensorValueStr.toDouble
      case _ => sensorValueStr
    }
  }

  def parseValue(str: String): (String,Any) = {
    val valueParts = str.split(" ", 2)

    var (valueType,valueStr) = if(valueParts.length==1)
      ("string",valueParts(0))
    else
      (valueParts(0).toLowerCase, valueParts(1))

    (valueType, valueType match {
      case "bool" => valueStr.toBoolean
      case "int" => valueStr.toInt
      case "double" => valueStr.toDouble
      case _ => valueStr
    })
  }

  def parseSensors(str: String): Map[String,Any] = {
    if (str==null || str == "") return Map()
    val sensorStrings = str.split("\\n")
    sensorStrings.map(parseSensor(_)).toMap
  }

  def parseSensor(str: String): (String,Any) = {
    val sensorStrParts = str.split(" ", 2)
    sensorStrParts(0) -> parseSensorValue(sensorStrParts(1))
  }

  /*
      * Get the extension of a file.
      */ def getExtension(f: File): String = {
    var ext: String = null
    val s: String = f.getName
    val i: Int = s.lastIndexOf('.')
    if (i > 0 && i < s.length - 1) {
      ext = s.substring(i + 1).toLowerCase
    }
    return ext
  }

  /** Returns an ImageIcon, or null if the path was invalid. */
  def createImageIcon(path: String): ImageIcon = {
    val imgURL: URL = (klass.getResource("/" + path))
    if (imgURL != null) {
      return new ImageIcon(imgURL)
    }
    else {
      System.err.println("Couldn't find file: " + path)
      return null
    }
  }

  def getScaledImage(srcImg: String, w: Int, h: Int): ImageIcon = {
    val i: ImageIcon = new ImageIcon(klass.getResource("/" + srcImg).getPath)
    val resizedImg: BufferedImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)
    val g2: Graphics2D = resizedImg.createGraphics
    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
    g2.drawImage(i.getImage, 0, 0, w, h, null)
    g2.dispose()
    val res: ImageIcon = new ImageIcon(resizedImg)
    res.setDescription(srcImg)
    return res
  }

  /**
    * immagine "selezionata"
    *
    * @param icon
    * @return
    */
  def getSelectedIcon(icon: Icon): ImageIcon = {
    val nameIcon: String = Paths.get((icon.asInstanceOf[ImageIcon]).getDescription).getFileName.toString
    if (nameIcon == "source.png" || nameIcon == "sourceSelect.png") {
      return getScaledImage("sourceSelect.png", icon.getIconWidth, icon.getIconHeight)
    }
    else if (nameIcon == "sensorOk.png" || nameIcon == "sensorOkSelect.png") {
      return getScaledImage("sensorOkSelect.png", icon.getIconWidth, icon.getIconHeight)
    }
    else {
      return getScaledImage("nodeSelect.png", icon.getIconWidth, icon.getIconHeight)
    }
  }

  /**
    * immagine "non selezionata"
    *
    * @param icon
    * @return
    */
  def getNotSelectIcon(icon: Icon): ImageIcon = {
    val nameIcon: String = Paths.get((icon.asInstanceOf[ImageIcon]).getDescription).getFileName.toString
    if (nameIcon == "sourceSelect.png" || nameIcon == "source.png") {
      return getScaledImage("source.png", icon.getIconWidth, icon.getIconHeight)
    }
    else if (nameIcon == "sensorOkSelect.png" || nameIcon == "sensorOk.png") {
      return getScaledImage("sensorOk.png", icon.getIconWidth, icon.getIconHeight)
    }
    else {
      return getScaledImage("node.png", icon.getIconWidth, icon.getIconHeight)
    }
  }

  /**
    * setta la dimensione del frame dell'applicazione
    *
    * @param d
    */
  def setDimensionFrame(d: Dimension) {
    frameDimension = d
  }

  /**
    *
    * @return dimensione del frame dell'applicazione
    */
  def getFrameDimension(): Dimension = {
    return frameDimension
  }

  /**
    * @return GuiNode Dimension 5% FrameWidth and 10% FrameHeight
    */
  def getSizeGuiNode(): Dimension = {
    return new Dimension((frameDimension.width * 10 / 100), (frameDimension.height * 10 / 100))
  }

  /**
    * @return Configuration Panel Dimension 50% Fame Dimension
    */
  def getConfPanelDim(): Dimension = {
    return new Dimension((Toolkit.getDefaultToolkit.getScreenSize.width / 2), (Toolkit.getDefaultToolkit.getScreenSize.height / 2))
  }

  /**
    * @return Menu panel of Simulation Panel Dimension
    */
  def getMenuSimulationPanelDim(): Dimension = {
    return new Dimension((frameDimension.width * 10 / 100), (frameDimension.height * 20 / 100))
  }

  /**
    * @return Menu panel of Simulation Panel Dimension
    */
  def getGuiNodeInfoPanelDim(): Dimension = {
    return new Dimension((frameDimension.width * 10 / 100), (frameDimension.height * 20 / 100))
  }

  /**
    * @return IconMenu Dimension
    */
  def getIconMenuDim(): Dimension = {
    return new Dimension((frameDimension.width * 1.5 / 100).toInt, (frameDimension.height * 2 / 100))
  }

  /**
    *
    * @param position
    * @return calcola la posizione del GuiNode
    */
  def calculatedGuiNodePosition(position: Point2D): Point = {
    // position.x : 1 = res.x : frame.getWidth();
    val res: Point = new Point
    res.x = (position.getX * (frameDimension.getWidth - getSizeGuiNode.getWidth)).toInt // Placing at the center of the frame
    res.y = (position.getY * (frameDimension.getHeight - getSizeGuiNode.getHeight)).toInt
    return res
  }

  /**
    *
    * @param position
    * @return calcola la posizione del nodo del model
    */
  def calculatedNodePosition(position: Point): Point2D = {
    return new Point2D.Double(position.getX / (getFrameDimension.getWidth - getSizeGuiNode.getWidth), position.getY / (getFrameDimension.getHeight - getSizeGuiNode.getHeight))
  }
}
