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
import com.jogamp.opengl.GL2;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


/** The base class for all manipulators. Fundamentally a manipulator
    must support a ray cast operation with itself and logic to
    understand what to do when that ray cast actually made the
    manipulator active. */

public abstract class Manip {
  private List motionListeners;

  public Manip() {
    motionListeners = new LinkedList();

    // FIXME: The ManipManager's list should probably be maintained
    // with weak references
    //    ManipManager.getManipManager().registerManip(this);
  }

  /** Returns true if the addition was successful, false otherwise */
  public boolean    addMotionListener(ManipMotionListener l) {
    return motionListeners.add(l);
  }

  /** Returns true if the removal was successful, false otherwise */
  public boolean removeMotionListener(ManipMotionListener l) {
    return motionListeners.remove(l);
  }

  /** Cast a ray in 3-space from the camera start position in the
      specified direction and test for intersections against all live
      portions of this manipulator. Add all hits, in arbitrary order,
      to the end of the given list in the form of HitPoints. Must not
      modify the results vector in any other way (i.e., must not
      remove any existing HitPoints from the results vector). */
  public abstract void intersectRay(Vec3f rayStart,
				    Vec3f rayDirection,
				    List results);

  /** Tell the manipulator to highlight the current portion of itself.
      This is merely visual feedback to the user. */
  public abstract void highlight(HitPoint hit);
  
  /** Tell the manipulator to clear the current highlight */
  public abstract void clearHighlight();

  /** If the ManipManager decides that this manipulator is to become
      active, it will pass back the HitPoint which made it make its
      decision. The manipulator can then change its state to look for
      drags of this portion of the manipulator. */
  public abstract void makeActive(HitPoint hit);

  /** When a manipulator is active, drags of the live portion cause
      motion of the manipulator. The ManipManager keeps track of which
      manipulator (if any) is active and takes care of calling the
      drag() method with the current ray start and direction. The
      manipulator must keep enough state to understand how it should
      position and/or rotate itself. NOTE that the base class provides
      an implementation for this method which you must call at the end
      of your overriding method. */
  public void drag(Vec3f rayStart,
		   Vec3f rayDirection) {
    for (Iterator iter = motionListeners.iterator(); iter.hasNext(); ) {
      ManipMotionListener m = (ManipMotionListener) iter.next();
      m.manipMoved(this);
    }
  }
  
  /** When the mouse button is released, makeInactive() is called. The
      manipulator should reset its state in preparation for the next
      drag. */
  public abstract void makeInactive();

  /** Render this Manipulator now using the given OpenGL routines and
      assuming an OpenGL context is current. */
  public abstract void render(GL2 gl);
}
