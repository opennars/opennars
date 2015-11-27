package nars.term.transform;

import nars.Global;
import nars.Memory;
import nars.NAR;
import nars.Op;
import nars.nal.nal4.Image;
import nars.term.*;

import java.util.Map;
import java.util.Random;


/* recurses a pair of compound term tree's subterms
across a hierarchy of sequential and permutative fanouts
where valid matches are discovered, backtracked,
and collected until a total solution is found.
the magnitude of a running integer depth metric ("power") serves
as a finite-time AIKR cutoff and its polarity as
returned indicates success value to the callee.  */
public class FindSubst implements Subst {

    private final Op type;

    /** X var -> Y term mapping */
    private final Map<Term, Term> xy;
    private boolean xyChanged = false;

    /** Y var -> X term mapping */
    private final Map<Term, Term> yx;
    private boolean yxChanged = false;

    private final Random random;
    private int power;

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

    @Override
    public final void clear() {
        xy.clear();
        yx.clear();
    }

    @Override
    public String toString() {
        return type + ":" + xy + ',' + yx;
    }

    private final void print(String prefix, Term a, Term b) {
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
    @Override
    public final boolean next(final Term x, final Term y, int power) {
        this.power = power;

        return match(x, y);

        /*
        System.out.println((power - Math.abs(endPower)) + " " +
                (endPower >= 0) + " " + x + " " + y + " " + power + " .. " + endPower);
        */
    }

    /**
     * recurses into the next sublevel of the term
     * @return
     *      if success: a POSITIVE next power value, after having subtracted the cost (>0)
     *      if fail: the NEGATED next power value (<=0)
     **
     * this effectively uses the sign bit of the integer as a success flag while still preserving the magnitude of the decreased power for the next attempt
     */
    private final boolean match(final Term x, final Term y) {

        //if ((power = power - 1 /*costFunction(X, Y)*/) < 0)
          //  return power; //fail due to insufficient power

        //System.out.println("  m: " + x + " " + y + " " + power);


        if (x.equals(y)) {
            /*if (x!=y)
                System.err.println("NOT SHARED: " + x);
            else
                System.err.println("    SHARED: " + x);*/

            return true; //match
        }

        if ((--power) < 0)
            return false;


        final Op type1 = this.type;
        final Op xOp = x.op();
        if (xOp == type1) {
            return matchXvar(x, y);
        }

        final Op yOp = y.op();
        if (yOp == type1) {
            return matchYvar(x, y);
        }

        if (xOp.isVar()) {
            if (yOp.isVar()) {
                nextVarX((Variable) x, y);
                return true;
            }
        }
        else {
            if ((xOp == yOp) && (x instanceof Compound)) {
                return matchCompound((Compound) x, (Compound) y);
            }
        }

        return false;
    }

    private boolean matchYvar(Term x, Term y) {
        final Term ySubst = yx.get(y);

        if (ySubst != null) {
            return match(x, ySubst);
        }
        else {
            yxPut((Variable) y, x);
            if (y instanceof CommonVariable) {
                xyPut((Variable) y, x);
            }
            return true;
        }
    }

    private boolean matchXvar(Term x, Term y) {
        final Term xSubst = xy.get(x);

        if (xSubst != null) {
            return match(xSubst, y);
        }
        else {
            nextVarX((Variable) x, y);
            return true;
        }
    }

    private static void printComparison(int power, Compound cx, Compound cy) {
        System.out.println(cx.structureString() + " " + cx.volume() + "\t" + cx);
        System.out.println(cy.structureString() + " " + cy.volume() + "\t" + cy);
        System.out.println(!cx.impossibleToMatch(cy) + "|" + !cy.impossibleToMatch(cx) + " ---> " + (power >= 0) + " " + power);
        System.out.println();
    }


    private final void nextVarX(final Variable xVar, final Term y) {
        final Op xOp = xVar.op();

        if (xOp == type) {
            putVarX(xVar, y);
        }
        else {
            final Op yOp = y.op();
            if (yOp == xOp) {
                 putCommon(xVar, (Variable) y);
            }
        }

    }


    /** returns the size of both compounds if matching and valid, or -1 if invalid match */
    private static int matchable(final Compound X, final Compound Y) {
        /** must have same # subterms */
        int xsize = X.size();
        if (xsize != Y.size()) {
            return -1;
        }

        //TODO see if there is a volume or structural constraint that can terminate early here

        /** if they are images, they must have same relationIndex */
        //TODO simplify comparison with Image base class
        if (X instanceof Image) {
            if (((Image) X).relationIndex != ((Image) Y).relationIndex)
                return -1;
        }

        return xsize;
    }

    /**
     * X and Y are of the same operator type and length (arity)
     * X's permutations matched against constant Y
     */
    private final boolean matchCompound(final Compound X, final Compound Y) {

        switch (matchable(X, Y)) {
            case -1:
                return false;
            case 0:
                return true;  //match
            case 1:
                return match(X.term(0), Y.term(0));
            default:  /*if (xLen >= 1) {*/
                if (X.isCommutative()) {
                    //commutative, try permutations
                    return matchPermute(X, Y);
                } else {
                    //non-commutative (must all match), or no permutation necessary (0 or 1 arity)
                    return matchSequence(X.subterms(), Y.subterms());
                }
        }
    }

    /**
     * @param x the compound which is permuted/shuffled
     * @param y what is being compared against
     *
     */
    private final boolean matchPermute(Compound x, Compound y) {
        //DequePool<ShuffledPermutations> pp = this.permutationPool;

        //final int len = x.size();

        //final int minAttempts = len; //heuristic assumption


        final ShuffleTermVector perm = new ShuffleTermVector(random, x);

        final Map<Term, Term> xy = this.xy; //local copy on stack
        final Map<Term, Term> yx = this.yx; //local copy on stack


        //push/save:
        final Map<Term, Term> savedXY = Global.newHashMap(xy);
        final Map<Term, Term> savedYX = Global.newHashMap(yx);
        xyChanged = yxChanged = false;

        boolean matched = false;

        while (perm.hasNext()) {

            perm.next();

            matched = matchSequence(perm, y);

            if (power < 0) {
                return false;
            }

            if (matched /*|| power <= 0*/)
                break;


            //try again; invert negated power back to a positive value for next attempt

            //pop/restore
            if (yxChanged) {
                yxChanged = false;
                restore(savedYX, yx);
            }

            if (xyChanged) {
                xyChanged = false;
                restore(savedXY, xy);
            }

            //ready to continue on next permutation

        }


        //finished
        return matched;

    }


    /**
     * elimination
     */
    private final void putVarX(final Variable x, final Term y) {
        xyPut(x, y);
        if (x instanceof CommonVariable) {
            yxPut(x, y);
        }
    }


    private void putCommon(final Variable x, final Variable y) {
        final Variable commonVar = CommonVariable.make(x, y);
        xyPut(x, commonVar);
        yxPut(y, commonVar);
        //return true;
    }

    private final void yxPut(Variable y, Term x) {
        yxChanged|= (yx.put(y, x)!=x);
    }

    private final void xyPut(Variable x, Term y) {
        xyChanged|= (xy.put(x, y)!=y);
    }


    private static void restore(Map<Term, Term> savedCopy, Map<Term, Term> originToRevert) {
        originToRevert.clear();
        originToRevert.putAll(savedCopy);
    }


    /**
     * a branch for comparing a particular permutation, called from the main next()
     */
    private boolean matchSequence(final TermContainer X, final TermContainer Y) {

        final int yLen = Y.size();

        boolean phase = false;

        int processed = 0;

        //process non-commutative subterms in phase 1, then phase 2
        do {

            for (int i = 0; i < yLen; i++) {

                Term xSub = X.term(i);

                if (xSub.isCommutative() == phase) {
                    if (!match(xSub, Y.term(i)))
                        return false;
                    processed++;
                }
            }

            phase = !phase;

        } while(processed < yLen);

        //success
        return true;
    }

    @Override
    public final void putXY(Term x, Term y) {
        xy.put(x, y);
    }

    @Override
    public final Term resolve(Term t, Substitution s) {
        //TODO make a half resolve that only does xy?

        Term ret = t.substituted(s, xy);
        if(ret != null) {
            ret = ret.substituted(s, yx);
        }
        return ret;

    }

    @Override public final Map<Term, Term> xy() { return xy; }
    @Override public final Map<Term, Term> yx() { return yx; }

    //    private static class ShuffledPermutationsDequePool extends DequePool<ShuffledPermutations> {
//        public ShuffledPermutationsDequePool() {
//            super(1);
//        }
//
//        @Override public final ShuffledPermutations create() {
//            return new ShuffledPermutations();
//        }
//    }
//
//    private static class MapDequePool extends DequePool<Map<Term,Term>> {
//        public MapDequePool() {
//            super(1);
//        }
//
//        @Override public final Map<Term,Term> create() {
//            return Global.newHashMap();
//        }
//
//        @Override
//        public final void put(Map<Term, Term> i) {
//            i.clear();
//            super.put(i);
//        }
//    }

}


//    private int permute3(final Compound X, final Compound Y, int power) {
//
//        final Term[] ySubterms = Y.term;
//        final Term a = ySubterms[0];
//        final Term b = ySubterms[1];
//        final Term c = ySubterms[2];
//
//        int tries = 6;
//        Term d, e, f;
//
//        int order = random.nextInt(6); //random starting permutation
//
//        do {
//            switch (order) {
//                case 0: d = a; e = b; f = c;     break;
//                case 1: d = a; e = c; f = b;     break;
//                case 2: d = b; e = a; f = c;     break;
//                case 3: d = b; e = c; f = a;     break;
//                case 4: d = c; e = a; f = b;     break;
//                case 5: d = c; e = b; f = a;     break;
//                default:
//                    throw new RuntimeException("invalid permutation");
//            }
//
//            if ((power = matchAll(power, X.term,
//                    new Term[]{ d, e, f}) )  >= 0)
//                return power; //success
//            else {
//                power = -power; //try again; reverse negated power back to a positive value for next attempt
//                order = (order + 1) % 6;
//                tries--;
//            }
//        } while (tries > 0);
//
//        return fail(power); //fail
//    }

//
//    private int permute2(final Compound X, final Compound Y, int power) {
//
//        final Term[] xSubterms = X.term;
//        Term x0 = xSubterms[0];
//        Term x1 = xSubterms[1];
//
//        //50% probabilty of an initial swap
//        if (random.nextBoolean()) {
//            Term t = x0;
//            x0 = x1;
//            x1 = t;
//        }
//
//        //SAVE
//        HashMap<Variable, Term> tmpXY = Maps.newHashMap(xy);
//        HashMap<Variable, Term> tmpYX = Maps.newHashMap(yx);
//
//        final Term[] ySubterms = Y.term;
//
//        //allocate half of the power for the first attempt.
//        //2nd attempt will have at least this much (what remains from the first).
//        int subPower = power/2;
//
//        int remainingSubPower =
//               matchAll2(subPower, x0, x1, ySubterms);
//
//        //subtract expense and add the surplus
//        power -= (subPower - Math.abs(remainingSubPower));
//        if (remainingSubPower >= 0) //success
//            return power;
//
//        //RESTORE
//        xy.clear(); xy.putAll(tmpXY);
//        yx.clear(); yx.putAll(tmpYX);
//
//        power = matchAll2(power, x1, x0, ySubterms);
//        if (power < 0) {
//            //RESTORE
//            xy.clear(); xy.putAll(tmpXY);
//            yx.clear(); yx.putAll(tmpYX);
//        }
//
//        return power;
//    }


//    final protected int matchAll2(int power, final Term x0, final Term x1, final Term[] ySubterms) {
//        if ((power = find(x0, ySubterms[0], power)) < 0)
//            return power;
//        return       find(x1, ySubterms[1], power);
//    }
