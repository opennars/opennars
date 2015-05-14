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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;



/** This class groups a set of ManipParts. Makes a set of ManipParts
    look like one. */

public class ManipPartGroup extends ManipPart {
  private boolean pickable = true;
  private boolean visible = true;
  private List children = new ArrayList();

  public void addChild(ManipPart child) {
    children.add(child);
  }

  public void removeChild(ManipPart child) {
    children.remove(child);
  }

  public int getNumChildren() {
    return children.size();
  }

  public ManipPart getChild(int index) {
    return (ManipPart) children.get(index);
  }

  public void intersectRay(Vec3f rayStart,
                           Vec3f rayDirection,
                           List results,
                           Manip caller) {
    if (!pickable) {
      return;
    }

    int topIdx = results.size();
    for (int i = 0; i < getNumChildren(); i++) {
      getChild(i).intersectRay(rayStart, rayDirection, results, caller);
    }

    // Fix up all HitPoints so we appear to be the manipulator part
    // which caused the intersection
    for (int i = topIdx; i < results.size(); i++) {
      ((HitPoint) results.get(i)).manipPart = this;
    }
  }

  public void setTransform(Mat4f xform) {
    for (int i = 0; i < getNumChildren(); i++) {
      getChild(i).setTransform(xform);
    }
  }

  public void highlight() {
    for (int i = 0; i < getNumChildren(); i++) {
      getChild(i).highlight();
    }
  }

  public void clearHighlight() {
    for (int i = 0; i < getNumChildren(); i++) {
      getChild(i).clearHighlight();
    }
  }

  public void setPickable(boolean pickable) {
    this.pickable = pickable;
  }

  public boolean getPickable() {
    return pickable;
  }

  public void setVisible(boolean visible) {
    this.visible = visible;
    for (Iterator iter = children.iterator(); iter.hasNext(); ) {
      ((ManipPart) iter.next()).setVisible(visible);
    }
  }

  public boolean getVisible() {
    return visible;
  }

  public void render(GL2 gl) {
    for (Iterator iter = children.iterator(); iter.hasNext(); ) {
      ((ManipPart) iter.next()).render(gl);
    }
  }
}
