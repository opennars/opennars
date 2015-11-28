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

import automenta.spacegraph.math.linalg.IntersectionPoint;
import automenta.spacegraph.math.linalg.Mat4f;
import automenta.spacegraph.math.linalg.Vec3f;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL2ES1;

import java.util.List;



/** Triangle-based manipulator part. This is the base class for most
    of the ManipParts that GLEEM uses internally. You can feel free to
    subclass this if you want to replace geometry in the manipulators,
    or re-derive from ManipPart. See ManipPartLineSeg for an example. */

public class ManipPartTriBased extends ManipPart {
  private Vec3f   color;
  private Vec3f   highlightColor;
  private boolean highlighted;
  private boolean pickable;
  private boolean visible;
  /** Direct references down to subclass-specific data */
  private Vec3f[] vertices;
  private Vec3f[] normals;
  private int[]   vertexIndices;
  private int[]   normalIndices;
  /** Current transformation matrix */
  private Mat4f   xform;
  /** Transformed vertices and normals */
  private Vec3f[] curVertices;
  private Vec3f[] curNormals;

  public ManipPartTriBased() {
    color          = new Vec3f(0.8f, 0.8f, 0.8f);
    highlightColor = new Vec3f(0.8f, 0.8f, 0.2f);
    highlighted    = false;
    pickable       = true;
    visible	   = true;
    vertices	   = null;
    normals        = null;
    vertexIndices  = null;
    normalIndices  = null;
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
			   List  results,
			   Manip caller) {
    consistencyCheck();
    if (!pickable) {
      return;
    }

    IntersectionPoint intPt = new IntersectionPoint();
    HitPoint hitPt = new HitPoint();
    hitPt.manipulator = caller;
    hitPt.manipPart = this;
    for (int i = 0; i < vertexIndices.length; i+=3) {
      int i0 = vertexIndices[i];
      int i1 = vertexIndices[i+1];
      int i2 = vertexIndices[i+2];
      if (RayTriangleIntersection.intersectRayWithTriangle(rayStart,
							   rayDirection,
							   curVertices[i0],
							   curVertices[i1],
							   curVertices[i2],
							   intPt)
	  == RayTriangleIntersection.INTERSECTION) {
	// Check for intersections behind the ray
	if (intPt.getT() >= 0) {
	  hitPt.rayStart = rayStart;
	  hitPt.rayDirection = rayDirection;
	  hitPt.intPt = intPt;
	  results.add(hitPt);
	}
      }
    }
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

  /** Default is pickable */
  public void setPickable(boolean pickable) {
    this.pickable = pickable;    
  }

  public boolean getPickable() {
    return pickable;
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
    boolean lightingOn = true;
    // FIXME: this is too expensive; figure out another way
    //  if (glIsEnabled(GL2ES1.GL_LIGHTING))
    //    lightingOn = true;

    if (lightingOn) {
      gl.glEnable(GL2ES1.GL_COLOR_MATERIAL);
      gl.glColorMaterial(GL.GL_FRONT_AND_BACK, GL2ES1.GL_AMBIENT_AND_DIFFUSE);
    }
    gl.glBegin(GL.GL_TRIANGLES);
    if (highlighted)
      gl.glColor3f(highlightColor.x(), highlightColor.y(), highlightColor.z());
    else
      gl.glColor3f(color.x(), color.y(), color.z());
    int i = 0;
    while (i < vertexIndices.length) {
      Vec3f n0 = curNormals[normalIndices[i]];
      Vec3f v0 = curVertices[vertexIndices[i]];
      gl.glNormal3f(n0.x(), n0.y(), n0.z());
      gl.glVertex3f(v0.x(), v0.y(), v0.z());
      i++;

      Vec3f n1 = curNormals[normalIndices[i]];
      Vec3f v1 = curVertices[vertexIndices[i]];
      gl.glNormal3f(n1.x(), n1.y(), n1.z());
      gl.glVertex3f(v1.x(), v1.y(), v1.z());
      i++;

      Vec3f n2 = curNormals[normalIndices[i]];
      Vec3f v2 = curVertices[vertexIndices[i]];
      gl.glNormal3f(n2.x(), n2.y(), n2.z());
      gl.glVertex3f(v2.x(), v2.y(), v2.z());
      i++;
    }
    gl.glEnd();
    if (lightingOn)
      gl.glDisable(GL2ES1.GL_COLOR_MATERIAL);
  }

  //----------------------------------------------------------------------
  // Used by subclasses to set up vertex, normals, and vertex and
  // normal indices.
  //
  
  protected void setVertices(Vec3f[] vertices) {
    this.vertices = vertices;
  }

  protected Vec3f[] getVertices() {
    return vertices;
  }

  protected void setNormals(Vec3f[] normals) {
    this.normals = normals;
  }

  protected Vec3f[] getNormals() {
    return normals;
  }

  protected void setVertexIndices(int[] vertexIndices) {
    this.vertexIndices = vertexIndices;
  }

  protected int[] getVertexIndices() {
    return vertexIndices;
  }

  protected void setNormalIndices(int[] normalIndices) {
    this.normalIndices = normalIndices;
  }

  protected int[] getNormalIndices() {
    return normalIndices;
  }

  //----------------------------------------------------------------------
  // Internals only below this point
  //

  private void consistencyCheck() {
    if (vertexIndices.length != normalIndices.length) {
      throw new RuntimeException("vertexIndices.length != normalIndices.length");
    }
    
    if ((vertexIndices.length % 3) != 0) {
      throw new RuntimeException("(vertexIndices % 3) != 0");
    }
    
    if ((curVertices != null) &&
        (vertices.length != curVertices.length)) {
      throw new RuntimeException("vertices.length != curVertices.length");
    }
  }

  private void recalcVertices() {
    if ((curVertices == null) ||
        (curVertices.length != vertices.length)) {
      curVertices = new Vec3f[vertices.length];
      for (int i = 0; i < vertices.length; i++) {
        curVertices[i] = new Vec3f();
      }
    }

    for (int i = 0; i < vertices.length; i++) {
      xform.xformPt(vertices[i], curVertices[i]);
    }

    if ((curNormals == null) ||
        (curNormals.length != normals.length)) {
      curNormals = new Vec3f[normals.length];
      for (int i = 0; i < normals.length; i++) {
        curNormals[i] = new Vec3f();
      }
    }

    for (int i = 0; i < normals.length; i++) {
      xform.xformDir(normals[i], curNormals[i]);
      curNormals[i].normalize();
    }
  }
}
