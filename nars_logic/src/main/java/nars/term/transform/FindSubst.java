package nars.term.transform;

import nars.Global;
import nars.Memory;
import nars.NAR;
import nars.Op;
import nars.nal.nal4.Image;
import nars.term.CommonVariable;
import nars.term.Compound;
import nars.term.Term;
import nars.term.Variable;
import org.apache.commons.math3.util.ArithmeticUtils;

import java.util.Arrays;
import java.util.Map;
import java.util.Random;


public class FindSubst {

    public final Op type;

    /** X -> Y term mapping */
    public final Map<Term, Term> xy;

    /** Y -> X term mapping */
    public final Map<Term, Term> yx;

    private final Random random;

    public FindSubst(Op type, NAR nar) {
        this(type, nar.memory);
    }

    public FindSubst(Op type, Memory memory) {
        this(type, memory.random);
    }

    public FindSubst(Op type, Random random) {
        this(type, newDefaultMap(), newDefaultMap(), random);
    }

    private static final Map<Term,Term> newDefaultMap() {
        return Global.newHashMap(0);
    }

    public FindSubst(Op type, Map<Term, Term> xy, Map<Term, Term> yx, Random random) {
        this.type = type;
        this.xy = xy;
        this.yx = yx;
        this.random = random;
    }

    public void clear() {
        xy.clear();
        yx.clear();
    }

    @Override
    public String toString() {
        return type + ":" + xy + ',' + yx;
    }

    private void print(String prefix, Term a, Term b) {
        System.out.print(prefix);
        if (a != null)
            System.out.println(" " + a + " ||| " + b);
        else
            System.out.println();
        System.out.println("     " + this);
    }

    /** find substitutions, returning the success state.
     * this method should be used only from the outside.
     * all internal purposes should use the find() method
     * in order to manage decrease in power correctly */
    public final boolean next(final Term x, final Term y, int power) {
        int endPower = find(x, y, power);
        return endPower >= 0; //non-negative power value indicates success
    }

    /**
     * recurses into the next sublevel of the term
     * @return
     *      if success: a POSITIVE next power value, after having subtracted the cost (>0)
     *      if fail: the NEGATED next power value (<=0)
     **
     * this effectively uses the sign bit of the integer as a success flag while still preserving the magnitude of the decreased power for the next attempt
     */
    int find(final Term x, final Term y, int power) {

        if ((power = power - costFunction(x, y)) <= 0)
            return power; //fail due to insufficient power

        final Op type = this.type;

        final Op xOp = x.op();
        final Op yOp = y.op();

        final boolean termsEqual = x.equals(y);


        if (xOp.isVar() && yOp.isVar()) {
            if (termsEqual) {
                return power; //match
            }

            if (xOp == yOp) {
                return nextVarX((Variable) x, y) ?
                        power : -power;
            }
        }

        if (xOp == type) {

            final Term xSubst = xy.get(x);

            if (xSubst != null) {
                return find(xSubst, y, power);
            }
            else {
                return nextVarX((Variable) x, y) ?
                        power : -power;
            }

        } else if (yOp == type) {

            final Term ySubst = yx.get(y);

            if (ySubst != null) {
                return find(x, ySubst, power);
            }
            else {
                return putVarY(x, (Variable) y) ?
                        power : -power;
            }

        } else if ((xOp == yOp) && (x instanceof Compound)) {
            return recurseAndPermute((Compound) x, (Compound) y, power);
        }

        return termsEqual ? power : -power;
    }

    /** cost subtracted in the re-entry method: next(x, y, power) */
    static int costFunction(Term x, Term y) {
        return Math.max(x.volume(), y.volume());
    }

//    /** decide whether to recurse into according to variable types */
//    final boolean recurseInto(final Term compound1, final Term compound2) {
//        final Op typ = this.type;
//
//        if(true) { //Why would it ever be allowed to skip this? this generate nonsensical results like not being able to unify (&|,<(*,$1,#2) --> on>,<(*,SELF,#2) --> at>)
//            return true; //with (&|,<(*,{t002},#1) --> on>,<(*,SELF,#1) --> at>) under $ type, same for all other types...
//        }
//
//        if (typ == Op.VAR_PATTERN) return true;
//
//        return compound1.hasVar(typ) || compound2.hasVar(typ);
//    }

    boolean nextVarX(final Variable xVar, final Term y) {
        Op yOp = null;

        final Variable yVar;
        if (y instanceof Variable) {
            yVar = (Variable) y;
            yOp = yVar.op();

            Op xOp = xVar.op();

            //variables can and need sometimes to change name in order to unify
            if(xOp == yOp) {  //and if its same op, its indeed variable renaming
                return putCommon(yVar, xVar);
            }
            if(type == Op.VAR_PATTERN && xOp == Op.VAR_PATTERN) {
                return putVarX(xVar, y);  //if its VAR_PATTERN unification, VAR_PATTERNS can can be matched with variables of any kind
            }

        } else {
            yVar = null;
        }

        if ((yVar != null) && (yOp == type)) {
            return putCommon(xVar, yVar);
        } else {

            if ((yVar != null) && !queryVarMatch(xVar.op(), yOp)) { //i highly doubt this is conceptionally correct, but this I will check another time
                return false;
            }

            return putVarX(xVar, y);
        }

    }

    /**
     * X and Y are of the same operator type and length (arity)
     */
    protected int recurseAndPermute(final Compound X, final Compound Y, final int power) {

        /** must have same # subterms */
        final int xLen = X.length();
        if (xLen != Y.length()) {
            return -power; //fail
        }

        //TODO see if there is a volume or structural constraint that can terminate early here

        /** if they are images, they must have same relationIndex */
        //TODO simplify comparison with Image base class
        if (X instanceof Image) {
            if (((Image) X).relationIndex != ((Image) Y).relationIndex)
                return -power; //fail
        }

        if (X.isCommutative() && xLen > 1) {

            switch (xLen) {
                case 2:  return permute2(X, Y, power);
                case 3:  return permute3(X, Y, power);
                default: return permuteN(X, Y, power);
            }
        }

        return matchAll(X, Y, power);
    }



    /**
     * //https://github.com/opennars/opennars/commit/dd70cb81d22ad968ece86a549057cd19aad8bff3
     */
    static protected boolean queryVarMatch(final Op xVar, final Op yVar) {

        final boolean xQuery = (xVar == Op.VAR_QUERY);
        final boolean yQuery = (yVar == Op.VAR_QUERY);

        return (xQuery ^ yQuery);
    }

    /**
     * elimination
     */
    private final boolean putVarY(final Term x, final Variable yVar) {
        yx.put(yVar, x);
        if (yVar instanceof CommonVariable) {
            xy.put(yVar, x);
        }
        return true;
    }

    /**
     * elimination
     */
    private final boolean putVarX(final Variable xVar, final Term y) {
        xy.put(xVar, y);
        if (xVar instanceof CommonVariable) {
            yx.put(xVar, y);
        }
        return true;
    }


    protected final boolean putCommon(final Variable x, final Variable y) {
        final Variable commonVar = CommonVariable.make(x, y);
        xy.put(x, commonVar);
        yx.put(y, commonVar);
        return true;
    }

    /** unoptimized N-ary permute, which requires allocating a temporary array for shuffling */
    int permuteN(final Compound X, final Compound Y, int power) {
        //TODO repeat with a new shuffle until power depleted?

        final Term[] yOriginalSubterms = Y.term;
        final int numSubterms = yOriginalSubterms.length;

        final long permutations = ArithmeticUtils.factorial(numSubterms);

        final Term[] ySubterms = Arrays.copyOf(yOriginalSubterms, numSubterms);

        int count = 0;
        do {
            Compound.shuffle(ySubterms, random);
            if ((power = matchAll(power, X, ySubterms)) >= 0)
                return power; //success
            else
                power = -power; //try again; reverse negated power back to a positive value for next attempt

            count++;
        } while ((power > 0) && (count <= permutations));

        return -power; //fail
    }


    private int permute3(final Compound X, final Compound Y, int power) {

        final Term[] ySubterms = Y.term;
        final Term a = ySubterms[0];
        final Term b = ySubterms[1];
        final Term c = ySubterms[2];

        int tries = 6;
        Term d, e, f;

        int order = random.nextInt(6); //random starting permutation

        do {
            switch (order) {
                case 0: d = a; e = b; f = c;     break;
                case 1: d = a; e = c; f = b;     break;
                case 2: d = b; e = a; f = c;     break;
                case 3: d = b; e = c; f = a;     break;
                case 4: d = c; e = a; f = b;     break;
                case 5: d = c; e = b; f = a;     break;
                default:
                    throw new RuntimeException("invalid permutation");
            }

            if ((power = matchAll(power, X, d, e, f)) >= 0)
                return power; //success
            else {
                power = -power; //try again; reverse negated power back to a positive value for next attempt
                order = (order + 1) % 6;
                tries--;
            }
        } while (tries > 0);

        return -power; //fail
    }

    private int permute2(final Compound X, final Compound Y, int power) {

        final Term[] yOriginalSubterms = Y.term;
        Term y0 = yOriginalSubterms[0];
        Term y1 = yOriginalSubterms[1];


        //50% probabilty of swap
        if (random.nextBoolean()) {
            Term t = y0;
            y0 = y1;
            y1 = t;
        }

        if ((power = matchAll(power, X, y0, y1)) >= 0)
            return power; //success
        else {
            power = -power; //restore to non-negative for next attempt
            return matchAll(power, X, y1, y0);
        }
    }

    /** matches all subterms in their existing order */
    final protected int matchAll(final Compound X, final Compound Y, int power) {
        Term[] ySubterms = Y.term;
        switch (ySubterms.length) {
            case 0:
                return Math.max(0, power - 1); //ex: empty product, success by default so it is min 0
            default:
                return matchAll(power, X, ySubterms);
        }
    }

    /**
     * a branch for comparing a particular permutation, called from the main next()
     */
    final protected int matchAll(int power, final Compound X, final Term... ySubterms) {

        final int yLen = ySubterms.length;

        final Term xSubterms[] = X.term;

        for (int i = 0; i < yLen; i++) {
            if ((power = find(xSubterms[i], ySubterms[i], power)) < 0)
                return power; //fail
        }

        return Math.max(0, power); //success
    }

//    /**
//     * optimized 3-arity case of matchAll
//     */
//    final protected int matchAll3(final Term a, final Term b, final Term c, final Compound x, int power) {
//
//        final Term X[] = x.term;
//
//        if ((power = find(a, X[0], power)) <= 0) return power;
//        if ((power = find(b, X[1], power)) <= 0) return power;
//        return       find(c, X[2], power);
//    }
//
//    /**
//     * optimized 2-arity case of matchAll
//     */
//    final protected int matchAll2(final Term a, final Term b, final Compound x, int power) {
//        final Term X[] = x.term;
//
//        if ((power = find(a, X[0], power)) <= 0) return power;
//        return       find(b, X[1], power);
//    }
//
//    /**
//     * optimized 1-arity case of matchAll
//     */
//    final protected int matchAll1(final Term a, final Compound x, int power) {
//        return find(a, x.term[0], power);
//    }

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
//
//    private boolean permute2_old(final Term cTerm1_0, final Term cTerm1_1, final Compound cTerm2, int power) {
//        if ((power/=2) <= 0)
//            return false;
//
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
//            //TODO make a special matchAll2 which specializes in 2-arity match, avoiding array allocation (just use 3 variables passed as params)
//
//            order = !order;
//            solved = matchAll(cTerm2, list, power);
//            tries++;
//        } while (tries < 2 && !solved);
//
//        return solved;
//    }
