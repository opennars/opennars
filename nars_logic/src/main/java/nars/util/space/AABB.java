/*
 *   __               .__       .__  ._____.           
 * _/  |_  _______  __|__| ____ |  | |__\_ |__   ______
 * \   __\/  _ \  \/  /  |/ ___\|  | |  || __ \ /  ___/
 *  |  | (  <_> >    <|  \  \___|  |_|  || \_\ \\___ \ 
 *  |__|  \____/__/\_ \__|\___  >____/__||___  /____  >
 *                   \/       \/             \/     \/ 
 *
 * Copyright (c) 2006-2011 Karsten Schmidt
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * http://creativecommons.org/licenses/LGPL/2.1/
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 */

package nars.util.space;



/**
 * Axis-aligned bounding box with basic intersection features for Ray, AABB and
 * Sphere classes.
 */
public class AABB extends BB implements Shape3D {

    protected Vec3D min, max;

    public AABB() {
        super();
        setExtent(new Vec3D());
    }

    /**
     * Creates an independent copy of the passed in box
     * 
     * @param box
     */
    public AABB(BB box) {
        this(box, box.getExtent());
    }

    /**
     * Creates a new box of the given size at the world origin.
     * 
     * @param extent
     */
    public AABB(float extent) {
        this(new Vec3D(), extent);
    }

    /**
     * Creates a new instance from centre point and uniform extent in all
     * directions.
     * 
     * @param center
     * @param extent half size, radius
     */
    public AABB(roVec3D center, float extent) {
        this(center, new Vec3D(extent, extent, extent));
    }

    /**
     * Creates a new instance from centre point and extent
     * 
     * @param center
     * @param extent
     *            box dimensions (the box will be double the size in each
     *            direction)
     */
    public AABB(XYZ center, Vec3D extent) {
        super(center);
        setExtent(extent);
    }

    @Override
    public BB copy() {
        return new AABB(this);
    }

    public final Vec3D getMax() {
        // return this.add(extent);
        return max.copy();
    }

    public final Vec3D getMin() {
        return min.copy();
    }

    public XYZ getNormalForPoint(roVec3D p) {
        p = p.sub(this);
        Vec3D pabs = extent.sub(p.getAbs());
        Vec3D psign = p.getSignum();
        XYZ normal = Vec3D.X_AXIS.scale(psign.x);
        float minDist = pabs.x;
        if (pabs.y < minDist) {
            minDist = pabs.y;
            normal = Vec3D.Y_AXIS.scale(psign.y);
        }
        if (pabs.z < minDist) {
            normal = Vec3D.Z_AXIS.scale(psign.z);
        }
        return normal;
    }

    /**
     * Adjusts the box size and position such that it includes the given point.
     * 
     * @param p
     *            point to include
     * @return itself
     */
    public BB growToContainPoint(roVec3D p) {
        min.minSelf(p);
        max.maxSelf(p);
        set(min.interpolateTo(max, 0.5f));
        extent.set(max.sub(min).scaleSelf(0.5f));
        return this;
    }

    /**
     * Calculates intersection with the given ray between a certain distance
     * interval.
     *
     * Ray-box intersection is using IEEE numerical properties to ensure the
     * test is both robust and efficient, as described in:
     *
     * Amy Williams, Steve Barrus, R. Keith Morley, and Peter Shirley: "An
     * Efficient and Robust Ray-Box Intersection Algorithm" Journal of graphics
     * tools, 10(1):49-54, 2005
     *
     * @param ray
     *            incident ray
     * @param minDist
     * @param maxDist
     * @return intersection point on the bounding box (only the first is
     *         returned) or null if no intersection
     */
    public Vec3D intersectsRay(Ray3D ray, float minDist, float maxDist) {
        Vec3D invDir = ray.getDirection().reciprocal();
        boolean signDirX = invDir.x < 0;
        boolean signDirY = invDir.y < 0;
        boolean signDirZ = invDir.z < 0;
        Vec3D bbox = signDirX ? max : min;
        float tmin = (bbox.x - ray.x) * invDir.x;
        bbox = signDirX ? min : max;
        float tmax = (bbox.x - ray.x) * invDir.x;
        bbox = signDirY ? max : min;
        float tymin = (bbox.y - ray.y) * invDir.y;
        bbox = signDirY ? min : max;
        float tymax = (bbox.y - ray.y) * invDir.y;
        if ((tmin > tymax) || (tymin > tmax)) {
            return null;
        }
        if (tymin > tmin) {
            tmin = tymin;
        }
        if (tymax < tmax) {
            tmax = tymax;
        }
        bbox = signDirZ ? max : min;
        float tzmin = (bbox.z - ray.z) * invDir.z;
        bbox = signDirZ ? min : max;
        float tzmax = (bbox.z - ray.z) * invDir.z;
        if ((tmin > tzmax) || (tzmin > tmax)) {
            return null;
        }
        if (tzmin > tmin) {
            tmin = tzmin;
        }
        if (tzmax < tmax) {
            tmax = tzmax;
        }
        if ((tmin < maxDist) && (tmax > minDist)) {
            return ray.getPointAtDistance(tmin);
        }
        return null;
    }

//    public boolean intersectsTriangle(Triangle3D tri) {
//        // use separating axis theorem to test overlap between triangle and box
//        // need to test for overlap in these directions:
//        //
//        // 1) the {x,y,z}-directions (actually, since we use the AABB of the
//        // triangle
//        // we do not even need to test these)
//        // 2) normal of the triangle
//        // 3) crossproduct(edge from tri, {x,y,z}-directin)
//        // this gives 3x3=9 more tests
//        Vec3D v0, v1, v2;
//        Vec3D normal, e0, e1, e2, f;
//
//        // move everything so that the boxcenter is in (0,0,0)
//        v0 = tri.a.sub(this);
//        v1 = tri.b.sub(this);
//        v2 = tri.c.sub(this);
//
//        // compute triangle edges
//        e0 = v1.sub(v0);
//        e1 = v2.sub(v1);
//        e2 = v0.sub(v2);
//
//        // test the 9 tests first (this was faster)
//        f = e0.getAbs();
//        if (testAxis(e0.z, -e0.y, f.z, f.y, v0.y, v0.z, v2.y, v2.z, extent.y,
//                extent.z)) {
//            return false;
//        }
//        if (testAxis(-e0.z, e0.x, f.z, f.x, v0.x, v0.z, v2.x, v2.z, extent.x,
//                extent.z)) {
//            return false;
//        }
//        if (testAxis(e0.y, -e0.x, f.y, f.x, v1.x, v1.y, v2.x, v2.y, extent.x,
//                extent.y)) {
//            return false;
//        }
//
//        f = e1.getAbs();
//        if (testAxis(e1.z, -e1.y, f.z, f.y, v0.y, v0.z, v2.y, v2.z, extent.y,
//                extent.z)) {
//            return false;
//        }
//        if (testAxis(-e1.z, e1.x, f.z, f.x, v0.x, v0.z, v2.x, v2.z, extent.x,
//                extent.z)) {
//            return false;
//        }
//        if (testAxis(e1.y, -e1.x, f.y, f.x, v0.x, v0.y, v1.x, v1.y, extent.x,
//                extent.y)) {
//            return false;
//        }
//
//        f = e2.getAbs();
//        if (testAxis(e2.z, -e2.y, f.z, f.y, v0.y, v0.z, v1.y, v1.z, extent.y,
//                extent.z)) {
//            return false;
//        }
//        if (testAxis(-e2.z, e2.x, f.z, f.x, v0.x, v0.z, v1.x, v1.z, extent.x,
//                extent.z)) {
//            return false;
//        }
//        if (testAxis(e2.y, -e2.x, f.y, f.x, v1.x, v1.y, v2.x, v2.y, extent.x,
//                extent.y)) {
//            return false;
//        }
//
//        // first test overlap in the {x,y,z}-directions
//        // find min, max of the triangle each direction, and test for overlap in
//        // that direction -- this is equivalent to testing a minimal AABB around
//        // the triangle against the AABB
//
//        // test in X-direction
//        if (MathUtils.min(v0.x, v1.x, v2.x) > extent.x
//                || MathUtils.max(v0.x, v1.x, v2.x) < -extent.x) {
//            return false;
//        }
//
//        // test in Y-direction
//        if (MathUtils.min(v0.y, v1.y, v2.y) > extent.y
//                || MathUtils.max(v0.y, v1.y, v2.y) < -extent.y) {
//            return false;
//        }
//
//        // test in Z-direction
//        if (MathUtils.min(v0.z, v1.z, v2.z) > extent.z
//                || MathUtils.max(v0.z, v1.z, v2.z) < -extent.z) {
//            return false;
//        }
//
//        // test if the box intersects the plane of the triangle
//        // compute plane equation of triangle: normal*x+d=0
//        normal = e0.cross(e1);
//        float d = -normal.dot(v0);
//        if (!planeBoxOverlap(normal, d, extent)) {
//            return false;
//        }
//        return true;
//    }

    private boolean planeBoxOverlap(Vec3D normal, float d, Vec3D maxbox) {
        Vec3D vmin = new Vec3D();
        Vec3D vmax = new Vec3D();

        if (normal.x > 0.0f) {
            vmin.x = -maxbox.x;
            vmax.x = maxbox.x;
        } else {
            vmin.x = maxbox.x;
            vmax.x = -maxbox.x;
        }

        if (normal.y > 0.0f) {
            vmin.y = -maxbox.y;
            vmax.y = maxbox.y;
        } else {
            vmin.y = maxbox.y;
            vmax.y = -maxbox.y;
        }

        if (normal.z > 0.0f) {
            vmin.z = -maxbox.z;
            vmax.z = maxbox.z;
        } else {
            vmin.z = maxbox.z;
            vmax.z = -maxbox.z;
        }
        if (normal.dot(vmin) + d > 0.0f) {
            return false;
        }
        if (normal.dot(vmax) + d >= 0.0f) {
            return true;
        }
        return false;
    }

    public BB set(BB box) {
        extent.set(box.extent);
        return set((XYZ) box);
    }

    /**
     * Updates the position of the box in space and calls
     * {@link #updateBounds()} immediately
     * 
     * @see toxi.geom.Vec3D#set(float, float, float)
     */
    @Override
    public Vec3D set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        updateBounds();
        return this;
    }

    /**
     * Updates the position of the box in space and calls
     * {@link #updateBounds()} immediately
     * 
     */
    @Override
    public BB set(XYZ v) {
        x = v.x();
        y = v.y();
        z = v.z();
        updateBounds();
        return this;
    }

    /**
     * Updates the size of the box and calls {@link #updateBounds()} immediately
     * 
     * @param extent
     *            new box size
     * @return itself, for method chaining
     */
    public BB setExtent(Vec3D extent) {
        this.extent = extent;
        return updateBounds();
    }

    private boolean testAxis(float a, float b, float fa, float fb, float va,
            float vb, float wa, float wb, float ea, float eb) {
        float p0 = a * va + b * vb;
        float p2 = a * wa + b * wb;
        float min, max;
        if (p0 < p2) {
            min = p0;
            max = p2;
        } else {
            min = p2;
            max = p0;
        }
        float rad = fa * ea + fb * eb;
        return (min > rad || max < -rad);
    }

//    public Mesh3D toMesh() {
//        return toMesh(null);
//    }
//
//    public Mesh3D toMesh(Mesh3D mesh) {
//        if (mesh == null) {
//            mesh = new TriangleMesh("aabb", 8, 12);
//        }
//        Vec3D a = min;
//        Vec3D g = max;
//        Vec3D b = new Vec3D(a.x, a.y, g.z);
//        Vec3D c = new Vec3D(g.x, a.y, g.z);
//        Vec3D d = new Vec3D(g.x, a.y, a.z);
//        Vec3D e = new Vec3D(a.x, g.y, a.z);
//        Vec3D f = new Vec3D(a.x, g.y, g.z);
//        Vec3D h = new Vec3D(g.x, g.y, a.z);
//        Vec2D ua = new Vec2D(0, 0);
//        Vec2D ub = new Vec2D(1, 0);
//        Vec2D uc = new Vec2D(1, 1);
//        Vec2D ud = new Vec2D(0, 1);
//        // left
//        mesh.addFace(a, b, f, ud, uc, ub);
//        mesh.addFace(a, f, e, ud, ub, ua);
//        // front
//        mesh.addFace(b, c, g, ud, uc, ub);
//        mesh.addFace(b, g, f, ud, ub, ua);
//        // right
//        mesh.addFace(c, d, h, ud, uc, ub);
//        mesh.addFace(c, h, g, ud, ub, ua);
//        // back
//        mesh.addFace(d, a, e, ud, uc, ub);
//        mesh.addFace(d, e, h, ud, ub, ua);
//        // top
//        mesh.addFace(e, f, h, ua, ud, ub);
//        mesh.addFace(f, g, h, ud, uc, ub);
//        // bottom
//        mesh.addFace(a, d, b, ud, uc, ua);
//        mesh.addFace(b, d, c, ua, uc, ub);
//        return mesh;
//    }

    /*
     * (non-Javadoc)
     * 
     * @see toxi.geom.Vec3D#toString()
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<aabb @").append(super.toString()).append("x")
                .append(extent);
        return sb.toString();
    }

    public BB union(AABB box) {
        min.minSelf(box.getMin());
        max.maxSelf(box.getMax());
        set(min.interpolateTo(max, 0.5f));
        extent.set(max.sub(min).scaleSelf(0.5f));
        return this;
    }

    /**
     * Updates the min/max corner points of the box. MUST be called after moving
     * the box in space by manipulating the public x,y,z coordinates directly.
     * 
     * @return itself
     */
    public final BB updateBounds() {
        // this is check is necessary for the constructor
        if (extent != null) {
            this.min = this.sub(extent);
            this.max = this.plus(extent);
        }
        return this;
    }

}