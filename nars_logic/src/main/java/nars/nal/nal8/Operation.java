/*
 * Inheritance.java
 *
 * Copyright (C) 2008  Pei Wang
 *
 * This file is part of Open-NARS.
 *
 * Open-NARS is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * Open-NARS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Open-NARS.  If not, see <http://www.gnu.org/licenses/>.
 */
package nars.nal.nal8;

import nars.Memory;
import nars.Symbols;
import nars.budget.Budget;
import nars.nal.NALOperator;
import nars.nal.Task;
import nars.nal.Truth;
import nars.nal.concept.Concept;
import nars.nal.nal1.Inheritance;
import nars.nal.nal3.SetExt1;
import nars.nal.nal4.Product;
import nars.nal.nal8.operator.eval;
import nars.nal.term.Compound;
import nars.nal.term.Term;
import nars.nal.term.Variable;
import nars.util.utf8.ByteBuf;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;

/**
 * An operation is interpreted as an Inheritance relation.
 */
public class Operation<T extends Term> extends Inheritance<SetExt1<Product>, T> {

    private final Product arg;

    transient private Task<Operation<T>> task; //this is set automatically prior to executing


    //public final static Term[] SELF_TERM_ARRAY = new Term[] { SELF };

    /**
     * Constructor with partial values, called by make
     */
    protected Operation(Product argProduct, T operator) {

        super(new SetExt1(argProduct), operator);

        this.arg = argProduct;
    }


    /**
     * Clone an object
     *
     * @return A new object, to be casted into a SetExt
     */
    @Override
    public Operation<T> clone() {
        return clone(arg());
    }

    public Operation<T> clone(Product args) {
        Operation x = new Operation(args, getOperator());
        x.setTask(getTask());
        return x;
    }


    /**
     * Try to make a new compound from two components. Called by the logic
     * rules.
     *
     * @param self specify a SELF, or null to use memory's current self
     * @return A compound generated or null
     */
    public static <T extends Term> Operation<T> make(final T oper, Product arg) {

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

        return new Operation<T>(arg, oper);
    }

    public T getOperator() {
        return (T) getPredicate();
    }


    /**
     * stores the currently executed task, which can be accessed by Operator execution
     */
    public void setTask(final Task<Operation<T>> task) {
        this.task = task;
    }

    public Task<Operation<T>> getTask() {
        return task;
    }

    public Product arg() {
        return arg;//getSubject().the();
    }

    public Term arg(int i) {
        return arg().term(i);
    }


    public Task newSubTask(Memory m, Compound content, char punctuation, Truth truth, long occ, Budget budget) {
        return  m.newTask(content)
                .punctuation(punctuation)
                .truth(truth)
                .budget(budget)
                .parent(getTask())
                .occurr(occ)
                .get();
    }


    public Term[] arg(Memory memory) {
        return arg(memory, false);
    }

    public Term[] arg(Memory memory, boolean evaluate) {
        return arg(memory, evaluate, true);
    }

    public Term[] arg(Memory memory, boolean evaluate, boolean removeSelf) {
        final Term[] rawArgs = argArray();
        int numInputs = rawArgs.length;

        if (removeSelf) {
            if (numInputs > 0) {
                if (rawArgs[numInputs - 1].equals(memory.self()))
                    numInputs--;
            }
        }

        if (numInputs > 0) {
            if (rawArgs[numInputs - 1] instanceof Variable)
                numInputs--;
        }


        Term[] x;

        if (evaluate) {

            x = new Term[numInputs];
            for (int i = 0; i < numInputs; i++) {
                x[i] = eval.eval(rawArgs[i], memory);
            }
        } else {
            x = Arrays.copyOfRange(rawArgs, 0, numInputs);
        }

        return x;
    }


    /**
     * produces a cloned instance with the replaced args + additional terms in a new argument product
     */
    public Operation cloneWithArguments(Term[] args, Term... additional) {
        return (Operation) cloneReplacingSubterm(0, Product.make(args, additional));
    }

    /**
     * returns a reference to the raw arguments as contained by the Product subject of this operation
     * avoid using this because it may involve creation of unnecessary array
     * if Product1.terms() is called
     */
    @Deprecated
    public Term[] argArray() {
        return arg().terms();
    }

    public Concept getConcept(Memory memory) {
        return memory.concept(getTerm());
    }

    public Truth getConceptDesire(Memory m) {
        Concept c = getConcept(m);
        if (c == null) return null;
        return c.getDesire();
    }

    public float getConceptExpectation(Memory m) {
        Truth tv = getConceptDesire(m);
        if (tv == null) return 0;
        return tv.getExpectation();
    }


    public Truth getTaskDesire() {
        return getTask().getDesire();
    }

    public float getTaskExpectation() {
        Truth tv = getTaskDesire();
        if (tv == null) return 0;
        return tv.getExpectation();
    }

    /**
     * deletes the concept of this operation, preventing it from
     * being executed again (unless invoked again by input).
     */
    public void stop(Memory memory) {
        memory.delete(getTerm());
    }

    /**
     * if any of the arguments are 'eval' operations, replace its result
     * in that position in a cloned Operation instance
     *
     * @return
     */
    public Operation inline(Memory memory, boolean removeSelf) {
        //TODO avoid clone if it does not involve any eval()
        //if (!hasEval()) return this;
        return clone(Product.make(arg(memory, true, removeSelf /* keep SELF term at this point */)));
    }

//    protected boolean hasEval() {
//        for (Term x : arg().term) {
//            if (x instanceof Operation) {
//                Operation o = (Operation)x;
//                if (o.getOperator().equals(eval.term)) {
//                    return true;
//                }
//            }
//        }
//        return false;
//    }

    /**
     * use this to restrict potential operator (predicate terms)
     */
    public static boolean validOperatorTerm(Term t) {
        return t instanceof Term;
    }

    public int args() {
        return arg().length();
    }

    public static boolean isA(Term x, Term someOperatorTerm) {
        if (x instanceof Operation) {
            Operation o = (Operation) x;
            if (o.getOperator().equals(someOperatorTerm))
                return true;
        }
        return false;
    }


    @Override
    public byte[] init() {

        byte[] op = getPredicate().bytes();
        //Term[] arg = argArray();

        int len = op.length + 1 + 1;
        int n = 0;

        final Term[] xt = arg().terms();
        for (final Term t : xt) {
            len += t.bytes().length;
            n++;
        }
        if (n > 1) len += n - 1;


        final ByteBuf b = ByteBuf.create(len);
        b.append(op); //add the operator name without leading '^'
        b.append((byte) NALOperator.COMPOUND_TERM_OPENER.ch);


        n = 0;
        for (final Term t : xt) {
            /*if(n==arg.length-1) {
                break;
            }*/
            if (n != 0)
                b.add((byte) Symbols.ARGUMENT_SEPARATOR);

            b.add(t.bytes());

            n++;
        }

        b.append((byte) NALOperator.COMPOUND_TERM_CLOSER.ch);

        return b.toBytes();
    }

    @Override
    public void append(Writer p, boolean pretty) throws IOException {

        final Term[] xt = arg().terms();

        getPredicate().append(p, pretty); //add the operator name without leading '^'
        p.append(NALOperator.COMPOUND_TERM_OPENER.ch);


        int n = 0;
        for (final Term t : xt) {
            if (n != 0)
                p.append(Symbols.ARGUMENT_SEPARATOR);

            t.append(p, pretty);

            n++;
        }

        p.append(NALOperator.COMPOUND_TERM_CLOSER.ch);

    }


}
