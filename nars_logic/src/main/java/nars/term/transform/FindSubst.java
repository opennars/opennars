package nars.term.transform;

import nars.Global;
import nars.Op;
import nars.nal.nal4.Image;
import nars.term.*;

import java.util.Map;
import java.util.Random;


public class FindSubst {
    private final Op type;
    public final Map<Term, Term> map1;
    public final Map<Term, Term> map2;
    private final Random random;

    public FindSubst(Op type, Random random) {
        this(type, null, null, random);
    }

    public FindSubst(Op type, Map<Term,Term> map1, Map<Term,Term> map2, Random random) {
        if (map1 == null)
            map1 = Global.newHashMap(0);
        if (map2 == null)
            map2 = Global.newHashMap(0);

        this.type = type;
        this.map1 = map1;
        this.map2 = map2;
        this.random = random;

    }

    public void clear() {
        map1.clear();
        map2.clear();
    }


    @Override
    public String toString() {
        return type + ":" + map1 + ',' + map2;
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
    public boolean next(Term term1, Term term2) {

        final Op type = this.type;

        do {

            final Op op1 = term1.op();
            final Op op2 = term2.op();

            final boolean termsEqual = term1.equals(term2);

            if (op1.isVar() && op2.isVar() && termsEqual) {
                return true;
            }

            if (op1 == type) {

                final Term t = map1.get(term1);

                if (t != null) {
                    //RECURSE, ie: return next(t, term2);
                    term1 = t; /*term2 = term2;*/
                    continue;
                }

                return nextTerm1Var(
                        (Variable)term1,
                        term2
                );

            } else if (op2 == type) {

                final Term t = map2.get(term2);

                if (t != null) {
                    //RECURSE, ie: return next(term1, t);
                    term2 = t; /* term1 = term1 */
                    continue;
                }

                put2To1(term1, (Variable)term2);
                return true;

            } else if ((op1 == op2) && (term1 instanceof Compound) && (term1.hasVar(type) || term2.hasVar(type))) {
                return recurseAndPermute((Compound) term1, (Compound) term2);
            }

            return termsEqual;

        } while (true);




        //throw new RuntimeException("substitution escape");
    }

    protected boolean nextTerm1Var(final Variable term1Var, final Term term2) {
        Op op2 = null;

        final Variable term2Var;
        if (term2 instanceof Variable) {
            term2Var = (Variable) term2;
            op2 = term2Var.op;
        }
        else
            term2Var = null;

        if ((term2Var != null) && (op2 == type)) {
            putCommon(term1Var, term2Var);
        } else {

            if ((term2Var != null) && !queryVarMatch(term1Var.op, op2))
                return false;

            put1To2(term2, term1Var);
        }

        return true;
    }

    protected boolean recurseAndPermute(final Compound term1, final Compound term2) {

        final Compound cTerm1 = term1;
        final Compound cTerm2 = term2;
        final int c1Len = cTerm1.length();
        if (c1Len != cTerm2.length()) {
            return false;
        }

        //TODO simplify comparison with Image base class
        if (cTerm1 instanceof Image) {
            if (((Image) cTerm1).relationIndex != ((Image) cTerm2).relationIndex)
                return false;
        }

        if (cTerm1.isCommutative() && c1Len > 1) {

            switch(c1Len) {
                case 2:
                    return permute2(cTerm1.term(0), cTerm1.term(1), cTerm2);
                case 3:
                    return permute3(cTerm1.term, cTerm2);
                default:
                    return permuteN(cTerm1, cTerm2);
            }
        }

        return matchAll(cTerm2, cTerm1.term);
    }

    boolean permuteN(final Compound cTerm1, final Compound cTerm2) {
        Term[] list = cTerm1.cloneTerms();
        Compound.shuffle(list, random);
        return matchAll(cTerm2, list);
    }


    /** //https://github.com/opennars/opennars/commit/dd70cb81d22ad968ece86a549057cd19aad8bff3 */
    static protected boolean queryVarMatch(final Op term1Var, final Op term2Var) {

        final boolean t1Query = (term1Var == Op.VAR_QUERY);
        final boolean t2Query = (term2Var == Op.VAR_QUERY);

        return (t1Query ^ t2Query);
    }

    /** elimination */
    private final void put2To1(final Term term1, final Variable term2Var) {
        if (term2Var instanceof CommonVariable) {
            map1.put(term2Var, term1);
        }
        map2.put(term2Var, term1);
    }

    /** elimination */
    private final void put1To2(final Term term2, final Variable term1Var) {
        map1.put(term1Var, term2);
        if (term1Var instanceof CommonVariable) {
            map2.put(term1Var, term2);
        }
    }

    /**
     * unification.
     *
     * override this to disable common variables.
     * for example, it may be required to default to variable a
     * (Term 1 if it is a variable and of the target type)
     * instead of a new common variable.
     * */
    protected void putCommon(Variable a, Variable b) {
        Variable commonVar = CommonVariable.make(a, b);
        map1.put(a, commonVar);
        map2.put(b, commonVar);
    }

    private boolean permute3(final Term[] c3, final Compound cTerm2) {
        int order = random.nextInt(6);
        final Term a = c3[0];
        final Term b = c3[1];
        final Term c = c3[2];
        final int maxTries = 6;
        int tries = 0;
        final Term[] list = new Term[3];
        boolean solved;
        do {
            switch (order) {
                case 0: list[0] = a; list[1] = b; list[2] = c; break;
                case 1: list[0] = a; list[1] = c; list[2] = b; break;
                case 2: list[0] = b; list[1] = a; list[2] = c; break;
                case 3: list[0] = b; list[1] = c; list[2] = a; break;
                case 4: list[0] = c; list[1] = a; list[2] = b; break;
                case 5: list[0] = c; list[1] = b; list[2] = a; break;
            }
            solved = matchAll(cTerm2, list);
            order = (order + 1) % 6;
            tries++;
        } while (tries < maxTries && !solved);
        /*if (solved && tries > 1) {
            System.out.println("solved true after " + tries);
        }*/
        return solved;
    }

    private boolean permute2(final Term cTerm1_0, final Term cTerm1_1, final Compound cTerm2) {
        Term[] list = new Term[2];
        boolean order = random.nextBoolean();
        int tries = 0;
        boolean solved;
        do {
            if (order) {
                list[0] = cTerm1_0;
                list[1] = cTerm1_1;
            } else {
                list[0] = cTerm1_1;
                list[1] = cTerm1_0;
            }
            order = !order;
            solved = matchAll(cTerm2, list);
            tries++;
        } while (tries < 2 && !solved);

//   if (solved) {
//      if (tries > 1)
        //   System.out.println("got it " + tries);
//   }

        return solved;
    }

    /**
     * a branch for comparing a particular permutation, called from the main next()
     */
    final protected boolean matchAll(final Compound x, final Term[] t) {
        final Term X[] = x.term;
        final int tlen = t.length;
        for (int i = 0; i < tlen; i++) {
            final boolean r = next(t[i], X[i]);
            if (!r) {
                return false;
            }
        }
        return true;
    }


}


//package nars.term.transform;
//
//        import nars.Global;
//        import nars.Op;
//        import nars.nal.nal4.Image;
//        import nars.term.Compound;
//        import nars.term.Term;
//        import nars.term.Variable;
//        import nars.term.Variables;
//
//        import java.util.Map;
//        import java.util.Random;
//
//
//public class FindSubst {
//    private final Op type;
//    public final Map<Term, Term> map1;
//    public final Map<Term, Term> map2;
//    private final Random random;
//
//    public FindSubst(Op type, Random random) {
//        this(type, null, null, random);
//    }
//
//    public FindSubst(Op type, Map<Term,Term> map1, Map<Term,Term> map2, Random random) {
//        if (map1 == null)
//            map1 = Global.newHashMap(0);
//        if (map2 == null)
//            map2 = Global.newHashMap(0);
//
//        this.type = type;
//        this.map1 = map1;
//        this.map2 = map2;
//        this.random = random;
//
//    }
//
//    public void clear() {
//        map1.clear();
//        map2.clear();
//    }
//
//
//    @Override
//    public String toString() {
//        return type + ":" + map1 + ',' + map2;
//    }
//
//    private void print(String prefix, Term a, Term b) {
//        System.out.print(prefix);
//        if (a != null)
//            System.out.println(" " + a + " ||| " + b);
//        else
//            System.out.println();
//        System.out.println("     " + this);
//    }
//
//    /**
//     * recursess into the next sublevel of the term
//     */
//    public boolean next(Term term1, Term term2) {
//
//        boolean equals;
//        final Op type = this.type;
//
//        do {
//
//            final Op op1 = term1.op();
//            final Op op2 = term2.op();
//
//            if (op1 == op2) {
//
//                boolean t1HasVar = term1.hasVar(type);
//
//                equals = term1.equals(term2);
//
//                if ((term1 instanceof Compound) && (t1HasVar || term2.hasVar(type))) {
//                    return recurseAndPermute((Compound) term1, (Compound) term2);
//                }
//                else if (term1 instanceof Variable) {
//                    //atom, or other term type
//                    if (equals)
//                        return true;
//                }
//                //else if (!t1HasVar) {
//                //no variable of the specific type involved; result is the terms equality
//                //return equals;
//                //}
//                //else if (t1HasVar) { /* it and t2 must be variables; continue below */
//            }
//            else {
//                equals = false;
//            }
//
//
//            if (op1 == type) {
//
//                final Term t = map1.get(term1);
//
//                if (t != null) {
//                    //RECURSE:  //return next(t, term2);
//                    term1 = t; /*term2 = term2;*/
//                    continue;
//                }
//
//                return nextTerm1Var( (Variable)term1, term2 );
//
//            } else if (op2 == type) {
//
//                final Term t = map2.get(term2);
//
//                if (t != null) {
//                    //RECURSE:  //return next(term1, t);
//                    term2 = t;
//                    continue;
//                }
//
//                associate(term1, (Variable)term2, map1, map2);
//                return true;
//
//            }
//
//            term1 = null; //reaching here causes the loop to break
//
//        } while (term1!=null);
//
//        return equals;
//    }
//
//    protected boolean nextTerm1Var(final Variable term1Var, final Term term2) {
//
//        final Op op2 = term2.op();
//
//        if (op2 == type) {
//
//            putCommon(term1Var, term2);
//            //should term1Var or term2 be reassigned by this?
//
//        } else {
//
//            if (!queryVarMatch(term1Var.op, op2))
//                return false;
//
//            associate(term2, term1Var, map1, map2);
//        }
//
//        return true;
//    }
//
//    protected boolean recurseAndPermute(final Compound term1, final Compound term2) {
//
//        final Compound cTerm1 = term1;
//        final Compound cTerm2 = term2;
//        final int c1Len = cTerm1.length();
//        if (c1Len != cTerm2.length()) {
//            return false;
//        }
//
//        //TODO simplify comparison with Image base class
//        if (cTerm1 instanceof Image) {
//            if (((Image) cTerm1).relationIndex != ((Image) cTerm2).relationIndex)
//                return false;
//        }
//
//        if (cTerm1.isCommutative() && c1Len > 1) {
//
//            switch(c1Len) {
//                case 2:
//                    return permute2(cTerm1.term(0), cTerm1.term(1), cTerm2);
//                case 3:
//                    return permute3(cTerm1.term, cTerm2);
//                default:
//                    return permuteN(cTerm1, cTerm2);
//            }
//        }
//
//        return findInSubTerms(cTerm2, cTerm1.term);
//    }
//
//    boolean permuteN(final Compound cTerm1, final Compound cTerm2) {
//        Term[] list = cTerm1.cloneTerms();
//        Compound.shuffle(list, random);
//        return findInSubTerms(cTerm2, list);
//    }
//
//
//    /** //https://github.com/opennars/opennars/commit/dd70cb81d22ad968ece86a549057cd19aad8bff3 */
//    static protected boolean queryVarMatch(final Op term1Var, final Op term2Var) {
//        return (term1Var == Op.VAR_QUERY) ^ (term2Var == Op.VAR_QUERY);
//    }
//
//    /** elimination */
//    private final void associate(final Term term1, final Variable term2Var,
//                                 final Map<Term, Term> map1, final Map<Term, Term> map2) {
//        if (term2Var instanceof Variables.CommonVariable) {
//            map1.put(term2Var, term1);
//        }
//        map2.put(term2Var, term1);
//    }
//
////    /** elimination */
////    private final void put1To2(final Term term2, final Variable term1Var) {
////        map1.put(term1Var, term2);
////        if (term1Var instanceof Variables.CommonVariable) {
////            map2.put(term1Var, term2);
////        }
////    }
//
//    /**
//     * unification.
//     *
//     * override this to disable common variables.
//     * for example, it may be required to default to variable a
//     * (Term 1 if it is a variable and of the target type)
//     * instead of a new common variable.
//     * */
//    protected void putCommon(Variable a, Term/*Variable*/ b) {
//        Variable commonVar = Variables.CommonVariable.make(a, b);
//        map1.put(a, commonVar);
//        map2.put(b, commonVar);
//    }
//
//    private boolean permute3(final Term[] c3, final Compound cTerm2) {
//        int order = random.nextInt(6);
//        final Term a = c3[0];
//        final Term b = c3[1];
//        final Term c = c3[2];
//        final int maxTries = 6;
//        int tries = 0;
//        final Term[] list = new Term[3];
//        boolean solved;
//        do {
//            switch (order) {
//                case 0: list[0] = a; list[1] = b; list[2] = c; break;
//                case 1: list[0] = a; list[1] = c; list[2] = b; break;
//                case 2: list[0] = b; list[1] = a; list[2] = c; break;
//                case 3: list[0] = b; list[1] = c; list[2] = a; break;
//                case 4: list[0] = c; list[1] = a; list[2] = b; break;
//                case 5: list[0] = c; list[1] = b; list[2] = a; break;
//            }
//            solved = findInSubTerms(cTerm2, list);
//            order = (order + 1) % 6;
//            tries++;
//        } while (tries < maxTries && !solved);
//        /*if (solved && tries > 1) {
//            System.out.println("solved true after " + tries);
//        }*/
//        return solved;
//    }
//
//    private boolean permute2(final Term cTerm1_0, final Term cTerm1_1, final Compound cTerm2) {
//        Term[] list = new Term[2];
//        boolean order = random.nextBoolean();
//        int tries = 0;
//        boolean solved;
//        do {
//            if (order) {
//                list[0] = cTerm1_0;
//                list[1] = cTerm1_1;
//            } else {
//                list[0] = cTerm1_1;
//                list[1] = cTerm1_0;
//            }
//            order = !order;
//            solved = findInSubTerms(cTerm2, list);
//            tries++;
//        } while (tries < 2 && !solved);
//
////   if (solved) {
////      if (tries > 1)
//        //   System.out.println("got it " + tries);
////   }
//
//        return solved;
//    }
//
//    /**
//     * a branch for comparing a particular permutation, called from the main next()
//     */
//    final protected boolean findInSubTerms(final Compound x, final Term[] t) {
//        final Term X[] = x.term;
//        final int tlen = t.length;
//        for (int i = 0; i < tlen; i++) {
//            if (!next(t[i], X[i])) {
//                return false;
//            }
//        }
//        return true;
//    }
//
//
//}
