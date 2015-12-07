package nars.nal;

import nars.MapIndex;
import nars.Op;
import nars.nal.meta.match.Ellipsis;
import nars.nal.nal4.Image;
import nars.term.Term;
import nars.term.compound.Compound;
import nars.term.compound.CompoundN;
import nars.term.transform.FindSubst;

import java.util.HashMap;

/**
 * Created by me on 12/7/15.
 */
public class PatternIndex extends MapIndex {

    public PatternIndex() {
        super(new HashMap(1024));
    }

    @Override
    protected <T extends Term> Compound<T> compileCompound(Compound<T> c) {
        Compound x = super.compileCompound(c);
        if (x instanceof Compound) {
            if (!x.isCommutative() && !Ellipsis.hasEllipsis(x)) {
                if (!(x instanceof Image)) {
                    x = new LinearCompoundPattern(x);
                }
            }
        }
        return x;
    }

    /** non-commutive simple compound which can match subterms in any order, but this order is prearranged optimally */
    final static class LinearCompoundPattern extends CompoundN {

        private final Compound seed;
        private final Op op;
        private final int structureCached;
        private final int sizeCached;

        public LinearCompoundPattern(Compound seed) {
            super((CompoundN)seed);
            this.seed = seed;
            this.op = seed.op();
            this.structureCached = seed.structure();
            this.sizeCached = seed.size();
        }

        @Override
        public final boolean match(Compound y, FindSubst subst) {
            if (y.size() != sizeCached)
                return false;
                    //this term as a pattern involves anything y does not?
                    //((yStructure | structure()) == yStructure) &&
                    //&&
                    // ?? && ( y.volume() >= volume() )

            return matchLinear(y, subst);
                /*
                        int s = size();
        if (s == 2) {
            //HACK - match smallest (least specific) first
            int v0 = term(0).volume();
            int v1 = term(1).volume();
            if (v0 <= v1) {
                return matchSubterm(0, y, subst)&&
                       matchSubterm(1, y, subst);
            } else {
                return matchSubterm(1, y, subst)&&
                       matchSubterm(0, y, subst);
            }
        } else {
            return subst.matchLinear(this, y, 0, size());
        }

                 */

        }


        @Override public int size() {
            return sizeCached;
        }

        @Override
        public int structure() {
            return structureCached;
        }


        @Override
        public Op op() {
            return op;
        }

        @Override
        public boolean isCommutative() {
            return false;
        }

        @Override
        public Term clone() {
            return seed.clone();
        }

        @Override
        public Term clone(Term[] replaced) {
            return seed.clone(replaced);
        }
    }
}
