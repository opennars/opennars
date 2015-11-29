package nars.nal.meta;

import nars.$;
import nars.nal.nal4.Product;
import nars.nal.nal8.Operation;
import nars.term.Atom;
import nars.term.Compound;
import nars.term.Term;
import nars.term.Variable;
import nars.util.utf8.Utf8;

import java.util.Map;

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
    public final static Atom Expand = Atom.the("..");


    public final Variable name;
    public final Term expression;

    public Ellipsis(Variable name, Term expression) {
        super(
            Utf8.toUtf8(name.toString().substring(1) /* exclude variable type char */
                    + ".." + expression.toString())
        );

        this.name = name;
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

    public static int countEllipsisSubterms(Compound x) {
        final int xs = x.size();
        int n = 0;
        for (int i = 0; i < xs; i++)
            if (x.term(i) instanceof Ellipsis) n++;
        return n;
    }

    public static int countNonEllipsisSubterms(Compound x) {
        final int xs = x.size();
        int n = xs;
        for (int i = 0; i < xs; i++)
            if (x.term(i) instanceof Ellipsis) n--;
        return n;
    }

    public static final Atom NOT = $.the("not");

    public Term match(Map<Term, Term> mapped, Compound y) {
        Operation o = (Operation)expression;

        //only NOT implemented currently
        if (!o.getOperatorTerm().equals(NOT)) {
            throw new RuntimeException("ellipsis operation " + expression + " not implemented" );
        }

        return matchNot(o.args(), mapped, y);
    }

    private static Term matchNot(Term[] oa, Map<Term, Term> mapped, Compound Y) {

        if (oa.length!=1) {
            throw new RuntimeException("only 1-arg not() implemented");
        }

        Term exclude = oa[0];

        final int ysize = Y.size();
        Term[] others = new Term[ysize-1];
        int k = 0;
        for (int j = 0; j < ysize; j++) {
            Term yt = Y.term(j);
            if (!mapped.get(exclude).equals(yt))
                others[k++] = yt;
        }
        return Product.make(others);
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
