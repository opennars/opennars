package nars.nal.meta;

import com.gs.collections.api.block.function.primitive.BooleanFunction;
import nars.Op;
import nars.nal.PremiseMatch;
import nars.term.compound.GenericCompound;

import java.util.function.Consumer;

/**

 < (&&, cond1, cond2, ...) ==> (&|, fork1, fork2, ... ) >
 < (&&, cond1, cond2, ...) ==> end >
 */
public final class PremiseBranch extends GenericCompound implements Consumer<PremiseMatch>{

    public PremiseBranch(BooleanCondition<PremiseMatch>[] precondition, PremiseBranch[] children) {
        super(Op.IMPLICATION, new AndCondition(precondition),
                children != null ? new ThenFork(children) : Return.the);
    }

//    public BooleanCondition<PremiseMatch>[] getConditions() {
//        return (BooleanCondition<PremiseMatch>[]) ((Compound)term(0)).terms();
//    }



    @Override public void accept(PremiseMatch m) {

        if (getCondition().booleanValueOf(m)) {
            getConsequences().accept(m);
        }

    }

    public final BooleanFunction<PremiseMatch> getCondition() {
        return (BooleanFunction<PremiseMatch>) term(0);
    }

    public final Consumer<PremiseMatch> getConsequences() {
        return (Consumer<PremiseMatch>) term(1);
    }


    public static final class AndCondition<C> extends GenericCompound<BooleanCondition<C>> implements BooleanFunction<C>  {

        public AndCondition(BooleanCondition<C>[] p) {
            super(Op.CONJUNCTION, p);
        }

        @Override
        public final boolean booleanValueOf(C m) {
            for (BooleanCondition<C> x : terms()) {
                if (!x.booleanValueOf(m))
                    return false;
            }
            return true;
        }
    }

    public static final class ThenFork extends GenericCompound<PremiseBranch> implements Consumer<PremiseMatch> {

        public ThenFork(PremiseBranch[] children) {
            super(Op.PARALLEL, children);
        }


        @Override
        public final void accept(PremiseMatch m) {

            int now = m.now();

            for (PremiseBranch s : terms()) {
                s.accept(m);
                m.revert(now);
            }
        }
    }

    public static final class Return extends BooleanCondition<PremiseMatch> implements Consumer<PremiseMatch> {

        public static final Return the = new Return();

        private Return() {
            super();
        }

        @Override
        public String toString() {
            return "return";
        }


        @Override
        public void accept(PremiseMatch versioneds) {

        }

        @Override
        public boolean booleanValueOf(PremiseMatch versioneds) {
            return false;
        }
    }

}
