package nars.nal.meta;

import com.gs.collections.api.block.predicate.primitive.IntObjectPredicate;
import com.gs.collections.api.set.primitive.ShortSet;
import nars.$;
import nars.nal.nal4.ShadowProduct;
import nars.nal.nal7.InvisibleAtom;
import nars.nal.nal7.Sequence;
import nars.term.*;
import nars.term.transform.Subst;
import nars.util.utf8.Utf8;

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
public class Ellipsis extends Variable.VarPattern { //TODO use Immutable


    /** a placeholder that indicates an expansion of one or more terms that will be provided by an Ellipsis match.
     *  necessary for terms which require > 1 argument but an expression that will expand one ellipsis variable will not construct a valid prototype of it
     *  ex:
     *    (|, %A, ..)
     *
     *  */
    public final static InvisibleAtom Expand = new InvisibleAtom("..") {

    };

    /** 1 or more */
    public static Term PLUS = Atom.the("+");

    /** 0 or more */
    public static Term ASTERISK = Atom.the("*");

    /** everything except, ex: not(%2) */
    public static final Atom NOT = $.the("not");



    public final Variable name;
    public final Term expression;

    public Ellipsis(Variable name, Term expression) {
        super(
            Utf8.toUtf8(name.toString().substring(1) /* exclude variable type char */
                    + ".." + expression.toString())
        );

        this.name= name;
        this.expression = expression;
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



    public ShadowProduct match(ShortSet ySubsExcluded, Compound y) {
        Term ex = this.expression;
        if ((ex == PLUS) || (ex == ASTERISK)) {
            return matchRemaining(y, ySubsExcluded);
        }

        throw new RuntimeException("unimplemented expression: " + ex);

//        else if (ex instanceof Operation) {
//
//            Operation o = (Operation) ex;
//
//            //only NOT implemented currently
//            if (!o.getOperatorTerm().equals(NOT)) {
//                throw new RuntimeException("ellipsis operation " + expression + " not implemented");
//            }
//
//            return matchNot(o.args(), mapped, y);
//        }
    }


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

    public boolean valid(int collectable) {
        Term exp = this.expression;

        if (exp == PLUS)
            return collectable > 0;
        else if (exp == ASTERISK)
            return collectable >= 0;

        return false;
    }

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
    public static Ellipsis getFirstUnmatchedEllipsis(Compound X, Subst ff) {
        final int xsize = X.size();
        for (int i = 0; i < xsize; i++) {
            Term xi = X.term(i);
            if (xi instanceof Ellipsis) {
                if (ff.getXY(X)==null)
                    return (Ellipsis) xi;
//                else {
//                    System.err.println("already matched");
//                }
            }
        }
        return null;
    }
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


    private static ShadowProduct matchedSubterms(Term[] subterms) {
        //return Product.make(subterms);
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
