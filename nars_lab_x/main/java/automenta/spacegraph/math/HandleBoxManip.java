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
import com.jogamp.opengl.GL2;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/** Patterned after Inventor's HandleBoxManip (by Paul Isaacs and
    David Mott) and TransformerManip (by Paul Isaacs). Center box
    allows 3-D translation. Outer six handles allow rotation about the
    box's six axes. When a handle is clicked, the axis of rotation is
    immediately chosen as that which is most parallel to the viewing
    direction (note there are at most two possibilities for the axis
    of each handle's rotation). Eight corner handles allow aspect
    ratio-preserving scaling; shift-dragging these handles allows
    non-uniform scaling in one of two possible directions using a
    "snap-to-axis" paradigm. These two directions are chosen as the
    axes of the adjacent face to the handle whose normal most directly
    faces the viewing direction. */

public class HandleBoxManip extends Manip {
  private ManipPart parts;
  private Vec3f translation;
  private Vec3f scale;
  private Vec3f geometryScale;
  private Rotf  rotation;
  /** Cumulative transform of this object */
  private Mat4f xform;

  /** Dragging state */
  private static final int INACTIVE          = 0;
  private static final int TRANSLATE         = 1;
  private static final int ROTATE            = 2;
  // Scaling based on all three axes, preserving current aspect ratio
  private static final int SCALE_XYZ         = 3;
  // Scaling along one of two axes (shift key + drag scale handle)
  private static final int SCALE_SINGLE_AXIS = 4;

  private int      dragState;
  private Plane    dragPlane;
  private Vec3f    dragOffset;
    
  /** Scale axes */
  private static final int SCALE_XY = 0;
  private static final int SCALE_YZ = 1;
  private static final int SCALE_ZX = 2;

  /** All of the line segments comprising the faces */
  private ManipPart[] lineSegs;

  static class FaceInfo {
    ManipPart[] lineSegs;
    /** The invisible square comprising this face's invisible, but
	pickable, geometry */
    ManipPart centerSquare;
    Vec3f origNormal;
    Vec3f normal;
    int scaleAxes;

    FaceInfo() {
      lineSegs = new ManipPart[4];
      origNormal = new Vec3f();
      normal = new Vec3f();
    }
  }
  /** List<FaceInfo> */
  private List faces;
  /** List<ManipPart> */
  private List highlightedGeometry;
  /** List<ManipPart> */
  private List draggedGeometry;

  /** Each rotation handle points off to two faces corresponding to
      the planes in which that handle can rotate. It also points to
      two circles which appear during dragging to indicate to the user
      in which plane the manipulator is being rotated. */
  static class RotateHandleInfo {
    ManipPart geometry;
    int faceIdx0;
    int faceIdx1;
  }
  /** List<RotateHandleInfo> */
  private List      rotateHandles;
  private PlaneUV   rotatePlane;
  private float     startAngle;
  private Rotf      startRot;

  private int scaleAxes;
  /** Each scale handle points off to its three adjacent faces. */
  static class ScaleHandleInfo {
    ManipPart geometry;
    /** The indices of the three adjacent faces */
    int[] faceIndices;

    ScaleHandleInfo() {
      faceIndices = new int[3];
    }
  }
  /** List<ScaleHandleInfo> */
  private List scaleHandles;
  // State variables for XYZ scaling
  /** This line's direction must point outward from the center of the
      cube. */
  private Line    scaleXYZLine;
  private float   origScaleLen;
  private Vec3f   origScale;
  // State variables for single-axis scaling
  private PlaneUV scaleAxisPlane;
  private Vec3f   scaleAxisOffset;
  private Vec2f   scaleAxisOrigUV;

  /** Default HandleBoxManip has translation (0, 0, 0) and the
      identity orientation */
  public HandleBoxManip() {
    parts = new ManipPartTwoWayArrow();
    translation = new Vec3f(0, 0, 0);
    scale = new Vec3f(1, 1, 1);
    geometryScale = new Vec3f(1, 1, 1);
    // Rotf should have a makeIdent() or at least initialize
    // itself to the unit quaternion (see NOTES.txt)
    rotation = new Rotf();
    xform = new Mat4f();

    dragState = INACTIVE;
    dragPlane = new Plane();
    dragOffset = new Vec3f();
    
    lineSegs = new ManipPart[12];

    faces = new ArrayList();
    highlightedGeometry = new ArrayList();
    draggedGeometry = new ArrayList();

    rotateHandles = new ArrayList();
    rotatePlane = new PlaneUV();
    startRot = new Rotf();

    scaleHandles = new ArrayList();
    scaleXYZLine = new Line();
    origScale = new Vec3f();
    scaleAxisPlane = new PlaneUV();
    scaleAxisOffset = new Vec3f();
    scaleAxisOrigUV = new Vec2f();
    
    createGeometry();
    recalc();
  }
  
  /** Set the translation of this HandleBoxManip. This moves its
      on-screen representation. Manipulations cause the translation to
      be modified, not overwritten. */
  public void setTranslation(Vec3f translation) {
    this.translation.set(translation);
    recalc();
  }

  /** Get the translation of this Translate1Manip. This corresponds to
      the center of its body. */
  public Vec3f getTranslation() {
    return new Vec3f(translation);
  }

  /** Set the rotation of this HandleBoxManip. */
  public void setRotation(Rotf rotation) {
    this.rotation.set(rotation);
    recalc();
  }

  /** Get the rotation of this HandleBoxManip. */
  public Rotf getRotation() {
    return new Rotf(rotation);
  }

  /** Set the scale of the HandleBoxManip. This corresponds to the
      scaling the user has performed. */
  public void setScale(Vec3f scale) {
    this.scale.set(scale);
    recalc();
  }

  public Vec3f getScale() {
    return new Vec3f(scale);
  }

  /** Set the scale of the HandleBoxManip's geometry. This only
      affects its on-screen representation. It is probably a bad idea
      to use a non-uniform scale here, because it'd be very confusing
      to the user. None of the components of the geometryScale vector
      may be negative. */
  public void setGeometryScale(Vec3f geometryScale) {
    this.geometryScale.set(geometryScale);
    recalc();
  }

  public Vec3f getGeometryScale() {
    return new Vec3f(geometryScale);
  }

  /** Get the cumulative transformation matrix of this
      HandleBoxManip. */
  public Mat4f getTransform() {
    // Transform is Scale, Rotation, Translation applied to incoming
    // column vectors in that order (SRT, or TRS since column vectors
    // are right-multiplied)
    Mat4f dest = new Mat4f();
    getTransform(dest);
    return dest;
  }

  /** Get the cumulative transformation matrix of this HandleBoxManip
      into the passed matrix. */
  public void getTransform(Mat4f dest) {
    Mat4f tmp1 = new Mat4f();
    Mat4f tmp2 = new Mat4f();
    tmp1.makeIdent();
    tmp2.makeIdent();
    tmp1.setScale(scale);
    dest.makeIdent();
    dest.setRotation(rotation);
    tmp2.mul(dest, tmp1);
    tmp1.makeIdent();
    tmp1.setTranslation(translation);
    dest.mul(tmp1, tmp2);
  }

  public void render(GL2 gl) {
    int i;
    for (i = 0; i < 12; i++)
      lineSegs[i].render(gl);
    for (i = 0; i < rotateHandles.size(); i++)
      ((RotateHandleInfo) rotateHandles.get(i)).geometry.render(gl);
    for (i = 0; i < scaleHandles.size(); i++)
      ((ScaleHandleInfo) scaleHandles.get(i)).geometry.render(gl);
  }

  public void intersectRay(Vec3f rayStart,
			   Vec3f rayDirection,
			   List results) {
    for (Iterator iter = faces.iterator(); iter.hasNext(); ) {
      ((FaceInfo) iter.next()).centerSquare.intersectRay(rayStart, rayDirection,
                                                         results, this);
    }
    for (Iterator iter = rotateHandles.iterator(); iter.hasNext(); ) {
      ((RotateHandleInfo) iter.next()).geometry.intersectRay(rayStart, rayDirection,
                                                             results, this);
    }
    for (Iterator iter = scaleHandles.iterator(); iter.hasNext(); ) {
      ((ScaleHandleInfo) iter.next()).geometry.intersectRay(rayStart, rayDirection,
                                                            results, this);
    }
  }

  public void highlight(HitPoint hit) {
    ManipPart part = hit.manipPart;
    // Search for this part in the FaceInfo array
    for (Iterator iter = faces.iterator(); iter.hasNext(); ) {
      FaceInfo info = (FaceInfo) iter.next();
      if (info.centerSquare == part) {
        for (int j = 0; j < 4; j++) {
          info.lineSegs[j].highlight();
          highlightedGeometry.add(info.lineSegs[j]);
        }
        return;
      }
    }

    // Otherwise, was a rotation or scale handle
    part.highlight();
    highlightedGeometry.add(part);
  }
  
  public void clearHighlight() {
    for (Iterator iter = highlightedGeometry.iterator(); iter.hasNext(); ) {
      ((ManipPart) iter.next()).clearHighlight();
    }
    highlightedGeometry.clear();
  }

  public void makeActive(HitPoint hit) {
    // Find which piece of geometry it is
    for (Iterator iter = faces.iterator(); iter.hasNext(); ) {
      FaceInfo face = (FaceInfo) iter.next();
      if (face.centerSquare == hit.manipPart) {
        dragState = TRANSLATE;
        dragPlane.setPoint(hit.intPt.getIntersectionPoint());
        dragPlane.setNormal(face.normal);
        dragOffset.sub(translation, hit.intPt.getIntersectionPoint());
        for (int j = 0; j < 4; j++) {
          face.lineSegs[j].highlight();
          draggedGeometry.add(face.lineSegs[j]);
        }
        return;
      }
    }

    for (Iterator iter = rotateHandles.iterator(); iter.hasNext(); ) {
      RotateHandleInfo rotInfo = (RotateHandleInfo) iter.next();
      if (rotInfo.geometry == hit.manipPart) {
        dragState = ROTATE;
        // Determine which direction we're rotating by taking dot
        // products of the ray direction with the rotating planes'
        // normals
        float dotp0 =
          Math.abs(hit.rayDirection.dot(((FaceInfo) faces.get(rotInfo.faceIdx0)).normal));
        float dotp1 =
          Math.abs(hit.rayDirection.dot(((FaceInfo) faces.get(rotInfo.faceIdx1)).normal));
        int faceIdx;
        if (dotp0 > dotp1)
          faceIdx = rotInfo.faceIdx0;
        else
          faceIdx = rotInfo.faceIdx1;
        FaceInfo face = (FaceInfo) faces.get(faceIdx);
        // Set up the rotation plane
        rotatePlane.setOrigin(translation);
        rotatePlane.setNormal(face.normal);
        Vec3f dummy = new Vec3f();
        Vec2f startUV = new Vec2f();
        rotatePlane.projectPoint(hit.intPt.getIntersectionPoint(), dummy, startUV);
        startAngle = (float) Math.atan2(startUV.y(), startUV.x());
        startRot.set(rotation);
        rotInfo.geometry.highlight();
        draggedGeometry.add(rotInfo.geometry);
        return;
      }
    }

    for (Iterator iter = scaleHandles.iterator(); iter.hasNext(); ) {
      ScaleHandleInfo info = (ScaleHandleInfo) iter.next();
      if (info.geometry == hit.manipPart) {
        if (hit.shiftDown) {
          dragState = SCALE_SINGLE_AXIS;
          // Figure out which are the two axes along which we're
          // going to allow scaling by taking dot products of the
          // ray direction with the normals of the adjacent faces
          // to the scale handle.
          float dotp = 0.0f;
          float tmpDotp;
          int faceIdx = 0;
          for (int i = 0; i < 3; i++) {
            FaceInfo faceInfo = (FaceInfo) faces.get(info.faceIndices[i]);
            tmpDotp = faceInfo.normal.dot(hit.rayDirection);
            if ((i == 0) || (tmpDotp < dotp)) {
              dotp = tmpDotp;
              faceIdx = info.faceIndices[i];
            }
          }
          scaleAxes = ((FaceInfo) faces.get(faceIdx)).scaleAxes;
          Vec3f uAxisOrig = new Vec3f();
          Vec3f vAxisOrig = new Vec3f();
          if (scaleAxes == SCALE_XY) {
            uAxisOrig.set(1, 0, 0);
            vAxisOrig.set(0, 1, 0);
          } else if (scaleAxes == SCALE_YZ) {
            uAxisOrig.set(0, 1, 0);
            vAxisOrig.set(0, 0, 1);
          } else {
            uAxisOrig.set(0, 0, 1);
            vAxisOrig.set(1, 0, 0);
          }
          Vec3f uAxis = new Vec3f();
          Vec3f vAxis = new Vec3f();
          Mat4f rotationTmpMat = new Mat4f();
          rotationTmpMat.makeIdent();
          rotationTmpMat.setRotation(rotation);
          rotationTmpMat.xformDir(uAxisOrig, uAxis);
          rotationTmpMat.xformDir(vAxisOrig, vAxis);
          Vec3f normal = new Vec3f();
          normal.cross(uAxis, vAxis);
          scaleAxisPlane.setNormalAndUV(normal, uAxis, vAxis);
          // We need to be able to constrain the scaling to be
          // nonnegative.
          Vec3f newOrigin = new Vec3f();
          Vec2f foo = new Vec2f();
          scaleAxisPlane.projectPoint(translation, newOrigin, foo);
          scaleAxisPlane.setOrigin(newOrigin);
          scaleAxisOffset.sub(hit.intPt.getIntersectionPoint(), newOrigin);
          // Now project intersection point onto plane
          Vec3f bar = new Vec3f();
          scaleAxisPlane.projectPoint(hit.intPt.getIntersectionPoint(),
                                      bar, scaleAxisOrigUV);
          // Put the plane back where it was
          scaleAxisPlane.setOrigin(hit.intPt.getIntersectionPoint());
          origScale.set(scale);
        } else {
          dragState = SCALE_XYZ;
          scaleXYZLine.setPoint(hit.intPt.getIntersectionPoint());
          Vec3f scaleDiffVec = new Vec3f();
          scaleDiffVec.sub(hit.intPt.getIntersectionPoint(), translation);
          scaleXYZLine.setDirection(scaleDiffVec);
          origScale.set(scale);
          origScaleLen = scaleDiffVec.length();
        }
        info.geometry.highlight();
        draggedGeometry.add(info.geometry);
        return;
      }
    }

    throw new RuntimeException("Couldn't find intersected piece of geometry");
  }

  public void drag(Vec3f rayStart,
		   Vec3f rayDirection) {
    if (dragState == TRANSLATE) {
      // Algorithm: Find intersection of ray with dragPlane. Add
      // dragOffset to this point to get new translation.
      IntersectionPoint intPt = new IntersectionPoint();
      if (dragPlane.intersectRay(rayStart,
                                 rayDirection,
                                 intPt) == false) {
        // Ray is parallel to plane. Punt.
        return;
      }
      translation.add(intPt.getIntersectionPoint(), dragOffset);
      recalc();
    } else if (dragState == ROTATE) {
      IntersectionPoint intPt = new IntersectionPoint();
      Vec2f uvCoords = new Vec2f();
      if (rotatePlane.intersectRay(rayStart,
                                   rayDirection,
                                   intPt,
                                   uvCoords) == false) {
        // Ray is parallel to plane. Punt.
        return;
      }
      // Compute offset rotation angle
      Rotf offsetRot = new Rotf();
      offsetRot.set(rotatePlane.getNormal(),
                    (float) Math.atan2(uvCoords.y(), uvCoords.x()) - startAngle);
      rotation.mul(offsetRot, startRot);
      recalc();
    } else if (dragState == SCALE_XYZ) {
      Vec3f closestPt = new Vec3f();
      boolean gotPt = scaleXYZLine.closestPointToRay(rayStart,
                                                     rayDirection,
                                                     closestPt);
      if (gotPt) {
        // How far have we moved?
        // Clamp scale to be positive.
        Vec3f newDiffVec = new Vec3f();
        newDiffVec.sub(closestPt, translation);
        if (newDiffVec.dot(scaleXYZLine.getDirection()) < 0) {
          scale.set(0, 0, 0);
        } else {
          float scaleChange = newDiffVec.length() / origScaleLen;
          scale.set(origScale);
          scale.scale(scaleChange);
        }
        recalc();	  
      }
    } else if (dragState == SCALE_SINGLE_AXIS) {
      IntersectionPoint intPt = new IntersectionPoint();
      Vec2f uvCoords = new Vec2f();
      if (scaleAxisPlane.intersectRay(rayStart, rayDirection, intPt, uvCoords)) {
        Vec2f faceCenteredUVCoords = new Vec2f();
        Vec3f foo = new Vec3f();
        Vec3f tmp = new Vec3f();
        tmp.set(intPt.getIntersectionPoint());
        tmp.add(scaleAxisOffset);
        scaleAxisPlane.projectPoint(tmp, foo, faceCenteredUVCoords);
        if ((MathUtil.sgn(faceCenteredUVCoords.x()) ==
             MathUtil.sgn(scaleAxisOrigUV.x())) &&
            (MathUtil.sgn(faceCenteredUVCoords.y()) ==
             MathUtil.sgn(scaleAxisOrigUV.y()))) {
          if (faceCenteredUVCoords.x() < 0)
            uvCoords.setX(uvCoords.x() * -1);
          if (faceCenteredUVCoords.y() < 0)
            uvCoords.setY(uvCoords.y() * -1);
          Vec3f scaleVec = new Vec3f();
          if (Math.abs(uvCoords.x()) > Math.abs(uvCoords.y())) {
            if (scaleAxes == SCALE_XY)
              scaleVec.setX(uvCoords.x());
            else if (scaleAxes == SCALE_YZ)
              scaleVec.setY(uvCoords.x());
            else
              scaleVec.setZ(uvCoords.x());
          } else {
            if (scaleAxes == SCALE_XY)
              scaleVec.setY(uvCoords.y());
            else if (scaleAxes == SCALE_YZ)
              scaleVec.setZ(uvCoords.y());
            else
              scaleVec.setX(uvCoords.y());
          }
          scaleVec.setX(scaleVec.x() / geometryScale.x());
          scaleVec.setY(scaleVec.y() / geometryScale.y());
          scaleVec.setZ(scaleVec.z() / geometryScale.z());
          scale.add(origScale, scaleVec);
          // This shouldn't be necessary anymore
          /*
            if (scale.x() < 0)
            scale.setX(0);
            if (scale.y() < 0)
            scale.setY(0);
            if (scale.z() < 0)
            scale.setZ(0);
          */
        } else {
          if (Math.abs(uvCoords.x()) > Math.abs(uvCoords.y())) {
            if (scaleAxes == SCALE_XY)
              scale.setX(0);
            else if (scaleAxes == SCALE_YZ)
              scale.setY(0);
            else
              scale.setZ(0);
          } else {
            if (scaleAxes == SCALE_XY)
              scale.setY(0);
            else if (scaleAxes == SCALE_YZ)
              scale.setZ(0);
            else
              scale.setX(0);
          }
        }
        recalc();
      }
    } else {
      throw new RuntimeException("HandleBoxManip::drag: ERROR: Unexpected drag state");
    }
    super.drag(rayStart, rayDirection);
  }

  public void makeInactive() {
    dragState = INACTIVE;
    for (Iterator iter = draggedGeometry.iterator(); iter.hasNext(); ) {
      ((ManipPart) iter.next()).clearHighlight();
    }
    draggedGeometry.clear();
  }
  
  //----------------------------------------------------------------------
  // Internals only below this point
  //

  private void createGeometry() {
    ManipPartGroup group = new ManipPartGroup();

    //
    // Lines
    //

    // Top face:
    // Front line
    lineSegs[0] = createLineSeg(new Vec3f(0, 1, 1),
                                new Vec3f(1, 0, 0),
                                new Vec3f(0, 1, 0));
    // Left line
    lineSegs[1] = createLineSeg(new Vec3f(-1, 1, 0),
                                new Vec3f(0, 0, 1),
                                new Vec3f(0, 1, 0));
    // Back line
    lineSegs[2] = createLineSeg(new Vec3f(0, 1, -1),
                                new Vec3f(1, 0, 0),
                                new Vec3f(0, 1, 0));
    // Right line
    lineSegs[3] = createLineSeg(new Vec3f(1, 1, 0),
                                new Vec3f(0, 0, 1),
                                new Vec3f(0, 1, 0));
    // Middle segments:
    // Front left
    lineSegs[4] = createLineSeg(new Vec3f(-1, 0, 1),
                                new Vec3f(0, -1, 0),
                                new Vec3f(1, 0, 0));
    // Back left
    lineSegs[5] = createLineSeg(new Vec3f(-1, 0, -1),
                                new Vec3f(0, -1, 0),
                                new Vec3f(1, 0, 0));
    // Back right
    lineSegs[6] = createLineSeg(new Vec3f(1, 0, -1),
                                new Vec3f(0, -1, 0),
                                new Vec3f(1, 0, 0));
    // Front right
    lineSegs[7] = createLineSeg(new Vec3f(1, 0, 1),
                                new Vec3f(0, -1, 0),
                                new Vec3f(1, 0, 0));
    // Bottom face:
    // Front line
    lineSegs[8] = createLineSeg(new Vec3f(0, -1, 1),
                                new Vec3f(1, 0, 0),
                                new Vec3f(0, 1, 0));
    // Left line
    lineSegs[9] = createLineSeg(new Vec3f(-1, -1, 0),
                                new Vec3f(0, 0, 1),
                                new Vec3f(0, 1, 0));
    // Back line
    lineSegs[10] = createLineSeg(new Vec3f(0, -1, -1),
                                 new Vec3f(1, 0, 0),
                                 new Vec3f(0, 1, 0));
    // Right line
    lineSegs[11] = createLineSeg(new Vec3f(1, -1, 0),
                                 new Vec3f(0, 0, 1),
                                 new Vec3f(0, 1, 0));

    for (int i = 0; i < 12; i++) {
      group.addChild(lineSegs[i]);
    }

    //
    // Faces
    //

    // Front face (index 0)
    FaceInfo info = new FaceInfo();
    info.origNormal.set(0, 0, 1);
    info.centerSquare =
      createFace(info.origNormal, info.origNormal, new Vec3f(0, 1, 0));
    info.lineSegs[0] = lineSegs[0];
    info.lineSegs[1] = lineSegs[4];
    info.lineSegs[2] = lineSegs[7];
    info.lineSegs[3] = lineSegs[8];
    info.scaleAxes = SCALE_XY;
    faces.add(info);
    // Right face (index 1)
    info = new FaceInfo();
    info.origNormal.set(1, 0, 0);
    info.centerSquare =
      createFace(info.origNormal, info.origNormal, new Vec3f(0, 1, 0));
    info.lineSegs[0] = lineSegs[3];
    info.lineSegs[1] = lineSegs[6];
    info.lineSegs[2] = lineSegs[7];
    info.lineSegs[3] = lineSegs[11];
    info.scaleAxes = SCALE_YZ;
    faces.add(info);
    // Back face (index 2)
    info = new FaceInfo();
    info.origNormal.set(0, 0, -1);
    info.centerSquare =
      createFace(info.origNormal, info.origNormal, new Vec3f(0, 1, 0));
    info.lineSegs[0] = lineSegs[2];
    info.lineSegs[1] = lineSegs[5];
    info.lineSegs[2] = lineSegs[6];
    info.lineSegs[3] = lineSegs[10];
    info.scaleAxes = SCALE_XY;
    faces.add(info);
    // Left face (index 3)
    info = new FaceInfo();
    info.origNormal.set(-1, 0, 0);
    info.centerSquare =
      createFace(info.origNormal, info.origNormal, new Vec3f(0, 1, 0));
    info.lineSegs[0] = lineSegs[1];
    info.lineSegs[1] = lineSegs[4];
    info.lineSegs[2] = lineSegs[5];
    info.lineSegs[3] = lineSegs[9];
    info.scaleAxes = SCALE_YZ;
    faces.add(info);
    // Top face (index 4)
    info = new FaceInfo();
    info.origNormal.set(0, 1, 0);
    info.centerSquare =
      createFace(info.origNormal, info.origNormal, new Vec3f(0, 0, -1));
    info.lineSegs[0] = lineSegs[0];
    info.lineSegs[1] = lineSegs[1];
    info.lineSegs[2] = lineSegs[2];
    info.lineSegs[3] = lineSegs[3];
    info.scaleAxes = SCALE_ZX;
    faces.add(info);
    // Bottom face (index 5)
    info = new FaceInfo();
    info.origNormal.set(0, -1, 0);
    info.centerSquare =
      createFace(info.origNormal, info.origNormal, new Vec3f(0, 0, 1));
    info.lineSegs[0] = lineSegs[8];
    info.lineSegs[1] = lineSegs[9];
    info.lineSegs[2] = lineSegs[10];
    info.lineSegs[3] = lineSegs[11];
    info.scaleAxes = SCALE_ZX;
    faces.add(info);

    for (Iterator iter = faces.iterator(); iter.hasNext(); ) {
      group.addChild(((FaceInfo) iter.next()).centerSquare);
    }

    //
    // Rotation handles
    //

    // Front handle. Rotates about top/bottom and left/right faces.
    // Maintain references to top and right faces.
    RotateHandleInfo rotInfo = new RotateHandleInfo();
    rotInfo.faceIdx0 = 4;
    rotInfo.faceIdx1 = 1;
    rotInfo.geometry = createRotateHandle(new Vec3f(0, 0, 1));
    rotateHandles.add(rotInfo);
    // Right handle. Rotates about top/bottom and front/back faces.
    // Maintain references to top and front faces.
    rotInfo = new RotateHandleInfo();
    rotInfo.faceIdx0 = 4;
    rotInfo.faceIdx1 = 0;
    rotInfo.geometry = createRotateHandle(new Vec3f(1, 0, 0));
    rotateHandles.add(rotInfo);
    // Back handle. Rotates about top/bottom and left/right faces.
    // Maintain references to top and right faces.
    rotInfo = new RotateHandleInfo();
    rotInfo.faceIdx0 = 4;
    rotInfo.faceIdx1 = 1;
    rotInfo.geometry = createRotateHandle(new Vec3f(0, 0, -1));
    rotateHandles.add(rotInfo);
    // Left handle. Rotates about top/bottom and front/back faces.
    // Maintain references to top and front faces.
    rotInfo = new RotateHandleInfo();
    rotInfo.faceIdx0 = 4;
    rotInfo.faceIdx1 = 0;
    rotInfo.geometry = createRotateHandle(new Vec3f(-1, 0, 0));
    rotateHandles.add(rotInfo);
    // Top handle. Rotates about front/back and left/right faces.
    // Maintain references to front and right faces.
    rotInfo = new RotateHandleInfo();
    rotInfo.faceIdx0 = 0;
    rotInfo.faceIdx1 = 1;
    rotInfo.geometry = createRotateHandle(new Vec3f(0, 1, 0));
    rotateHandles.add(rotInfo);
    // Bottom handle. Rotates about front/back and left/right faces.
    // Maintain references to front and right faces.
    rotInfo = new RotateHandleInfo();
    rotInfo.faceIdx0 = 0;
    rotInfo.faceIdx1 = 1;
    rotInfo.geometry = createRotateHandle(new Vec3f(0, -1, 0));
    rotateHandles.add(rotInfo);

    for (Iterator iter = rotateHandles.iterator(); iter.hasNext(); ) {
      group.addChild(((RotateHandleInfo) iter.next()).geometry);
    }

    // Scale handles
    // Top right front (order: front right top)
    ScaleHandleInfo scaleInfo = new ScaleHandleInfo();
    scaleInfo.geometry = createScaleHandle(new Vec3f(1, 1, 1));
    scaleInfo.faceIndices[0] = 0;
    scaleInfo.faceIndices[1] = 1;
    scaleInfo.faceIndices[2] = 4;
    scaleHandles.add(scaleInfo);
    // Top right back (order: right back top)
    scaleInfo = new ScaleHandleInfo();
    scaleInfo.geometry = createScaleHandle(new Vec3f(1, 1, -1));
    scaleInfo.faceIndices[0] = 1;
    scaleInfo.faceIndices[1] = 2;
    scaleInfo.faceIndices[2] = 4;
    scaleHandles.add(scaleInfo);
    // Bottom right front (order: front right bottom)
    scaleInfo = new ScaleHandleInfo();
    scaleInfo.geometry = createScaleHandle(new Vec3f(1, -1, 1));
    scaleInfo.faceIndices[0] = 0;
    scaleInfo.faceIndices[1] = 1;
    scaleInfo.faceIndices[2] = 5;
    scaleHandles.add(scaleInfo);
    // Bottom right back (order: right back bottom)
    scaleInfo = new ScaleHandleInfo();
    scaleInfo.geometry = createScaleHandle(new Vec3f(1, -1, -1));
    scaleInfo.faceIndices[0] = 1;
    scaleInfo.faceIndices[1] = 2;
    scaleInfo.faceIndices[2] = 5;
    scaleHandles.add(scaleInfo);
    // Top left front (order: front left top)
    scaleInfo = new ScaleHandleInfo();
    scaleInfo.geometry = createScaleHandle(new Vec3f(-1, 1, 1));
    scaleInfo.faceIndices[0] = 0;
    scaleInfo.faceIndices[1] = 3;
    scaleInfo.faceIndices[2] = 4;
    scaleHandles.add(scaleInfo);
    // Top left back (order: back left top)
    scaleInfo = new ScaleHandleInfo();
    scaleInfo.geometry = createScaleHandle(new Vec3f(-1, 1, -1));
    scaleInfo.faceIndices[0] = 2;
    scaleInfo.faceIndices[1] = 3;
    scaleInfo.faceIndices[2] = 4;
    scaleHandles.add(scaleInfo);
    // Bottom left front (order: front left bottom)
    scaleInfo = new ScaleHandleInfo();
    scaleInfo.geometry = createScaleHandle(new Vec3f(-1, -1, 1));
    scaleInfo.faceIndices[0] = 0;
    scaleInfo.faceIndices[1] = 3;
    scaleInfo.faceIndices[2] = 5;
    scaleHandles.add(scaleInfo);
    // Bottom left back (order: back left bottom)
    scaleInfo = new ScaleHandleInfo();
    scaleInfo.geometry = createScaleHandle(new Vec3f(-1, -1, -1));
    scaleInfo.faceIndices[0] = 2;
    scaleInfo.faceIndices[1] = 3;
    scaleInfo.faceIndices[2] = 5;
    scaleHandles.add(scaleInfo);

    for (Iterator iter = scaleHandles.iterator(); iter.hasNext(); ) {
      group.addChild(((ScaleHandleInfo) iter.next()).geometry);
    }

    parts = group;
  }

  private ManipPart createLineSeg(Vec3f translation,
                                  Vec3f xAxis,
                                  Vec3f yAxis) {
    ManipPartTransform xform = new ManipPartTransform();
    ManipPartLineSeg lineSeg = new ManipPartLineSeg();
    xform.addChild(lineSeg);
    Mat4f offset = new Mat4f();
    offset.makeIdent();
    Vec3f zAxis = new Vec3f();
    zAxis.cross(xAxis, yAxis);
    offset.set(0, 0, xAxis.x());
    offset.set(1, 0, xAxis.y());
    offset.set(2, 0, xAxis.z());
    offset.set(0, 1, yAxis.x());
    offset.set(1, 1, yAxis.y());
    offset.set(2, 1, yAxis.z());
    offset.set(0, 2, zAxis.x());
    offset.set(1, 2, zAxis.y());
    offset.set(2, 2, zAxis.z());
    offset.set(0, 3, translation.x());
    offset.set(1, 3, translation.y());
    offset.set(2, 3, translation.z());
    xform.setOffsetTransform(offset);
    return xform;
  }

  private ManipPart createFace(Vec3f translation,
                               Vec3f normal,
                               Vec3f up) {
    ManipPartTransform xform = new ManipPartTransform();
    ManipPartSquare square   = new ManipPartSquare();
    square.setVisible(false);
    xform.addChild(square);
    Mat4f offset = new Mat4f();
    offset.makeIdent();
    Vec3f right = new Vec3f();
    right.cross(up, normal);
    offset.set(0, 0, right.x());
    offset.set(1, 0, right.y());
    offset.set(2, 0, right.z());
    offset.set(0, 1, up.x());
    offset.set(1, 1, up.y());
    offset.set(2, 1, up.z());
    offset.set(0, 2, normal.x());
    offset.set(1, 2, normal.y());
    offset.set(2, 2, normal.z());
    offset.set(0, 3, translation.x());
    offset.set(1, 3, translation.y());
    offset.set(2, 3, translation.z());
    xform.setOffsetTransform(offset);
    return xform;
  }

  private ManipPart createRotateHandle(Vec3f direction) {
    ManipPartCube handle = new ManipPartCube();
    Mat4f offset = new Mat4f();
    offset.makeIdent();
    offset.set(0, 0, 0.1f);
    offset.set(1, 1, 0.1f);
    offset.set(2, 2, 0.1f);
    Vec3f scaledDirection = new Vec3f(direction);
    scaledDirection.scale(2.0f);
    offset.setTranslation(scaledDirection);
    ManipPartTransform xform = new ManipPartTransform();
    xform.addChild(handle);
    xform.setOffsetTransform(offset);
    return xform;
  }

  private ManipPart createScaleHandle(Vec3f position) {
    ManipPartCube handle = new ManipPartCube();
    Mat4f offset = new Mat4f();
    offset.makeIdent();
    offset.set(0, 0, 0.1f);
    offset.set(1, 1, 0.1f);
    offset.set(2, 2, 0.1f);
    offset.setTranslation(position);
    ManipPartTransform xform = new ManipPartTransform();
    xform.addChild(handle);
    xform.setOffsetTransform(offset);
    return xform;
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
    scaleMat.set(0, 0, scale.x() * geometryScale.x());
    scaleMat.set(1, 1, scale.y() * geometryScale.y());
    scaleMat.set(2, 2, scale.z() * geometryScale.z());
    rotMat.makeIdent();
    rotMat.setRotation(rotation);
    xlateMat.makeIdent();
    xlateMat.set(0, 3, translation.x());
    xlateMat.set(1, 3, translation.y());
    xlateMat.set(2, 3, translation.z());
    tmpMat.mul(xlateMat, rotMat);
    xform.mul(tmpMat, scaleMat);
    int i;
    for (i = 0; i < 12; i++) {
      lineSegs[i].setTransform(xform);
    }
    for (i = 0; i < faces.size(); i++) {
      FaceInfo face = (FaceInfo) faces.get(i);
      face.centerSquare.setTransform(xform);
      xform.xformDir(face.origNormal, face.normal);
      face.normal.normalize();
      
      RotateHandleInfo rotInfo = (RotateHandleInfo) rotateHandles.get(i);
      rotInfo.geometry.setTransform(xform);
    }
    for (i = 0; i < scaleHandles.size(); i++) {
      ((ScaleHandleInfo) scaleHandles.get(i)).geometry.setTransform(xform);
    }
  }
}
