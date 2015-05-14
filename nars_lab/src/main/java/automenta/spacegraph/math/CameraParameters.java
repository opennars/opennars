/*
 * gleem -- OpenGL Extremely Easy-To-Use Manipulators.
 * Copyright (C) 1998-2003 Kenneth B. Russell (kbrussel@alum.mit.edu)
 *
 * Copying, distribution and use of this software in source and binary
 * forms, with or without modification, is permitted provided that the
 * following conditions are met:
 *
 * Distributions of source code must reproduce the copyright notice,
 * this list of conditions and the following disclaimer in the source
 * code header files; and Distributions of binary code must reproduce
 * the copyright notice, this list of conditions and the following
 * disclaimer in the documentation, Read me file, license file and/or
 * other materials provided with the software distribution.
 *
 * The names of Sun Microsystems, Inc. ("Sun") and/or the copyright
 * holder may not be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS," WITHOUT A WARRANTY OF ANY
 * KIND. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, NON-INTERFERENCE, ACCURACY OF
 * INFORMATIONAL CONTENT OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. THE
 * COPYRIGHT HOLDER, SUN AND SUN'S LICENSORS SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL THE
 * COPYRIGHT HOLDER, SUN OR SUN'S LICENSORS BE LIABLE FOR ANY LOST
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
 * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
 * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
 * INABILITY TO USE THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGES. YOU ACKNOWLEDGE THAT THIS SOFTWARE IS NOT
 * DESIGNED, LICENSED OR INTENDED FOR USE IN THE DESIGN, CONSTRUCTION,
 * OPERATION OR MAINTENANCE OF ANY NUCLEAR FACILITY. THE COPYRIGHT
 * HOLDER, SUN AND SUN'S LICENSORS DISCLAIM ANY EXPRESS OR IMPLIED
 * WARRANTY OF FITNESS FOR SUCH USES.
 */

package automenta.spacegraph.math;

import automenta.spacegraph.math.linalg.Mat4f;
import automenta.spacegraph.math.linalg.Rotf;
import automenta.spacegraph.math.linalg.Vec3f;

/** Container class for camera's parameters. */

public class CameraParameters {
  private Vec3f position         = new Vec3f();
  private Vec3f forwardDirection = new Vec3f();
  private Vec3f upDirection      = new Vec3f();
  private Rotf  orientation      = new Rotf();
  private Mat4f modelviewMatrix  = new Mat4f();
  private Mat4f projectionMatrix = new Mat4f();
  float vertFOV;
  float imagePlaneAspectRatio;
  int xSize;
  int ySize;

  public CameraParameters() {}

  public CameraParameters(Vec3f position,
                          Vec3f forwardDirection,
                          Vec3f upDirection,
                          Rotf  orientation,
                          Mat4f modelviewMatrix,
                          Mat4f projectionMatrix,
                          float vertFOV,
                          float imagePlaneAspectRatio,
                          int   xSize,
                          int   ySize) {
    setPosition(position);
    setForwardDirection(forwardDirection);
    setUpDirection(upDirection);
    setOrientation(orientation);
    setModelviewMatrix(modelviewMatrix);
    setProjectionMatrix(projectionMatrix);
    setVertFOV(vertFOV);
    setImagePlaneAspectRatio(imagePlaneAspectRatio);
    setXSize(xSize);
    setYSize(ySize);
  }

  public void set(CameraParameters params) {
    setPosition(params.getPosition());
    setForwardDirection(params.getForwardDirection());
    setUpDirection(params.getUpDirection());
    setOrientation(params.getOrientation());
    setModelviewMatrix(params.getModelviewMatrix());
    setProjectionMatrix(params.getProjectionMatrix());
    setVertFOV(params.getVertFOV());
    setImagePlaneAspectRatio(params.getImagePlaneAspectRatio());
    setXSize(params.getXSize());
    setYSize(params.getYSize());
  }

  public Object clone() {
    CameraParameters params = new CameraParameters();
    params.set(this);
    return params;
  }

  public CameraParameters copy() {
    return (CameraParameters) clone();
  }

  /** Sets 3-space origin of camera */
  public void  setPosition(Vec3f position)           { this.position.set(position);   }
  /** Gets 3-space origin of camera */
  public Vec3f getPosition()                         { return position;               }
  /** Sets 3-space forward direction of camera. Does not need to be
      normalized. */
  public void  setForwardDirection(Vec3f fwd)        { forwardDirection.set(fwd);     }
  /** Gets 3-space forward direction of camera. */
  public Vec3f getForwardDirection()                 { return forwardDirection;       }
  /** Sets 3-space upward direction of camera. This must be orthogonal
      to the viewing direction, but does not need to be normalized. */
  public void  setUpDirection(Vec3f up)              { upDirection.set(up);           }
  /** Gets 3-space upward direction of camera. */
  public Vec3f getUpDirection()                      { return upDirection;            }
  /** Sets orientation of camera. NOTE: user is responsible for
      ensuring this corresponds with the up and forward vectors. */
  public void setOrientation(Rotf orientation)       { this.orientation.set(orientation); }
  /** Gets orientation of camera. */
  public Rotf getOrientation()                       { return orientation; }
  /** Sets the modelview matrix corresponding to the orientation and
      position of the camera. NOTE: user is responsible for ensuring
      this corresponds to the rest of the camera parameters. */
  public void setModelviewMatrix(Mat4f matrix) {
    modelviewMatrix.set(matrix);
  }
  /** Gets the modelview matrix corresponding to the orientation and
      position of the camera. */
  public Mat4f getModelviewMatrix() {
    return modelviewMatrix;
  }
  /** Sets the projection matrix corresponding to the camera's
      field-of-view and aspect ratio parameters. NOTE: user is
      responsible for ensuring this matrix corresponds to these
      parameters.*/
  public void setProjectionMatrix(Mat4f matrix) {
    projectionMatrix.set(matrix);
  }
  /** Gets the projection matrix corresponding to the camera's
      field-of-view and aspect ratio parameters. */
  public Mat4f getProjectionMatrix() {
    return projectionMatrix;
  }
  
  /** Takes HALF of the vertical angular span of the frustum,
      specified in radians. For example, if your <b>fovy</b> argument
      to gluPerspective() is 90, then this would be Math.PI / 4. */
  public void  setVertFOV(float vertFOV)             { this.vertFOV = vertFOV;        }
  /** Returns HALF of the vertical angular span of the frustum,
      specified in radians. */
  public float getVertFOV()                          { return vertFOV;                }
  /** Sets the aspect ratio of the image plane. Note that this does not
      necessarily have to correspond to the aspect ratio of the
      window. */
  public void  setImagePlaneAspectRatio(float ratio) { imagePlaneAspectRatio = ratio; }
  /** Gets the aspect ratio of the image plane. */
  public float getImagePlaneAspectRatio()            { return imagePlaneAspectRatio;  }

  /** Sets the horizontal size of the window, in pixels */
  public void setXSize(int xSize)                    { this.xSize = xSize;            }
  /** Gets the horizontal size of the window, in pixels */
  public int  getXSize()                             { return xSize;                  }
  /** Sets the vertical size of the window, in pixels */
  public void setYSize(int ySize)                    { this.ySize = ySize;            }
  /** Gets the vertical size of the window, in pixels */
  public int  getYSize()                             { return ySize;                  }
}
