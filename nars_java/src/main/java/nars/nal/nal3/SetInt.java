package nars.nal.nal3;

import nars.nal.NALOperator;
import nars.nal.Terms;
import nars.nal.term.Compound;
import nars.nal.term.Compound1;
import nars.nal.term.Term;

import java.util.Collection;

/**
 * Created by me on 5/2/15.
 */
public interface SetInt extends SetTensional {


    public static Compound make(Collection<Term> l) {
        return make(l.toArray(new Term[l.size()]));
    }


    public static Compound make(Term... t) {
        t = Terms.toSortedSetArray(t);
        switch (t.length) {
            case 0: return null;
            case 1: return new SetInt1(t[0]);
            default: return new SetIntN(t);
        }
    }

    public static class SetInt1 extends Compound1 implements SetInt, SetTensional {

        public SetInt1(Term the) {
            super(the);

            init(term);
        }

        @Override
        public NALOperator operator() {
            return NALOperator.SET_INT;
        }

        @Override
        public Term clone() {
            return new SetInt1(the());
        }

        @Override
        public Term clone(Term[] replaced) {
            return SetInt.make(replaced);
        }

        @Override
        protected CharSequence makeName() {
            return SetTensional.makeSetName(NALOperator.SET_INT_OPENER.ch, NALOperator.SET_INT_CLOSER.ch, the());
        }
    }

}
