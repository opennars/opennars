package nars.term.transform;

import com.gs.collections.api.map.ImmutableMap;
import com.gs.collections.api.set.MutableSet;
import nars.Global;
import nars.Memory;
import nars.NAR;
import nars.Op;
import nars.term.Term;
import nars.term.TermContainer;
import nars.term.Termlike;
import nars.term.compound.Compound;
import nars.term.constraint.MatchConstraint;
import nars.term.match.Ellipsis;
import nars.term.match.EllipsisMatch;
import nars.term.match.EllipsisTransform;
import nars.term.match.ImageMatch;
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
public abstract class FindSubst extends Versioning implements Subst {


    public final Random random;

    public final Op type;

    /**
     * variables whose contents are disallowed to equal each other
     */
    public ImmutableMap<Term, MatchConstraint> constraints = null;

    //public abstract Term resolve(Term t, Substitution s);

    public final VarCachedVersionMap xy;
    public final VarCachedVersionMap yx;

    /**
     * current "y"-term being matched against
     */
    public final Versioned<Term> term;

    /**
     * parent, if in subterms
     */
    public final Versioned<Compound> parent;


    public final List<Termutator> termutes = Global.newArrayList();


    @Override
    public String toString() {
        return "subst:{" +
                "now:" + now() +
                ", type:" + type +
                ", term:" + term +
                ", parent:" + parent +
                //"random:" + random +
                ", xy:" + xy +
                ", yx:" + yx +
                '}';
    }


    protected FindSubst(Op type, NAR nar) {
        this(type, nar.memory);
    }

    protected FindSubst(Op type, Memory memory) {
        this(type, memory.random);
    }

    protected FindSubst(Op type, Random random) {
        this.random = random;
        this.type = type;

        xy = new VarCachedVersionMap(this);
        yx = new VarCachedVersionMap(this);
        term = new Versioned(this);
        parent = new Versioned(this);
        //branchPower = new Versioned(this);


    }

    /**
     * called each time all variables are satisfied in a unique way
     */
    public abstract boolean onMatch();
//    /**
//     * called each time a match is not fully successful
//     */
//    public void onPartial() {
//
//    }

    @Override
    public void clear() {
        revert(0);
        constraints = null;
    }


    @Override
    public Term getXY(Object t) {
        return xy.getXY(t);
    }

    public Term getYX(Term t) {
        return yx.getXY(t);
    }


    public final void goSubterm(int index) {
        term.set(parent.get().term(index));
    }

    public final void matchAll(Term x, Term y) {
        matchAll(x, y, true);
    }

    /** setting finish=false allows matching in pieces before finishing */
    public final void matchAll(Term x, Term y, boolean finish) {
        if (match(x, y) && finish) {
            if (!termutes.isEmpty())
                matchTermutes();
            else
                onMatch(); //no termutations, it matched, we're done
        }
    }

    private void matchTermutes() {
        //repeat until # of termutes stabilizes
        int termutesPre;
        //System.out.println("termutes start");
        do {
            //System.out.println("  termutes: " + termutes);
            termutesPre = termutes.size();
            matchTermutations(0, termutesPre);
        } while (termutes.size() != termutesPre);

        termutes.clear();
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
     * recurses into the next sublevel of the term
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
            return ((Compound) x).match((Compound) y, this);
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
            putYX(/*(Variable)*/ y, x);
            if (y instanceof CommonVariable) {
                return putXY(/*(Variable)*/ y, x);
            }
            return true;
        }
    }

    public boolean matchXvar(Variable x, Term y) {
        Term xSubst = getXY(x);

        return (xSubst != null) ?
                match(xSubst, y) :
                nextVarX(x, y);
    }

//    private static void printComparison(int power, Compound cx, Compound cy) {
//        System.out.println(cx.structureString() + " " + cx.volume() + "\t" + cx);
//        System.out.println(cy.structureString() + " " + cy.volume() + "\t" + cy);
//        System.out.println(!cx.impossibleToMatch(cy) + "|" + !cy.impossibleToMatch(cx) + " ---> " + (power >= 0) + " " + power);
//        System.out.println();
//    }


    private boolean nextVarX(Variable xVar, Term y) {
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

                EllipsisTransform et = (EllipsisTransform) e;
                if (et.from.equals(Op.Imdex)) {

                    Term n = apply(et.to);
                    if (n == null)
                        return false;

                    //the indicated term should be inserted
                    //at the index location of the image
                    //being processed. (this is the opposite
                    //of the other condition of this if { })
                    if (matchEllipsedLinear(X, e, Y)) {
                        EllipsisMatch raw = (EllipsisMatch) getXY(e);
                        xy.put(e, null); //HACK clear it to replace with a new value
                        return putXY(e, ImageMatch.put(raw.term, n, Y));
                    }
                } else {
                    Term n = apply(et.from);
                    if (n == null)
                        return false;

                    //resolving may be possible to defer to substitution if
                    //Y and et.from are components of ImageShrinkEllipsisMatch

                    int imageIndex = Y.indexOf(n);
                    return ((imageIndex != -1) && matchEllipsedLinear(X, e, Y)) &&
                            putXY(e, ImageMatch.take((EllipsisMatch) getXY(e), imageIndex));

                }
                return false;
            }

            /** if they are images, they must have same relationIndex */
            if (X.op().isImage()) { //PRECOMPUTABLE

                //if the ellipsis is normal, then interpret the relationIndex as it is
                if (Ellipsis.countNumNonEllipsis(X) > 0) {

                    int xEllipseIndex = X.indexOf(e);
                    int xRelationIndex = X.relation();
                    int yRelationIndex = Y.relation();

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

            return matchEllipsedLinear(X, e, Y);
        }

    }

//    private boolean matchEllipsisImage(Compound x, Ellipsis e, Compound y) {
//        /*  ex:
//           (M --> (A..B=_..+))
//        */
//        putXY(e, new ShadowProduct(x.terms()));
//        return false;
//    }

    protected boolean addTermutator(Termutator t) {

        //resolve termutator interferences that the addition may cause
        Termlike a = t.resultKey;
        for (int i = 0; i < termutes.size(); i++) {
            Termutator s = termutes.get(i);
            Termlike b = s.resultKey;
            if (a.equals(b)) {
                //TODO maybe bifurcate a termutator tree with an OR branch?

                //if exact same conditions, dont add duplicate
                if (a.getClass() == b.getClass() &&
                        s.toString().equals(t.toString()))
                    return true;
                else
                    continue;
            }
            if (a.containsTerm((Term) b)) {
                //insert b before a since it is more specific
                termutes.add(i, t);
                return true;
            } /*else if (b.containsTerm((Term) a)) {
                //a contained by b; append to end (after a)
                continue;
            } */

        }

        //unique, add
        termutes.add(t);
        return true;
    }

    public final boolean matchPermute(TermContainer x, Compound y) {
        if
                (((type != Op.VAR_PATTERN && (0 == (x.structure() & type.bit()))) ||
                ((type == Op.VAR_PATTERN) && !Variable.hasPatternVariable(x))))

        {
            //SPECIAL CASE: no variables
            return matchLinear(x, y);
        }

        return addTermutator(new CommutivePermutations(this, x, y));
    }


    /**
     * commutive compound match: Y into X which contains one ellipsis
     * <p>
     * X pattern contains:
     * <p>
     * one unmatched ellipsis (identified)
     * <p>                    //HACK should not need new list
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
        Set<Term> xSpecific = Global.newHashSet(0); //Global.newHashSet(0);

        //constant terms which have been verified existing in Y and will not need matched
        Set<Term> ineligible = Global.newHashSet(0);

        boolean ellipsisMatched = false;
        for (Term x : X.terms()) {

            boolean xVar = x.op() == type;
            Term v = xVar ? getXY(x) : x;

            if (v == null) {

                //ellipsis to be matched in stage 2
                if (x == Xellipsis || x.equals(Ellipsis.Shim)) {
                    continue;
                }

            } else {
                if (v instanceof EllipsisMatch) {
                    //assume it's THE ellipsis here, ie. x == xEllipsis
                    ellipsisMatched = true;
                    Xellipsis = null;

                    //check that Y contains all of these
                    if (!((EllipsisMatch) v).addWhileMatching(Y, ineligible))
                        return false;
                } else if (!xVar) {
                    if (!Y.containsTerm(v))
                        return false;
                    ineligible.add(v);
                    continue;
                }

            }

            if (!xVar)
                throw new RuntimeException("fault");

            if (x != Xellipsis)
                xSpecific.add(x);


        }

        MutableSet<Term> yFree = Y.toSet();

        if (ellipsisMatched) {
            //Xellipsis = null;
            return ineligible.equals(yFree);
        }

        yFree.removeAll(ineligible);

        if (!Xellipsis.valid(yFree.size() - xSpecific.size())) {
            //wouldnt be enough remaining matches to satisfy ellipsis cardinality
            return false;
        }

        return matchCommutiveRemaining(Xellipsis, xSpecific, yFree);

    }


    /**
     * toMatch matched into some or all of Y's terms
     */
    private boolean matchCommutiveRemaining(Term xEllipsis, Set<Term> x, MutableSet<Term> yFree) {
        int xs = x.size();

        switch (xs) {
            case 0:
                return putXY(xEllipsis, new EllipsisMatch(yFree));
            case 1:
                return addTermutator(new Choose1(this,
                        xEllipsis, x.iterator().next(), yFree));
            case 2:
                return addTermutator(new Choose2(this,
                        xEllipsis, x.toArray(new Term[x.size()]), yFree));
            default:
                //3 or more combination
                throw new RuntimeException("unimpl: " + xs + " arity combination unimplemented");
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

        //TODO check for shim and subtract xsize?

        while (i < xsize) {
            Term x = X.term(i++);

            boolean expansionFollows = i < xsize && X.term(i) == Ellipsis.Shim;
            if (expansionFollows) i++; //skip over it

            if (x instanceof Ellipsis) {
                int available = ysize - j;

                Term eMatched = getXY(x); //EllipsisMatch, or null
                if (eMatched == null) {

                    //COLLECT
                    if (i == xsize) {
                        //SUFFIX
                        if (!Xellipsis.valid(available))
                            return false;

                        //TODO special handling to extract intermvals from Sequence terms here

                        if (!putXY(Xellipsis,
                                new EllipsisMatch(
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
//                        //SUFFIX - match the remaining terms against what the ellipsis previously collected
//                        //HACK this only works with EllipsisMatch type
//                        Term[] sp = ((EllipsisMatch) eMatched).term;
//                        if (sp.length!=available)
//                            return false; //incorrect size
//
//                        //match every item
//                        for (Term aSp : sp) {
//                            if (!match(aSp, Y.term(j++)))
//                                return false;
//                        }
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
    private boolean putVarX(Variable x, Term y) {
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
    public boolean matchLinear(TermContainer X, TermContainer Y) {
        int s = X.size();
        switch (s) {
            case 0: return true;
            case 1: return matchSub(X, Y, 0);
            case 2:
                if (X.term(1).op(type))
                    return matchLinearReverse(X, Y);
        }
        return matchLinearForward(X, Y);
    }


    public boolean matchLinearForward(TermContainer X, TermContainer Y) {
        final int s = X.size();
        for (int i = 0; i < s; i++) {
            if (!matchSub(X, Y, i)) return false;
        }
        return true;
    }

    public boolean matchSub(TermContainer X, TermContainer Y, int i) {
        return match(X.term(i), Y.term(i));
    }

    public boolean matchLinearReverse(TermContainer X, TermContainer Y) {
        for (int i = X.size() - 1; i >= 0; i--) {
            if (!matchSub(X, Y, i)) return false;
        }
        return true;
    }



    private void matchTermutations(int i, int max) {

        if (i == max) {
            if (this.termutes.size() == max)
                onMatch();
            else {
                //throw new RuntimeException("termutes modified");
                //have to restart
            }
            return;
        }

        Termutator t = termutes.get(i);
        t.reset();

        int revert = now();

        while (t.hasNext()) {


            //System.out.println("@" + now() + " " + i + ": " + t + " ");
            //System.out.println("->  " + xy);

            if (t.next()) {
                //System.out.println("@" + now() + " -->  " + xy);
                matchTermutations(i + 1, max);
            }

            revert(revert);
            //System.out.println("@" + revert + " <-  " + xy);

        }

    }


    /**
     * returns true if the assignment was allowed, false otherwise
     */
    public final boolean putXY(Term x /* usually a Variable */, Term y) {

//        if (x.equals(y))
//            throw new RuntimeException("x==y");

        VarCachedVersionMap xy = this.xy;

        Versioned<Term> v = xy.map.get(x);
        Term vv = (v != null) ? v.get() : null;
        if (vv != null) {
            return y.equals(vv);
        }
        if (!assignable(x, y))
            return false;

        if (v == null) {
            v = xy.getOrCreateIfAbsent(x);
        }

        v.set(y);
        return true;
    }

    /**
     * true if the match assignment is allowed by constraints
     */
    public boolean assignable(Term x, Term y) {
        if (constraints == null) return true;
        MatchConstraint c = constraints.get(x);
        if (c == null) return true;
        return !c.invalid(x, y, this);
    }

    public final void putYX(Term y /* usually a Variable */, Term x) {
        yx.put(y, x);
    }

    public Term apply(Term t) {
        throw new RuntimeException("unimpl");
    }


    /** default compound matching; op will already have been compared. no ellipsis will be involved */
    public final boolean matchCompound(Compound x, Compound y) {
        int xs = x.size();
        if ((xs==y.size()) && (x.relation()==y.relation())) {
            return ((xs > 1) && (x.isCommutative())) ?
                    matchPermute(x, y) :
                    matchLinear(x.subterms(), y.subterms());
        }
        return false;
    }
}


