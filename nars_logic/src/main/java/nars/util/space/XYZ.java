package nars.util.space;

import java.io.Serializable;

/**
 * Created by me on 6/13/15.
 */
public interface XYZ extends Serializable {

    float x();

    float y();

    float z();

    default XYZ interpolateTo(XYZ v, float f) {
        return new Vec3D(x() + (v.x() - x()) * f, y() + (v.y() - y()) * f, z()
                + (v.z() - z()) * f);
    }
//
//    default public XYZ interpolateTo(XYZ v, float f,
//                                     InterpolateStrategy s) {
//        return new Vec3D(s.interpolate(x(), v.x(), f),
//                s.interpolate(y(), v.y(), f), s.interpolate(z(), v.z(), f));
//    }

    default Vec3D interpolateTo(Vec3D v, float f) {
        return new Vec3D(x() + (v.x - x()) * f, y() + (v.y - y()) * f, z() + (v.z - z())
                * f);
    }

//    default public XYZ interpolateTo(Vec3D v, float f, InterpolateStrategy s) {
//        return new Vec3D(s.interpolate(x(), v.x, f), s.interpolate(y(), v.y, f),
//                s.interpolate(z(), v.z, f));
//    }


    default Vec3D scale(float s) {
        return new Vec3D(x() * s, y() * s, z() * s);
    }

    default Vec3D sub(XYZ v) {
        return new Vec3D(x() - v.x(), y() - v.y(), z() - v.z());
    }

    default float distanceTo(XYZ v) {
        if (v != null) {
            final float dx = x() - v.x();
            final float dy = y() - v.y();
            final float dz = z() - v.z();
            return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        } else {
            return Float.NaN;
        }
    }

    default float distanceToSquared(roVec3D v) {
        if (v != null) {
            final float dx = x() - v.x();
            final float dy = y() - v.y();
            final float dz = z() - v.z();
            return dx * dx + dy * dy + dz * dz;
        } else {
            return Float.NaN;
        }
    }


    /*
     * (non-Javadoc)
     *
     * @see toxi.geom.ReadonlyVec3D#getNormalized()
     */
    default Vec3D getNormalized() {
        return new Vec3D(this).normalize();
    }


    /*
     * (non-Javadoc)
     *
     * @see toxi.geom.ReadonlyVec3D#isInAABB(toxi.geom.AABB)
     */
    default boolean isInAABB(final BB box) {
        return box.contains(this);
    }

    default float magnitude() {
        return (float) Math.sqrt(magSquared());
    }

    default float magSquared() {
        final float x = x();
        final float y = y();
        final float z = z();
        return x*x + y*y + z*z;
    }


    default XYZ copy() { return new Vec3D(this); }
}