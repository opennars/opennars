package nars;

import nars.java.AtomObject;
import nars.nal.nal1.Inheritance;
import nars.nal.nal1.Negation;
import nars.nal.nal2.Similarity;
import nars.nal.nal3.SetExt;
import nars.nal.nal3.SetInt;
import nars.nal.nal4.Product;
import nars.nal.nal5.Implication;
import nars.nal.nal7.CyclesInterval;
import nars.nal.nal7.Tense;
import nars.nal.nal8.Operator;
import nars.task.MutableTask;
import nars.term.Term;
import nars.term.atom.Atom;
import nars.term.compound.Compound;
import nars.term.compound.GenericCompound;
import nars.term.variable.Variable;
import nars.truth.Truth;
import nars.util.utf8.Utf8;

import java.util.Collection;

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
        return new MutableTask(t)
                .punctuation(punc)
                .eternal();
                //.normalized();
    }

    public static <O> AtomObject<O> ref(final String term, O instance) {
        return new AtomObject(term, instance);
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
     *  returns a Term if the two inputs are equal to each other
     */
    public static <T extends Term> T inh(Term subj, Term pred) {
        return (T) Inheritance.inheritance(subj, pred);
    }


    public static <T extends Term> T inh(String subj, String pred) {
        return inh((Term)$(subj), (Term)$(pred));
    }


    public static Term simi(Term subj, Term pred) {
        return Similarity.make(subj, pred);
    }

    public static Compound oper(String operator, String... args) {
        return oper(Operator.the(operator), $.p(args));
    }


    public static Compound oper(Operator opTerm, Term... arg) {
        return oper(opTerm, $.p(arg));
    }

    public static Compound oper(Operator opTerm, Product arg) {
        return new GenericCompound(Op.INHERITANCE,
                $.inh(opTerm, arg));
    }


    public static Compound impl(Term a, Term b) {
        return Implication.make(a, b);
    }

    public static <X extends Term> X not(Term x) {
        return (X) Negation.negation(x);
    }

    public static CyclesInterval cycles(int numCycles) {
        return CyclesInterval.make(numCycles);
    }


    public static Compound p(Term... t) {
        return Product.make(t);
    }

    public static Compound<Atom> p(String... t) {
        return Product.make($.the(t));
    }

    public static Variable v(Op type, int i) {
        return Variable.the(type, i);
    }

    public static Variable v(Op type, String s) {
        return Variable.the(type.ch, Utf8.toUtf8(s));
    }


    public static Variable varDep(int i) {
        return v(Op.VAR_DEPENDENT, i);
    }

    public static Variable varDep(String s) {
        return v(Op.VAR_DEPENDENT, s);
    }

    public static Variable varIndep(int i) {
        return v(Op.VAR_INDEPENDENT, i);
    }

    public static Variable varIndep(String s) {
        return v(Op.VAR_INDEPENDENT, s);
    }

    public static Variable varQuery(int i) {
        return v(Op.VAR_QUERY, i);
    }

    public static Variable varQuery(String s) {
        return v(Op.VAR_QUERY, s);
    }

    public static Variable varPattern(int i) {
        return v(Op.VAR_PATTERN, i);
    }

    public static Variable varPattern(String s) {
        return v(Op.VAR_PATTERN, s);
    }

    /**
     * Try to make a new compound from two components. Called by the logic rules.
     * <p>
     *  A {-- B becomes {A} --> B
     * @param subj The first component
     * @param pred The second component
     * @return A compound generated or null
     */
    public static Compound instance(Term subj, Term pred) {
        return (Compound) $.inh(SetExt.make(subj), pred);
    }


    /**
     * Try to make a new compound from two components. Called by the logic rules.
     * <p>
     *  A {-] B becomes {A} --> [B]
     * @param subject The first component
     * @param predicate The second component
     * @return A compound generated or null
     */
    final public static Compound instprop(final Term subject, final Term predicate) {
        return (Compound) $.inh(SetExt.make(subject), SetInt.make(predicate));
    }

//    public static Term term(final Op op, final Term... args) {
//        return Terms.term(op, args);
//    }

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

    public static <T extends Term> SetExt<T> extset(Collection<T> t) {
        return SetExt.make(t);
    }

    public static Compound intset(Term... t) {
        return SetInt.make(t);
    }

    /**
     * Try to make a new compound from two components. Called by the logic rules.
     * <p>
     *  A --] B becomes A --> [B]
     * @param subject The first component
     * @param predicate The second component
     * @return A compound generated or null
     */
    public static Term property(Term subject, Term predicate) {
        return inh(subject, $.intset(predicate));
    }
}
