//package nars.concept;
//
//import nars.Memory;
//import nars.budget.Budget;
//import nars.term.Term;
//import nars.util.data.Util;
//
//import java.util.regex.Pattern;
//
///**
// * WARNING: this can cause significant slowdown because it converts
// * every new concept term to a string.  For better performance,
// * use another ConceptBuilder which can compare Terms by structure
// */
//public class StringPatternConceptBuilder implements ConceptBuilder {
//
//    private final Pattern pattern;
//    private final ConceptBuilder builder;
//    final boolean pretty = false; //match toString(pretty)
//
//    public StringPatternConceptBuilder(String glob, ConceptBuilder builder) {
//
//        pattern = Pattern.compile(Util.globToRegEx(glob));
//        this.builder = builder;
//    }
//
//    @Override
//    public Concept newConcept(Term t, Budget b, Memory m) {
//
//        String ts = t.toString();
//
//        if (pattern.matcher(ts).matches()) {
//            return builder.newConcept(t, b, m);
//        }
//        return null;
//    }
//
// }
