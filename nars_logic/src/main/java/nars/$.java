package nars;

import nars.nal.nal1.Inheritance;
import nars.nal.nal1.Negation;
import nars.nal.nal2.Instance;
import nars.nal.nal2.Similarity;
import nars.nal.nal3.SetExt;
import nars.nal.nal4.Product;
import nars.nal.nal5.Implication;
import nars.nal.nal7.CyclesInterval;
import nars.nal.nal7.Tense;
import nars.nal.nal8.Operation;
import nars.nal.nal8.Operator;
import nars.task.MutableTask;
import nars.term.Term;
import nars.term.Terms;
import nars.term.atom.Atom;
import nars.term.compound.Compound;
import nars.term.variable.Variable;
import nars.truth.Truth;
import nars.util.utf8.Utf8;

/**
 * core utility class for:
    --building any type of value, either programmatically or parsed from string input
       (which can be constructed in a static context)
    --??
 */
abstract public class $  {


    public static final <T extends Term> T $(final String term) {
        return (T)Narsese.the().term(term);
        //        try { }
        //        catch (InvalidInputException e) { }
    }

    public static final <C extends Compound> MutableTask $(final String term, char punc) {
        C t = Narsese.the().term(term);
        if (t == null) return null;
        return (MutableTask) new MutableTask(t).punctuation(punc).eternal().normalized();
    }


    public static Atom the(String id) {
        return Atom.the(id);
    }

    public static Atom[] the(String... id) {
        final int l = id.length;
        final Atom[] x = new Atom[l];
        for (int i = 0; i < l; i++)
            x[i] = Atom.the(id[i]);
        return x;
    }


    public static Atom the(int i) {
        return Atom.the(i);
    }

    /**
     * Op.INHERITANCE from 2 Terms: subj --> pred
     */
    public static <A extends Term, B extends Term> Inheritance<A, B> inh(A subj, B pred) {
        return Inheritance.make(subj, pred);
    }

    public static <A extends Term, B extends Term> Inheritance<A, B> inh(String subj, String pred) {
        return Inheritance.make((A)$(subj), (B)$(pred));
    }


    public static Term simi(Term subj, Term pred) {
        return Similarity.make(subj, pred);
    }


    public static Operation<Atom> oper(String operator, String... args) {
        return oper(Operator.the(operator), Product.make(args));
    }

    public static <A extends Term> Operation<A> oper(Operator opTerm, A... arg) {
        return oper(opTerm, pro(arg));
    }

    public static <A extends Term> Operation<A> oper(final Operator oper, Product<A> arg) {
        return new Operation(oper, arg);
    }


    public static Compound impl(Term a, Term b) {
        return Implication.make(a, b);
    }

    public static <X extends Term> X not(Term x) {
        return (X) Negation.make(x);
    }

    public static CyclesInterval cycles(int numCycles) {
        return CyclesInterval.make(numCycles);
    }

    public static Product pro(Term... t) {
        return Product.make(t);
    }

    public static Product<Atom> pro(String... t) {
        return Product.make($.the(t));
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

    public static Variable varIndep(int i) {
        return var(Op.VAR_INDEPENDENT, i);
    }

    public static Variable varIndep(String s) {
        return var(Op.VAR_INDEPENDENT, s);
    }

    public static Variable varQuery(int i) {
        return var(Op.VAR_QUERY, i);
    }

    public static Variable varQuery(String s) {
        return var(Op.VAR_QUERY, s);
    }

    public static Variable varPattern(int i) {
        return var(Op.VAR_PATTERN, i);
    }

    public static Variable varPattern(String s) {
        return var(Op.VAR_PATTERN, s);
    }

    public static <P extends Term, S extends Term> Inheritance<SetExt<S>, P>
    inst(S subj, P pred) {
        return Instance.make(subj, pred);
    }

    public static Term term(final Op op, final Term... args) {
        return Terms.term(op, args);
    }

    public static MutableTask belief(Compound term, Truth copyFrom) {
        return belief(term, copyFrom.getFrequency(), copyFrom.getConfidence());
    }

    public static MutableTask belief(Compound term, float freq, float conf) {
        return new MutableTask(term).belief().truth(freq, conf);
    }

    public static MutableTask goal(Compound term, float freq, float conf) {
        return new MutableTask(term).goal().truth(freq, conf);
    }

    public static Implication implForward(Term condition, Term consequence) {
        return Implication.make(condition, consequence, Tense.ORDER_FORWARD);
    }
}
