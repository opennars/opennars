package nars.nal.concept;

import nars.Memory;
import nars.budget.Budget;
import nars.nal.term.Term;
import nars.util.data.Util;

import java.util.regex.Pattern;

/**
 * Created by me on 5/19/15.
 */
public class PatternConceptBuilder implements ConceptBuilder {

    private final Pattern pattern;
    private final ConceptBuilder builder;

    public PatternConceptBuilder(String glob, ConceptBuilder builder) {

        pattern = Pattern.compile(Util.globToRegEx(glob));
        this.builder = builder;
    }

    @Override
    public Concept newConcept(Term t, Budget b, Memory m) {
        if (pattern.matcher(t.toString()).matches()) {
            return builder.newConcept(t, b, m);
        }
        return null;
    }

}
