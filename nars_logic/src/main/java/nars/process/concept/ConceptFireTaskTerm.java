//package nars.process.concept;
//
//import nars.link.TermLink;
//import nars.nal.LogicStage;
//import nars.process.ConceptProcess;
//
///** when a concept fires a tasklink that fires a termlink */
//abstract public class ConceptFireTaskTerm<C extends ConceptProcess> implements LogicStage<C> {
//
//
//    abstract public boolean apply(C f, @Deprecated TermLink termLink);
//
//    @Override
//    public final boolean test(final C f) {
//
//        final TermLink ftl = f.getTermLink();
//
//        if (ftl !=null) {
//            return apply(f, ftl);
//        }
//
//        //continue by default
//        return true;
//    }
//
//}
//
