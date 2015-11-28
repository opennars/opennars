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

import automenta.spacegraph.math.linalg.Line;
import automenta.spacegraph.math.linalg.Mat4f;
import automenta.spacegraph.math.linalg.MathUtil;
import automenta.spacegraph.math.linalg.Vec3f;
import com.jogamp.opengl.GL2;

import java.util.List;



/** A Translate1Manip is a Manip which translates in only one
    dimension and whose default representation is a two-way arrow. */

public class Translate1Manip extends Manip {
  private ManipPart parts;
  private Vec3f translation;
  /** Normalized */
  private Vec3f axis;
  private Vec3f scale;
  /** Local-to-world transform for geometry */
  private Mat4f xform;

  /** Dragging state */
  private Line dragLine;
  /** Dragging state */
  private Vec3f dragOffset;

  public Translate1Manip() {
    parts       = new ManipPartTwoWayArrow();
    translation = new Vec3f(0, 0, 0);
    axis        = new Vec3f(1, 0, 0);
    scale       = new Vec3f(1, 1, 1);
    xform	= new Mat4f();
    dragLine	= new Line();
    dragOffset  = new Vec3f();
    recalc();
  }
  
  /** Set the translation of this Translate1Manip. This moves its
      on-screen representation. Manipulations cause the translation to
      be modified, not overwritten, so if you want the default
      Translate1Manip to go through the point (0, 1, 0) but still
      translate along the X axis, then setTranslation(0, 1, 0). */
  public void setTranslation(Vec3f translation) {
    this.translation.set(translation);
    recalc();
  }

  /** Get the translation of this Translate1Manip. This corresponds to
      the center of its body. */
  public Vec3f getTranslation() {
    return new Vec3f(translation);
  }

  /** Set the axis of this Translate1Manip. This is the direction
      along which it will travel. Does not need to be normalized, but
      must not be the zero vector. */
  public void setAxis(Vec3f axis) {
    this.axis.set(axis);
    recalc();
  }

  /** Get the axis of this Translate1Manip. */
  public Vec3f getAxis() {
    return new Vec3f(axis);
  }
  
  /** Set the scale of the Translate1Manip. This only affects the size
      of the on-screen geometry. */
  public void setScale(Vec3f scale) {
    this.scale.set(scale);
    recalc();
  }

  public Vec3f getScale() {
    return new Vec3f(scale);
  }

  /** Change the geometry of this manipulator to be the user-defined
      piece. */
  public void replaceGeometry(ManipPart geom) {
    parts = geom;
  }

  public void intersectRay(Vec3f rayStart,
			   Vec3f rayDirection,
			   List results) {
    parts.intersectRay(rayStart, rayDirection, results, this);
  }

  public void highlight(HitPoint hit) {
    if (hit.manipPart != parts) {
      throw new RuntimeException("My old geometry disappeared; how did this happen?");
    }
    parts.highlight();
  }
  
  public void clearHighlight() {
    parts.clearHighlight();
  }

  public void makeActive(HitPoint hit) {
    parts.highlight();
    dragLine.setDirection(axis);
    dragLine.setPoint(hit.intPt.getIntersectionPoint());
    dragOffset.sub(translation, hit.intPt.getIntersectionPoint());
  }

  public void drag(Vec3f rayStart,
		   Vec3f rayDirection) {
    // Algorithm: Find closest point of ray to dragLine. Add dragOffset
    // to this point to get new translation.
    Vec3f closestPoint = new Vec3f();
    if (dragLine.closestPointToRay(rayStart,
				   rayDirection,
				   closestPoint) == false) {
      // Drag axis is parallel to ray. Punt.
      return;
    }
    translation.set(closestPoint);
    translation.add(dragOffset);
    recalc();
    super.drag(rayStart, rayDirection);
  }

  public void makeInactive() {
    parts.clearHighlight();
  }
  
  public void render(GL2 gl) {
    parts.render(gl);
  }

  private void recalc() {
    // Construct local to world transform for geometry.
    // Scale, Rotation, Translation. Since we're right multiplying
    // column vectors, the actual matrix composed is TRS.
    Mat4f scaleMat = new Mat4f();
    Mat4f rotMat   = new Mat4f();
    Mat4f xlateMat = new Mat4f();
    Mat4f tmpMat   = new Mat4f();
    scaleMat.makeIdent();
    scaleMat.set(0, 0, scale.x());
    scaleMat.set(1, 1, scale.y());
    scaleMat.set(2, 2, scale.z());
    // Perpendiculars
    Vec3f p0 = new Vec3f();
    Vec3f p1 = new Vec3f();
    MathUtil.makePerpendicular(axis, p0);
    p1.cross(axis, p0);
    // axis, p0, p1 correspond to x, y, z
    p0.normalize();
    p1.normalize();
    rotMat.makeIdent();
    rotMat.set(0, 0, axis.x());
    rotMat.set(1, 0, axis.y());
    rotMat.set(2, 0, axis.z());
    rotMat.set(0, 1, p0.x());
    rotMat.set(1, 1, p0.y());
    rotMat.set(2, 1, p0.z());
    rotMat.set(0, 2, p1.x());
    rotMat.set(1, 2, p1.y());
    rotMat.set(2, 2, p1.z());
    xlateMat.makeIdent();
    xlateMat.set(0, 3, translation.x());
    xlateMat.set(1, 3, translation.y());
    xlateMat.set(2, 3, translation.z());
    tmpMat.mul(xlateMat, rotMat);
    xform.mul(tmpMat, scaleMat);
    parts.setTransform(xform);
  }
}
