package nars.term.compile;

import com.gs.collections.api.set.MutableSet;
import nars.Op;
import nars.nal.nal7.Parallel;
import nars.nal.nal7.Sequence;
import nars.term.*;
import nars.term.compound.Compound;
import nars.term.compound.GenericCompound;
import nars.term.transform.CompoundTransform;
import nars.term.transform.VariableNormalization;

import java.util.Arrays;
import java.util.Collection;
import java.util.TreeSet;

import static java.util.Arrays.copyOfRange;
import static nars.Op.*;
import static nars.term.Statement.pred;
import static nars.term.Statement.subj;

/**
 * Created by me on 1/2/16.
 */
public interface TermBuilder {

    /** unifies a term with this; by default it passes through unchanged */
    default Termed the(Term t) {
        return t;
    }

    default Termed the(Termed t) {
        return the(t.term());
    }
    default Term theTerm(Termed t) {
        return the(t).term();
    }

    default Termed normalized(Term t) {
        if (t.isNormalized()) {
            return t;
        }
        Compound x = transform((Compound) t, VariableNormalization.normalizeFast((Compound) t));
        if (x != t) {
            //if modified, set normalization flag HACK
            ((GenericCompound)x).setNormalized();
        }
        return the(x);
    }

//    default Term newTerm(Op op, Term... t) {
//        return newTerm(op, -1, t);
//    }


    default Term newTerm(Op op, Collection<Term> t) {
        return newTerm(op, -1, t);
    }
    default Term newTerm(Op op, int relation, Collection<Term> t) {
        return newTerm(op, relation, new TermVector(t));
    }
    default Term newTerm(Op op, Term singleton) {
        return newTerm(op, -1, new TermVector(singleton));
    }
    default Term newTerm(Op op, Term a, Term b) {
        return newTerm(op, -1, new TermVector(a, b));
    }


    default <X extends Compound> X transform(Compound src, CompoundTransform t) {
        return transform(src, t, true);
    }

    default <X extends Compound> X transform(Compound src, CompoundTransform t, boolean requireEqualityForNewInstance) {
        if (t.testSuperTerm(src)) {

            Term[] cls = new Term[src.size()];

            int mods = transform(src, t, cls, 0);

            if (mods == -1) {
                return null;
            }
            else if (!requireEqualityForNewInstance || (mods > 0)) {
                return (X) newTerm(src, new TermVector(cls));
            }
            //else if mods==0, fall through:
        }
        return (X) src; //nothing changed
    }


    /** returns how many subterms were modified, or -1 if failure (ex: results in invalid term) */
    default <T extends Term> int transform(Compound src, CompoundTransform<Compound<T>, T> trans, Term[] target, int level) {
        int n = src.size();

        int modifications = 0;

        for (int i = 0; i < n; i++) {
            Term x = src.term(i);
            if (x == null)
                throw new RuntimeException("null subterm");

            if (trans.test(x)) {

                Term y = trans.apply( (Compound<T>)src, (T) x, level);
                if (y == null)
                    return -1;

                if (x!=y) {
                    modifications++;
                    x = y;
                }

            } else if (x instanceof Compound) {
                //recurse
                Compound cx = (Compound) x;
                if (trans.testSuperTerm(cx)) {

                    Term[] yy = new Term[cx.size()];
                    int submods = transform(cx, trans, yy, level + 1);

                    if (submods == -1) return -1;
                    if (submods > 0) {
                        x = newTerm(cx, new TermVector(yy));
                        if (x == null)
                            return -1;
                        modifications+= (cx!=x) ? 1 : 0;
                    }
                }
            }
            target[i] = x;
        }

        return modifications;
    }

    default Term newTerm(Compound csrc, TermContainer<?> subs) {
        if (csrc.subterms().equals(subs))
            return csrc;
        return newTerm(csrc.op(), csrc.relation(), subs);
    }
    default Term newTerm(Op op, TermContainer<?> subs) {
        return newTerm(op, -1, subs);
    }
    default Term newTerm(Op op, int relation, TermContainer<?> subs) {
        return newTerm(op, relation,
            subs.terms() //TODO use subs directly not Term[] in callee
        );
    }


//    /** "clone"  */
//    @Deprecated default Term newTerm(Compound csrc, Term... subs) {
//        if (csrc.subterms().equals(subs))
//            return csrc;
//        return newTerm(csrc.op(), csrc.relation(), subs);
//    }

    default Term newTerm(Op op, int relation, Term... t) {

        if (t == null)
            return null;

        /* special handling */
        switch (op) {
            case NEGATE:
                if (t.length!=1)
                    throw new RuntimeException("invalid negation subterms: " + Arrays.toString(t));
                return negation(t[0]);
            case SEQUENCE:
                return Sequence.makeSequence(t);
            case PARALLEL:
                return Parallel.makeParallel(t);
            case INSTANCE:
                return inst(t[0], t[1]);
            case PROPERTY:
                return prop(t[0], t[1]);
            case INSTANCE_PROPERTY:
                return instprop(t[0], t[1]);
            case CONJUNCTION:
                return junction(CONJUNCTION, t);
            case DISJUNCTION:
                return junction(DISJUNCTION, t);
            case IMAGE_INT:
            case IMAGE_EXT:
                //if no relation was specified and it's an Image,
                //it must contain a _ placeholder
                if (TermIndex.hasImdex(t)) {
                    //TODO use result of hasImdex in image construction to avoid repeat iteration to find it
                    return image(op, t);
                }
                if ((relation == -1) || (relation > t.length))
                    return null;
                //throw new RuntimeException("invalid index relation: " + relation + " for args " + Arrays.toString(t));

                break;
            case DIFF_EXT:
                Term et0 = t[0], et1 = t[1];
                if ((et0.op(SET_EXT) && et1.op(SET_EXT) )) {
                    return subtractSet(Op.SET_EXT, (Compound)et0, (Compound)et1);
                }
                if (et0.equals(et1))
                    return null;
                break;
            case DIFF_INT:
                Term it0 = t[0], it1 = t[1];
                if ((it0.op(SET_INT) && it1.op(SET_INT) )) {
                    return subtractSet(Op.SET_INT, (Compound)it0, (Compound)it1);
                }
                if (it0.equals(it1))
                    return null;
                break;
            case INTERSECT_EXT: return newIntersectEXT(t);
            case INTERSECT_INT: return newIntersectINT(t);
        }

        if (op.isStatement()) {

            return statement(op, t);

        } else {
            int arity = t.length;
            if (op.minSize > 1 && arity == 1) {
                return t[0]; //reduction
            }

            if (!op.validSize(arity)) {
                //throw new RuntimeException(Arrays.toString(t) + " invalid size for " + op);
                return null;
            }

            return internCompound(op, relation, op.isCommutative() ?
                    TermSet.the(t) : new TermVector(t)).term();
        }

    }

    Termed internCompound(Op op, int relation, TermContainer subterms);

    default Term inst(Term subj, Term pred) {
        return newTerm(Op.INHERIT, newTerm(Op.SET_EXT, subj), pred);
    }
    default Term prop(Term subj, Term pred) {
        return newTerm(Op.INHERIT, subj, newTerm(Op.SET_INT, pred));
    }
    default Term instprop(Term subj, Term pred) {
        return newTerm(Op.INHERIT, newTerm(Op.SET_EXT, subj), newTerm(Op.SET_INT, pred));
    }

    default Term negation(Term t) {
        if (t.op() == Op.NEGATE) {
            // (--,(--,P)) = P
            return ((TermContainer) t).term(0);
        }
        return internCompound(Op.NEGATE, -1, new TermVector(t)).term();
    }

    default Term image(Op o, Term[] res) {

        int index = 0, j = 0;
        for (Term x : res) {
            if (x.equals(Op.Imdex)) {
                index = j;
            }
            j++;
        }

        if (index == -1) {
            throw new RuntimeException("invalid image subterms: " + Arrays.toString(res));
        } else {
            int serN = res.length - 1;
            Term[] ser = new Term[serN];
            System.arraycopy(res, 0, ser, 0, index);
            System.arraycopy(res, index + 1, ser, index, (serN - index));
            res = ser;
        }

        return newTerm(
                o,
                index, res);
    }

    default Term junction(Op op, Term[] t) {
        if (t.length == 0) return null;

        boolean done = true;

        //TODO use a more efficient flattening that doesnt involve recursion and multiple array creations
        TreeSet<Term> s = new TreeSet();
        for (Term x : t) {
            if (x.op(op)) {
                for (Term y : ((TermContainer) x).terms()) {
                    s.add(y);
                    if (y.op(op))
                        done = false;
                }
            } else {
                s.add(x);
            }
        }

        Term[] sa = s.toArray(new Term[s.size()]);

        if (!done) {
            return junction(op, sa);
        }

        if (sa.length == 1) return sa[0];

        return internCompound(op, -1, TermSet.the(sa)).term();

    }

    default Term statement(Op op, Term[] t) {

        switch (t.length) {
            case 1:
                return t[0];
            case 2:

                Term subject = t[0];
                Term predicate = t[1];

                if (subject == null || predicate == null)
                    return null;

                //special statement filters
                switch (op) {
                    case EQUIV:
                    case EQUIV_AFTER:
                    case EQUIV_WHEN:
                        if (!TermIndex.validEquivalenceTerm(subject)) return null;
                        if (!TermIndex.validEquivalenceTerm(predicate)) return null;
                        break;

                    case IMPLICATION:
                    case IMPLICATION_AFTER:
                    case IMPLICATION_BEFORE:
                    case IMPLICATION_WHEN:
                        if (subject.isAny(TermIndex.InvalidEquivalenceTerm)) return null;
                        if (predicate.isAny(TermIndex.InvalidImplicationPredicate)) return null;

                        if (predicate.isAny(Op.ImplicationsBits)) {
                            Term oldCondition = subj(predicate);
                            if ((oldCondition.isAny(Op.ConjunctivesBits)) && oldCondition.containsTerm(subject))
                                return null;

                            return impl2Conj(op, subject, predicate, oldCondition);
                        }
                        break;
                    default:
                        break;
                }

                if (t.length == 1)
                    return theTerm(t[0]); //reduced to one

                if (!Statement.invalidStatement(t[0], t[1])) {
                    return internCompound(op, -1,
                        op.isCommutative() ? TermSet.the(t) : new TermVector(t)
                    ).term();
                }

                return null;
            default:
                throw new RuntimeException("invalid statement arguments");
        }
    }

    default Term subtractSet(Op setType, Compound A, Compound B) {
        if (A.equals(B))
            return null; //empty set
        return newTerm(setType, new TermVector(TermContainer.difference(A, B)));
    }

    default Term impl2Conj(Op op, Term subject, Term predicate, Term oldCondition) {
        Op conjOp;
        switch (op) {
            case IMPLICATION:
                conjOp = CONJUNCTION;
                break;
            case IMPLICATION_AFTER:
                conjOp = SEQUENCE;
                break;
            case IMPLICATION_WHEN:
                conjOp = PARALLEL;
                break;
            case IMPLICATION_BEFORE:
                conjOp = SEQUENCE;
                //swap order
                Term x = oldCondition;
                oldCondition = subject;
                subject = x;
                break;
            default:
                throw new RuntimeException("invalid case");
        }

        return newTerm(op,
                newTerm(conjOp, subject, oldCondition),
                    pred(predicate));
    }

    default Term newIntersectINT(Term[] t) {
        return newIntersection(t,
                Op.INTERSECT_INT,
                Op.SET_INT,
                Op.SET_EXT);
    }

    default Term newIntersectEXT(Term[] t) {
        return newIntersection(t,
                Op.INTERSECT_EXT,
                Op.SET_EXT,
                Op.SET_INT);
    }

    default Term newIntersection(Term[] t, Op intersection, Op setUnion, Op setIntersection) {
        switch (t.length) {
            case 1:
                return t[0];
            case 2:
                return newIntersection2(t[0], t[1], intersection, setUnion, setIntersection);
            default:
                //HACK use more efficient way
                return newIntersection2(
                    newIntersection2(t[0], t[1], intersection, setUnion, setIntersection),
                    newIntersection(copyOfRange(t, 2, t.length), intersection, setUnion, setIntersection),
                    intersection, setUnion, setIntersection
                );
        }

        //return newCompound(intersection, t, -1, true);
    }

    @Deprecated default Term newIntersection2(Term term1, Term term2, Op intersection, Op setUnion, Op setIntersection) {


        Op o1 = term1.op();
        Op o2 = term2.op();

        if ((o1 == setUnion) && (o2 == setUnion)) {
            //the set type that is united
            return newTerm(setUnion, TermContainer.union((Compound) term1, (Compound) term2));
        }


        if ((o1 == setIntersection) && (o2 == setIntersection)) {
            //the set type which is intersected
            return intersect(setIntersection, (Compound) term1, (Compound) term2);
        }

        if (o2 == intersection && o1!=intersection) {
            //put them in the right order so everything fits in the switch:
            Term x = term1;
            term1 = term2;
            term2 = x;
            o2 = o1;
            o1 = intersection;
        }

        //reduction between one or both of the intersection type

        if (o1 == intersection) {
            Term[] suffix;

            suffix = o2 == intersection ? ((TermContainer) term2).terms() : new Term[]{term2};

            return internCompound(intersection, -1,
                    TermSet.the(Terms.concat(
                        ((TermContainer) term1).terms(), suffix
                    ))
            ).term();
        }

        if (term1.equals(term2))
            return term1;

        return internCompound(intersection, -1, TermSet.the(term1, term2)).term();


    }

    default Term intersect(Op resultOp, Compound a, Compound b) {
        MutableSet<Term> i = TermContainer.intersect(a, b);
        if (i.isEmpty()) return null;
        return newTerm(resultOp, i);
    }

    default Term difference(Compound a, Compound b) {

        if (a.size() == 1) {

            return b.containsTerm(a.term(0)) ? null : a;

        } else {
            //MutableSet dd = Sets.difference(a.toSet(), b.toSet());
            MutableSet dd = a.toSet();
            boolean changed = false;
            for (Term bb : b.terms()) {
                changed |= dd.remove(bb);
            }

            Term r;
            if (!changed) {
                r = a;
            }
            else if (dd.isEmpty()) {
                r = null;
            }
            else {
                r = newTerm(a.op(),  new TermVector(dd));
            }
            return r;
        }

    }
}
