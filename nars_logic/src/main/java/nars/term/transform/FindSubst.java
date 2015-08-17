package nars.term.transform;

import nars.Global;
import nars.Symbols;
import nars.nal.nal4.Image;
import nars.term.Compound;
import nars.term.Term;
import nars.term.Variable;
import nars.term.Variables;

import java.util.Map;
import java.util.Random;

/**
 * Created by me on 8/17/15.
 */
public class FindSubst {
    private final char type;
    public final Map<Term, Term> map0;
    public final Map<Term, Term> map1;
    private final Random random;

    /**
     */
    public FindSubst(char type, Map map0, Map map1, Random random) {
        if (map0 == null)
            map0 = Global.newHashMap(0);
        if (map1 == null)
            map1 = Global.newHashMap(0);

        this.type = type;
        this.map0 = map0;
        this.map1 = map1;
        this.random = random;

    }

    public void clear() {
        map0.clear();
        map1.clear();
    }

    @Deprecated
    public Map<Term, Term>[] getMap() {
        return new Map[]{map0, map1};
    }

    public final boolean get(final Term term1, final Term term2) {
        //print("Before", term1, term2);
        boolean r = next(term1, term2);
        //print("  " + r + " After", null, null);
        return r;
    }

    @Override
    public String toString() {
        return type + ":" + map0 + "," + map1;
    }

    private void print(String prefix, Term a, Term b) {
        System.out.print(prefix);
        if (a != null)
            System.out.println(" " + a + " ||| " + b);
        else
            System.out.println();
        System.out.println("     " + this);
    }

    /**
     * recursess into the next sublevel of the term
     */
    protected boolean next(final Term term1, final Term term2) {

        final Variable term1Var = term1 instanceof Variable ? (Variable) term1 : null;
        final Variable term2Var = term2 instanceof Variable ? (Variable) term2 : null;

        final boolean termsEqual = term1.equals(term2);
        if (term1Var != null && term2Var != null && termsEqual) {
            return true;
        }

        if (term1Var != null && term1Var.getType() == type) {

            final Term t = map0.get(term1Var);

            if (t != null)
                return next(t, term2);

            if (term2Var != null && term2Var.getType() == type) {
                Variable commonVar = Variables.CommonVariable.make(term1, term2);
                map0.put(term1Var, commonVar);
                map1.put(term2Var, commonVar);
            } else {
                if (term2Var != null) {
                    //https://github.com/opennars/opennars/commit/dd70cb81d22ad968ece86a549057cd19aad8bff3

                    boolean t1Query = term1Var.getType() == Symbols.VAR_QUERY;
                    boolean t2Query = term2Var.getType() == Symbols.VAR_QUERY;

                    if ((t2Query && !t1Query) || (!t2Query && t1Query)) {
                        return false;
                    }
                }

                map0.put(term1Var, term2);
                if (term1Var instanceof Variables.CommonVariable) {
                    map1.put(term1Var, term2);
                }
            }

            return true;

        } else if (term2Var != null && term2Var.getType() == type) {

            final Term t = map1.get(term2Var);

            if (t != null)
                return next(term1, t);

            map1.put(term2Var, term1);
            if (term2Var instanceof Variables.CommonVariable) {
                map0.put(term2Var, term1);
            }

            return true;

        } else if ((term1.hasVar(type) || term2.hasVar(type)) && (term1 instanceof Compound) && ((term1.operator() == term2.operator()))) {
            final Compound cTerm1 = (Compound) term1;
            final Compound cTerm2 = (Compound) term2;
            if (cTerm1.length() != cTerm2.length()) {
                return false;
            }
            //TODO simplify comparison with Image base class
            if ((cTerm1 instanceof Image) && (((Image) cTerm1).relationIndex != ((Image) cTerm2).relationIndex)) {
                return false;
            }

            final Term[] list;
            final int clen = cTerm1.length();
            if (cTerm1.isCommutative() && clen > 1) {
                if (clen == 2) {
                    list = new Term[2];
                    boolean order = random.nextBoolean();
                    int tries = 0;
                    boolean solved;
                    do {
                        if (order) {
                            list[0] = cTerm1.term[0];
                            list[1] = cTerm1.term[1];
                        } else {
                            list[0] = cTerm1.term[1];
                            list[1] = cTerm1.term[0];
                        }
                        order = !order;
                        solved = next(cTerm2, list);
                        tries++;
                    } while (tries < 2 && !solved);
//                        if (solved) {
//                            if (tries > 1)
//                                System.out.println("got it " + tries);
//                        }
                    return solved;
                } else {
                    list = cTerm1.cloneTerms();
                    Compound.shuffle(list, random);
                }
            } else {
                list = cTerm1.term;
            }

            return next(cTerm2, list);
        }

        return termsEqual;
    }

    /**
     * a branch for comparing a particular permutation, called from the main next()
     */
    protected boolean next(Compound c, Term[] list) {
        for (int i = 0; i < list.length; i++) {
            final Term t1 = list[i];
            final Term t2 = c.term[i];
            if (!next(t1, t2)) {
                return false;
            }
        }

        return true;
    }


}
