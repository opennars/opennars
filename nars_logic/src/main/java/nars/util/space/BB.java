package nars.util.space;


import java.util.List;

/**
 * Created by me on 6/14/15.
 */
public abstract class BB extends Vec3D {


    protected Vec3D extent;

    public BB() {
        super();
    }

    public BB(XYZ v) {
        super(v);
    }

    public BB(Vec3D center, Vec3D radius) {
        super(center);
        this.extent = (radius);
    }

    /**
     * Creates a new instance from two vectors specifying opposite corners of
     * the box
     *
     * @param min
     *            first corner point
     * @param max
     *            second corner point
     * @return new AABB with centre at the half point between the 2 input
     *         vectors
     */
    public static final BB fromMinMax(Vec3D min, Vec3D max) {
        Vec3D a = Vec3D.min(min, max);
        Vec3D b = Vec3D.max(min, max);
        return new AABB(a.interpolateTo(b, 0.5f), b.sub(a).scaleSelf(0.5f));
    }

    /**
     * Factory method, computes & returns the bounding box for the given list of
     * points.
     *
     * @param points
     * @return bounding rect
     */
    public static final BB getBoundingBox(List<? extends XYZ> points) {
        if (points == null || points.size() == 0) {
            return null;
        }
        XYZ first = points.get(0);
        Vec3D min = new Vec3D(first);
        Vec3D max = new Vec3D(first);
        int n = points.size();
        if (n > 1) {
            for (int i = 1; i < n; i++) {
                XYZ p = points.get(i);
                min.minSelf(p);
                max.maxSelf(p);
            }
        }
        return fromMinMax(min, max);
    }

    public boolean containsPoint(XYZ p) {
        return p.isInAABB(this);
    }

    public Sphere getBoundingSphere() {
        return new Sphere(this, extent.magnitude());
    }

    /**
     * Returns the current box size as new Vec3D instance (updating this vector
     * will NOT update the box size! Use {@link #setExtent(roVec3D)} for
     * those purposes)
     *
     * @return box size
     */
    public final Vec3D getExtent() {
        return extent.copy();
    }

    /**
     * Checks if the box intersects the passed in one.
     *
     * @param box
     *            box to check
     * @return true, if boxes overlap
     */
    public boolean intersectsBox(final BB box) {
        return MathUtils.abs(box.x - x) <= (extent.x + box.extent.x)
                && MathUtils.abs(box.y - y) <= (extent.y + box.extent.y)
                && MathUtils.abs(box.z - z) <= (extent.z + box.extent.z);
    }



    public boolean intersectsSphere(Sphere s) {
        return intersectsSphere(s, s.radius);
    }

    /**
     * @param c
     *            sphere centre
     * @param r
     *            sphere radius
     * @return true, if AABB intersects with sphere
     */
    public boolean intersectsSphere(Vec3D c, float r) {
        float s, d = 0;
        // find the square of the distance
        // from the sphere to the box
        if (c.x < minX()) {
            s = c.x - minX();
            d = s * s;
        } else if (c.x > maxX()) {
            s = c.x - maxX();
            d += s * s;
        }

        if (c.y < minY()) {
            s = c.y - minY();
            d += s * s;
        } else if (c.y > maxY()) {
            s = c.y - maxY();
            d += s * s;
        }

        if (c.z < minZ()) {
            s = c.z - minZ();
            d += s * s;
        } else if (c.z > maxZ()) {
            s = c.z - maxZ();
            d += s * s;
        }

        return d <= r * r;
    }

    public boolean contains(final XYZ v) {
        final float x = v.x();
        if (x < minX() || x > maxX()) {
            return false;
        }

        final float y = v.y();
        if (y < minY() || y > maxY()) {
            return false;
        }

        final float z = v.z();
        if (z < minZ() || z > maxZ()) {
            return false;
        }

        return true;
    }


    public float minX() { return x - extent.x(); }
    public float maxX() { return x + extent.x(); }
    public float minY() { return y - extent.y(); }
    public float maxY() { return y + extent.y(); }
    public float minZ() { return z - extent.z(); }
    public float maxZ() { return z + extent.z(); }

}