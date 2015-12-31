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

    public static ProcTerm<PremiseMatch> branch(BooleanCondition<PremiseMatch>[] condition, ProcTerm<PremiseMatch>... conseq) {
        if (conseq!=null && conseq.length > 0) {
            return new PremiseBranch( condition, conseq);
        } else {
            return new PremiseBranch(condition, Return.the );
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

    protected PremiseBranch(BooleanCondition<PremiseMatch>[] cond, ProcTerm<PremiseMatch>... conseq) {
        super(Op.IMPLICATION, new AndCondition(cond),  new ThenFork(conseq));
        this.cond = (AndCondition<PremiseMatch>) term(0);
        this.conseq = (ProcTerm<PremiseMatch>) term(1);
    }

    @Override public void accept(PremiseMatch m) {
        if (cond.booleanValueOf(m)) {
            conseq.accept(m);
        }
    }

    public static final class AndCondition<C> extends GenericCompound<BooleanCondition<C>> implements BooleanFunction<C>  {

        public AndCondition(BooleanCondition<C>[] p) {
            super(Op.CONJUNCTION, p);
        }

        @Override
        public boolean booleanValueOf(C m) {
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

        //private final MethodHandle method;

        public ThenFork(ProcTerm<PremiseMatch>[] children) {
            super(Op.PARALLEL, children);


//            try {
//                MethodHandles.Lookup l = MethodHandles.publicLookup();
//
//                Binder b = null;
//                for (ProcTerm p : children) {
//                    //MethodHandle ph = l.findVirtual(p.getClass(), "accept", methodType(PremiseMatch.class));
//
//
//                    Binder a = new Binder(Binder.from(PremiseMatch.class, PremiseMatch.class)
//                            .append(ProcTerm.class, p))
//                            .foldStatic(ThenFork.class, "fork").dropLast(2);
//
//                    if (b!=null)
//                        b = a.to(b);
//                    else
//                        b = a;
//
//                }
//                this.method = b!=null ? b.identity() : null;
//
//            } catch (Exception e) {
//                e.printStackTrace();
//                throw new RuntimeException(e);
//            }
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

        public static PremiseMatch fork(PremiseMatch m, ProcTerm proc) {
            int revertTime = m.now();
            proc.accept(m);
            m.revert(revertTime);
            return m;
        }

        @Override
        public void accept(PremiseMatch m) {

//            try {
//                method.invoke(m);
//            } catch (Throwable throwable) {
//                throwable.printStackTrace();
//            }

            int revertTime = m.now();
            for (ProcTerm<PremiseMatch> s : terms()) {
                s.accept(m);
                m.revert(revertTime);
            }
        }
    }

    public static final class Return extends Atom implements ProcTerm<PremiseMatch> {

        public static final ProcTerm<PremiseMatch> the = new Return();

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
