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
import nars.Op;
import nars.Symbols;
import nars.budget.Budget;
import nars.concept.Concept;
import nars.nal.nal1.Inheritance;
import nars.nal.nal3.SetExt;
import nars.nal.nal3.SetExt1;
import nars.nal.nal4.ImageExt;
import nars.nal.nal4.Product;
import nars.nal.nal8.operator.eval;
import nars.task.Task;
import nars.term.Compound;
import nars.term.Term;
import nars.term.Variable;
import nars.truth.Truth;
import nars.util.utf8.ByteBuf;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;

/**
 * An operation is interpreted as an Inheritance relation with an operator.
 */
public class Operation extends Inheritance<SetExt1<Product>, Operator> {

    /**
     * the invoking task.
     * this is set automatically prior to execution
     */
    transient private Task<Operation> task;
    transient private Memory memory = null;


    //public final static Term[] SELF_TERM_ARRAY = new Term[] { SELF };

    protected Operation(Operator operator, SetExt1<Product> args) {
        super(args, operator);
    }

    /**
     *
     * Constructor with partial values, called by make
     */
    protected Operation(Operator operator, Product argProduct) {
        this(operator, new SetExt1(argProduct));
    }


    /**
     * Clone an object
     *
     * @return A new object, to be casted into a SetExt
     */
    @Override
    public Operation clone() {
        return clone(arg());
    }

    public Operation clone(Product args) {
        Operation x = new Operation(getPredicate(), args);
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
    public static Operation make(Product arg, final Operator oper) {

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

        SetExt1<Product> subject = new SetExt1(arg);

        return make(subject, oper);
    }

    public static Operation make(SetExt1<Product> arg, final Operator oper) {
        return new Operation( oper, arg );
    }


    /** gets the term wrapped by the Operator predicate */
    public Term getOperator() {
        return getPredicate().the();
    }


    /**
     * stores the currently executed task, which can be accessed by Operator execution
     */
    public void setTask(final Task<Operation> task) {
        this.task = task;
    }

    public Task<Operation> getTask() {
        return task;
    }

    public Product arg() {
        return getSubject().the();
    }

    public Term arg(int i) {
        return arg().term(i);
    }


    public Task newSubTask(Memory m, Compound content, char punctuation, Truth truth, long occ, Budget budget) {
        return newSubTask(m, content, punctuation, truth, occ, budget.getPriority(), budget.getDurability(), budget.getQuality());
    }
    public Task newSubTask(Memory m, Compound content, char punctuation, Truth truth, long occ, float p, float d, float q) {
        return  m.newTask(content)
                .punctuation(punctuation)
                .truth(truth)
                .budget(p, d, q)
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
        final Term[] rawArgs = args();
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
    public Term[] args() {
        return arg().terms();
    }

    public Concept getConcept() {
        final Memory m = getMemory();
        if (m == null) return null;
        return m.concept(getTerm());
    }

    public Truth getConceptDesire() {
        Concept c = getConcept();
        if (c == null) return null;
        return c.getDesire();
    }

    public float getConceptExpectation() {
        Truth tv = getConceptDesire();
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
    public void stop() {
        getMemory().delete(getTerm());
    }

//    /**
//     * if any of the arguments are 'eval' operations, replace its result
//     * in that position in a cloned Operation instance
//     *
//     * @return
//     */
//    public Operation inline(Memory memory, boolean removeSelf) {
//        //TODO avoid clone if it does not involve any eval()
//        //if (!hasEval()) return this;
//        return clone(Product.make(arg(memory, true, removeSelf /* keep SELF term at this point */)));
//    }

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


    public int numArgs() {
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

        byte[] op = getOperator().bytes();
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
        b.append((byte) Op.COMPOUND_TERM_OPENER.ch);


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

        b.append((byte) Op.COMPOUND_TERM_CLOSER.ch);

        return b.toBytes();
    }

    @Override
    public void append(Writer p, boolean pretty) throws IOException {

        Term predTerm = getOperator();

        if ((predTerm.getVolume()!=1) || (predTerm.hasVar())) {
            //if the predicate (operator) of this operation (inheritance) is not an atom, use Inheritance's append format
            super.append(p, pretty);
            return;
        }


        final Term[] xt = arg().terms();

        predTerm.append(p, pretty); //add the operator name without leading '^'
        p.append(Op.COMPOUND_TERM_OPENER.ch);


        int n = 0;
        for (final Term t : xt) {
            if (n != 0) {
                p.append(Symbols.ARGUMENT_SEPARATOR);
                if (pretty)
                    p.append(' ');
            }

            t.append(p, pretty);



            n++;
        }

        p.append(Op.COMPOUND_TERM_CLOSER.ch);

    }


    final static int ProductInSetExtPattern =
            Op.bitStructure(
                    Op.SET_EXT,
                    Op.PRODUCT);

    public static Product getArgumentProduct(Compound c) {
        if (!c.impossibleSubStructure(ProductInSetExtPattern)) {
            if (c instanceof SetExt) {
                SetExt sc = ((SetExt)c);
                if (sc.length() ==1) {
                    final Term scp = sc.term(0);
                    if (scp instanceof Product) {
                        return (Product) scp;
                    }
                }
            }
        }
        return null;
    }

    /** null until the operation is executed, and null again afterward */
    public Memory getMemory() {
        return memory;
    }

    public boolean isExecuting() {
        return getMemory()!=null;
    }

    public boolean setMemory(Memory memory) {
        if (memory!=null && this.memory!=null) return false;
        this.memory = memory;
        return true;
    }

    public String argString() {
        return Arrays.toString(args());
    }

    /** creates a result term in the conventional format */
    public static Inheritance result(Operator op, Product x, Term y) {
        return Inheritance.make(y,
                ImageExt.make(x, op, (short)(x.length()-1) /* position of the variable */)
        );
    }
}
