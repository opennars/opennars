package nars.util.space;

import nars.term.Atom;
import nars.util.data.Util;

/**
 * Octree indexed by terms
 */
public class OctTermTree extends OctBox {

    public static class FloatAtom extends Atom {

        //TODO more efficient representation
        public FloatAtom(float f, float epsilon) {
            super(String.valueOf(Util.round(f, epsilon)));
        }

    }

    public OctTermTree(Vec3D o, Vec3D extents, Vec3D resolution) {
        super(o, extents, resolution);
    }

}
