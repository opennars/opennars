package nars.term.transform;

import com.gs.collections.api.map.ImmutableMap;
import com.gs.collections.api.set.MutableSet;
import nars.Global;
import nars.Memory;
import nars.NAR;
import nars.Op;
import nars.term.Term;
import nars.term.TermContainer;
import nars.term.compound.Compound;
import nars.term.compound.GenericCompound;
import nars.term.constraint.MatchConstraint;
import nars.term.match.*;
import nars.term.variable.CommonVariable;
import nars.term.variable.Variable;
import nars.util.version.Versioned;
import nars.util.version.Versioning;

import java.util.List;
import java.util.Random;
import java.util.Set;


/* recurses a pair of compound term tree's subterms
across a hierarchy of sequential and permutative fanouts
where valid matches are discovered, backtracked,
and collected until power is depleted. */
abstract public class FindSubst extends Versioning implements Subst {


    public final Random random;

    public final Op type;

    final List<Termutator> termutes = Global.newArrayList();

    public abstract class Termutator {
        public abstract boolean test();

        public abstract void reset();

        public abstract int total();


        @Override
        public boolean equals(Object obj) {
            return toString().equals(obj.toString());
        }

        @Override
        public int hashCode() {
            return toString().hashCode();
        }
    }


    @Override
    public String toString() {
        return "subst:{" +
                "now:" + now() +
                ", type:" + type +
                ", term:" + term +
                ", parent:" + parent +
                //"random:" + random +
                ", power:" + power +
                ", xy:" + xy +
                ", yx:" + yx +
                '}';
    }



    /** variables whose contents are disallowed to equal each other */
    public ImmutableMap<Term, MatchConstraint> constraints = null;

    //public abstract Term resolve(Term t, Substitution s);

    public final VarCachedVersionMap xy;
    public final VarCachedVersionMap yx;

    /** current "y"-term being matched against */
    public final Versioned<Term> term;

    /** parent, if in subterms */
    public final Versioned<Compound> parent;

    /** unification power available at start of current branch */
    //public final Versioned<Integer> branchPower;

    /** unification power remaining in the current branch */
    int power;

    /** current power divisor which divides power to
     *  limits the # of permutations
     *  that can be tried, or the # of subterms that can be compared
     */
    @Deprecated int powerDivisor;



    public FindSubst(Op type, NAR nar) {
        this(type, nar.memory);
    }

    public FindSubst(Op type, Memory memory) {
        this(type, memory.random);
    }

    public FindSubst(Op type, Random random) {
        this.random = random;
        this.type = type;

        xy = new VarCachedVersionMap(this);
        yx = new VarCachedVersionMap(this);
        term = new Versioned(this);
        parent = new Versioned(this);
        //branchPower = new Versioned(this);


    }

    public void setPower(int startPower) {
        power = startPower;
        powerDivisor = 1;
    }

    /** called each time all variables are satisfied in a unique way */
    abstract public boolean onMatch();

    /** called each time a match is not fully successful */
    public void onPartial() {

    }

    @Override
    public void clear() {
        revert(0);
        constraints = null;
    }


    public Term getXY(Object t) {
        return xy.getXY(t);
    }

    public Term getYX(Term t) {
        return yx.getXY(t);
    }


    public final void goSubterm(int index) {
        Term pp = parent.get().term(index);
        /*if (pp == null)
            throw new RuntimeException("null subterm");*/
        term.set( pp );
    }

    public void matchAll(Term x, Term y) {
        termutes.clear();
        if (match(x, y)) {
            if (!termutes.isEmpty()) {
                matchTermutations(0);
            } else {
                onMatch();
            }
        } else {
            onPartial();
        }
    }

    private final void print(String prefix, Term a, Term b) {
        System.out.print(prefix);
        if (a != null)
            System.out.println(" " + a + " ||| " + b);
        else
            System.out.println();
        System.out.println("     " + this);
    }


    /** null to disable exclusions */
    public void setConstraints(ImmutableMap<Term,MatchConstraint> constraints) {
        if (constraints == null || constraints.isEmpty())
            constraints = null;
        else
            this.constraints = constraints;
    }


    /**
     * find substitutions, returning the success state.
     */
    public final void matchAll(Term x, Term y, int startPower) {

        setPower(startPower);

        matchAll(x, y);

        //System.out.println(startPower + "\t" + power);
    }



    /**
     * recurses into the next sublevel of the term
     *
     * @return if success: a POSITIVE next power value, after having subtracted the cost (>0)
     * if fail: the NEGATED next power value (<=0)
     * *
     * this effectively uses the sign bit of the integer as a success flag while still preserving the magnitude of the decreased power for the next attempt
     */
    public final boolean match(Term x, Term y) {

         if (x.equals(y)) {
            return true;
        }

        /*if ((--power) < 0)
            return false;*/

        Op t = type;
        Op xOp = x.op();
        Op yOp = y.op();

        if ((xOp == yOp) && (x instanceof Compound)) {
            return ((Compound)x).match((Compound)y, this);
        }

        if (xOp == t) {
            return matchXvar((Variable) x, y);
        }

        if (yOp == t) {
            return matchYvar(x, /*(Variable)*/y);
        }

        if (xOp.isVar() && yOp.isVar()) {
            return nextVarX((Variable) x, y);
        }

        return false;
    }

    private boolean matchYvar(Term x, Term y) {
        Term ySubst = getYX(y);

        if (ySubst != null) {
            return match(x, ySubst); //loop
        } else {
            putYX((Variable) y, x);
            if (y instanceof CommonVariable) {
                return putXY((Variable) y, x);
            }
            return true;
        }
    }

    public boolean matchXvar(Variable x, Term y) {
        Term xSubst = getXY(x);

        if (xSubst != null /*&& !xSubst.equals(x)*/) {
            return match(xSubst, y);
        } else {
            return nextVarX(x, y);
        }
    }

//    private static void printComparison(int power, Compound cx, Compound cy) {
//        System.out.println(cx.structureString() + " " + cx.volume() + "\t" + cx);
//        System.out.println(cy.structureString() + " " + cy.volume() + "\t" + cy);
//        System.out.println(!cx.impossibleToMatch(cy) + "|" + !cy.impossibleToMatch(cx) + " ---> " + (power >= 0) + " " + power);
//        System.out.println();
//    }


    private final boolean nextVarX(Variable xVar, Term y) {
        Op xOp = xVar.op();

        if (xOp == type) {
            return putVarX(xVar, y);
        } else {
            Op yOp = y.op();
            if (yOp == xOp) {
                return putCommon(xVar, (Variable) y);
            }
        }

        return false;
    }

    @Override
    public boolean isEmpty() {
        //throw new RuntimeException("unimpl");
        return xy.isEmpty();
    }

    public final boolean matchCompoundWithEllipsis(Compound X, Compound Y) {

        int xsize = X.size();

//        final int numNonpatternVars;
//        int ellipsisToMatch = Ellipsis.numUnmatchedEllipsis(X, this);
//        if (ellipsisToMatch == 0) {
//
//            int ellipsisTotal = Ellipsis.numEllipsis(X);
//            if (ellipsisTotal > 0) {
//                //compute a virtual set of subterms based on an existing Ellipsis match
//                Term XX = X.substituted(this);
//                return (match(XX, Y));
//            }
//
//            /** NORMAL: match subterms but do not collect for ellipsis */
//            if (xsize != Y.size()) {
//                return false;
//            }
//            numNonpatternVars = xsize;
//        } else {
//            numNonpatternVars = Ellipsis.countNumNonEllipsis(X);
//        }

        //TODO see if there is a volume or structural constraint that can terminate early here


        Ellipsis e = Ellipsis.getFirstEllipsis(X);

        int ysize = Y.size();

//        if (!e.valid(numNonpatternVars, ysize)) {
//            return false;
//        }


        if (X.isCommutative()) {
            return matchEllipsedCommutative(
                    X, e, Y
            );
        } else {

            if (e instanceof EllipsisTransform) {
                //this involves a special "image ellipsis transform"

                EllipsisTransform et = (EllipsisTransform)e;
                if (et.from.equals(Op.Imdex)) {

                    //the indicated term should be inserted
                    //at the index location of the image
                    //being processed. (this is the opposite
                    //of the other condition of this if { })
                    if (matchEllipsedLinear(X, e, Y)) {
                        ArrayEllipsisMatch raw = (ArrayEllipsisMatch) getXY(e);
                        return putXY(e, new ImagePutMatch(
                                raw.term, et.to, Y)); //HACK somehow just create this in the first place without the intermediate ShadowProduct
                    }
                } else {
                    Term n = apply(et.from, false);
                    if (n == null)
                        return false;

                    //resolving may be possible to defer to substitution if
                    //Y and et.from are components of ImageShrinkEllipsisMatch

                    int imageIndex = Y.indexOf(n);
                    if (imageIndex == -1) {
                        //this specified term that should be
                        //substituted with the relation index
                        //is not contained in this compound;
                        //does not match
                        return false;
                    }

                    if (matchEllipsedLinear(X, e, Y)) {
                        ArrayEllipsisMatch raw = (ArrayEllipsisMatch) getXY(e);
                        return putXY(e, new ImageTakeMatch(
                            raw.term, imageIndex)); //HACK somehow just create this in the first place without the intermediate ShadowProduct
                    }
                }
                return false;
            }

            /** if they are images, they must have same relationIndex */
            if (X.op().isImage()) { //PRECOMPUTABLE

                //if the ellipsis is normal, then interpret the relationIndex as it is
                if (Ellipsis.countNumNonEllipsis(X) > 0) {

                    int xEllipseIndex = X.indexOf(e);
                    int xRelationIndex = ((GenericCompound) X).relation();
                    int yRelationIndex = ((GenericCompound) Y).relation();

                    if (xEllipseIndex >= xRelationIndex) {
                        //compare relation from beginning as in non-ellipsis case
                        if (xRelationIndex != yRelationIndex)
                            return false;
                    } else {
                        //compare relation from end
                        if ((xsize - xRelationIndex) != (ysize - yRelationIndex))
                            return false;
                    }
                } else {
                    //ignore the location of imdex in the pattern and match everything

                }

            }

            return matchEllipsedLinear( X, e, Y );
        }

    }

//    private boolean matchEllipsisImage(Compound x, Ellipsis e, Compound y) {
//        /*  ex:
//           (M --> (A..B=_..+))
//        */
//        putXY(e, new ShadowProduct(x.terms()));
//        return false;
//    }

    public class CommutivePermutations extends Termutator {
        final ShuffledSubterms perm;
        private final TermContainer y;
        private transient String id;

        @Override
        public String toString() {
            if (this.id == null) {
                return this.id = "CommutivePermutations{" +
                        "perm=" + perm.compound /* should be in normal sorted order if commutive */ +
                        ", y=" + y +
                        '}';
            }
            return this.id;
        }

        public CommutivePermutations(TermContainer x, TermContainer Y) {
            this.perm = new ShuffledSubterms(random,x);
            this.y = Y;
        }

        public int total() { return perm.total(); }

        public boolean test() {
            boolean b = matchLinear(perm, y, 0, perm.size());
            if (perm.hasNext())
                perm.next();
            return b;
        }

        public void reset() {
            perm.reset();
            perm.next();
        }

        public boolean hasNext() {
            return perm.hasNext();
        }
    }

    protected void termute(Termutator t) {
        if (!termutes.contains(t))
            termutes.add(t);
    }

    public final boolean matchPermute(TermContainer x, Compound y) {
        termute(new CommutivePermutations(x, y));
        return true;
    }


    /**
     * commutive compound match: Y into X which contains one ellipsis
     * <p>
     * X pattern contains:
     * <p>
     * one unmatched ellipsis (identified)
     * <p>                    //HACK should not need new list

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

        //terms in Y which have previously been matched and should not be matched by the ellipsis
        Set<Term> ineligible = Global.newHashSet(0);
        for (Term x : X.terms()) {

            Term alreadyMatched = getXY(x);
            if (alreadyMatched == null) {

                //ellipsis to be matched in stage 2
                if (x == Xellipsis || x.equals(Ellipsis.Shim))
                    continue;

            } else {
                if (alreadyMatched instanceof EllipsisMatch) {
                    //check that Y contains all of these
                    if (!((EllipsisMatch)alreadyMatched).addContained(Y, ineligible))
                        return false;
                } else {
                    if (!Y.containsTerm(alreadyMatched))
                        return false;
                    ineligible.add(alreadyMatched);
                }

                continue;

            }

            matchFirst.add(x);


//            if (x.op().isVar()) { // == type) {
//                Term r = getXY(x);
//                if (r instanceof EllipsisMatch) {
//                    //adds what would ordinarily be inlined in a Substitution
//                    //which is not necessarily all of the terms it contains
//                    //if it were iterated directly
//                    ((EllipsisMatch) r).applyTo(this, matchFirst, false);
//                    continue;
//                } //else r == null
//            }

        }

        int numMatchable = Y.size() - matchFirst.size(); //remaining
        if (!Xellipsis.valid(numMatchable)) {
            //wouldnt be enough remaining matches to satisfy ellipsis cardinality
            return false;
        }

        MutableSet<Term> yFree = Y.toSet();
        yFree.removeAll(ineligible);
        if (yFree.isEmpty()) {
            //nothing to match, it is already all valid
            return true;
        }


        if (!matchFirst.isEmpty()) {

            //first match all the fixed-position subterms (later)
            matchCommutiveRemaining(Xellipsis, matchFirst, yFree);
            return true;
        }

        //select all remaining
        return putXY(Xellipsis, new CollectionEllipsisMatch(yFree));
    }



    /** toMatch matched into some or all of Y's terms */
    private boolean matchCommutiveRemaining(Term xEllipsis, Set<Term> xToMatch, MutableSet<Term> yFree) {
        int xsize = xToMatch.size();

        switch (xsize) {
            case 0:
                return true;
            case 1:
                termute(new Choose1(xEllipsis, xToMatch.iterator().next(), yFree));
                return true;
            case 2:
                termute(new Choose2(xEllipsis, xToMatch.toArray(new Term[xsize]), yFree));
                return true;
            default:
                //3 or more combination
                throw new RuntimeException("unimpl: " + xsize + " arity combination unimplemented");
        }

    }

//    private boolean matchChoose2(Term[] x, MutableSet<Term> y) {
//        int prePermute = now();
//        MutableSet<Term> yCopy = y.clone(); //because matchChoose1 will remove on match
//
//        //initial shuffle
//        if (random.nextBoolean()) {
//            Term p = x[0];
//            x[0] = x[1];
//            x[1] = p;
//        }
//
//        int startDivisor = powerDivisor;
//        if (!powerDividable(2))
//            return false;
//
//        boolean matched = false;
//        for (int i = 0; i < 2; i++) {
//
//            boolean modified = false;
//            if (matchChoose1(x[0], y)) {
//                modified = true;
//                if (matchChoose1(x[1], y)) {
//                    matched = true;
//                    break;
//                }
//            }
//
//            if (modified) {
//                y.addAll(yCopy); //restore the original set if any where removed during an incomplete match
//            }
//
//            revert(prePermute);
//
//            /* swap */
//            Term p = x[0];
//            x[0] = x[1];
//            x[1] = p;
//        }
//
//        powerDivisor = startDivisor;
//        return matched;
//    }

    /**
     * choose 1 at a time from a set of N, which means iterating up to N
     * will remove the chosen item(s) from Y if successful before returning
     */
    public class Choose1 extends Termutator {

        private final Set<Term> yFree;
        private final Term x;
        private final Term xEllipsis;
        private int shuffle;
        private final Term[] yy;
        private transient String id;

        @Override
        public String toString() {
            if (this.id == null) {
                return this.id = "Choose1{" +
                        "yFree=" + yFree +
                        ", xEllipsis=" + xEllipsis +
                        ", x=" + x +
                        '}';
            }
            return this.id;
        }

        public Choose1(Term xEllipsis, Term x, Set<Term> yFree) {
            this.x = x;
            this.yFree = yFree;
            this.xEllipsis = xEllipsis;
            int ysize = yFree.size();
            yy = yFree.toArray(new Term[ysize]);
        }

        @Override
        public int total() {
            return yFree.size();
        }

        @Override
        public void reset() {
            this.shuffle = random.nextInt(total()); //randomize starting offset
        }

        @Override
        public boolean test() {
            final int ysize = yy.length;

            Term y = yy[(shuffle++) % ysize];

            boolean matched = match(x, y);

            if (matched) {
                return putXY(xEllipsis, new CollectionEllipsisMatch(yFree, y));
            }

            return false;
        }
    }

    public class Choose2 extends Termutator {

        private final Set<Term> yFree;
        private final Term[] x;
        private final Term xEllipsis;
        private int shuffle, shuffle2;
        private final Term[] yy;
        private transient String id;
        private int n;

        @Override
        public String toString() {
            if (this.id == null) {
                return this.id = "Choose2{" +
                        "yFree=" + yFree +
                        ", xEllipsis=" + xEllipsis +
                        ", x=" + x[0] + "," + x[1] +
                         '}';
            }
            return this.id;
        }

        public Choose2(Term xEllipsis, Term[] x, Set<Term> yFree) {
            this.x = x;
            this.yFree = yFree;
            this.xEllipsis = xEllipsis;
            int ysize = yFree.size();
            yy = yFree.toArray(new Term[ysize]);
        }

        @Override
        public int total() {
            int n = yFree.size();
            return n * (n-1);
        }

        @Override
        public void reset() {
            int t = total();
            this.shuffle = random.nextInt(t); //randomize starting offset
            this.shuffle2 = random.nextInt(t -1); //randomize starting offset
            this.n = 0;
        }

        @Override
        public boolean test() {
            final int ysize = yy.length;

            if (n % ysize == 0) {
                shuffle2++;
            }

            Term y1 = yy[(n + shuffle) % ysize];
            n++;

            if (match(x[0], y1)) {

                int n2 = shuffle2;
                if (n2 == (n-1 /* to account for the n++ */ + shuffle) )
                    n2 = (n2 + 1);

                Term y2 = yy[n2% ysize];
                if (match(x[1], y2)) {
                    return putXY(xEllipsis, new CollectionEllipsisMatch(yFree, y1, y2));
                }
            }
            return false;
        }
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

            boolean expansionFollows = i < xsize && X.term(i) == Ellipsis.Shim;
            if (expansionFollows) i++; //skip over it

            if (x instanceof Ellipsis) {
                Term eMatched = getXY(x); //EllipsisMatch, or null
                if (eMatched == null) {
                    //COLLECT
                    if (i == xsize) {
                        //SUFFIX
                        int available = ysize - j;
                        if (!Xellipsis.valid(available))
                            return false;

                        //TODO special handling to extract intermvals from Sequence terms here

                        if (!putXY(Xellipsis,
                                new ArrayEllipsisMatch(
                                        Y, j, j + available
                                ))) {
                            return false;
                        }
                    } else {
                        //PREFIX the ellipsis occurred at the start and there are additional terms following it
                        //TODO
                        return false;
                    }
                } else {
                    //previous match exists, match against what it had
                    if (i == xsize) {
                        //SUFFIX - match the remaining terms against what the ellipsis previously collected
                        //HACK this only works with ArrayEllipsisMatch type
                        Term[] sp = ((ArrayEllipsisMatch) eMatched).term;
                        for (Term aSp : sp) {
                            if (!match(aSp, Y.term(j++)))
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
    private final boolean putVarX(Variable x, Term y) {
        if (putXY(x, y)) {
            if (x instanceof CommonVariable) {
                putYX(x, y);
            }
            return true;
        }
        return false;
    }


    private boolean putCommon(Variable x, Variable y) {
        Variable commonVar = CommonVariable.make(x, y);
        if (putXY(x, commonVar)) {
            putYX(y, commonVar);
            return true;
        }
        return false;
    }

    /**
     * a branch for comparing a particular permutation, called from the main next()
     */
    public boolean matchLinear(TermContainer X, TermContainer Y, int start, int stop) {

        int startDivisor = powerDivisor;
        if (!powerDividable(stop-start))
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

    private boolean matchTermutations(int i) {

        if (i == termutes.size()) {
            //System.out.println("SOLUTION: " + xy);
            onMatch();
            return true;
        }

        Termutator t = termutes.get(i);
        t.reset();

        int revert = now();

        for (int j = 0; j < t.total(); j++) {


            //System.out.println("@" + now() + " " + i + ": " + t + " ");
            //System.out.println("->  " + xy);

            if (t.test()) {
                //System.out.println("@" + now() + " -->  " + xy);
                if (matchTermutations(i + 1)) {

                }
            }

            revert(revert);
            //System.out.println("@" + revert + " <-  " + xy);

        }

        return true;// matches!=0;
    }


    private boolean powerDividable(int factor) {
        if (powerAvailable() < factor) return false;

        powerDivisor = Math.max(1, powerDivisor * factor);

        return true;
    }


    private int powerAvailable() {
        //if (powerDivisor <= 0) powerDivisor = 1; //HACK
        return power / powerDivisor;
    }

    /** returns true if the assignment was allowed, false otherwise */
    public final boolean putXY(Term x /* usually a Variable */, Term y) {

//        if (x.equals(y))
//            throw new RuntimeException("x==y");

        VarCachedVersionMap xy = this.xy;

        Versioned<Term> v = xy.map.get(x);
        Term vv = v!=null ? v.get() : null;
        if (vv!=null) {
            if (y.equals(vv)) {
                return true; //same value
            } else {
                return false; //already assigned
            }
        }
        if (!assignable(x, y))
            return false;

        if (v == null) {
            v = xy.getOrCreateIfAbsent(x);
        }

        v.set(y);
        return true;
    }

    /** true if the match assignment is allowed by constraints */
    public boolean assignable(Term x, Term y) {
        if (constraints == null) return true;
        MatchConstraint c = constraints.get(x);
        if (c == null) return true;
        return !c.invalid(x, y, this);
    }

    public final void putYX(Term y /* usually a Variable */, Term x) {
        yx.put(y, x);
    }


    public final Term apply(Term t, boolean fullMatch) {
        //TODO make a half resolve that only does xy?


        Term ret = t.apply(this, fullMatch);

        if ((ret != null) /*&& (!yx.isEmpty())*/) {
            ret = ret.apply(yx, fullMatch);
        }
        return ret;

    }


}


