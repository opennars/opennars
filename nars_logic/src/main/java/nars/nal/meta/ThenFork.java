package nars.nal.meta;

import nars.Op;
import nars.term.compound.GenericCompound;

/**
 * Created by me on 12/31/15.
 */
abstract public class ThenFork<C> extends GenericCompound<ProcTerm<C>> implements ProcTerm<C> {

    //private final MethodHandle method;

    public ThenFork(ProcTerm<C>... actions) {
        super(Op.PARALLEL, actions);


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
        for (ProcTerm<C> p : terms()) {
            s.append("\t\t");
            p.appendJavaProcedure(s);
            s.append("\n");
        }
    }

//    public static PremiseMatch fork(PremiseMatch m, ProcTerm<PremiseMatch> proc) {
//        int revertTime = m.now();
//        proc.accept(m);
//        m.revert(revertTime);
//        return m;
//    }

//        @Override public void accept(C m) {
//
////            try {
////                method.invoke(m);
////            } catch (Throwable throwable) {
////                throwable.printStackTrace();
////            }
//
//            int revertTime = m.now();
//            for (ProcTerm<PremiseMatch> s : terms()) {
//                s.accept(m);
//                m.revert(revertTime);
//            }
//        }
}
