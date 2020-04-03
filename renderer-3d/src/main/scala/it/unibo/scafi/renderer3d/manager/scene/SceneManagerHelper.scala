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

import java.awt.Image
import java.awt.image.BufferedImage

/** Helper object for [[SceneManager]] with various utility methods. */
import javafx.scene.input.{MouseButton, MouseEvent}

private[scene] object SceneManagerHelper {

  /** Converts a java.awt.Image to BufferedImage.
   * Code From https://stackoverflow.com/questions/13605248/java-converting-image-to-bufferedimage
   * @param image the image to convert
   * @return the converted buffered image */
  def toBufferedImage(image: Image): BufferedImage = {
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

  /** Checks that the event source is the primary button of the mouse.
   * @param event the mouse event
   * @return whether the event is a primary button event */
  def isPrimaryButton(event: MouseEvent): Boolean = event.getButton == MouseButton.PRIMARY

  /** Checks that the event source is the middle button of the mouse.
   * @param event the mouse event
   * @return whether the event is a middle button event */
  def isMiddleMouse(event: MouseEvent): Boolean = event.getButton == MouseButton.MIDDLE

}
