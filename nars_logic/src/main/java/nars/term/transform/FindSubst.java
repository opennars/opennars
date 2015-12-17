package nars.term.transform;

import com.google.common.collect.ListMultimap;
import com.gs.collections.api.map.ImmutableMap;
import com.gs.collections.api.set.MutableSet;
import com.gs.collections.impl.factory.Maps;
import nars.Global;
import nars.Memory;
import nars.NAR;
import nars.Op;
import nars.nal.meta.PreCondition;
import nars.nal.meta.TermPattern;
import nars.nal.meta.match.*;
import nars.term.Term;
import nars.term.TermContainer;
import nars.term.compound.Compound;
import nars.term.compound.GenericCompound;
import nars.term.variable.CommonVariable;
import nars.term.variable.Variable;
import nars.util.version.VersionMap;
import nars.util.version.Versioned;
import nars.util.version.Versioning;

import java.util.*;


/* recurses a pair of compound term tree's subterms
across a hierarchy of sequential and permutative fanouts
where valid matches are discovered, backtracked,
and collected until power is depleted. */
abstract public class FindSubst extends Versioning implements Subst {


    public final Random random;

    public final Op type;


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
    protected final Versioned<Term> term;

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
    int powerDivisor;



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


    final void goSubterm(int index) {
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
        }
    }


    public static final class VarCachedVersionMap extends VersionMap<Term, Term> implements Subst {

        public VarCachedVersionMap(Versioning context) {
            super(context);
        }
        public VarCachedVersionMap(Versioning context, Map<Term,Versioned<Term>> map) {
            super(context, map);
        }

        @Override
        public final boolean cache(Term key) {
            //since these should always be normalized variables, they will not exceed a predictable range of entries (ex: $1, $2, .. $n)
            return key instanceof Variable;
        }

        @Override
        public final Term getXY(Object t) {
            Versioned<Term> v = map.get(t);
            if (v == null) return null;
            return v.get();
        }

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
        public boolean run(FindSubst ff) {
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
        private final transient String id;


        public SubTermStructure(Op matchingType, int subterm, int bits) {
            this.subterm = subterm;

//            if (matchingType != Op.VAR_PATTERN)
//                bits &= (~matchingType.bit());
            bits &= ~(Op.VariableBits);

            this.bits = bits;
            id = "t" + subterm + ':' +
                    Integer.toString(bits, 16);
        }


        @Override
        public String toString() {
            return id;
        }

        @Override
        boolean run(FindSubst ff) {
            Compound t = (Compound) ff.term.get();
            return !t.term(subterm).impossibleStructureMatch(bits);
        }
    }

    /**
     * requires a specific subterm type
     */
    public static final class SubTermOp extends PatternOp {
        public final int subterm;
        public final Op op;
        private final transient String id;


        public SubTermOp(int subterm, Op op) {
            this.subterm = subterm;
            this.op = op;
            id = "t" + subterm + ':' + op;
        }

        @Override
        public String toString() {
            return id;
        }

        @Override
        boolean run(FindSubst ff) {
            Compound parent = (Compound) ff.term.get();
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
            return ((Compound) t).relation() == index;
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
        private final String id;
        private final ImmutableMap<Term,MatchConstraint> constraints;

        public MatchTerm(Term term, ListMultimap<Term, MatchConstraint> c) {
            x = term;
            if (c == null || c.isEmpty()) {
                this.id = x.toString();
                this.constraints = null;
            } else {
                Map<Term,MatchConstraint> con = Global.newHashMap();
                c.asMap().forEach( (t, cc)-> {
                    switch (cc.size()) {
                        case 0: return;
                        case 1: con.put(t, cc.iterator().next());
                                break;
                        default:
                            con.put(t, new AndConstraint(cc));
                            break;
                    }
                });


                this.constraints = Maps.immutable.ofAll(con);
                this.id = x.toStringCompact() + "^" + con;

//                this.id = new StringBuilder(x.toString() + "∧neq(").append(
//                    Joiner.on(",").join(notEquals.stream().map(v -> {
//                        return ( v.getOne() + "==" + v.getTwo() );
//                    }).collect(Collectors.toList()))
//                ).append(")").toString();
            }
        }

        @Override
        public boolean run(FindSubst ff) {
            ff.setConstraints(constraints);
            ff.matchAll(x, ff.term.get());
            return true;
        }

        @Override
        public String toString() {
            return id;
        }
    }

    /** null to disable exclusions */
    public void setConstraints(ImmutableMap<Term,MatchConstraint> constraints) {
        if (constraints == null || constraints.isEmpty())
            constraints = null;
        else
            this.constraints = constraints;
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
        public boolean run(FindSubst ff) {

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
        public boolean run(FindSubst f) {
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
        public boolean run(FindSubst f) {
            f.term.set(f.parent.get());
            f.parent.set(parent);

            return true;
        }

        @Override
        public String toString() {
            return "parent(" + parent + ')'; //s for subterm and sibling
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
    public final void matchAll(Term x, Term y, int startPower) {

        setPower(startPower);

        matchAll(x, y);

        //System.out.println(startPower + "\t" + power);
    }

    /**
     * find substitutions using a pre-compiled term pattern
     */
    @Deprecated
    public final boolean matchAll(TermPattern x, Term y, int startPower) {

        term.set(y);

        setPower(startPower);

        boolean match = true;

        for (PreCondition o : x.code) {
            if (!(o instanceof PatternOp)) continue;
            if (!((PatternOp) o).run(this)) {
                match = false;
                break;
            }
        }

        if (powerDivisor != 1.0f)
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




    final List<Termutation> termutes = Global.newArrayList();

    public interface Termutation {
        public boolean test();

        public void reset();

        int total();
    }

    public class TermutationPermutation implements Termutation {
        final Termutator perm;
        private final TermContainer y;

        @Override
        public String toString() {
            return "TermutationPermutation{" +
                    "perm=" + perm +
                    ", y=" + y +
                    '}';
        }

        public TermutationPermutation(TermContainer x, TermContainer Y) {
            this.perm = new Termutator(random,x);
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

    public final boolean matchPermute(TermContainer x, Compound y) {
        termutes.add(new TermutationPermutation(x, y));
        return true;
    }

    /**
     * @param x the compound which is permuted/shuffled
     * @param y what is being compared against
     */
    public final boolean matchPermuteOLD(TermContainer x, Compound y) {

        int len = x.size();

        /* heuristic: use the term size as the subset # of permutations to try */

        int startDivisor = powerDivisor;

        Termutator perm = new Termutator(random, x);
        int attempts = Math.min(perm.total(), powerDivided(len));


        int prePermute = now();


        boolean matched = false;
        while (attempts-- > 0) { // && perm.hasNext()) {

            perm.next();

            if (matchLinear(perm, y, 0, len)) {
                matched = true;
                break;
            } else {
                revert(prePermute);
            }
        }

        powerDivisor = startDivisor;

        //finished
        return matched;

    }

//    public final boolean matchEllipsisAll(Ellipsis Xellipsis, Compound Y) {
//        putXY(Xellipsis, Ellipsis.matchedSubterms(Y));
//        return true;
//    }

    public final boolean matchEllipsisAll(Ellipsis Xellipsis, Collection<Term> Y) {
        return putXY(Xellipsis, new CollectionEllipsisMatch(Y));
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
                    //HACK should not need new list
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

            //first match all the fixed-position subterms
            if (!matchAllCommutive(matchFirst, yFree)) {
                return false;
            }
        }

        //select all remaining
        return matchEllipsisAll(Xellipsis, yFree);
    }



    /** toMatch matched into some or all of Y's terms */
    private boolean matchAllCommutive(Set<Term> toMatch, MutableSet<Term> y) {
        int xsize = toMatch.size();

        switch (xsize) {
            case 0:
                return true;
            case 1:
                return matchChoose1(toMatch.iterator().next(), y);
            case 2:
                return matchChoose2(toMatch.toArray(new Term[xsize]), y);
            default:
                //3 or more combination
                throw new RuntimeException("unimpl: " + xsize + " arity combination unimplemented");
        }
    }

    private boolean matchChoose2(Term[] x, MutableSet<Term> y) {
        int prePermute = now();
        MutableSet<Term> yCopy = y.clone(); //because matchChoose1 will remove on match

        //initial shuffle
        if (random.nextBoolean()) {
            Term p = x[0];
            x[0] = x[1];
            x[1] = p;
        }

        int startDivisor = powerDivisor;
        if (!powerDividable(2))
            return false;

        boolean matched = false;
        for (int i = 0; i < 2; i++) {

            boolean modified = false;
            if (matchChoose1(x[0], y)) {
                modified = true;
                if (matchChoose1(x[1], y)) {
                    matched = true;
                    break;
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

        powerDivisor = startDivisor;
        return matched;
    }

    /**
     * choose 1 at a time from a set of N, which means iterating up to N
     * will remove the chosen item(s) from Y if successful before returning
     */
    private boolean matchChoose1(Term X, Set<Term> Yfree) {

        int ysize = Yfree.size();
        int shuffle = random.nextInt(ysize); //randomize starting offset

        int prePermute = now();

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

//    public boolean matchLinear(TermContainer X, TermContainer Y) {
//        return matchLinear(X, Y, 0, X.size());
//    }

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

        Termutation t = termutes.get(i);
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

        powerDivide(factor);

        return true;
    }


    private void powerDivide(int factor) {
        /*if (factor <= 0)
            factor = 1; //HACK*/
        powerDivisor = Math.max(1, powerDivisor * factor);
    }

    private int powerDivided(int factor) {
        if (powerAvailable() < factor) return 0;

        powerDivide(factor);

        return powerAvailable();
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
        if (v!=null) {
            if (x.equals(v.get())) {
                return true; //same value
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