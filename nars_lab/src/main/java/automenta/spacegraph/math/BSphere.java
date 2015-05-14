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
import automenta.spacegraph.math.linalg.Vec3f;

/** Represents a bounding sphere. */

public class BSphere {
  private Vec3f center = new Vec3f();
  private float radius;
  private float radSq;

  /** Default constructor creates a sphere with center (0, 0, 0) and
      radius 0 */
  public BSphere() {
    makeEmpty();
  }

  public BSphere(Vec3f center, float radius) {
    set(center, radius);
  }

  /** Re-initialize this sphere to center (0, 0, 0) and radius 0 */
  public void makeEmpty() {
    center.set(0, 0, 0);
    radius = radSq = 0;
  }

  public void  setCenter(Vec3f center) { this.center.set(center); }
  public Vec3f getCenter()             { return center;           }

  public void  setRadius(float radius) { this.radius = radius;
                                         radSq = radius * radius; }
  public float getRadius()             { return radius;           }

  public void set(Vec3f center, float radius) {
    setCenter(center); setRadius(radius); 
  }
  /** Returns radius and mutates passed "center" vector */
  float get(Vec3f center) {
    center.set(this.center); return radius;
  }

  /** Mutate this sphere to encompass both itself and the argument.
      Ignores zero-size arguments. */
  public void extendBy(BSphere arg) {
    if ((radius == 0.0f) || (arg.radius == 0.0f))
      return;
    // FIXME: This algorithm is a quick hack -- minimum bounding
    // sphere of a set of other spheres is a well studied problem, but
    // not by me
    Vec3f diff = arg.center.minus(center);
    if (diff.lengthSquared() == 0.0f) {
      setRadius(Math.max(radius, arg.radius));
      return;
    }
    IntersectionPoint[] intPt = new IntersectionPoint[4];
    for (int i = 0; i < intPt.length; i++) {
      intPt[i] = new IntersectionPoint();
    }
    int numIntersections;
    numIntersections = intersectRay(center, diff, intPt[0], intPt[1]);
    assert numIntersections == 2;
    numIntersections = intersectRay(center, diff, intPt[2], intPt[3]);
    assert numIntersections == 2;
    IntersectionPoint minIntPt = intPt[0];
    IntersectionPoint maxIntPt = intPt[0];
    // Find minimum and maximum t values, take associated intersection
    // points, find midpoint and half length of line segment -->
    // center and radius.
    for (int i = 0; i < 4; i++) {
      if (intPt[i].getT() < minIntPt.getT()) {
        minIntPt = intPt[i];
      } else if (intPt[i].getT() > maxIntPt.getT()) {
        maxIntPt = intPt[i];
      }
    }
    // Compute the average -- this is the new center
    center.add(minIntPt.getIntersectionPoint(),
               maxIntPt.getIntersectionPoint());
    center.scale(0.5f);
    // Compute half the length -- this is the radius
    setRadius(
      0.5f *
      minIntPt.getIntersectionPoint().
        minus(maxIntPt.getIntersectionPoint()).
          length()
    );
  }

  /** Intersect a ray with the sphere. This is a one-sided ray
      cast. Mutates one or both of intPt0 and intPt1. Returns number
      of intersections which occurred. */
  int intersectRay(Vec3f rayStart,
                   Vec3f rayDirection,
                   IntersectionPoint intPt0,
                   IntersectionPoint intPt1) {
    // Solve quadratic equation
    float a = rayDirection.lengthSquared();
    if (a == 0.0)
      return 0;
    float b = 2.0f * (rayStart.dot(rayDirection) - rayDirection.dot(center));
    Vec3f tempDiff = center.minus(rayStart);
    float c = tempDiff.lengthSquared() - radSq;
    float disc = b * b - 4 * a * c;
    if (disc < 0.0f)
      return 0;
    int numIntersections;
    if (disc == 0.0f)
      numIntersections = 1;
    else
      numIntersections = 2;
    intPt0.setT((0.5f * (-1.0f * b + (float) Math.sqrt(disc))) / a);
    if (numIntersections == 2)
      intPt1.setT((0.5f * (-1.0f * b - (float) Math.sqrt(disc))) / a);
    Vec3f tmp = new Vec3f(rayDirection);
    tmp.scale(intPt0.getT());
    tmp.add(tmp, rayStart);
    intPt0.setIntersectionPoint(tmp);
    if (numIntersections == 2) {
      tmp.set(rayDirection);
      tmp.scale(intPt1.getT());
      tmp.add(tmp, rayStart);
      intPt1.setIntersectionPoint(tmp);
    }
    return numIntersections;
  }
}
