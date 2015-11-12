package nars.term.transform;

import com.gs.collections.impl.map.mutable.UnifiedMap;
import nars.term.Compound;
import nars.term.Term;

import java.util.Map;
import java.util.function.Function;

/** holds a substitution and any metadata that can eliminate matches as early as possible */
public class Substitution implements Function<Compound,Term> {

    public Map<? extends Term, Term> subs;

    int numSubs;

    int numDep, numIndep, numQuery;


    /** creates a substitution of one variable; more efficient than supplying a Map */
    public Substitution(Term termFrom, Term termTo) {
        this(UnifiedMap.newWithKeysValues(termFrom, termTo));
    }

    public Substitution() {
        reset();
    }

    public Substitution(final Map<? extends Term, Term> subs) {
        reset(subs);
    }

    /** reset but keep the same map */
    public Substitution reset() {
        this.numSubs = -1;
        return this;
    }

    public Substitution reset(final Map<? extends Term, Term> subs) {
        this.subs = subs;
        return reset();
    }

    /** call if the map has changed (ex: during re-use) */
    public void prepare() {
        final int numSubs = this.numSubs = subs.size();

        int numDep = 0, numIndep = 0, numQuery = 0;

        if (numSubs > 0) {
            for (final Map.Entry<? extends Term, Term> e : subs.entrySet()) {

                final Term m = e.getKey();

                //if (m instanceof Variable) {
                    switch (m.op()) {
                        case VAR_DEPENDENT:
                            numDep++;
                            break;
                        case VAR_INDEPENDENT:
                            numIndep++;
                            break;
                        case VAR_QUERY:
                            numQuery++;
                            break;
                    }
                //}
            }
        }

        this.numDep = numDep;
        this.numIndep = numIndep;
        this.numQuery = numQuery;
    }

    /** if eliminates all conditions with regard to a specific compound */
    public final boolean impossible(final Term superterm) {

        int subsApplicable = numSubs;

        int numDep = this.numDep;
        if (numDep > 0 && !superterm.hasVarDep()) {
            subsApplicable -= numDep;
            if (subsApplicable <= 0)
                return true;
        }

        int numIndep = this.numIndep;
        if (numIndep > 0 && !superterm.hasVarIndep()) {
            subsApplicable -= numIndep;
            if (subsApplicable <= 0)
                return true;
        }

        int numQuery = this.numQuery;
        if (numQuery > 0 && !superterm.hasVarQuery()) {
            subsApplicable -= numQuery;
            if (subsApplicable <= 0)
                return true;
        }


        //there exist variables that can match, and the term can theoretically equal or contain it
        return false;

    }


    /** gets the substitute */
    final public Term get(final Term t) {
        return subs.get(t);
    }

    @Override public Term apply(final Compound c) {
        if (numSubs < 0)
            prepare();

        return _apply(c);
    }

    public Term _apply(final Compound c) {

        if (impossible(c))
            return c;

        /** subterms */
        Term[] sub = null;

        final int len = c.size();


        for (int i = 0; i < len; i++) {
            //t holds the
            final Term t = c.term(i);

            // s holds a replacement substitution for t (i-th subterm of c)
            Term s;

            //attempt 1: apply known substitution
            if ((s = get(t))!=null) {

                //prevents infinite recursion
                if (s.containsTerm(t))
                    s = null;

            }

            //attempt 2: if substitution still not found, recurse if subterm is compound term
            if (s == null && (t instanceof Compound)) { //additional constraint here?
                s = _apply((Compound)t);

                if (s == null) {
                    //null means the clone at the end of this method failed,
                    //so the resulting substituted term would be invalid
                    return null;
                }

                //if the same thing was provided, ignore
                if (t.equals(s))
                    s = null;
            }

            //if substitute found
            if (s!=null) {

                //ensure using a modified result Term[]
                if (sub == null) sub = c.cloneTerms();

                //replace the value at the current index
                sub[i] = s; //s.clone();

            }
        }

        if (sub == null) {
            //a new Term[] was not created, meaning nothing changed. return the input term
            return c;
        }

        return c.clone(sub);
    }

    @Override
    public String toString() {
        return "Substitution{" +
                "subs=" + subs +
                ", numSubs=" + numSubs +
                ", numDep=" + numDep +
                ", numIndep=" + numIndep +
                ", numQuery=" + numQuery +
                '}';
    }
}


//        /* collapse a substitution map to each key's ultimate destination
//         *  in the case of values that are equal to other keys */
//            if (numSubs >= 2) {
//                final Term o = e.getValue(); //what the original mapping of this entry's key
//
//                Term k = o, prev = o;
//                int hops = 1;
//                while ((k = subs.getOrDefault(k, k)) != prev) {
//                    prev = k;
//                    if (hops++ == numSubs) {
//                        //cycle detected
//                        throw new RuntimeException("Cyclical substitution map: " + subs);
//                    }
//                }
//                if (!k.equals(o)) {
//                    //replace with the actual final mapping
//                    e.setValue(k);
//                }
//            }
