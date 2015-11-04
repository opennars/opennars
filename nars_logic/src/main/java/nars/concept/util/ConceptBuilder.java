package nars.concept.util;

import nars.concept.Concept;
import nars.term.Term;

import java.util.function.Function;


@FunctionalInterface public interface ConceptBuilder extends Function<Term, Concept> {


    ///** builds a concept. the budget will be set by the callee overwritten
//    Concept newConcept(Term t, Budget b, Memory m);

}
