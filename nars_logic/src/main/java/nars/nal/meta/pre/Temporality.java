package nars.nal.meta.pre;

import nars.nal.PremiseMatch;
import nars.nal.meta.AtomicBooleanCondition;
import nars.nal.meta.BooleanCondition;

public abstract class Temporality extends AtomicBooleanCondition<PremiseMatch> {

    /** both task and belief are temporal (non-eternal) */
    public static final BooleanCondition<PremiseMatch> both = new Temporality() {

        @Override public String toString() {
            return "Event";
        }

        @Override
        public boolean booleanValueOf(PremiseMatch m) {
            return m.premise.isEvent();
        }

    };

//    /** either task or belief is temporal (non-eternal) */
//    public static final PreCondition either = new Temporality() {
//
//        @Override public String toString() {
//            return "Temporal";
//        }
//
//        @Override
//        public boolean test(RuleMatch m) {
//            return m.premise.isTemporal();
//        }
//
//    };

    protected Temporality() {
    }

}
