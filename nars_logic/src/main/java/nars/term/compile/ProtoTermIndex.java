//package nars.term.compile;
//
//import nars.term.atom.Atom;
//import nars.term.Term;
//import nars.term.Termed;
//import nars.term.transform.CompoundTransform;
//import net.bytebuddy.ByteBuddy;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.function.Consumer;
//
//
//public class ProtoTermIndex extends ByteBuddy implements TermIndex {
//
//    public final Map<Term, Term> terms = new HashMap();
//
//    @Override
//    public Termed get(Term t) {
//        return terms.computeIfAbsent(t, this::compile);
//    }
//
////    abstract public static class ProtoAtomic extends Atomic {
////
////        //public static final byte[]
////    }
//
//    public Term compile(Term t) {
//        if (t instanceof Atom) {
////            return new AbstractAtomic() {
////
////            };
//
//
////            DynamicType.Unloaded<AbstractAtomic> uc = subclass(AbstractAtomic.class)
////                    .method(named("bytes")).intercept(value(t.bytes()))
////                    .method(named("getByteLen")).intercept(value(t.getByteLen()))
////                    .method(named("hashCode")).intercept(value(t.hashCode()))
////                    .method(named("setBytes")).intercept(StubMethod.INSTANCE)
////                    .method(named("op")).intercept(value(t.op()))
////                    .method(named("structure")).intercept(value(t.structure()))
////                    //.method(named("toString")).intercept(SuperMethodCall.INSTANCE)
////                    .make();
////
//
//        }
//        return t;
//    }
//
//    @Override
//    public final void forEachTerm(Consumer<Termed> c) {
//        terms.forEach((k, v) -> c.accept(v));
//    }
//
//    @Override
//    public CompoundTransform getCompoundTransformer() {
//        return null;
//    }
//
//    public static void main(String[] args) {
//        ProtoTermIndex i = new ProtoTermIndex() {
//            @Override
//            public Termed get(Term t) {
//                Termed u = super.get(t);
//                System.out.println(t);
//                System.out.println(u);
//                System.out.println(u.hashCode());
//                System.out.println(u.equals(t));
//                return u;
//            }
//        };
//        i.get(Atom.the("xyzxyzxyz"));
//    }
//}
