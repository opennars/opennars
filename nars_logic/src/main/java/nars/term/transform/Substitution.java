package nars.term.transform;

import nars.Op;
import nars.nal.meta.Ellipsis;
import nars.nal.nal4.InvisibleProduct;
import nars.term.Compound;
import nars.term.Term;
import nars.term.Variable;

import java.util.function.Function;

/** holds a substitution and any metadata that can eliminate matches as early as possible */
public interface Substitution extends Function<Compound,Term> {


    Term getXY(final Term t);


    @Override default Term apply(final Compound c) {
        //TODO optimization exclusion conditions, currently broke
        /*if (appliesTo < 0) {
            prepare();
            if (!isApplicable(c))
                return c;
        }*/

        return _apply(c);
    }

    default Term _apply(final Compound c) {

        /*if (!isApplicable(c))
            return c;*/

        final int len = c.size();
        final int targetLen = getResultSize(c);
        if (targetLen < 0) return c;

        /** result */
        final Term[] sub = new Term[targetLen];
        boolean changed = targetLen!=len;

        int j = 0;
        for (int i = 0; i < len; i++) {
            //t holds the
            final Term t = c.term(i);

            if (t instanceof Ellipsis) {

                changed = true;

                Term[] expansion = ((InvisibleProduct)getXY(t)).term;

                final int es = expansion.length;

                for (int e = 0; e < es; ) {
                    Term xx = expansion[e++];
                    if (xx==Ellipsis.Expand)
                        continue; //ignore any '..' which may be present in the expansion
                    sub[j++] = xx;
                }
            } else if (t == Ellipsis.Expand) {
                continue; //skip
            } else {
                // s holds a replacement substitution for t (i-th subterm of c)
                Term s;

                //attempt 1: apply known substitution
                if ((s = getXY(t)) != null) {

                    //prevents infinite recursion
                    if (s.containsTerm(t))
                        s = null;
                }

                //attempt 2: if substitution still not found, recurse if subterm is compound term
                if (s == null && (t instanceof Compound)) { //additional constraint here?
                    s = _apply((Compound) t);

                    if (s == null) {
                        //null means the clone at the end of this method failed,
                        //so the resulting substituted term would be invalid
                        return c;
                    }

                }

                //if substitute found
                if (s != null) {
                    //replace the value at the current index
                    changed |= !(t.equals(s));
                }
                else {
                    s = t;
                }

                sub[j++] = s;
            }
        }

        if (!changed) {
            //a new Term[] was not created, meaning nothing changed. return the input term
            return c;
        }

        return c.clone(sub);
    }

    default int getResultSize(Compound c) {
        int s = c.size();
        int n = s;
        for (int i = 0; i < s; i++) {
            Term t = c.term(i);
            if (t == Ellipsis.Expand) n--; //skip expansion placeholder terms
            if (t instanceof Ellipsis) {
                Term expanded = getXY(t);
                if (expanded == null) return -1; //missing ellipsis match
                n += expanded.size() - 1; //-1 for the existing term already accounted for
            }
        }
        return n;
    }


    /** returns non-null result only if substitution with regard to a given variable Operator was complete */
    default Term applyCompletely(Compound t, Op o) {
        Term a = apply(t);

        if (a == null)
            return null;

        if (!isSubstitutionComplete(a, o))
            return null;

        return a;
    }

    static boolean isSubstitutionComplete(Term a, Op o) {
        if (o == Op.VAR_PATTERN) {
            return !Variable.hasPatternVariable(a);
        }
        else {
            return !a.hasAny(o);
        }
    }

    boolean isEmpty();

    void putXY(Term x, Term y);

}


//        /* collapse a substitution map to each key's ultimate destination
//         *  in the case of values that are equal to other id */
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
