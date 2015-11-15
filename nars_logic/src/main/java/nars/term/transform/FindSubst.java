package nars.term.transform;

import nars.Global;
import nars.Memory;
import nars.NAR;
import nars.Op;
import nars.nal.nal4.Image;
import nars.term.*;
import nars.util.data.DequePool;
import nars.util.math.ShuffledPermutations;

import java.util.Map;
import java.util.Random;


/* recurses a pair of compound term tree's subterms
across a hierarchy of sequential and permutative fanouts
where valid matches are discovered, backtracked,
and collected until a total solution is found.
the magnitude of a running integer depth metric ("power") serves
as a finite-time AIKR cutoff and its polarity as
returned indicates success value to the callee.  */
public class FindSubst {

    public final Op type;

    /** X var -> Y term mapping */
    public final Map<Term, Term> xy;
    boolean xyChanged = false;

    /** Y var -> X term mapping */
    public final Map<Term, Term> yx;
    boolean yxChanged = false;

    private final Random random;

    final DequePool<ShuffledPermutations> permutationPool = new ShuffledPermutationsDequePool();
    final DequePool<Map<Term,Term>> mapPool = new MapDequePool();


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
        int endPower = match(x, y, power);

        /*
        System.out.println((power - Math.abs(endPower)) + " " +
                (endPower >= 0) + " " + x + " " + y + " " + power + " .. " + endPower);
        */

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
    final int match(final Term x, final Term y, int power) {

        if ((power = power - 1 /*costFunction(X, Y)*/) < 0)
            return power; //fail due to insufficient power

        //System.out.println("  m: " + x + " " + y + " " + power);

        if (x.equals(y)) {
            return power; //match
        }

        return matchNotEqual(x, y, power);
    }

    /** at this point, x and y have been determined not equal
     * but there is still the possibility of a match.
     */
    private final int matchNotEqual(Term x, Term y, int power) {


        final Op type = this.type;
        final Op xOp = x.op();
        if (xOp == type) {

            final Term xSubst = xy.get(x);

            if (xSubst != null) {
                return match(xSubst, y, power);
            }
            else {
                nextVarX((Variable) x, y);
                return power;
            }

        }

        final Op yOp = y.op();
        if (yOp == type) {

            final Term ySubst = yx.get(y);

            if (ySubst != null) {
                return match(x, ySubst, power);
            }
            else {
                putVarY(x, (Variable) y);
                return power;
            }

        }

        if (xOp.isVar()) {
            if (yOp.isVar()) {
                nextVarX((Variable) x, y);
                return power;
            }
        }
        else {
            if ((xOp == yOp) && (x instanceof Compound)) {
                return matchCompound((Compound)x, (Compound)y, power);
            }
        }


        return fail(power);
    }

    private static void printComparison(int power, Compound cx, Compound cy) {
        System.out.println(cx.structureString() + " " + cx.volume() + "\t" + cx);
        System.out.println(cy.structureString() + " " + cy.volume() + "\t" + cy);
        System.out.println(!cx.impossibleToMatch(cy) + "|" + !cy.impossibleToMatch(cx) + " ---> " + (power >= 0) + " " + power);
        System.out.println();
    }

    /** compare variable type to determine if matchable */
    private final boolean matchable(Op xVarOp/*, Op yOp*/) {
        //if (xVarOp == Op.VAR_PATTERN) return true;
//        if (xVarOp == Op.VAR_QUERY) {
//            return yOp!=Op.VAR_QUERY; //dep or indep. it will not be the same query variable because equality has already been tested
//        }

        return (xVarOp == type);
    }

//    /** cost subtracted in the re-entry method: next(x, y, power) */
//    static final int costFunction(final Compound x, final Compound y) {
//        return 1;
//        //return Math.min(x.volume(), y.volume());
//        //return x.volume() + y.volume();
//    }



    final void nextVarX(final Variable xVar, final Term y) {
        final Op xOp = xVar.op();

        //boolean m = false;

        if (matchable(xOp/*, yOp*/)) {
            putVarX(xVar, y);
        }
        else {
            final Op yOp = y.op();
            if (yOp == xOp) {
                 putCommon(xVar, (Variable) y);
            }
        }

        //return power; //m ? power : fail(power);

//
//            if(type == Op.VAR_PATTERN && xOp == Op.VAR_PATTERN) {
//                return putVarX(xVar, yVar);  //if its VAR_PATTERN unification, VAR_PATTERNS can can be matched with variables of any kind
//            }
//
//            //variables can and need sometimes to change name in order to unify
//            if(xOp == yOp) {  //and if its same op, its indeed variable renaming
//                return putCommon(xVar, yVar);
//            }
//
//        } else {
//            yVar = null;
//        }

    }


    protected static boolean matchable(final Compound X, final Compound Y) {
        /** must have same # subterms */
        if (X.size() != Y.size()) {
            return false;
        }

        //TODO see if there is a volume or structural constraint that can terminate early here

        /** if they are images, they must have same relationIndex */
        //TODO simplify comparison with Image base class
        if (X instanceof Image) {
            if (((Image) X).relationIndex != ((Image) Y).relationIndex)
                return false;
        }

        return true;
    }

    /**
     * X and Y are of the same operator type and length (arity)
     * X's permutations matched against constant Y
     */
    protected int matchCompound(final Compound X, final Compound Y, int power) {

        if (!matchable(X, Y))
            return fail(power);

        final int xLen = X.size();

        if (xLen == 0)
            return power;
        else if (xLen == 1)
            return match(X.term(0), Y.term(0), power);
        else { /*if (xLen >= 1) {*/
            if (!X.isCommutative()) {
                //non-commutative (must all match), or no permutation necessary (0 or 1 arity)
                return matchSequence(X.subterms(), Y.subterms(), power);
            }
            else {
                //commutative, try permutations
                return matchPermute(X, Y, power);
            }
        }
    }

    private final int matchPermute(Compound x, Compound y, int power) {
        DequePool<ShuffledPermutations> pp = this.permutationPool;

        final ShuffledPermutations perm = pp.get();

        final int result = permute(perm, x, y, power);

        pp.put(perm);

        return result;
    }


//    /**
//     * //https://github.com/opennars/opennars/commit/dd70cb81d22ad968ece86a549057cd19aad8bff3
//     */
//    static protected boolean queryVarMatch(final Op xVar, final Op yVar) {
//
//        final boolean xQuery = (xVar == Op.VAR_QUERY);
//        final boolean yQuery = (yVar == Op.VAR_QUERY);
//
//        return (xQuery ^ yQuery);
//    }

    /**
     * elimination
     */
    private final void putVarY(final Term x, final Variable yVar) {
        /*if (yVar.op()!=type) {
            throw new RuntimeException("tried to set invalid map: " + yVar + "->" + x + " but type=" + type);
        }*/
        yxPut(yVar, x);
        if (yVar instanceof CommonVariable) {
            xyPut(yVar, x);
        }
        //return true;
    }

    /**
     * elimination
     */
    private final void putVarX(final Variable xVar, final Term y) {
        /*if (xVar.op()!=type) {
            throw new RuntimeException("tried to set invalid map: " + xVar + "->" + y + " but type=" + type);
        }*/
        xyPut(xVar, y);
        if (xVar instanceof CommonVariable) {
            yxPut(xVar, y);
        }
        //return true;
    }


    protected final void putCommon(final Variable x, final Variable y) {
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



    /**
     * @param X the compound which is permuted/shuffled
     * @param Y what is being compared against
     *
     * use with compounds with >= 2 subterms
     *
     * unoptimized N-ary permute,
     *  which requires allocating a temporary array for shuffling */
    int permute(final ShuffledPermutations perm, final Compound X, final Compound Y, int power) {


        final int len = X.size();

        final int minAttempts = len; //heuristic assumption

        int permPower = power / minAttempts; //power allocate to each permutation

        final int subPower = permPower / len; //power allocated to each permutation's subterm
        if (subPower < 1) return fail(power);

        perm.restart(len, random);

        final Map<Term, Term> xy = this.xy; //local copy on stack
        final Map<Term, Term> yx = this.yx; //local copy on stack


        //push/save:
        Map<Term, Term> savedXY = acquireCopy(xy);
        xyChanged = false;
        Map<Term, Term> savedYX = acquireCopy(yx);
        yxChanged = false;

        boolean matched = false;

        ShuffleTermVector xv = new ShuffleTermVector(X, perm);

        while (perm.hasNext()) {

            perm.next();

            int sp = matchSequence(xv, Y, permPower);
            int cost = permPower - Math.abs(sp);
            power -= cost;

            matched = sp >= 0;

            if (matched || power <= 0) break;

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

        releaseCopies(savedXY, savedYX);

        //finished: succeeded (+) or depleted power (-)
        if (!matched)
            power = fail(power);

        return power;
    }

    private static void restore(Map<Term, Term> savedCopy, Map<Term, Term> originToRevert) {
        originToRevert.clear();
        originToRevert.putAll(savedCopy);
    }

    private final Map<Term, Term> acquireCopy(Map<Term, Term> init) {
        Map<Term, Term> m = mapPool.get();
        m.putAll(init);
        return m;
    }

    private final void releaseCopies(Map<Term, Term> a, Map<Term, Term> b) {
        final DequePool<Map<Term, Term>> mp = this.mapPool;
        mp.put(a);
        mp.put(b);
    }



    static final int fail(int powerMagnitude) {
        return (powerMagnitude > 0) ?
                -powerMagnitude : Math.min(-1, powerMagnitude);
    }


    /**
     * a branch for comparing a particular permutation, called from the main next()
     */
    final protected int matchSequence(final TermContainer X, final TermContainer Y, int power) {

        final int yLen = Y.size();

        //distribute recursion equally among subterms, though should probably be in proportion to their volumes
        final int subPower = power / yLen;
        if (subPower < 1) return fail(power);


        boolean phase = false;

        int processed = 0;

        //process non-commutative subterms in phase 1, then phase 2
        for (int j = 0; j < 2; j++) {

            for (int i = 0; i < yLen; i++) {

                Term xSub = X.term(i);

                if (xSub.isCommutative() == phase) {
                    int s;
                    s = match(xSub, Y.term(i), subPower);
                    power -= (subPower - Math.abs(s));
                    if (s < 0) {
                        return fail(power);
                    }
                    processed++;
                }
            }

            if (processed == yLen) break; //exit early if all have been processed
            phase = !phase;
        }

        return power; //success
    }

    private static class ShuffledPermutationsDequePool extends DequePool<ShuffledPermutations> {
        public ShuffledPermutationsDequePool() {
            super(1);
        }

        @Override public final ShuffledPermutations create() {
            return new ShuffledPermutations();
        }
    }

    private static class MapDequePool extends DequePool<Map<Term,Term>> {
        public MapDequePool() {
            super(1);
        }

        @Override public final Map<Term,Term> create() {
            return Global.newHashMap();
        }

        @Override
        public final void put(Map<Term, Term> i) {
            i.clear();
            super.put(i);
        }
    }

    private static class ShuffleTermVector extends TermVector {
        private ShuffledPermutations perm;
        private Compound compound;

        public ShuffleTermVector(Compound x, ShuffledPermutations perm) {
            reset(x, perm);
        }


        public void reset(Compound c, ShuffledPermutations s) {
            this.compound = c;
            this.perm = s;
        }

        @Override
        public final int size() {
            return compound.size();
        }

        @Override
        public final Term term(int i) {
            return compound.term( perm.get(i) );
        }
    }
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
