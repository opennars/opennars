package nars;

import nars.nal.nal1.Inheritance;
import nars.nal.nal1.Negation;
import nars.nal.nal2.Instance;
import nars.nal.nal2.Similarity;
import nars.nal.nal3.SetExt;
import nars.nal.nal3.SetExt1;
import nars.nal.nal4.Product;
import nars.nal.nal5.Implication;
import nars.nal.nal8.Operation;
import nars.nal.nal8.Operator;
import nars.task.FluentTask;
import nars.term.Atom;
import nars.term.Compound;
import nars.term.Term;
import nars.term.Variable;
import nars.util.utf8.Utf8;

/**
 * core utility class
 */
public class $ {




    public static final Term _(final String term) {
        return Narsese.the().term(term);
        //        try { }
        //        catch (InvalidInputException e) { }
    }

    public static final <C extends Compound> FluentTask _(final String term, char punc) {
        C t = Narsese.the().term(term);
        if (t == null) return null;
        return (FluentTask) new FluentTask(t).punctuation(punc).eternal().normalized();
    }

    /**
     * Op.ATOM from String
     */
    public static Atom the(String id) { return Atom.the(id); }

    public static Atom[] the(String... id) {
        final int l = id.length;
        final Atom[] x = new Atom[l];
        for (int i = 0; i < l; i++)
            x[i] = Atom.the(id[i]);
        return x;
    }

    /**
     * Op.ATOM from int
     */
    public static Atom the(int i) { return Atom.the(i); }

    /**
     * Op.INHERITANCE from 2 Terms: subj --> pred
     */
    public static <A extends Term,B extends Term> Inheritance<A,B> inh(A subj, B pred) {
        return Inheritance.make(subj, pred);
    }

    public static <A extends Term,B extends Term> Inheritance<A,B> inh(String subj, String pred) {
        return Inheritance.make(_(subj), _(pred));
    }


    public static /* TODO <A extends T,B extends T>*/ Term sim(Term subj, Term pred) {
        return Similarity.make(subj, pred);
    }


    public static Operation op(String operator, String... args) {
        return op(Operator.the(operator), Product.make(args)
        );
    }

    public static Operation op(Operator opTerm, Term... arg) {
        return op(opTerm, Product.make(arg));
    }

    /**
     * OPERATION
     *
     *
     * @return A compound generated or null
     */
    public static <A extends Term> Operation<A> op(final Operator oper, Product<A> arg) {

//        if (Variables.containVar(arg)) {
//            throw new RuntimeException("Operator contains variable: " + oper + " with arguments " + Arrays.toString(arg) );
//        }

//        if (self == null) {
//            self = oper.getMemory().getSelf();
//        }

//        if((arg.length == 0) || ( !arg[arg.length-1].equals(null)) ) {
//            Term[] arg2=new Term[arg.length+1];
//            System.arraycopy(arg, 0, arg2, 0, arg.length);
//            arg2[arg.length] = null;
//            arg=arg2;
//        }

        /*if (invalidStatement(subject, oper)) {
            return null;
        }*/

        return new Operation(oper, new SetExt1(arg));
    }

    /** implies */
    public static Compound imp(Term a, Term b) {
        return Implication.make(a, b);
    }

    public static <X extends Term> X not(Term x) {
        return (X)Negation.make(x);
    }

    public static Product pro(Term[] t) {
        return Product.make(t);
    }
    public static Product pro(String[] t) {
        return Product.make( $.the(t) );
    }

    public static Variable var(Op type, int i) {
        return Variable.the(type, i);
    }

    public static Variable var(Op type, String s) {
        return Variable.the(type.ch, Utf8.toUtf8(s));
    }


    public static Variable varDep(int i) {
        return var(Op.VAR_DEPENDENT, i);
    }

    public static Variable varDep(String s) {
        return var(Op.VAR_DEPENDENT, s);
    }

    public static <P extends Term, S extends Term> Inheritance<SetExt<S>, P>
        inst(S subj, P pred) {
            return Instance.make(subj, pred);
    }
}
