package nars.nal.meta.pre;

import nars.nal.RuleMatch;
import nars.nal.meta.PreCondition;

abstract public class Temporality extends PreCondition {

    /** both task and belief are temporal (non-eternal) */
    public static final PreCondition both = new Temporality() {

        @Override public final String toString() {
            return "Event";
        }

        @Override
        public final boolean test(final RuleMatch m) {
            return m.premise.isEvent();
        }

    };

    /** either task or belief is temporal (non-eternal) */
    public static final PreCondition either = new Temporality() {

        @Override public final String toString() {
            return "Temporal";
        }

        @Override
        public final boolean test(final RuleMatch m) {
            return m.premise.isTemporal();
        }

    };

    protected Temporality() {
        super();
    }

}
