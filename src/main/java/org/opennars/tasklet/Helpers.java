package org.opennars.tasklet;

import org.opennars.language.CompoundTerm;
import org.opennars.language.Conjunction;
import org.opennars.language.Interval;
import org.opennars.language.Term;

import java.util.ArrayList;
import java.util.List;

public class Helpers {
    public static Conjunction subConjuction(Conjunction conj, int startIdx, int exclusiveEndIdx) {
        Term[] arr = new Term[exclusiveEndIdx-startIdx];
        for(int i=startIdx;i<exclusiveEndIdx;i++) {
            arr[i-startIdx] = conj.term[i];
        }
        return(Conjunction)Conjunction.make(arr, conj.temporalOrder);
    }

    public static Term removeIntervals(Conjunction term) {
        List<Term> components = new ArrayList<>();
        for(Term iTerm : term.term) {
            if (!(iTerm instanceof Interval)) {
                components.add(iTerm);
            }
        }
        Term[] componentsAsArray = components.toArray(new Term[components.size()]);
        return Conjunction.make(componentsAsArray, term.temporalOrder);
    }

    /* commented because not used
    // encodes a term to an sdr
    public static boolean[] encode(Term term) {
        int hash = term.hashCode();

        boolean[] arr = new boolean[512];

        for(int c=0;c<512/10;c++) {
            arr[hash % 512] = true;
            hash = hash*17 + c;
        }

        return arr;
    }
     */
}
