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
 * Readonly, immutable interface wrapper for Vec3D instances. Used throughout
 * the library for safety purposes.
 */
public interface roVec3D extends XYZ {

    /**
     * Adds vector {a,b,c} and returns result as new vector.
     * 
     * @param a
     *            X coordinate
     * @param b
     *            Y coordinate
     * @param c
     *            Z coordinate
     * 
     * @return result as new vector
     */
    public Vec3D plus(float a, float b, float c);

    public Vec3D plus(roVec3D v);

    /**
     * Add vector v and returns result as new vector.
     * 
     * @param v
     *            vector to add
     * 
     * @return result as new vector
     */
    public Vec3D plus(Vec3D v);

    /**
     * Computes the angle between this vector and vector V. This function
     * assumes both vectors are normalized, if this can't be guaranteed, use the
     * alternative implementation {@link #angleBetween(roVec3D, boolean)}
     * 
     * @param v
     *            vector
     * 
     * @return angle in radians, or NaN if vectors are parallel
     */
    public float angleBetween(XYZ v);

    /**
     * Computes the angle between this vector and vector V.
     * 
     * @param v
     *            vector
     * @param forceNormalize
     *            true, if normalized versions of the vectors are to be used
     *            (Note: only copies will be used, original vectors will not be
     *            altered by this method)
     * 
     * @return angle in radians, or NaN if vectors are parallel
     */
    public float angleBetween(XYZ v, boolean forceNormalize);

    /**
     * Compares the length of the vector with another one.
     * 
     * @param v
     *            vector to compare with
     * 
     * @return -1 if other vector is longer, 0 if both are equal or else +1
     */
    public int compareTo(roVec3D v);

    /**
     * Copy.
     * 
     * @return a new independent instance/copy of a given vector
     */
    public Vec3D copy();

    /**
     * Calculates cross-product with vector v. The resulting vector is
     * perpendicular to both the current and supplied vector.
     *
     * @param v
     *            vector to cross
     *
     * @return cross-product as new vector
     */
    public Vec3D cross(XYZ v);

    /**
     * Calculates cross-product with vector v. The resulting vector is
     * perpendicular to both the current and supplied vector and stored in the
     * supplied result vector.
     * 
     * @param v
     *            vector to cross
     * @param result
     *            result vector
     * 
     * @return result vector
     */
    public Vec3D crossInto(XYZ v, Vec3D result);


    /**
     * Computes the scalar product (dot product) with the given vector.
     * 
     * @param v
     *            the v
     * 
     * @return dot product
     * 
     * @see <a href="http://en.wikipedia.org/wiki/Dot_product">Wikipedia
     *      entry</a>
     */
    public float dot(XYZ v);

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj);

    /**
     * Compares this vector with the one given. The vectors are deemed equal if
     * the individual differences of all component values are within the given
     * tolerance.
     * 
     * @param v
     *            the v
     * @param tolerance
     *            the tolerance
     * 
     * @return true, if equal
     */
    public boolean equalsWithTolerance(roVec3D v, float tolerance);

    /**
     * Gets the abs.
     * 
     * @return the abs
     */
    public Vec3D getAbs();

    /**
     * Converts the spherical vector back into cartesian coordinates.
     * 
     * @return new vector
     */
    public XYZ getCartesian();

//    public Axis getClosestAxis();
//
//    public float getComponent(Axis id);

    public float getComponent(int id);

    /**
     * Creates a copy of the vector which forcefully fits in the given AABB.
     * 
     * @param box
     *            the box
     * 
     * @return fitted vector
     */
    public XYZ getConstrained(AABB box);

    /**
     * Creates a new vector whose components are the integer value of their
     * current values.
     * 
     * @return result as new vector
     */
    public XYZ getFloored();

    /**
     * Creates a new vector whose components are the fractional part of their
     * current values.
     * 
     * @return result as new vector
     */
    public XYZ getFrac();

    /**
     * Scales vector uniformly by factor -1 ( v = -v ).
     * 
     * @return result as new vector
     */
    public Vec3D getInverted();

    /**
     * Creates a copy of the vector with its magnitude limited to the length
     * given.
     * 
     * @param lim
     *            new maximum magnitude
     * 
     * @return result as new vector
     */
    public XYZ getLimited(float lim);

//    /**
//     * Produces a new vector with its coordinates passed through the given
//     * {@link ScaleMap}.
//     *
//     * @param map
//     * @return mapped vector
//     */
//    public XYZ getMapped(ScaleMap map);


    /**
     * Produces a new vector normalized to the given length.
     * 
     * @param len
     *            new desired length
     * 
     * @return new vector
     */
    public Vec3D getNormalizedTo(float len);

    /**
     * Returns a multiplicative inverse copy of the vector.
     * 
     * @return new vector
     */
    public XYZ getReciprocal();

    public XYZ getReflected(roVec3D normal);

    /**
     * Gets the rotated around axis.
     * 
     * @param axis
     *            the axis
     * @param theta
     *            the theta
     * 
     * @return new result vector
     */
    public XYZ getRotatedAroundAxis(roVec3D axis, float theta);

    /**
     * Creates a new vector rotated by the given angle around the X axis.
     * 
     * @param theta
     *            the theta
     * 
     * @return rotated vector
     */
    public XYZ getRotatedX(float theta);

    /**
     * Creates a new vector rotated by the given angle around the Y axis.
     * 
     * @param theta
     *            the theta
     * 
     * @return rotated vector
     */
    public XYZ getRotatedY(float theta);

    /**
     * Creates a new vector rotated by the given angle around the Z axis.
     * 
     * @param theta
     *            the theta
     * 
     * @return rotated vector
     */
    public XYZ getRotatedZ(float theta);

    /**
     * Creates a new vector with its coordinates rounded to the given precision
     * (grid alignment).
     * 
     * @param prec
     * @return grid aligned vector
     */
    public XYZ getRoundedTo(float prec);

    /**
     * Creates a new vector in which all components are replaced with the signum
     * of their original values. In other words if a components value was
     * negative its new value will be -1, if zero => 0, if positive => +1
     * 
     * @return result vector
     */
    public Vec3D getSignum();

    /**
     * Converts the vector into spherical coordinates. After the conversion the
     * vector components are to be interpreted as:
     * <ul>
     * <li>x = radius</li>
     * <li>y = azimuth</li>
     * <li>z = theta</li>
     * </ul>
     * 
     * @return new vector
     */
    public XYZ getSpherical();

    /**
     * Computes the vector's direction in the XY plane (for example for 2D
     * points). The positive X axis equals 0 degrees.
     * 
     * @return rotation angle
     */
    public float headingXY();

    /**
     * Computes the vector's direction in the XZ plane. The positive X axis
     * equals 0 degrees.
     * 
     * @return rotation angle
     */
    public float headingXZ();

    /**
     * Computes the vector's direction in the YZ plane. The positive Z axis
     * equals 0 degrees.
     * 
     * @return rotation angle
     */
    public float headingYZ();


    /**
     * Checks if the point is inside the given AABB.
     * 
     * @param box
     *            bounding box to check
     * 
     * @return true, if point is inside
     */


    /**
     * Checks if the point is inside the given axis-aligned bounding box.
     * 
     * @param boxOrigin
     *            bounding box origin/center
     * @param boxExtent
     *            bounding box extends (half measure)
     * 
     * @return true, if point is inside the box
     */

    public boolean isInAABB(Vec3D boxOrigin, Vec3D boxExtent);

    /**
     * Checks if the vector is parallel with either the X or Y axis (any
     * direction).
     * 
     * @param tolerance
     * @return true, if parallel within the given tolerance
     */
    public boolean isMajorAxis(float tolerance);

    /**
     * Checks if vector has a magnitude equals or close to zero (tolerance used
     * is {@link MathUtils#EPS}).
     * 
     * @return true, if zero vector
     */
    public boolean isZeroVector();







    /**
     * Scales vector non-uniformly by vector v and returns result as new vector.
     * 
     * @param s
     *            scale vector
     * 
     * @return new vector
     */
    public XYZ scale(roVec3D s);

    /**
     * Subtracts vector {a,b,c} and returns result as new vector.
     * 
     * @param a
     *            X coordinate
     * @param b
     *            Y coordinate
     * @param c
     *            Z coordinate
     * 
     * @return result as new vector
     */
    public XYZ sub(float a, float b, float c);

    /**
     * Subtracts vector v and returns result as new vector.
     * 
     * @param v
     *            vector to be subtracted
     * 
     * @return result as new vector
     */
    public Vec3D sub(roVec3D v);

//    /**
//     * Creates a new 2D vector of the XY components.
//     *
//     * @return new vector
//     */
//    public Vec2D to2DXY();
//
//    /**
//     * Creates a new 2D vector of the XZ components.
//     *
//     * @return new vector
//     */
//    public Vec2D to2DXZ();

//    /**
//     * Creates a new 2D vector of the YZ components.
//     *
//     * @return new vector
//     */
//    public Vec2D to2DYZ();
//
//    /**
//     * Creates a Vec4D instance of this vector with the w component set to 1.0
//     *
//     * @return 4d vector
//     */
//    public Vec4D to4D();
//
//    /**
//     * Creates a Vec4D instance of this vector with it w component set to the
//     * given value.
//     *
//     * @param w
//     * @return weighted 4d vector
//     */
//    public Vec4D to4D(float w);

    public float[] toArray3();

    public float[] toArray4(float w);

}