package nars.util.space;

import nars.Global;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;


public class OctBox<V extends XYZ> extends BB implements Shape3D {

    /**
     * alternative tree recursion limit, number of world units when cells are
     * not subdivided any further
     */
    protected Vec3D resolution;

    public final OctBox<V> parent;

    protected OctBox<V>[] children;

    protected Collection<V> points;


    /**
     * Constructs a new AbstractOctree node within the AABB cube volume: {o.x, o.y,
     * o.z} ... {o.x+size, o.y+size, o.z+size}
     *
     * @param p
     *            parent node
     * @param o
     *            tree origin
     * @param halfSize
     *            half length of the tree volume along a single axis
     */
    private OctBox(OctBox p, Vec3D o, Vec3D halfSize) {
        super(o.plus(halfSize), new Vec3D(halfSize));
        this.parent = p;
        if (parent != null) {
            resolution = parent.resolution;
        }


    }

    public final int depth() {
        OctBox p = this.parent;
        if (p == null) return 0;
        return p.depth() + 1;
    }

    /**
     * Constructs a new AbstractOctree node within the AABB cube volume: {o.x, o.y,
     * o.z} ... {o.x+size, o.y+size, o.z+size}
     *
     * @param o
     *            tree origin
     *            size of the tree volume along a single axis
     */
    public OctBox(Vec3D o, Vec3D extents, Vec3D resolution) {
        this(null, o, extents);
        this.resolution = resolution;
    }


    /**
     * Adds all points of the collection to the octree.
     *
     * @param points
     *            point collection
     * @return how many points were added
     */
    public int putAll(final Iterable<? extends V> points) {
        int count = 0;
        for (final V p : points) {
            if (ADD(p)!=null) count++;
        }
        return count;
    }

    //TODO memoize this result in a special leaf subclass
    public boolean belowResolution() {
        Vec3D extent = this.extent;
        Vec3D resolution = this.resolution;
        return extent.x <= resolution.x ||
                extent.y <= resolution.y ||
                extent.z <= resolution.z;
    }
    /**
     * Adds a new point/particle to the tree structure. All points are stored
     * within leaf nodes only. The tree implementation is using lazy
     * instantiation for all intermediate tree levels.
     *
     * @param p
     * @return the box it was inserted to, or null if wasn't
     */
    public OctBox<V> ADD(final V p) {



        // check if point is inside cube
        if (containsPoint(p)) {
            // only add points to leaves for now
            if (belowResolution()) {
                if (points == null) {
                    points = newPointsCollection();
                }
                points.add(p);
                return this;
            } else {
                if (children == null) {
                    children = new OctBox[8];
                }
                int octant = getOctantID(p);
                if (children[octant] == null) {
                    Vec3D e = this.extent;
                    Vec3D off = new Vec3D(
                            minX() + ((octant & 1) != 0 ? e.x() : 0),
                            minY() + ((octant & 2) != 0 ? e.y() : 0),
                            minZ() + ((octant & 4) != 0 ? e.z() : 0));
                    children[octant] = new OctBox(this, off,
                            e.scale(0.5f));
                }
                return children[octant].ADD(p);
            }
        }
        return null;
    }

    /** can be a list, set, etc.. */
    protected Collection<V> newPointsCollection() {
        return Global.newArrayList();
    }


    /**
     * Applies the given {@link OctreeVisitor} implementation to this node and
     * all of its children.
     */
    public void forEachInBox(Consumer<OctBox<V>> visitor) {
        visitor.accept(this);
        if (children!=null) {
            for (OctBox<V> c : children) {
                if (c != null) {
                    c.forEachInBox(visitor);
                }
            }
        }
    }



    public boolean containsPoint(XYZ p) {
        return p.isInAABB(this);
    }

    public void clear() {
        zero();
        children = null;
        points = null;
    }

    /**
     * @return a copy of the child nodes array
     */
    public OctBox[] getChildrenCopy() {
        if (children != null) {
            OctBox[] clones = new OctBox[8];
            System.arraycopy(children, 0, clones, 0, 8);
            return clones;
        }
        return null;
    }



    /**
     * Finds the leaf node which spatially relates to the given point
     *
     * @return leaf node or null if point is outside the tree dimensions
     */
    public OctBox getLeafForPoint(final XYZ p) {
        // if not a leaf node...
        if (p.isInAABB(this)) {
            final OctBox[] children = this.children;
            if (children!=null) {
                int octant = getOctantID(p);
                if (children[octant] != null) {
                    return children[octant].getLeafForPoint(p);
                }
            } else if (points != null) {
                return this;
            }
        }
        return null;
    }



    /**
     * Returns the minimum size of nodes (in world units). This value acts as
     * tree recursion limit since nodes smaller than this size are not
     * subdivided further. Leaf node are always smaller or equal to this size.
     *
     * @return the minimum size of tree nodes
     */
    public Vec3D getResolution() {
        return resolution;
    }


//    /**
//     * Computes the local child octant/cube index for the given point
//     *
//     * @param plocal
//     *            point in the node-local coordinate system
//     * @return octant index
//     */
//    protected final int getOctantID(final Vec3D plocal) {
//        final XYZ h = this.extent;
//
//        return (plocal.x >= h.x() ? 1 : 0) + (plocal.y >= h.y() ? 2 : 0)
//                + (plocal.z >= h.z() ? 4 : 0);
//    }

    /** computes getOctantID for the point subtracted by another point,
     *  without needing to allocate a temporary object

     */
    private int getOctantID(final XYZ p) {
        //final XYZ h = this.extent;
        return ((p.x() - x) >= 0 ? 1 : 0) + ((p.y() - y) >= 0 ? 2 : 0)
                + ((p.z() - z) >= 0 ? 4 : 0);
    }



    /**
     * @return the parent
     */
    public OctBox getParent() {
        return parent;
    }

    public Collection<V> getPoints() {
        if (points == null) return Collections.EMPTY_LIST;
        return points;
    }

    public int countPointsRecursively() {
        final int[] x = {0};
        forEachInBox(n -> x[0] += n.countPoints());
        return x[0];
    }

    public int countPoints() {
        if (points == null) return 0;
        return points.size();
    }

    public List<V> getPointsRecursively() {
        return getPointsRecursively(new ArrayList());
    }

    /**
     * @return the points
     */
    public List<V> getPointsRecursively(List<V> results) {
        if (points != null) {
            results.addAll(points);
        } else if (children!=null) {
            for (int i = 0; i < 8; i++) {
                if (children[i] != null) {
                    children[i].getPointsRecursively(results);
                }
            }
        }
        return results;
    }

    /**
     * Selects all stored points within the given axis-aligned bounding box.
     *
     * @param b
     *            AABB
     * @return all points with the box volume
     */
    @Deprecated public List<V> getPointsWithinBox(BB b) {
        ArrayList<V> results = null;
        if (this.intersectsBox(b)) {
            if (points != null) {
                for (V q : points) {
                    if (q.isInAABB(b)) {
                        if (results == null) {
                            results = new ArrayList();
                        }
                        results.add(q);
                    }
                }
            } else if (children!=null) {
                for (int i = 0; i < 8; i++) {
                    if (children[i] != null) {
                        List<V> points = children[i].getPointsWithinBox(b);
                        if (points != null) {
                            if (results == null) {
                                results = new ArrayList();
                            }
                            results.addAll(points);
                        }
                    }
                }
            }
        }
        return results;
    }

    public void forEachInBox(BB b, Consumer<V> c) {
        if (this.intersectsBox(b)) {
            if (points != null) {
                for (V q : points) {
                    if (q.isInAABB(b)) {
                        c.accept(q);
                    }
                }
            } else if (children!=null) {
                for (int i = 0; i < 8; i++) {
                    if (children[i] != null) {
                        children[i].forEachInBox(b, c);
                    }
                }
            }
        }
    }

    public void forEachNeighbor(V item, XYZ boxRadius, Consumer<OctBox> visitor) {
        //SOON
        throw new UnsupportedOperationException();
    }

    public void forEachInSphere(Sphere s, Consumer<V> c) {

        if (this.intersectsSphere(s)) {
            if (points != null) {
                for (V q : points) {
                    if (s.containsPoint(q)) {
                        c.accept(q);
                    }
                }
            } else if (children!=null) {
                for (int i = 0; i < 8; i++) {
                    OctBox cc = children[i];
                    if (cc != null) {
                        cc.forEachInSphere(s, c);
                    }
                }
            }
        }
    }


    /**
     * Selects all stored points within the given sphere volume
     *
     * @param s
     *            sphere
     * @return selected points
     */
    @Deprecated public List<XYZ> getPointsWithinSphere(Sphere s) {
        ArrayList<XYZ> results = null;
        if (this.intersectsSphere(s)) {
            if (points != null) {
                for (XYZ q : points) {
                    if (s.containsPoint(q)) {
                        if (results == null) {
                            results = new ArrayList();
                        }
                        results.add(q);
                    }
                }
            } else if (children!=null) {
                for (int i = 0; i < 8; i++) {
                    if (children[i] != null) {
                        List<XYZ> points = children[i].getPointsWithinSphere(s);
                        if (points != null) {
                            if (results == null) {
                                results = new ArrayList();
                            }
                            results.addAll(points);
                        }
                    }
                }
            }
        }
        return results;
    }



    /**
     * Selects all stored points within the given sphere volume
     *
     * @param sphereOrigin
     * @param clipRadius
     * @return selected points
     */
    public void forEachInSphere(Vec3D sphereOrigin, float clipRadius, Consumer<V> c) {
        forEachInSphere(new Sphere(sphereOrigin, clipRadius), c);
    }



    private void reduceBranch() {
        if (points != null && points.size() == 0) {
            points = null;
        }
        if (children!=null) {
            for (int i = 0; i < 8; i++) {
                if (children[i] != null && children[i].points == null) {
                    children[i] = null;
                }
            }
        }
        if (parent != null) {
            parent.reduceBranch();
        }
    }

    /**
     * Removes a point from the tree and (optionally) tries to release memory by
     * reducing now empty sub-branches.
     *
     * @return true, if the point was found & removed
     */
    public boolean remove(Object _p) {
        boolean found = false;
        V p = (V)_p;
        OctBox leaf = getLeafForPoint(p);
        if (leaf != null) {
            if (leaf.points.remove(p)) {
                found = true;
                if (leaf.points.size() == 0) {
                    leaf.reduceBranch();
                }
            }
        }
        return found;
    }

    public boolean removeAll(Collection<?> points) {
        boolean allRemoved = true;
        for (Object p : points) {
            allRemoved &= remove(p);
        }
        return allRemoved;
    }

    /*
     * (non-Javadoc)
     *
     * @see toxi.geom.AABB#toString()
     */
    public String toString() {
        String x = "<OctBox @" + super.toString() + '>';
        if (points!=null)
            x += "=" + points.toString();
        return x;
    }
}