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
import automenta.spacegraph.math.linalg.Vec3f;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL2ES1;

import java.util.List;



/** A line segment from (-1, 0, 0) to (1, 0, 0). */

public class ManipPartLineSeg extends ManipPart {
  private Vec3f color;
  private Vec3f highlightColor;
  private boolean highlighted;
  private boolean visible;
  private static Vec3f[] vertices = new Vec3f[] {
    new Vec3f(-1, 0, 0),
    new Vec3f(1, 0, 0)
  };
  /** Current transformation matrix */
  private Mat4f xform;
  /** Transformed vertices */
  private Vec3f[] curVertices;

  public ManipPartLineSeg() {
    color          = new Vec3f(0.8f, 0.8f, 0.8f);
    highlightColor = new Vec3f(0.8f, 0.8f, 0.2f);
    highlighted    = false;
    visible	   = true;
    xform          = new Mat4f();
    xform.makeIdent();
    curVertices    = null;
  }

  /** Default color is (0.8, 0.8, 0.8) */
  public void setColor(Vec3f color) {
    this.color.set(color);
  }

  public Vec3f getColor() {
    return new Vec3f(color);
  }

  /** Default highlight color is (0.8, 0.8, 0) */
  public void setHighlightColor(Vec3f highlightColor) {
    this.highlightColor.set(highlightColor);
  }
  
  public Vec3f getHighlightColor() {
    return new Vec3f(highlightColor);
  }

  public void intersectRay(Vec3f rayStart,
			   Vec3f rayDirection,
			   List results,
			   Manip caller) {
  }

  public void setTransform(Mat4f xform) {
    this.xform.set(xform);
    recalcVertices();
  }

  public void highlight() {
    highlighted = true;
  }

  public void clearHighlight() {
    highlighted = false;
  }

  public void setPickable(boolean pickable) {
  }

  public boolean getPickable() {
    return false;
  }

  /** Default is visible */
  public void setVisible(boolean visible) {
    this.visible = visible;
  }

  public boolean getVisible() {
    return visible;
  }

  public void render(GL2 gl) {
    if (!visible)
      return;
    // FIXME: probably too slow
    boolean reenable = gl.glIsEnabled(GL2ES1.GL_LIGHTING);
    gl.glDisable(GL2ES1.GL_LIGHTING);
    gl.glBegin(GL.GL_LINES);
    if (highlighted)
      gl.glColor3f(highlightColor.x(), highlightColor.y(), highlightColor.z());
    else
      gl.glColor3f(color.x(), color.y(), color.z());
    for (int i = 0; i < curVertices.length; i++) {
      Vec3f v = curVertices[i];
      gl.glVertex3f(v.x(), v.y(), v.z());
    }
    gl.glEnd();
    if (reenable)
      gl.glEnable(GL2ES1.GL_LIGHTING);
  }

  //----------------------------------------------------------------------
  // Internals only below this point
  //

  private void recalcVertices() {
    if ((curVertices == null) ||
        (curVertices.length != vertices.length)) {
      curVertices = new Vec3f[vertices.length];
      for (int i = 0; i < vertices.length; i++) {
        curVertices[i] = new Vec3f();
      }
    }

    for (int i = 0; i < curVertices.length; i++) {
      xform.xformPt(vertices[i], curVertices[i]);
    }
  }
}
