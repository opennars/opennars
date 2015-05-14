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

import automenta.spacegraph.math.linalg.Vec3f;


/** Calculates normals for a set of polygons. */

public class NormalCalc {

  /** Set of normals computed using {@link gleem.NormalCalc}. */
  public static class NormalInfo {
    public Vec3f[] normals;
    public int[]   normalIndices;

    NormalInfo(Vec3f[] normals, int[] normalIndices) {
      this.normals = normals;
      this.normalIndices = normalIndices;
    }
  }

  /** Returns null upon failure, or a set of Vec3fs and integers
      which represent faceted (non-averaged) normals, but per-vertex.
      Performs bounds checking on indices with respect to vertex list.
      Index list must represent independent triangles; indices are
      taken in groups of three. If index list doesn't represent
      triangles or other error occurred then returns null. ccw flag
      indicates whether triangles are specified counterclockwise when
      viewed from top or not. */

  public static NormalInfo computeFacetedNormals(Vec3f[] vertices,
                                                 int[] indices,
                                                 boolean ccw) {
    if ((indices.length % 3) != 0) {
      System.err.println("NormalCalc.computeFacetedNormals: numIndices wasn't " +
                         "divisible by 3, so it can't possibly " +
                         "represent a set of triangles");
      return null;
    }
    
    Vec3f[] outputNormals  = new Vec3f[indices.length / 3];
    int[] outputNormalIndices = new int[indices.length];

    Vec3f d1 = new Vec3f();
    Vec3f d2 = new Vec3f();
    int curNormalIndex = 0;
    for (int i = 0; i < indices.length; i += 3) {
      int i0 = indices[i];
      int i1 = indices[i+1];
      int i2 = indices[i+2];
      if ((i0 < 0) || (i0 >= indices.length) ||
	  (i1 < 0) || (i1 >= indices.length) ||
	  (i2 < 0) || (i2 >= indices.length)) {
	  System.err.println("NormalCalc.computeFacetedNormals: ERROR: " +
                             "vertex index out of bounds or no end of triangle " +
                             "index found");
          return null;
        }
      Vec3f v0 = vertices[i0];
      Vec3f v1 = vertices[i1];
      Vec3f v2 = vertices[i2];
      d1.sub(v1, v0);
      d2.sub(v2, v0);
      Vec3f n = new Vec3f();
      if (ccw) {
        n.cross(d1, d2);
      } else {
        n.cross(d2, d1);
      }
      n.normalize();
      outputNormals[curNormalIndex] = n;
      outputNormalIndices[i] = curNormalIndex;
      outputNormalIndices[i+1] = curNormalIndex;
      outputNormalIndices[i+2] = curNormalIndex;
      curNormalIndex++;
    }
    return new NormalInfo(outputNormals, outputNormalIndices);
  }
}
