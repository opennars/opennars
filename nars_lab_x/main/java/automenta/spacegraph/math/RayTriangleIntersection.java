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

import automenta.spacegraph.math.linalg.*;


/** Implements ray casting against a 3D triangle. */

public class RayTriangleIntersection {
  public static final int ERROR           = 0;
  public static final int NO_INTERSECTION = 1;
  public static final int INTERSECTION    = 2;

  /** Allow roundoff error of this amount. Be very careful adjusting
      this. Too big a value may cause valid triangles to be rejected.
      Too small a value may trigger an assert in the code to create an
      orthonormal basis in intersectRayWithTriangle. */
  private static final float epsilon = 1.0e-3f;

  /** Cast a ray starting at rayOrigin with rayDirection into the
      triangle defined by vertices v0, v1, and v2. If intersection
      occurred returns INTERSECTION and sets intersectionPoint
      appropriately, including t parameter (scale factor for
      rayDirection to reach intersection plane starting from
      rayOrigin). Returns NO_INTERSECTION if no intersection, or ERROR
      if triangle was degenerate or line was parallel to plane of
      triangle. */
  public static int intersectRayWithTriangle(Vec3f rayOrigin,
					     Vec3f rayDirection,
					     Vec3f v0,
					     Vec3f v1,
					     Vec3f v2,
					     IntersectionPoint intersectionPoint) {
    // Returns INTERSECTION if intersection computed, NO_INTERSECTION
    // if no intersection with triangle, ERROR if triangle was
    // degenerate or line did not intersect plane containing triangle.

    // NOTE these rays are TWO-SIDED.

    // Find point on line. P = ray origin, D = ray direction.
    //   P + tD = W
    // Find point on plane. X, Y = orthonormal bases for plane; O = its origin.
    //   O + uX + vY = W
    // Set equal
    //   O + uX + vY = tD + P
    //   uX + vY - tD = P - O = "B"
    //   [X0 Y0 -D0] [u]   [B0]
    //   [X1 Y1 -D1] [v] = [B1]
    //   [X2 Y2 -D2] [t]   [B2]
    // Now we have u, v coordinates for the intersection point (if system
    // wasn't degenerate).
    // Find u, v coordinates for three points of triangle. (DON'T DUPLICATE
    // WORK.) Now easy to do 2D inside/outside test.
    // If point is inside, do some sort of interpolation to compute the
    // 3D coordinates of the intersection point (may be unnecessary --
    // can reuse X, Y bases from above) and texture coordinates of this
    // point (maybe compute "texture coordinate" bases using same algorithm
    // and just use u, v coordinates??).

    Vec3f O = new Vec3f(v0);
    Vec3f p2 = new Vec3f();
    p2.sub(v1, O);
    Vec3f p3 = new Vec3f();
    p3.sub(v2, O);

    Vec3f X = new Vec3f(p2);
    Vec3f Y = new Vec3f(p3);
  
    // Normalize X
    if (X.length() < epsilon)
      return ERROR;  // coincident points in triangle
    X.normalize();

    // Use Gramm-Schmitt to orthogonalize X and Y
    Vec3f tmp = new Vec3f(X);
    tmp.scale(X.dot(Y));
    Y.sub(tmp);
    if (Y.length() < epsilon) {
      return ERROR;  // coincident points in triangle
    }
    Y.normalize();

    // X and Y are now orthonormal bases for the plane defined by the
    // triangle.

    Vec3f Bv = new Vec3f();
    Bv.sub(rayOrigin, O);

    Mat3f A = new Mat3f();
    A.setCol(0, X);
    A.setCol(1, Y);
    Vec3f tmpRayDir = new Vec3f(rayDirection);
    tmpRayDir.scale(-1.0f);
    A.setCol(2, tmpRayDir);
    if (!A.invert()) {
      return ERROR;
    }
    Vec3f B = new Vec3f();
    A.xformVec(Bv, B);

    Vec2f W = new Vec2f(B.x(), B.y());

    // Compute u,v coords of triangle
    Vec2f[] uv = new Vec2f[3];
    uv[0]      = new Vec2f(0,0);
    uv[1]      = new Vec2f(p2.dot(X), p2.dot(Y));
    uv[2]      = new Vec2f(p3.dot(X), p3.dot(Y));

    if (!(Math.abs(uv[1].y()) < epsilon)) {
      throw new RuntimeException("Math.abs(uv[1].y()) >= epsilon");
    }

    // Test. For each of the sides of the triangle, is the intersection
    // point on the same side as the third vertex of the triangle?
    // If so, intersection point is inside triangle.
    for (int i = 0; i < 3; i++) {
      if (approxOnSameSide(uv[i], uv[(i+1)%3],
			   uv[(i+2)%3], W) == false) {
	return NO_INTERSECTION;
      }
    }

    // Blend coordinates and texture coordinates according to
    // distances from 3 points
    // To do: find u,v coordinates of intersection point in coordinate
    // system of axes defined by uv[1] and uv[2].
    // Blending coords == a, b. 0 <= a,b <= 1.
    if (!(Math.abs(uv[2].y()) > epsilon)) {
      throw new RuntimeException("Math.abs(uv[2].y()) <= epsilon");
    }
    if (!(Math.abs(uv[1].x()) > epsilon)) {
      throw new RuntimeException("Math.abs(uv[1].x()) <= epsilon");
    }
    float a, b;
    b = W.y() / uv[2].y();
    a = (W.x() - b * uv[2].x()) / uv[1].x();

    p2.scale(a);
    p3.scale(b);
    O.add(p2);
    O.add(p3);
    intersectionPoint.setIntersectionPoint(O);
    intersectionPoint.setT(B.z());
    return INTERSECTION;
  }

  private static boolean approxOnSameSide(Vec2f linePt1, Vec2f linePt2,
					  Vec2f testPt1, Vec2f testPt2) {
    // Evaluate line equation for testPt1 and testPt2

    // ((y2 - y1) / (x2 - x1)) - ((y1 - y) / (x1 - x))
    // y - (mx + b)
    float num0 = linePt2.y() - linePt1.y();
    float den0 = linePt2.x() - linePt1.x();
    float num1 = linePt1.y() - testPt1.y();
    float den1 = linePt1.x() - testPt1.x();
    float num2 = linePt1.y() - testPt2.y();
    float den2 = linePt1.x() - testPt2.x();

    if (Math.abs(den0) < epsilon) {
      // line goes vertically.
      if ((Math.abs(den1) < epsilon) ||
	  (Math.abs(den2) < epsilon)) {
	return true;
      }
      
      if (MathUtil.sgn(den1) == MathUtil.sgn(den2)) {
	return true;
      }
      
      return false;
    }

    float m = num0 / den0;
    // (y - y1) - m(x - x1)
    float val1 = testPt1.y() - linePt1.y() - m * (testPt1.x() - linePt1.x());
    float val2 = testPt2.y() - linePt1.y() - m * (testPt2.x() - linePt1.x());
    if ((Math.abs(val1) < epsilon) ||
	(Math.abs(val2) < epsilon)) {
      return true;
    }

    if (MathUtil.sgn(val1) == MathUtil.sgn(val2)) {
      return true;
    }

    return false;
  }
}
