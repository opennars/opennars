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


/** Piece of geometry defining a two-way arrow, used in Translate1 and
    Translate2 manips. */

public class ManipPartTwoWayArrow extends ManipPartTriBased {
  private static final Vec3f[] vertices = {
    // Left tetrahedron
    new Vec3f(-1.0f, 0.0f, 0.0f),
    new Vec3f(-0.666666f, 0.166666f, 0.166666f),
    new Vec3f(-0.666666f, -0.166666f, 0.166666f),
    new Vec3f(-0.666666f, -0.166666f, -0.166666f),
    new Vec3f(-0.666666f, 0.166666f, -0.166666f),

    // Box at center
    new Vec3f(-0.666666f, 0.041666f, 0.0416666f),
    new Vec3f(-0.666666f, -0.041666f, 0.0416666f),
    new Vec3f(-0.666666f, -0.041666f, -0.0416666f),
    new Vec3f(-0.666666f, 0.041666f, -0.0416666f),
    new Vec3f(0.666666f, 0.041666f, 0.0416666f),
    new Vec3f(0.666666f, -0.041666f, 0.0416666f),
    new Vec3f(0.666666f, -0.041666f, -0.0416666f),
    new Vec3f(0.666666f, 0.041666f, -0.0416666f),

    // Right tetrahedron
    new Vec3f(0.666666f, 0.166666f, 0.166666f),
    new Vec3f(0.666666f, 0.166666f, -0.166666f),
    new Vec3f(0.666666f, -0.166666f, -0.166666f),
    new Vec3f(0.666666f, -0.166666f, 0.166666f),
    new Vec3f(1.0f, 0.0f, 0.0f),
  };

  private static final int[] vertexIndices = {
    // Left tetrahedron
    1, 0, 2,
    2, 0, 3,
    3, 0, 4,
    4, 0, 1,
    1, 2, 3,
    1, 3, 4,

    // Box
    5, 7, 6,   // left face
    5, 8, 7,
    5, 6, 10,  // front face
    5, 10, 9,
    6, 7, 11,  // bottom face
    6, 11, 10,
    7, 8, 12,  // back face
    7, 12, 11,
    8, 5, 9,   // top face
    8, 9, 12,
    9, 10, 11, // right face
    9, 11, 12,

    // Right tetrahedron
    13, 14, 15,
    13, 15, 16,
    17, 14, 13,
    17, 15, 14,
    17, 16, 15,
    17, 13, 16
  };

  private static Vec3f[] normals  = null;
  private static int[] normalIndices = null;

  public ManipPartTwoWayArrow() {
    super();

    if (normals == null) {
      NormalCalc.NormalInfo normInfo =
        NormalCalc.computeFacetedNormals(vertices, vertexIndices, true);
      normals = normInfo.normals;
      normalIndices = normInfo.normalIndices;
    }

    setVertices(vertices);
    setVertexIndices(vertexIndices);
    setNormals(normals);
    setNormalIndices(normalIndices);
  }
}
