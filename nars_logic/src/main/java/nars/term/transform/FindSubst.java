package nars.term.transform;

import com.gs.collections.api.set.MutableSet;
import nars.Global;
import nars.Memory;
import nars.NAR;
import nars.Op;
import nars.nal.meta.Ellipsis;
import nars.nal.meta.PreCondition;
import nars.nal.meta.TermPattern;
import nars.nal.nal4.Image;
import nars.nal.nal4.ShadowProduct;
import nars.term.*;

import java.util.Collection;
import java.util.Random;
import java.util.Set;


/* recurses a pair of compound term tree's subterms
across a hierarchy of sequential and permutative fanouts
where valid matches are discovered, backtracked,
and collected until a total solution is found.
the magnitude of a running integer depth metric ("power") serves
as a finite-time AIKR cutoff and its polarity as
returned indicates success value to the callee.  */
public class FindSubst extends Subst implements Substitution {

    public FindSubst(Op type, NAR nar) {
        this(type, nar.memory);
    }

    public FindSubst(Op type, Memory memory) {
        this(type, memory.random);
    }

    public FindSubst(Op type, Random random) {
        super(random, type);
    }


    /**
     * push in to children
     */
    public static final PatternOp Subterms = new PatternOp() {

        @Override
        public String toString() {
            return "Sub";
        }

        @Override
        public boolean run(Subst ff) {
            ff.parent.set((Compound) ff.term.get());
            return true;
        }
    };


    //    @Override
//    public final Subst clone() {
//        FindSubst x = new FindSubst(type,
//                Global.newHashMap(xy),
//                Global.newHashMap(yx),
//                random);
//        x.parent = parent;
//        x.xyChanged = xyChanged; //necessary?
//        x.yxChanged = yxChanged; //necessary?
//        x.y = y;
//        x.power = power;
//        return x;
//    }


    private final void print(String prefix, Term a, Term b) {
        System.out.print(prefix);
        if (a != null)
            System.out.println(" " + a + " ||| " + b);
        else
            System.out.println();
        System.out.println("     " + this);
    }


    public static final class TermEquals extends MatchOp {
        public final Term a;

        public TermEquals(Term a) {
            this.a = a;
        }

        @Override
        public boolean match(Term t) {
            return a.equals(t);
        }

        @Override
        public String toString() {
            return "=" + a;
        }
    }

    public static final class TermSizeEquals extends MatchOp {
        public final int size;

        public TermSizeEquals(int size) {
            this.size = size;
        }

        @Override
        public boolean match(Term t) {
            return t.size() == size;
        }

        @Override
        public String toString() {
            return "size=" + size;
        }
    }

    public static final class TermVolumeMin extends MatchOp {
        public final int volume;

        public TermVolumeMin(int volume) {
            this.volume = volume;
        }

        @Override
        public boolean match(Term t) {
            return t.volume() >= volume;
        }

        @Override
        public String toString() {
            return "vol>=" + volume;
        }
    }

    public static final class TermStructure extends MatchOp {
        public final int bits;

        public TermStructure(Op matchingType, int bits) {
            this.bits = bits & (~matchingType.bit());
        }

        @Override
        public boolean match(Term t) {
            int s = t.structure();
            return (s | bits) == s;
        }

        @Override
        public String toString() {
            return /*"Struct = " + */ Integer.toString(bits, 2);
        }
    }

    /**
     * requires a specific subterm to have minimum bit structure
     */
    public static final class SubTermStructure extends PatternOp {
        public final int subterm;
        public final int bits;
        private transient final String id;


        public SubTermStructure(Op matchingType, int subterm, int bits) {
            this.subterm = subterm;

            if (matchingType != Op.VAR_PATTERN)
                bits &= (~matchingType.bit());

            this.bits = bits;
            this.id = "t" + subterm + ":" +
                    Integer.toString(bits, 16);
        }


        @Override
        public String toString() {
            return id;
        }

        @Override
        boolean run(Subst ff) {
            Compound parent = ff.parent.get();
            int s = parent.term(subterm).structure();
            return (s | bits) == s;
        }
    }

    /**
     * requires a specific subterm type
     */
    public static final class SubTermOp extends PatternOp {
        public final int subterm;
        public final Op op;
        private transient final String id;


        public SubTermOp(int subterm, Op op) {
            this.subterm = subterm;
            this.op = op;
            this.id = "t" + subterm + ":" + op;
        }

        @Override
        public String toString() {
            return id;
        }

        @Override
        boolean run(Subst ff) {
            Compound parent = ff.parent.get();
            return parent.term(subterm).op() == op;
        }
    }

    public static final class TermOpEquals extends MatchOp {
        public final Op type;

        public TermOpEquals(Op type) {
            this.type = type;
        }

        @Override
        public boolean match(Term t) {
            return t.op() == type;
        }

        @Override
        public String toString() {
            return type.toString(); /* + " "*/
        }
    }

//    public static final class SubOpEquals extends MatchOp {
//        public final Op type;
//        private final int subterm;
//
//        public SubOpEquals(int subterm, Op type) {
//            this.subterm = subterm;
//            this.type = type;
//        }
//
//        @Override
//        public boolean match(Term t) {
//            return t.term(subterm).op() == type;
//        }
//
//        @Override
//        public String toString() {
//            return "SubOpEq{" + subterm + "," + type + '}';
//        }
//    }


    /**
     * Imdex == image index
     */
    public static final class ImageIndexEquals extends MatchOp {
        public final int index;

        public ImageIndexEquals(int index) {
            this.index = index;
        }

        @Override
        public boolean match(Term t) {
            return ((Image) t).relationIndex == index;
        }

        @Override
        public String toString() {
            return "imdex:" + index;
        }
    }

//    public static class MatchTerm implements PatternOp {
//        public final Term term;
//
//        public MatchTerm(Term term) {
//            this.term = term;
//        }
//    }


    /**
     * invokes a dynamic FindSubst match via the generic entry method: match(Term,Term)
     */
    public static class MatchTerm extends PatternOp {
        public final Term x;

        public MatchTerm(Term c) {
            this.x = c;
        }

        @Override
        public boolean run(Subst ff) {
            return ff.match(x, ff.term.get());
        }

        @Override
        public String toString() {
            return x.toString();
        }
    }

//    /** invokes a dynamic FindSubst match via the matchVarX entry method;
//     *  this is more specific than match() so slightly faster */
//    public static class MatchXVar extends PatternOp {
//        public final Variable x;
//
//        public MatchXVar(Variable c) {
//            this.x = c;
//        }
//
//        @Override
//        public boolean run(Subst ff) {
//            return ff.matchXvar(x, ff.y);
//        }
//
//        @Override
//        public String toString() {
//            return "XVar{" + x + '}';
//        }
//    }

    public final static class MatchCompound extends PatternOp {
        public final Compound x;

        public MatchCompound(Compound c) {
            this.x = c;
        }

        @Override
        public boolean run(Subst ff) {
            return ff.matchCompound(x, ((Compound) ff.term.get()));
        }

        @Override
        public String toString() {
            return "..`" + x + '`';
        }
    }

//    public final static class MatchPermute extends PatternOp {
//        public final Compound x;
//
//        public MatchPermute(Compound c) {
//            this.x = c;
//        }
//
//        @Override
//        public boolean run(Subst ff) {
//            return ff.matchPermute(x, ((Compound) ff.y));
//        }
//
//        @Override
//        public String toString() {
//            return "MatchPermute{" + x + '}';
//        }
//    }

    /**
     * pop out to parent
     */
    public static final PatternOp Superterm = new PatternOp() {

        @Override
        public String toString() {
            return "Super";
        }

        @Override
        public boolean run(Subst ff) {

            ff.term.set(ff.parent.get());
            ff.parent.set(null);
            return true;
        }
    };


    /**
     * selects the ith sibling subterm of the current parent
     */
    public static final class Subterm extends PatternOp {
        public final int index;

        public Subterm(int index) {
            this.index = index;
        }

        @Override
        public final boolean run(Subst f) {
            f.goSubterm(index);
            return true;
        }

        @Override
        public String toString() {
            return "t" + index; //s for subterm and sibling
        }
    }

    /**
     * sets the term to its parent, and the parent to a hardcoded value (its parent)
     */
    public static final class ParentTerm extends PatternOp {
        public final Compound parent;

        public ParentTerm(Compound parent) {
            this.parent = parent;
        }

        @Override
        public final boolean run(Subst f) {
            f.term.set(f.parent.get());
            f.parent.set(this.parent);

            return true;
        }

        @Override
        public String toString() {
            return "parent(" + parent + ")"; //s for subterm and sibling
        }
    }


//    public static final class MatchSubterm extends PatternOp {
//        public final int index;
//        private final Term x;
//
//        public MatchSubterm(Term x, int index) {
//            this.index = index;
//            this.x = x;
//        }
//
//        @Override
//        public boolean run(Subst ff) {
//            Term y = ff.y = ff.parent.term(index);
//            return ff.match(x, y);
//        }
//
//
//        @Override
//        public String toString() {
//            return "MatchSubterm{" + x + "," + index + '}';
//        }
//    }

//    /**
//     * match 0th subterm (fast)
//     */
//    public static final class MatchTheSubterm extends PatternOp {
//
//        private final Term x;
//
//        public MatchTheSubterm(Term x) {
//            this.x = x;
//        }
//
//        @Override
//        public boolean run(Subst ff) {
//            return ff.match(x, ff.y.term(0));
//        }
//
//        @Override
//        public String toString() {
//            return "MatchTheSubterm{" + x + '}';
//        }
//    }


    /**
     * find substitutions, returning the success state.
     */
    @Override
    public final boolean next(final Term x, final Term y, int startPower) {

        setPower(startPower);

        boolean b = match(x, y);

        //System.out.println(startPower + "\t" + power);

        return b;
    }

    /**
     * find substitutions using a pre-compiled term pattern
     */
    @Override
    @Deprecated
    public final boolean next(final TermPattern x, final Term y, int startPower) {

        this.term.set(y);

        setPower(startPower);

        boolean match = true;

        for (PreCondition o : x.code) {
            if (!(o instanceof PatternOp)) continue;
            if (!((PatternOp) o).run(this)) {
                match = false;
                break;
            }
        }

        if (powerDivisor != 1f)
            throw new RuntimeException("power divisor not restored");

        return match;

    }


    /**
     * recurses into the next sublevel of the term
     *
     * @return if success: a POSITIVE next power value, after having subtracted the cost (>0)
     * if fail: the NEGATED next power value (<=0)
     * *
     * this effectively uses the sign bit of the integer as a success flag while still preserving the magnitude of the decreased power for the next attempt
     */
    public final boolean match(final Term x, final Term y) {

        if (x.equals(y)) {
            return true;
        }

        /*if ((--power) < 0)
            return false;*/

        final Op t = this.type;
        final Op xOp = x.op();
        final Op yOp = y.op();

        if ((xOp == yOp) && (x instanceof Compound)) {
            return matchCompound((Compound) x, (Compound) y);
        }

        if (xOp == t) {
            return matchXvar((Variable) x, y);
        }

        if (yOp == t) {
            return matchYvar(x, /*(Variable)*/y);
        }

        if (xOp.isVar() && yOp.isVar()) {
            nextVarX((Variable) x, y);
            return true;
        }

        return false;
    }

    private boolean matchYvar(Term x, Term y) {
        final Term ySubst = getYX(y);

        if (ySubst != null) {
            return match(x, ySubst); //loop
        } else {
            putYX((Variable) y, x);
            if (y instanceof CommonVariable) {
                putXY((Variable) y, x);
            }
            return true;
        }
    }

    public boolean matchXvar(Variable x, Term y) {
        final Term xSubst = getXY(x);

        if (xSubst != null) {
            return match(xSubst, y);
        } else {
            nextVarX(x, y);
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
        } else {
            final Op yOp = y.op();
            if (yOp == xOp) {
                putCommon(xVar, (Variable) y);
            }
        }

    }

    @Override
    public boolean isEmpty() {
        //throw new RuntimeException("unimpl");
        return xy.isEmpty();
    }

    public final boolean matchCompoundWithEllipsis(Compound X, final Compound Y) {

        int xsize = X.size();

        final int numNonpatternVars;
        int ellipsisToMatch = Ellipsis.numUnmatchedEllipsis(X, this);
        if (ellipsisToMatch == 0) {

            int ellipsisTotal = Ellipsis.numEllipsis(X);
            if (ellipsisTotal > 0) {
                //compute a virtual set of subterms based on an existing Ellipsis match
                Term XX = X.substituted(this);
                return (match(XX, Y));
            }

            /** NORMAL: match subterms but do not collect for ellipsis */
            if (xsize != Y.size()) {
                return false;
            }
            numNonpatternVars = xsize;
        } else {
            numNonpatternVars = Ellipsis.countNumNonEllipsis(X);
        }

        //TODO see if there is a volume or structural constraint that can terminate early here


        Ellipsis e = Ellipsis.getFirstUnmatchedEllipsis(X, this);

        final int ysize = Y.size();

        if (!e.valid(numNonpatternVars, ysize)) {
            return false;
        }

        if (numNonpatternVars == 0) {
            //all are to be matched, regardless of commutivity
            return matchEllipsisAll(e, Y);
        }

        if (X.isCommutative()) {
            return matchEllipsedCommutative(
                    X, e, Y
            );
        } else {
            //TODO case where relation is after the ellipsis

            /** if they are images, they must have same relationIndex */
            if (X instanceof Image) { //PRECOMPUTABLE

                int xEllipseIndex = X.indexOf(e);
                int xRelationIndex = ((Image) X).relationIndex;
                int yRelationIndex = ((Image) Y).relationIndex;

                if (xEllipseIndex >= xRelationIndex) {
                    //compare relation from beginning as in non-ellipsis case
                    if (xRelationIndex != yRelationIndex)
                        return false;
                } else {
                    //compare relation from end
                    if ((xsize - xRelationIndex) != (ysize - yRelationIndex))
                        return false;
                }
            }

            return matchEllipsedLinear(
                    X, e, Y
            );
        }

    }

    /**
     * X contains no ellipsis to consider (simple/fast)
     */
    public final boolean matchCompoundWithoutEllipsis(Compound X, final Compound Y) {
        int xsize = X.size();
        int ysize = Y.size();

        if (xsize != ysize)
            return false;

        /** if they are images, they must have same relationIndex */
        if (X instanceof Image) { //PRECOMPUTABLE
            if (((Image) X).relationIndex != ((Image) Y).relationIndex)
                return false;
        }

        if (xsize == 1) {
            return match(X.term(0), Y.term(0));
        } else {
            if (X.isCommutative()) {
                return matchPermute(X, Y); //commutative, try permutations
            } else {
                return matchLinear(X.subterms(), Y.subterms()); //non-commutative (must all match), or no permutation necessary (0 or 1 arity)
            }
        }
    }

    /**
     * X and Y are of the same operator type and length (arity)
     * X's permutations matched against constant Y
     */
    public final boolean matchCompound(Compound X, final Compound Y) {

        int xsize = X.size();
        if (xsize == 0)
            return true; //empty product, ex: ()

        if (Ellipsis.hasEllipsis(X)) //PRECOMPUTABLE
            return matchCompoundWithEllipsis(X, Y);
        else
            return matchCompoundWithoutEllipsis(X, Y);
    }

    /**
     * @param x the compound which is permuted/shuffled
     * @param y what is being compared against
     */
    public final boolean matchPermute(TermContainer x, Compound y) {

        final int len = x.size();

        /* heuristic: use the term size as the subset # of permutations to try */

        int startDivisor = this.powerDivisor;

        final Termutator perm = new Termutator(random, x);
        int attempts = Math.min(perm.total(), powerDivided(len));


        int prePermute = now();


        boolean matched = false;
        while ((attempts-- > 0) && perm.hasNext()) {

            perm.next();

            matched = matchLinear(perm, y);

            if (matched /*|| power <= 0*/) {
                break;
            } else {
                revert(prePermute);
            }
        }

        powerDivisor = startDivisor;

        //finished
        return matched;

    }

    public final boolean matchEllipsisAll(Ellipsis Xellipsis, Compound Y) {
        putXY(Xellipsis, Ellipsis.matchedSubterms(Y));
        return true;
    }

    public final boolean matchEllipsisAll(Ellipsis Xellipsis, Collection<Term> Y) {
        putXY(Xellipsis, Ellipsis.matchedSubterms(Y));
        return true;
    }


    /**
     * commutive compound match: Y into X which contains one ellipsis
     * <p>
     * X pattern contains:
     * <p>
     * one unmatched ellipsis (identified)
     * <p>
     * zero or more "constant" (non-pattern var) terms
     * all of which Y must contain
     * <p>
     * zero or more (non-ellipsis) pattern variables,
     * each of which may be matched or not.
     * matched variables whose resolved values that Y must contain
     * unmatched variables determine the amount of permutations/combinations:
     * <p>
     * if the number of matches available to the ellipse is incompatible with the ellipse requirements, fail
     * <p>
     * (total eligible terms) Choose (total - #normal variables)
     * these are then matched in revertable frames.
     * <p>
     * *        proceed to collect the remaining zero or more terms as the ellipse's match using a predicate filter
     *
     * @param X the pattern term
     * @param Y the compound being matched into X
     */
    public final boolean matchEllipsedCommutative(Compound X, Ellipsis Xellipsis, Compound Y) {

        //ALL OF THIS CAN BE PRECOMPUTED
        Set<Term> matchFirst = Global.newHashSet(0); //Global.newHashSet(0);
        for (Term x : X.terms()) {
            if (x == Xellipsis) continue;
            if (x.op() == type) {
                Term r = getXY(x);
                if (r != null) {
                    if (r instanceof ShadowProduct) {
                        /* this is a subsequent instance of
                           an already matched ellipse, expand
                           its contents as if it were part of X  */
                        ((ShadowProduct) r).addAllTo(matchFirst);
                    } else {
                        matchFirst.add(r);
                    }
                } else {
                    matchFirst.add(x);
                }
            } else {
                matchFirst.add(x);
            }
        }

        int numMatchable = Y.size() - matchFirst.size(); //remaining
        if (!Xellipsis.valid(numMatchable)) {
            //wouldnt be enough remaining matches to satisfy ellipsis cardinality
            return false;
        }

        //match all the fixed-position subterms
        MutableSet<Term> yFree = Y.toSet();
        if (!matchAllCommutive(matchFirst, yFree)) {
            return false;
        }

        //select all remaining
        return matchEllipsisAll(Xellipsis, yFree);
    }



    /** toMatch matched into some or all of Y's terms */
    private boolean matchAllCommutive(Set<Term> toMatch, MutableSet<Term> y) {
        int xsize = toMatch.size();
        Term[] x = toMatch.toArray(new Term[xsize]);
        if (xsize == 1) {

            return matchChoose1(x[0], y);

        } else if (xsize == 2) {

            int prePermute = now();
            MutableSet<Term> yCopy = y.clone(); //because matchChoose1 will remove on match

            //initial shuffle
            if (random.nextBoolean()) {
                Term p = x[0];
                x[0] = x[1];
                x[1] = p;
            }

            for (int i = 0; i < 2; i++) {

                boolean modified = false;
                if (matchChoose1(x[0], y)) {
                    modified = true;
                    if (matchChoose1(x[1], y)) {
                        return true;
                    }
                }

                if (modified) {
                    y.addAll(yCopy); //restore the original set if any where removed during an incomplete match
                }

                revert(prePermute);

                /* swap */
                Term p = x[0];
                x[0] = x[1];
                x[1] = p;
            }

            return false;
        } else {
            //3 or more combination
            throw new RuntimeException("unimpl");
        }


    }

    /**
     * choose 1 at a time from a set of N, which means iterating up to N
     * will remove the chosen item(s) from Y if successful before returning
     */
    private boolean matchChoose1(Term X, Set<Term> Yfree) {

        final int ysize = Yfree.size();
        int shuffle = random.nextInt(ysize); //randomize starting offset

        final int prePermute = now();

        int iterations = Math.min(ysize, (powerAvailable()));


        Term[] yy = Yfree.toArray(new Term[ysize]);

        for (int i = 0; i < iterations; i++) {

            Term y = yy[(shuffle++) % ysize];

            boolean matched = match(X, y);

            if (matched) {
                Yfree.remove(y); //exclude this item from the set of free terms
                return true;
            } else {
                revert(prePermute);
                //else: continue on next permutation
            }
        }

        //finished
        return false;

    }

    /**
     * non-commutive compound match
     * X will contain at least one ellipsis
     * <p>
     * match subterms in sequence
     * <p>
     * WARNING this implementation only works if there is one ellipse in the subterms
     * this is not tested for either
     */
    public final boolean matchEllipsedLinear(Compound X, Ellipsis Xellipsis, Compound Y) {

        int i = 0, j = 0;
        int xsize = X.size();
        int ysize = Y.size();
        while (i < xsize) {
            Term x = X.term(i++);

            boolean expansionFollows = i < xsize && X.term(i) == Ellipsis.Expand;
            if (expansionFollows) i++; //skip over it

            if (x instanceof Ellipsis) {
                Term eMatched = getXY(x); //ShadowProduct if non null
                if (eMatched == null) {
                    //COLLECT
                    if (i == xsize) {
                        //SUFFIX
                        int available = ysize - j;
                        if (!Xellipsis.valid(available))
                            return false;

                        //TODO special handling to extract intermvals from Sequence terms here

                        putXY(Xellipsis, new ShadowProduct(
                                Y.terms(j, ysize)
                        ));
                    } else if (i == 0) {
                        //PREFIX the ellipsis occurred at the start and there are additional terms following it
                        //TODO
                        return false;
                    } else {
                        //INNER the ellipsis occurred before the end, we need to handle internal ranges, not just suffix
                        //TODO
                        return false;
                    }
                } else {
                    //previous match exists, match against what it had
                    if (i == xsize) {
                        //SUFFIX - match the remaining terms against what the ellipsis previously collected
                        Term[] sp = ((ShadowProduct) eMatched).term;
                        for (int k = 0; k < sp.length; k++) {
                            if (!match(sp[k], Y.term(j++)))
                                return false;
                        }
                    } else {
                        //TODO other cases
                        return false;
                    }

                }
            } else {
                if (!match(x, Y.term(j++)))
                    return false;
            }
        }

        return true;
    }


    /**
     * elimination
     */
    private final void putVarX(final Variable x, final Term y) {
        putXY(x, y);
        if (x instanceof CommonVariable) {
            putYX(x, y);
        }
    }


    private void putCommon(final Variable x, final Variable y) {
        final Variable commonVar = CommonVariable.make(x, y);
        putXY(x, commonVar);
        putYX(y, commonVar);
        //return true;
    }

    public boolean matchLinear(final TermContainer X, final TermContainer Y) {
        return matchLinear(X, Y, 0, X.size());
    }

    /**
     * a branch for comparing a particular permutation, called from the main next()
     */
    public boolean matchLinear(final TermContainer X, final TermContainer Y, int start, int stop) {

        final int yLen = Y.size();

        int startDivisor = powerDivisor;
        if (!powerDividable(yLen))
            return false;

        boolean success = true;
        for (int i = start; i < stop; i++) {
            if (!match(X.term(i), Y.term(i))) {
                success = false;
                break;
            }
        }

        powerDivisor = startDivisor;

        //success
        return success;
    }


    private boolean powerDividable(int factor) {
        if (powerAvailable() < factor) return false;

        powerDivide(factor);

        return true;
    }


    private void powerDivide(int factor) {
        if (factor <= 0)
            factor = 1; //HACK
        this.powerDivisor = Math.max(1, this.powerDivisor * factor);
    }

    private int powerDivided(int factor) {
        if (powerAvailable() < factor) return 0;

        powerDivide(factor);

        return powerAvailable();
    }

    private int powerAvailable() {
        if (powerDivisor <= 0) powerDivisor = 1; //HACK
        return power / powerDivisor;
    }

    @Override
    public final void putXY(Term x /* usually a Variable */, Term y) {
        xy.put(x, y);
    }

    public final void putYX(Term y /* usually a Variable */, Term x) {
        yx.put(y, x);
    }


    public final Term resolve(Term t) {
        //TODO make a half resolve that only does xy?

        Term ret = t.substituted(xy);
        if (ret != null) {
            ret = ret.substituted(yx);
        }
        return ret;

    }


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

/*
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

        } while (processed < yLen);

 */