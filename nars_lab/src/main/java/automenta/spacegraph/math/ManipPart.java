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
import com.jogamp.opengl.GL2;

import java.util.List;



/** A ManipPart is a visible or invisible sub-part of a manipulator.
    ManipParts are organized into trees. */

public abstract class ManipPart {
  private ManipPartGroup parent;

  /** Get the parent ManipPartGroup, or null if none (NOTE that this
      differs from the C++ API) */
  public ManipPartGroup getParent() {
    return parent;
  }

  /** Set the parent ManipPartGroup of this ManipPart (NOTE that this
      differs from the C++ API) */
  public void setParent(ManipPartGroup parent) {
    this.parent = parent;
  }
  
  /** Intersect a ray with this part, returning all intersected points
      as HitPoints in the result list. The same rules as
      Manip.intersectRay() apply. */
  public abstract void intersectRay(Vec3f rayStart,
				    Vec3f rayDirection,
				    List results,
				    Manip caller);
  
  /** Sets the transform of this part. */
  public abstract void setTransform(Mat4f xform);

  /** Highlight this part */
  public abstract void highlight();

  /** Unhighlight this part */
  public abstract void clearHighlight();

  /** Is this part pickable, or just decorative? Not pickable implies
      that intersectRay() will return immediately. */
  public abstract void setPickable(boolean pickable);
  public abstract boolean getPickable();

  /** Is this part visible? */
  public abstract void setVisible(boolean visible);
  public abstract boolean getVisible();

  /** Render this ManipPart now using the given OpenGL routines and
      assuming an OpenGL context is current. */
  public abstract void render(GL2 gl);
}
