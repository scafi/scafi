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

package it.unibo.scafi.renderer3d.manager.scene

import java.awt.image.BufferedImage
import java.awt.{Color, Image}

import it.unibo.scafi.renderer3d.camera.{FpsCamera, SimulationCamera}
import it.unibo.scafi.renderer3d.manager.node.NodeManager
import it.unibo.scafi.renderer3d.manager.selection.SelectionManager
import it.unibo.scafi.renderer3d.util.Rendering3DUtils
import it.unibo.scafi.renderer3d.util.RichScalaFx._
import javafx.beans.{InvalidationListener, Observable}
import javafx.scene.input.{MouseButton, MouseEvent}
import javafx.scene.paint.ImagePattern
import javafx.stage.Window
import org.scalafx.extras.onFX
import scalafx.embed.swing.SwingFXUtils
import scalafx.geometry.Point3D
import scalafx.scene.transform.Rotate
import scalafx.scene.{Group, Scene, SceneAntialiasing}

/** Trait that contains some of the main API of the renderer-3d module regarding the scene. */
private[manager] trait SceneManager {
  this: NodeManager with SelectionManager => //NodeManager and SelectionManager have to also be mixed in

  private final val DEFAULT_SCENE_SIZE = 1000
  private final val simulationCamera: SimulationCamera = FpsCamera()
  protected val mainScene: Scene
  protected final var sceneSize = 1d

  /** Sets the scene's size and also the camera, connections and nodes scale accordingly, so that the nodes and
   * connections are visible. Setting this value correctly makes it possible to update the labels' position only when
   * needed. ATTENTION: big values will cause performance problems, while small values move the labels too far away from
   * the nodes, so a value of 1000 or so is ideal. This means that the 3d points should be positioned in a
   * 1000*1000*1000 space. This method has to be called before setting the scale of the nodes.
   * @param sceneSize it's the side's length of the imaginary cube that encloses the whole scene
   * @return Unit, since it has the side effect of changing the scene's size */
  final def setSceneSize(sceneSize: Double): Unit =
    onFX {this.sceneSize = sceneSize; mainScene.getCamera.setScale(sceneSize/10000)}

  protected final def sceneScaleMultiplier: Double = sceneSize / DEFAULT_SCENE_SIZE

  /** Sets the background image of the scene.
   * @param image the image to set as background
   * @return Unit, since it has the side effect of setting the scene's background image */
  final def setBackgroundImage(image: Image): Unit = onFX{
    //scalastyle:off null
    val javaFxImage = SwingFXUtils.toFXImage(toBufferedImage(image), null)
    mainScene.setFill(new ImagePattern(javaFxImage))
  }

  /** From https://stackoverflow.com/questions/13605248/java-converting-image-to-bufferedimage */
  private final def toBufferedImage(image: Image): BufferedImage = {
    //scalastyle:off null
    image match {case image: BufferedImage => image; case _ =>
      val bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null),
        BufferedImage.TYPE_INT_ARGB)
      val graphics2D = bufferedImage.createGraphics
      graphics2D.drawImage(image, 0, 0, null)
      graphics2D.dispose()
      bufferedImage
    }
  }

  /** Sets the background color of the scene.
   * @param color the color to set
   * @return Unit, since it has the side effect of setting the scene's color */
  final def setBackgroundColor(color: Color): Unit = onFX {mainScene.setFill(color.toScalaFx)}

  /** Resets the scene: this means deleting all the nodes and connections, obtaining an empty scene (the light and
   *  camera will still exist, though)
   * @return Unit, since it has the side effect of resetting the scene */
  final def resetScene(): Unit = onFX {
    getAllNetworkNodes.foreach(node => removeNode(node.UID))
    mainScene.getCamera.moveTo(Point3D.Zero)
    mainScene.getCamera.lookAtOnXZPlane(Rotate.XAxis)
  }

  /** Sets the camera scale to the new one, making it bigger or smaller. This is useful if the scene is not well visible
   * because the nodes are too big or small to be seen.
   * @param scale the scale to set */
  final def setCameraScale(scale: Double): Unit = mainScene.getCamera.setScale(scale)

  protected final def createScene(): Scene =
    new Scene(0, 0, true, SceneAntialiasing.Balanced) {
      camera = simulationCamera
      root = new Group(simulationCamera, Rendering3DUtils.createAmbientLight)
      simulationCamera.initialize(this,
        () => if(System.currentTimeMillis()%2==0) rotateNodeLabelsIfNeeded(simulationCamera))
      setMouseInteraction(this, simulationCamera)
    }

  protected final def stopMovingOnFocusLoss(window: Window): Unit  = {//call after initialization, so window is != null
    val listener = new InvalidationListener { //without this the camera would sometimes keep moving without user input
      override def invalidated(observable: Observable): Unit = simulationCamera.stopMovingAndRotating()
    }
    Seq(window.focusedProperty(), window.heightProperty(), window.widthProperty()).foreach(_.addListener(listener))
  }

  private[this] final def setMouseInteraction(scene: Scene, camera: SimulationCamera): Unit = {
    setMousePressedAndDragged(scene, camera)
    scene.setOnDragDetected(_ => scene.startFullDrag())
    scene.onMouseDragEntered = event => if(isPrimaryButton(event) && !isSelectionComplete) startSelection(event)
    scene.onMouseReleased = event => if(isPrimaryButton(event)) {
      endSelectionMovementIfNeeded(event)
      endSelection(event)
    }
  }

  private[this] final def setMousePressedAndDragged(scene: Scene, camera: SimulationCamera): Unit = {
    scene.setOnMousePressed(event => if(isPrimaryButton(event)) {
      if(isSelectionComplete && !movementComplete) setMousePosition(event) else setSelectionVolumeCenter(event)
    } else if(isMiddleMouse(event)) {
      camera.startMouseRotation(event)
    })
    scene.onMouseDragged = event =>
      if(isPrimaryButton(event)) {
        if(isSelectionComplete) moveSelectedNodesIfNeeded(camera, event) else modifySelectionVolumeIfNeeded(camera, event)
    } else if(isMiddleMouse(event)) {
      camera.rotateByMouseEvent(event)
    }
  }

  private[this] final def isPrimaryButton(event: MouseEvent): Boolean = event.getButton == MouseButton.PRIMARY

  private[this] final def isMiddleMouse(event: MouseEvent): Boolean = event.getButton == MouseButton.MIDDLE

}
