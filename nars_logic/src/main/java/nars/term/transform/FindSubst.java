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

    /** X var -> Y term mapping */
    public final Map<Variable, Term> xy;

    /** Y var -> X term mapping */
    public final Map<Variable, Term> yx;

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
        } else {
            return termsEqual ? power : -power;
        }
    }

    /** cost subtracted in the re-entry method: next(x, y, power) */
    static int costFunction(Term x, Term y) {
        return Math.max(x.volume(), y.volume());
    }


    boolean nextVarX(final Variable xVar, final Term y) {
        final Op xOp = xVar.op();
        final Op yOp = y.op();

        final Variable yVar;
        if (y instanceof Variable) {
            yVar = (Variable) y;

            //variables can and need sometimes to change name in order to unify
            if(xOp == yOp) {  //and if its same op, its indeed variable renaming
                return putCommon(xVar, yVar);
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

            if ((yVar != null) && !queryVarMatch(xOp, yOp)) { //i highly doubt this is conceptionally correct, but this I will check another time
                return false; //FAIL
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
            return -power; //FAIL
        }

        //TODO see if there is a volume or structural constraint that can terminate early here

        /** if they are images, they must have same relationIndex */
        //TODO simplify comparison with Image base class
        if (X instanceof Image) {
            if (((Image) X).relationIndex != ((Image) Y).relationIndex)
                return -power; //FAIL
        }

        if ((xLen <= 1) || (!X.isCommutative())) {
            //non-commutative (must all match), or no permutation necessary (0 or 1 arity)
            return matchAll(power, X.term, Y.term);
        }
        else {
            //X's permutations matched against Y
            switch (xLen) {
                case 2:  return permute2(X, Y, power);
                //case 3:  return permute3(Y, X, power);
                default: return permuteN(X, Y, power);
            }
        }

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

    /**
     * @param X the compound which is permuted/shuffled
     * @param Y what is being compared against
     *
     * unoptimized N-ary permute,
     *  which requires allocating a temporary array for shuffling */
    int permuteN(final Compound X, final Compound Y, int power) {
        //TODO repeat with a new shuffle until power depleted?

        final Term[] xOriginalSubterms = X.term;
        final int numSubterms = xOriginalSubterms.length;

        int permutations = (int)ArithmeticUtils.factorial(numSubterms);

        final Term[] xSubterms = Arrays.copyOf(xOriginalSubterms, numSubterms);
        final Term[] yTerms = Y.term;

        int count = 0;
        do {
            Compound.shuffle(xSubterms, random);
            if ((power = matchAll(power, xSubterms, yTerms)) >= 0)
                return power; //success
            else
                power = -power; //try again; reverse negated power back to a positive value for next attempt

            count++;
        } while ((power > 0) && (count < permutations));

        return fail(power); //fail
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

            if ((power = matchAll(power, X.term,
                    new Term[]{ d, e, f}) )  >= 0)
                return power; //success
            else {
                power = -power; //try again; reverse negated power back to a positive value for next attempt
                order = (order + 1) % 6;
                tries--;
            }
        } while (tries > 0);

        return fail(power); //fail
    }


    private int permute2(final Compound X, final Compound Y, int power) {

        final Term[] xSubterms = X.term;
        Term x0 = xSubterms[0];
        Term x1 = xSubterms[1];


        //50% probabilty of swap
        if (random.nextBoolean()) {
            Term t = x0;
            x0 = x1;
            x1 = t;
        }

        final Term[] ySubterms = Y.term;

        int subPower = power/2;

        int remainingSubPower = matchAll(subPower,
                new Term[] { x0, x1 }, ySubterms);
        power -= (subPower - Math.abs(remainingSubPower));
        if (remainingSubPower >= 0) //success
            return power;

        //invert power to non-negative for next attempt
        return matchAll(power,
                new Term[] { x1, x0 }, ySubterms);
    }

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
                return power; //fail
        }

        return power; //success
    }


}