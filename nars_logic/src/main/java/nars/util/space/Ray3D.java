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
 * A simple 3D ray datatype
 */
public class Ray3D extends Vec3D {

    protected Vec3D dir;

    public Ray3D() {
        super();
        dir = Vec3D.Y_AXIS.copy();
    }

    public Ray3D(float x, float y, float z, XYZ d) {
        super(x, y, z);
        dir = d.getNormalized();
    }

    public Ray3D(XYZ o, XYZ d) {
        this(o.x(), o.y(), o.z(), d);
    }

    /**
     * Returns a copy of the ray's direction vector.
     * 
     * @return vector
     */
    public Vec3D getDirection() {
        return dir.copy();
    }

    /**
     * Calculates the distance between the given point and the infinite line
     * coinciding with this ray.
     * 
     * @param p
     * @return distance
     */
    public float getDistanceToPoint(Vec3D p) {
        Vec3D sp = p.sub(this);
        return sp.distanceTo(dir.scale(sp.dot(dir)));
    }

    /**
     * Returns the point at the given distance on the ray. The distance can be
     * any real number.
     * 
     * @param dist
     * @return vector
     */
    public Vec3D getPointAtDistance(float dist) {
        return plus(dir.scale(dist));
    }

    /**
     * Uses a normalized copy of the given vector as the ray direction.
     * 
     * @param d
     *            new direction
     * @return itself
     */
    public Ray3D setDirection(roVec3D d) {
        dir.set(d).normalize();
        return this;
    }

    public Ray3D setNormalizedDirection(roVec3D d) {
        dir.set(d);
        return this;
    }
//
//    /**
//     * Converts the ray into a 3D Line segment with its start point coinciding
//     * with the ray origin and its other end point at the given distance along
//     * the ray.
//     *
//     * @param dist
//     *            end point distance
//     * @return line segment
//     */
//    public Line3D toLine3DWithPointAtDistance(float dist) {
//        return new Line3D(this, getPointAtDistance(dist));
//    }

    public String toString() {
        return "origin: " + super.toString() + " dir: " + dir;
    }
}