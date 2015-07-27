package nars.term.transform;

import nars.term.Compound;
import nars.term.Term;
import nars.term.Variable;

import java.util.Collections;
import java.util.Map;

/** holds a substitution and any metadata that can eliminate matches as early as possible */
public class Substitution {
    final Map<Term, Term> subs;

    public int minMatchVolume = Integer.MAX_VALUE;
    int maxMatchVolume = Integer.MIN_VALUE;
    final int numSubs;

    int numDep = 0, numIndep = 0, numQuery = 0;


    /** creates a substitution of one variable; more efficient than supplying a Map */
    public Substitution(Term termFrom, Term termTo) {
        this(Collections.singletonMap(termFrom, termTo));
    }

    public Substitution(final Map<Term, Term> subs) {
        this.subs = subs;

        numSubs = subs.size();

        for (final Map.Entry<Term,Term> e : subs.entrySet()) {

            final Term m = e.getKey();

            final int v = m.getVolume();
            if (minMatchVolume > v) minMatchVolume = v;
            if (maxMatchVolume < v) maxMatchVolume = v;

            if (m instanceof Variable) {
                final Variable vv = (Variable) m;
                if (vv.hasVarIndep()) numIndep++;
                if (vv.hasVarDep()) numDep++;
                if (vv.hasVarQuery()) numQuery++;
            }

        /* collapse a substitution map to each key's ultimate destination
         *  in the case of values that are equal to other keys */
            if (numSubs >= 2) {
                final Term o = e.getValue(); //what the original mapping of this entry's key

                Term k = o, prev = o;
                int hops = 1;
                while ((k = subs.getOrDefault(k, k)) != prev) {
                    prev = k;
                    if (hops++ == numSubs) {
                        //cycle detected
                        throw new RuntimeException("Cyclical substitution map: " + subs);
                    }
                }
                if (!k.equals(o)) {
                    //replace with the actual final mapping
                    e.setValue(k);
                }
            }
        }

    }


    /** if eliminates all conditions with regard to a specific compound */
    public boolean impossible(Term superterm) {
        int subsApplicable = numSubs;

        if (superterm instanceof Compound) {
            if (((Compound)superterm).impossibleSubTermOrEqualityVolume(minMatchVolume)) {
                //none of the subs could possibly fit inside or be equal to the superterm
                return true;
            }
        }

        if (!superterm.hasVarDep()) subsApplicable -= numDep;
        if (subsApplicable <= 0) return true;

        if (!superterm.hasVarIndep()) subsApplicable -= numIndep;
        if (subsApplicable <= 0) return true;

        if (!superterm.hasVarQuery()) subsApplicable -= numQuery;
        if (subsApplicable <= 0) return true;


        //there exist variables that can match, and the term can theoretically equal or contain it
        return false;

    }


    public Term get(final Term t) {
        return subs.get(t);
    }

    public Term apply(Compound t) {
        return t.applySubstitute(this);
    }

}
