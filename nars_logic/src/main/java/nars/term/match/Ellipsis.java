package nars.term.match;

import nars.Op;
import nars.nal.nal7.ShadowAtom;
import nars.term.Term;
import nars.term.TermContainer;
import nars.term.compound.Compound;
import nars.term.transform.VariableNormalization;
import nars.term.variable.Variable;

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
public abstract class Ellipsis extends VarPattern { //TODO use Immutable


    /** a placeholder that indicates an expansion of one or more terms that will be provided by an Ellipsis match.
     *  necessary for terms which require > 1 argument but an expression that will expand one ellipsis variable will not construct a valid prototype of it
     *  ex:
     *    (|, %A, ..)
     *
     *
     * IMPORTANT: InvisibleAtom's default compareTo of -1
     * ensures this will appear always at the end of any ordering */
    public static final ShadowAtom Shim = new ShadowAtom("..") {
        @Override public Op op() {
            return Op.INTERVAL;
        }
    };

    public abstract Variable clone(Variable newVar, VariableNormalization normalizer);



    //public final Variable target;


    public Ellipsis(Variable target, String suffix) {
        super(
            target.toString().substring(1) /* exclude variable type char */
                    + suffix
        );

        //this.target = target;
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
    public static boolean hasEllipsisTransform(Compound x) {
        int xs = x.size();
        for (int i = 0; i < xs; i++)
            if (x.term(i) instanceof EllipsisTransform) return true;
        return false;
    }

    /** recursively */
    public static boolean containsEllipsis(Compound x) {
        int xs = x.size();

        for (int i = 0; i < xs; i++) {
            Term y = x.term(i);
            if (y instanceof Ellipsis) return true;
            if (y instanceof Compound) {
                if (containsEllipsis((Compound)y))
                    return true;
            }
        }
        return false;
    }

//    public static int numUnmatchedEllipsis(Compound x, FindSubst ff) {
//
//        int xs = x.size();
//
//        Map<Term, Term> xy = ff.xy;
//        if (xy.isEmpty()) {
//            //map is empty so return total # ellipsis
//            return numEllipsis(x);
//        }
//
//        int n = 0;
//        for (int i = 0; i < xs; i++) {
//            Term xt = x.term(i);
//            if (xt instanceof Ellipsis) {
//                if (!xy.containsKey(xt))
//                    n++;
//            }
//        }
//        return n;
//    }

    public static int numEllipsis(TermContainer x) {
        int xs = x.size();
        int n = 0;
        for (int i = 0; i < xs; i++) {
            if (x.term(i) instanceof Ellipsis)
                n++;
        }
        return n;
    }

    public static int numNonEllipsisSubterms(Compound x) {
        int xs = x.size();
        int n = xs;
        for (int i = 0; i < xs; i++) {
            Term xt = x.term(i);

            if ((xt instanceof Ellipsis)
             || (xt==Ellipsis.Shim)) //ignore expansion placeholders
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
        return Ellipsis.numNonEllipsisSubterms(x);
    }
//
//    @Deprecated public boolean valid(int numNonVarArgs, int ysize) {
//        int collectable = ysize - numNonVarArgs;
//        return valid(collectable);
//    }


    public abstract boolean valid(int collectable);

    public static Ellipsis getFirstEllipsis(Compound X) {
        int xsize = X.size();
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
//    public static Term getFirstNonEllipsis(Compound X) {
//        int xsize = X.size();
//        for (int i = 0; i < xsize; i++) {
//            Term xi = X.term(i);
//            if (!(xi instanceof Ellipsis)) {
//                return xi;
//            }
//        }
//        return null;
//    }

//    public static ArrayEllipsisMatch matchRemaining(Compound Y, ShortSet ySubsExcluded) {
//        return EllipsisMatch.matchedSubterms(Y, (index, term) ->
//                !ySubsExcluded.contains((short)index) );
//    }
//
//    public static ArrayEllipsisMatch matchedSubterms(Compound Y) {
//        Term[] arrayGen =
//                !(Y instanceof Sequence) ?
//                        Y.terms() :
//                        ((Sequence)Y).toArrayWithIntervals();
//
//        return new ArrayEllipsisMatch(arrayGen);
//    }


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
