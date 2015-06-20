/*******************************************************************************
 * Copyright (c) 2013, Daniel Murphy
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package nars.rover.physics;

import com.google.common.base.Preconditions;
import org.jbox2d.common.IViewportTransform;
import org.jbox2d.common.Mat22;
import org.jbox2d.common.OBBViewportTransform;
import org.jbox2d.common.Vec2;

public class PhysicsCamera {
    private final OBBViewportTransform targetTransform;


  public static enum ZoomType {
    ZOOM_IN, ZOOM_OUT
  }

  private final Vec2 initPosition = new Vec2();
  private float initScale;

  private final OBBViewportTransform transform;

  private final Mat22 upScale;
  private final Mat22 downScale;
  float targetScale, scale;

    public float getTargetScale() {
        return targetScale;
    }

  
  public PhysicsCamera(Vec2 initPosition, float initScale, float zoomScaleDiff) {
    Preconditions.checkArgument(zoomScaleDiff > 0, "Zoom scale %d must be > 0", zoomScaleDiff);
    
    this.transform = new OBBViewportTransform();
    this.targetTransform = new OBBViewportTransform();
    this.targetScale = initScale;
    
    transform.setCamera(initPosition.x, initPosition.y, initScale);
    targetTransform.setCamera(initPosition.x, initPosition.y, initScale);
        
    //this.initPosition.set(initPosition);
    //this.initScale = initScale;
    upScale = Mat22.createScaleTransform(1 + zoomScaleDiff);
    downScale = Mat22.createScaleTransform(1 - zoomScaleDiff);
  }

  /**
   * Resets the camera to the initial position
   */
  public void reset() {
    //setCamera(initPosition, initScale);
  }

  /**
   * Sets the camera center position
   */
  public void setCamera(Vec2 worldCenter) {
    targetTransform.setCenter(worldCenter);
  }
  
  public void update(float dt) {
      float m = 0.8f; //center momentum
      float n = 1.0f - m;
      
      float ms = 0.99f; //scale momentum
      float ns = 1.0f - ms;
      
      float x = transform.getCenter().x;
      float y = transform.getCenter().y;
      float s = scale;
      float tx = targetTransform.getCenter().x;
      float ty = targetTransform.getCenter().y;
      float ts = targetScale;
      
      transform.setCamera( x * m + tx * n , y * m + ty * n, s * ms + ts * ns);
  }

  /**
   * Sets the camera center position and scale
   */
  public void setCamera(Vec2 worldCenter, float scale) {
    targetTransform.setCamera(worldCenter.x, worldCenter.y, scale);
  }

  private final Vec2 oldCenter = new Vec2();
  private final Vec2 newCenter = new Vec2();

  /**
   * Zooms the camera to a point on the screen. The zoom amount is given on camera initialization.
   */
  public void zoomToPoint(Vec2 screenPosition, ZoomType zoomType) {
    //Mat22 zoom;
    float scaleRate = 1f;
    switch (zoomType) {
      case ZOOM_IN:
        //zoom = upScale;
        targetScale += scaleRate;
        break;
      case ZOOM_OUT:
        //zoom = downScale;
        targetScale -= scaleRate;
        break;
      default:
        Preconditions.checkArgument(false, "Zoom type invalid");
        return;
    }

    //System.out.println(zoom + " "+ transform.getExtents() + " bef " + targetTransform.getExtents());

//    targetTransform.getScreenToWorld(screenPosition, oldCenter);
//    targetTransform.mulByTransform(zoom);
//    targetTransform.getScreenToWorld(screenPosition, newCenter);
//
//
//    Vec2 transformedMove = oldCenter.subLocal(newCenter);
//    // set, just in case bad impl by someone
//    if (!targetTransform.isYFlip()) {
//      transformedMove.y = -transformedMove.y;
//    }
    //targetTransform.setCenter(targetTransform.getCenter().addLocal(transformedMove));

    //targetTransform.setCamera(newCenter.x, newCenter.y, 1.5f);
    
  }

  private final Vec2 worldDiff = new Vec2();

  /**
   * Moves the camera by the given distance in screen coordinates.
   */
  public void moveWorld(Vec2 screenDiff) {
    targetTransform.getScreenVectorToWorld(screenDiff, worldDiff);
    if (targetTransform.isYFlip()) {
      worldDiff.y = +worldDiff.y;
    }
    targetTransform.setCenter(targetTransform.getCenter().addLocal(worldDiff));
  }

  public IViewportTransform getTransform() {
    return transform;
  }
    void setExtents(float halfWidth, float halfHeight) {
        targetTransform.setExtents(halfWidth, halfHeight);
    }

}
