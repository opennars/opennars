package nars.term.transform;

import com.gs.collections.impl.map.mutable.UnifiedMap;
import nars.Global;
import nars.Memory;
import nars.NAR;
import nars.Op;
import nars.nal.nal4.Image;
import nars.term.CommonVariable;
import nars.term.Compound;
import nars.term.Term;
import nars.term.Variable;
import nars.util.data.DequePool;
import nars.util.math.ShuffledPermutations;

import java.util.Map;
import java.util.Random;


public class FindSubst {

    public final Op type;

    /** X var -> Y term mapping */
    public final Map<Variable, Term> xy;

    /** Y var -> X term mapping */
    public final Map<Variable, Term> yx;

    private final Random random;

    final DequePool<ShuffledPermutations> permutationPool = new DequePool<ShuffledPermutations>(1) {
        @Override public ShuffledPermutations create() {
            return new ShuffledPermutations();
        }
    };
    final DequePool<Map<Variable,Term>> mapPool = new DequePool<Map<Variable,Term>>(1) {
        @Override public Map<Variable,Term> create() {
            return new UnifiedMap<>();
        }

        @Override
        public void put(Map<Variable, Term> i) {
            i.clear();
            super.put(i);
        }
    };


    public FindSubst(Op type, NAR nar) {
        this(type, nar.memory);
    }

    public FindSubst(Op type, Memory memory) {
        this(type, memory.random);
    }

    public FindSubst(Op type, Random random) {
        this(type, newDefaultMap(), newDefaultMap(), random);
    }

    private static final Map<Variable,Term> newDefaultMap() {
        return Global.newHashMap(0);
    }

    public FindSubst(Op type, Map<Variable, Term> xy, Map<Variable, Term> yx, Random random) {
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

        //System.out.println(x + " " + y + " " + endPower);

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

        if ((power = power - costFunction(x, y)) < 0)
            return power; //fail due to insufficient power

        final Op type = this.type;

        final Op xOp = x.op();
        final Op yOp = y.op();

        final boolean termsEqual = x.equals(y);


        if (xOp.isVar() && yOp.isVar()) {
            if (termsEqual) {
                return power; //match
            }
            else {
                Variable xVar = (Variable) x;
                if (nextVarX(xVar, y)) {
                    return power;
                }
                return -power; //no match
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
            Compound cx = (Compound) x;
            Compound cy = (Compound) y;
            if (!isPossibleMatch(cx, cy))
                power = -power;
            else
                power = permute(cx, cy, power);
        }
        else {
            if (!termsEqual)
                power = -power;
        }

        return power;
    }

    /** compare variable types to determine if one can match for another */
    private boolean isPossibleMatch(Op xVarOp, Op yOp) {
        if (xVarOp == Op.VAR_PATTERN) return true;
//        if (xVarOp == Op.VAR_QUERY) {
//            return yOp!=Op.VAR_QUERY; //dep or indep. it will not be the same query variable because equality has already been tested
//        }

        return (xVarOp == type);
    }

    /** cost subtracted in the re-entry method: next(x, y, power) */
    static int costFunction(Term x, Term y) {
        return Math.max(x.volume(), y.volume());
    }



    boolean nextVarX(final Variable xVar, final Term y) {
        final Op xOp = xVar.op();
        final Op yOp = y.op();

        boolean subsumes = isPossibleMatch(xOp, yOp);
        boolean sameType = (yOp == xOp);

        if (subsumes) {
            return putVarX(xVar, y);
        }
        else if (sameType) {
            return putCommon(xVar, (Variable)y);
        }

        return false;

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


    protected final boolean isPossibleMatch(final Compound X, final Compound Y) {
        /** must have same # subterms */
        if (X.length() != Y.length()) {
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
    protected int permute(final Compound X, final Compound Y, final int power) {

        final int xLen = X.length();


        if ((xLen > 1) && (!X.isCommutative())) {
            //non-commutative (must all match), or no permutation necessary (0 or 1 arity)
            return matchAll(power, X.term, Y.term);
        }
        else {
            switch (xLen) {
                case 0: return power-1;
                case 1: return find(X.term[0], Y.term[0], power);

                //case 2:  return permute2(X, Y, power);
                //case 3:  return permute3(Y, X, power);
                default: return permuteN(X, Y, power);
            }
        }
    }

    private final int permuteN(Compound x, Compound y, int power) {
        DequePool<ShuffledPermutations> pp = this.permutationPool;

        final ShuffledPermutations perm = pp.get();
        final int result = permuteN(perm, x, y, power);

        pp.put(perm);

        return result;
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
        if (yVar.op()!=type) {
            throw new RuntimeException("tried to set invalid map: " + yVar + "->" + x + " but type=" + type);
        }
        yxPut(yVar, x);
        if (yVar instanceof CommonVariable) {
            xyPut(yVar, x);
        }
        return true;
    }

    /**
     * elimination
     */
    private final boolean putVarX(final Variable xVar, final Term y) {
        if (xVar.op()!=type) {
            throw new RuntimeException("tried to set invalid map: " + xVar + "->" + y + " but type=" + type);
        }
        xyPut(xVar, y);
        if (xVar instanceof CommonVariable) {
            yxPut(xVar, y);
        }
        return true;
    }


    protected final boolean putCommon(final Variable x, final Variable y) {
        final Variable commonVar = CommonVariable.make(x, y);
        xyPut(x, commonVar);
        yxPut(y, commonVar);
        return true;
    }

    private final void yxPut(Variable y, Term x) {
        yx.put(y, x);
    }

    private final void xyPut(Variable x, Term y) {
        xy.put(x, y);
    }

    /**
     * @param X the compound which is permuted/shuffled
     * @param Y what is being compared against
     *
     * use with compounds with >= 2 subterms
     *
     * unoptimized N-ary permute,
     *  which requires allocating a temporary array for shuffling */
    int permuteN(final ShuffledPermutations perm, final Compound X, final Compound Y, int power) {

        final Term[] xTerms = X.term;
        final Term[] yTerms = Y.term;

        final int len = xTerms.length;
        perm.restart(len, random);
        //int permutations = perm.total();

        //push/save:
        Map<Variable, Term> savedXY = getMap(xy);
        Map<Variable, Term> savedYX = getMap(yx);

        while (power > 0 && perm.hasNext()) {

            perm.next();

            //matchAll:
            for (int i = 0; i < len; i++) {
                int s = perm.get(i);
                if ((power = find(xTerms[s], yTerms[i], power)) < 0)
                    break; //fail
            }

            if (power < 0) {
                power = -power; //try again; invert negated power back to a positive value for next attempt

                //pop/restore (TODO only if changed and will attempt again):
                xy.clear(); xy.putAll(savedXY);
                yx.clear(); yx.putAll(savedYX);
            } else {
                returnMap(savedXY, savedYX);
                return power; //success
            }
        }

        returnMap(savedXY, savedYX);
        return fail(power); //fail
    }

    private Map<Variable, Term> getMap(Map<Variable, Term> init) {
        Map<Variable, Term> m = mapPool.get();
        m.putAll(init);
        return m;
    }

    private void returnMap(Map<Variable, Term> a, Map<Variable, Term> b) {
        mapPool.put(a);
        mapPool.put(b);
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

    static final int fail(int powerMagnitude) {
        return (powerMagnitude > 0) ? -powerMagnitude : powerMagnitude;
    }


    /**
     * a branch for comparing a particular permutation, called from the main next()
     */
    final protected int matchAll(int power, final Term[] xSubterms, final Term[] ySubterms) {

        final int yLen = ySubterms.length;

        for (int i = 0; i < yLen; i++) {
            if ((power = find(xSubterms[i], ySubterms[i], power)) < 0)
                break; //fail
        }

        return power; //success
    }

//    final protected int matchAll2(int power, final Term x0, final Term x1, final Term[] ySubterms) {
//        if ((power = find(x0, ySubterms[0], power)) < 0)
//            return power;
//        return       find(x1, ySubterms[1], power);
//    }
}