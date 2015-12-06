package nars.term.transform;

import nars.Global;
import nars.Op;
import nars.nal.meta.match.Ellipsis;
import nars.nal.meta.match.TransformingEllipsisMatch;
import nars.nal.nal4.ShadowProduct;
import nars.term.Compound;
import nars.term.Term;
import nars.term.Terms;
import nars.term.Variable;

import java.util.List;
import java.util.function.Function;

/** holds a substitution and any metadata that can eliminate matches as early as possible */
public interface Substitution extends Function<Compound,Term> {


    /** resolve a term */
    Term getXY(final Term t); //should this be renamed to 'resolve' ?



    @Override default Term apply(final Compound c) {
        //TODO optimization exclusion conditions, currently broke
        /*if (appliesTo < 0) {
            prepare();
            if (!isApplicable(c))
                return c;
        }*/

        return subst(c);
    }

    default Term subst(final Compound c) {

        /*if (!isApplicable(c))
            return c;*/

        final int len = c.size();
        List<Term> sub = Global.newArrayList(len);

        TransformingEllipsisMatch post = null;


        for (int i = 0; i < len; i++) {
            //t holds the
            final Term t = c.term(i);

            if (t instanceof Ellipsis) {

                Term te = getXY(t);

                ShadowProduct sp = (ShadowProduct) te;
                Term[] expansion;
                if (sp instanceof TransformingEllipsisMatch) {
                    if (post!=null) throw new RuntimeException("substitution alread involves a post-filter: " + post + " which conflicts with " + sp);
                    post = (TransformingEllipsisMatch)sp;
                    if (!post.resolve(this, sub))
                        return c;
                } else {
                    //default
                    for (Term xx : sp.term) {
                        if (xx== Ellipsis.Shim)
                            continue; //ignore any '..' which may be present in the expansion
                        sub.add(xx);
                    }
                }

            } else if (t == Ellipsis.Shim) {
                continue; //skip
            } else {
                // s holds a replacement substitution for t (i-th subterm of c)
                Term s = subst(t);
                if (s == null) {
                    s = t;
                }

                sub.add(s);
            }
        }

        final Term[] r = Terms.toArray(sub);

        if (post!=null) {
            return post.build(r, c);
        } else {
            //default
            return c.clone(r);
        }
    }

    default Term subst(Term t) {
        Term s;
        //attempt 1: apply known substitution
        if ((s = getXY(t)) != null) {

            //prevents infinite recursion
            if (s.containsTerm(t))
                s = null;
        }

        //attempt 2: if substitution still not found, recurse if subterm is compound term
        if (s == null && (t instanceof Compound)) { //additional constraint here?
            s = subst((Compound) t);

            if (s == null) {
                //null means the clone at the end of this method failed,
                //so the resulting substituted term would be invalid
                return null;
            }

        }
        return s;
    }

//    default Term substEllipsisTransform(Compound c, int j, EllipsisTransform et) {
//
//        Term[] sub = c.terms();
//        Term te = getXY(et);
//
//
//        Term from = et.from;
//
//        if (et.to.equals(Image.Index)) {
//            Term fromRes = getXY(et.from);
//
//            int index = 0; // the _ term was not present, which means it occurrs at start (index 0)
//
//            List<Term> build = Global.newArrayList(et.size());
//            int k = 1;
//            for (Term x : sub) {
//                if (x.equals(fromRes)) {
//                    x = Image.Index; //replace with '_'
//                    index = k;
//                }
//                build.add(x);
//                k++;
//            }
//
//            Term[] buildt = build.toArray(new Term[build.size()]);
//
//            if (c.op() == Op.IMAGE_EXT)
//                return new ImageExt(buildt, index);
//            else if (c.op() == Op.IMAGE_INT)
//                return new ImageInt(buildt, index);
//            else
//                throw new RuntimeException("expected image type");
//        }
//        else {
////            Term to = et.to;
////            Term toRes = getXY(et.to);
////            return Image.make
//        }
//
//
//
//        Term[] expansion = ((ShadowProduct) te).term;
//
//        return null;
//    }



//    @Deprecated default int getResultSize(Compound c) {
//        int s = c.size();
//        int n = s;
//        for (int i = 0; i < s; i++) {
//            Term t = c.term(i);
//            if (t == Ellipsis.Shim) n--; //skip expansion placeholder terms
//            if (t instanceof Ellipsis) {
//                Term expanded = getXY(t);
//                if (expanded == null) return -1; //missing ellipsis match
//                n += expanded.size() - 1; //-1 for the existing term already accounted for
//            }
//        }
//        return n;
//    }


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
