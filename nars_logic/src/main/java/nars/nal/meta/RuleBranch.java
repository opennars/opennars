package nars.nal.meta;

import nars.Op;
import nars.nal.PremiseMatch;
import nars.term.compound.Compound;
import nars.term.compound.GenericCompound;

/**

 < (&&, cond1, cond2, ...) =/> (&|, fork1, fork2, ... ) >
 < (&&, cond1, cond2, ...) =/> end >
 */
public final class RuleBranch extends GenericCompound {

    public static final class RuleCondition extends GenericCompound<BooleanCondition<PremiseMatch>> {
        public RuleCondition(BooleanCondition<PremiseMatch>[] p) {
            super(Op.CONJUNCTION, p);
        }
    }

    public static final class RuleFork extends GenericCompound<RuleBranch> {
        public RuleFork(RuleBranch[] children) {
            super(Op.PARALLEL, children);
        }
    }

    public static final class Return extends BooleanCondition<PremiseMatch> {

        public static final Return the = new Return();

        private Return() {
            super();
        }

        @Override
        public String toString() {
            return "return";
        }

        @Override
        public boolean eval(PremiseMatch context) {
            return false;
        }
    }

    public RuleBranch(BooleanCondition<PremiseMatch>[] precondition, RuleBranch[] children) {
        super(Op.IMPLICATION, new RuleCondition(precondition),
                children != null ? new RuleFork(children) : Return.the);
    }

    public BooleanCondition<PremiseMatch>[] getConditions() {
        return (BooleanCondition<PremiseMatch>[]) ((Compound)term(0)).terms();
    }

}
