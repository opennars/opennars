package nars.nal.meta;

import com.google.common.base.Joiner;
import com.gs.collections.api.block.function.primitive.BooleanFunction;
import nars.Op;
import nars.nal.PremiseMatch;
import nars.term.atom.Atom;
import nars.term.compound.GenericCompound;

import java.util.stream.Stream;

/**

 < (&&, cond1, cond2, ...) ==> (&|, fork1, fork2, ... ) >
 < (&&, cond1, cond2, ...) ==> end >
 */
public final class PremiseBranch extends GenericCompound implements ProcTerm<PremiseMatch> {

    public final transient AndCondition<PremiseMatch> cond;
    public final transient ProcTerm<PremiseMatch> conseq;

    public static ProcTerm<PremiseMatch> branch(BooleanCondition<PremiseMatch>[] condition, ProcTerm<PremiseMatch>[] conseq) {
        if (conseq!=null && conseq.length > 0) {
            return new PremiseBranch(condition,conseq);
        } else {
            return Return.the;
        }
    }

    @Override
    public void appendJavaProcedure(StringBuilder s) {
        s.append("if (");
        cond.appendJavaCondition(s);
        s.append(") {\n");
        s.append("\t ");
        conseq.appendJavaProcedure(s);
        s.append("\n}");
    }

    protected PremiseBranch(BooleanCondition<PremiseMatch>[] cond, ProcTerm<PremiseMatch>[] conseq) {
        super(Op.IMPLICATION, new AndCondition(cond),  new ThenFork(conseq));
        this.cond = (AndCondition<PremiseMatch>) term(0);
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

        public void appendJavaCondition(StringBuilder s) {
            Joiner.on(" && ").appendTo(s, Stream.of(terms()).map(
                b -> ('(' + b.toJavaConditionString() + ')'))
                    .iterator()
            );
        }
    }

    public static final class ThenFork extends GenericCompound<ProcTerm<PremiseMatch>> implements ProcTerm<PremiseMatch> {

        public ThenFork(ProcTerm<PremiseMatch>[] children) {
            super(Op.PARALLEL, children);
        }

        @Override
        public void appendJavaProcedure(StringBuilder s) {
            //s.append("/* " + this + "*/");
            for (ProcTerm<PremiseMatch> p : terms()) {
                s.append("\t\t");
                p.appendJavaProcedure(s);
                s.append("\n");
            }
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
        public void appendJavaProcedure(StringBuilder s) {
            s.append("return;");
        }

        @Override
        public void accept(PremiseMatch versioneds) {

        }

    }

}
