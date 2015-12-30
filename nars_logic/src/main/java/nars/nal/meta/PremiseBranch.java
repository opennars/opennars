package nars.nal.meta;

import com.gs.collections.api.block.function.primitive.BooleanFunction;
import nars.Op;
import nars.nal.PremiseMatch;
import nars.term.atom.Atom;
import nars.term.compound.GenericCompound;

/**

 < (&&, cond1, cond2, ...) ==> (&|, fork1, fork2, ... ) >
 < (&&, cond1, cond2, ...) ==> end >
 */
public final class PremiseBranch extends GenericCompound implements ProcTerm<PremiseMatch> {

    public final transient BooleanFunction<PremiseMatch> cond;
    public final transient ProcTerm<PremiseMatch> conseq;

    public static ProcTerm<PremiseMatch> branch(BooleanCondition<PremiseMatch>[] condition, ProcTerm<PremiseMatch>[] conseq) {
        if (conseq!=null && conseq.length > 0) {
            return new PremiseBranch(condition,conseq);
        } else {
            return Return.the;
        }
    }


    protected PremiseBranch(BooleanCondition<PremiseMatch>[] cond, ProcTerm<PremiseMatch>[] conseq) {
        super(Op.IMPLICATION, new AndCondition(cond),  new ThenFork(conseq));
        this.cond = (BooleanFunction<PremiseMatch>) term(0);
        this.conseq = (ProcTerm<PremiseMatch>) term(1);
    }

    @Override public final void accept(PremiseMatch m) {
        if (cond.booleanValueOf(m)) {
            conseq.accept(m);
        }
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

    public static final class ThenFork extends GenericCompound<ProcTerm<PremiseMatch>> implements ProcTerm<PremiseMatch> {

        public ThenFork(ProcTerm<PremiseMatch>[] children) {
            super(Op.PARALLEL, children);
        }


        @Override
        public final void accept(PremiseMatch m) {

            int now = m.now();

            for (ProcTerm<PremiseMatch> s : terms()) {
                s.accept(m);
                m.revert(now);
            }
        }
    }

    public static final class Return extends Atom implements ProcTerm<PremiseMatch> {

        public static final Return the = new Return();

        private Return() {
            super("return");
        }

        @Override
        public void accept(PremiseMatch versioneds) {

        }

    }

}
