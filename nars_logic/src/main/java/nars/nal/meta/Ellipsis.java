package nars.nal.meta;

import com.gs.collections.api.block.predicate.primitive.IntObjectPredicate;
import com.gs.collections.api.set.primitive.ShortSet;
import nars.Op;
import nars.nal.nal4.ShadowProduct;
import nars.nal.nal7.InvisibleAtom;
import nars.nal.nal7.Sequence;
import nars.term.Compound;
import nars.term.Term;
import nars.term.TermContainer;
import nars.term.Variable;
import nars.term.transform.Subst;
import nars.term.transform.VariableNormalization;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

/**
 * Meta-term of the form:
 *      variable..exppression
 * for selecting a range of subterms
 *
 * ex:
 *   A..not(X)
 *   B..not(X,Y)
 *   B..not(first)
 *   B..not(first,last)
 */
abstract public class Ellipsis extends Variable.VarPattern { //TODO use Immutable


    /** a placeholder that indicates an expansion of one or more terms that will be provided by an Ellipsis match.
     *  necessary for terms which require > 1 argument but an expression that will expand one ellipsis variable will not construct a valid prototype of it
     *  ex:
     *    (|, %A, ..)
     *
     *
     * IMPORTANT: InvisibleAtom's default compareTo of -1
     * ensures this will appear always at the end of any ordering */
    public final static InvisibleAtom Expand = new InvisibleAtom("..") {
        @Override public Op op() {
            return Op.INTERVAL;
        }
    };

    public abstract Variable clone(Variable newVar, VariableNormalization normalizer);


    public static class EllipsisOneOrMore extends Ellipsis {

        public EllipsisOneOrMore(Variable name) {
            this(name, "..+");
        }

        @Override
        public Variable clone(Variable newVar, VariableNormalization normalizer) {
            return new EllipsisOneOrMore(newVar);
        }

        public EllipsisOneOrMore(Variable name, String s) {
            super(name, s);
        }

        @Override
        public boolean valid(int collectable) {
            return collectable > 0;
        }
    }

    public static class EllipsisZeroOrMore extends Ellipsis {
        public EllipsisZeroOrMore(Variable name) {
            super(name, "..*");
        }

        @Override
        public boolean valid(int collectable) {
            return collectable >= 0;
        }
        @Override
        public Variable clone(Variable newVar, VariableNormalization normalizer) {
            return new EllipsisZeroOrMore(newVar);
        }
    }

    /** ellipsis that transforms one of its elements, which it is required to match within */
    public static class EllipsisTransform extends EllipsisOneOrMore {

        public final Term from;
        public final Term to;

        public EllipsisTransform(Variable name, Term from, Term to) {
            super(name, ".." + from + "=" + to + "..+");
            this.from = from;
            this.to = to;
        }

        @Override
        public Variable clone(Variable newVar, VariableNormalization normalizer) {
            throw new RuntimeException("HACK - this is handled by TaskRule.TaskRuleVariableNormalization");
        }

    }

    public final Variable target;


    public Ellipsis(Variable target, String suffix) {
        super(
            target.toString().substring(1) /* exclude variable type char */
                    + suffix
        );

        this.target = target;
    }



    @Override
    public int volume() {
        return 0;
    }

    public static boolean hasEllipsis(Compound x) {
        int xs = x.size();
        for (int i = 0; i < xs; i++)
            if (x.term(i) instanceof Ellipsis) return true;
        return false;
    }

    public static int numUnmatchedEllipsis(Compound x, Subst ff) {

        int xs = x.size();

        Map<Term, Term> xy = ff.xy;
        if (xy.isEmpty()) {
            //map is empty so return total # ellipsis
            return numEllipsis(x);
        }

        int n = 0;
        for (int i = 0; i < xs; i++) {
            Term xt = x.term(i);
            if (xt instanceof Ellipsis) {
                if (!xy.containsKey(xt))
                    n++;
            }
        }
        return n;
    }

    public static int numEllipsis(TermContainer x) {
        final int xs = x.size();
        int n = 0;
        for (int i = 0; i < xs; i++) {
            if (x.term(i) instanceof Ellipsis)
                n++;
        }
        return n;
    }

    public static int numNonEllipsisSubterms(Compound x) {
        final int xs = x.size();
        int n = xs;
        for (int i = 0; i < xs; i++) {
            Term xt = x.term(i);

            if ((xt instanceof Ellipsis)
             || (xt==Ellipsis.Expand)) //ignore expansion placeholders
                n--;
        }
        return n;
    }



//    public ShadowProduct match(ShortSet ySubsExcluded, Compound y) {
//        Term ex = this.expression;
//        if ((ex == PLUS) || (ex == ASTERISK)) {
//            return matchRemaining(y, ySubsExcluded);
//        }
//
//        throw new RuntimeException("unimplemented expression: " + ex);
//
////        else if (ex instanceof Operation) {
////
////            Operation o = (Operation) ex;
////
////            //only NOT implemented currently
////            if (!o.getOperatorTerm().equals(NOT)) {
////                throw new RuntimeException("ellipsis operation " + expression + " not implemented");
////            }
////
////            return matchNot(o.args(), mapped, y);
////        }
//    }


//    private static Term matchNot(Term[] oa, Map<Term, Term> mapped, Compound Y) {
//
//        if (oa.length!=1) {
//            throw new RuntimeException("only 1-arg not() implemented");
//        }
//
//        Term exclude = oa[0];
//
//        final int ysize = Y.size();
//        Term[] others = new Term[ysize-1];
//        int k = 0;
//        for (int j = 0; j < ysize; j++) {
//            Term yt = Y.term(j);
//            if (!mapped.get(exclude).equals(yt))
//                others[k++] = yt;
//        }
//        return Product.make(others);
//    }

    /**
     * @param x a compound which contains one or more ellipsis terms */
    public static int countNumNonEllipsis(Compound x) {
        //TODO depending on the expression, determine the sufficient # of terms Y must contain
        int numNonVarArgs = Ellipsis.numNonEllipsisSubterms(x);
        return numNonVarArgs;
    }

    @Deprecated public boolean valid(int numNonVarArgs, int ysize) {
        int collectable = ysize - numNonVarArgs;
        return valid(collectable);
    }


    abstract public boolean valid(int collectable);

    public static Ellipsis getFirstEllipsis(Compound X) {
        final int xsize = X.size();
        for (int i = 0; i < xsize; i++) {
            Term xi = X.term(i);
            if (xi instanceof Ellipsis) {
                return (Ellipsis) xi;
            }
        }
        return null;
    }
//    public static Ellipsis getFirstUnmatchedEllipsis(Compound X, Subst ff) {
//        final int xsize = X.size();
//        for (int i = 0; i < xsize; i++) {
//            Term xi = X.term(i);
//            if (xi instanceof Ellipsis) {
//                if (ff.getXY(X)==null)
//                    return (Ellipsis) xi;
////                else {
////                    System.err.println("already matched");
////                }
//            }
//        }
//        return null;
//    }
    public static Term getFirstNonEllipsis(Compound X) {
        final int xsize = X.size();
        for (int i = 0; i < xsize; i++) {
            Term xi = X.term(i);
            if (!(xi instanceof Ellipsis)) {
                return xi;
            }
        }
        return null;
    }

    public static ShadowProduct matchRemaining(Compound Y, ShortSet ySubsExcluded) {
        return matchedSubterms(Y, (index, term) ->
                !ySubsExcluded.contains((short)index) );
    }

    public static ShadowProduct matchedSubterms(Compound Y) {
        Term[] arrayGen =
                !(Y instanceof Sequence) ?
                        Y.terms() :
                        ((Sequence)Y).toArrayWithIntervals();

        return matchedSubterms(arrayGen);
    }

    public static ShadowProduct matchedSubterms(Compound Y, IntObjectPredicate<Term> filter) {
        Function<IntObjectPredicate,Term[]> arrayGen =
                !(Y instanceof Sequence) ?
                        Y::terms :
                        ((Sequence)Y)::toArrayWithIntervals;

        return matchedSubterms(arrayGen.apply( filter ) );
    }

    public static ShadowProduct matchedSubterms(Collection<Term> subterms) {
        return new ShadowProduct(subterms);
    }


    private static ShadowProduct matchedSubterms(Term[] subterms) {
        return new ShadowProduct(subterms);
    }


    //    public static RangeTerm rangeTerm(String s) {
//        int uscore = s.indexOf("_");
//        if (uscore == -1) return null;
//        int periods = s.indexOf("..");
//        if (periods == -1) return null;
//        if (periods < uscore) return null;
//
//        String prefix = s.substring(0, uscore);
//        int from = Integer.parseInt( s.substring(uscore, periods) );
//        String to = s.substring(periods+2);
//        if (to.length() > 1) return null;
//
//        return new RangeTerm(prefix, from, to.charAt(0));
//    }
}
