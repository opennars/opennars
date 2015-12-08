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

import nars.$;
import nars.Memory;
import nars.Symbols;
import nars.budget.Budget;
import nars.concept.Concept;
import nars.nal.nal1.Inheritance;
import nars.nal.nal4.ImageExt;
import nars.nal.nal4.Product;
import nars.task.MutableTask;
import nars.task.Task;
import nars.term.Term;
import nars.term.compound.Compound;
import nars.term.variable.Variable;
import nars.truth.Truth;
import nars.util.utf8.ByteBuf;

import java.io.IOException;
import java.util.Arrays;

import static nars.Symbols.COMPOUND_TERM_CLOSER;
import static nars.Symbols.COMPOUND_TERM_OPENER;

/**
 * An operation is interpreted as an Inheritance relation with an operator.
 *
 * TODO generalize it so that Prduct<A> is moved to the generic part, allowing non-products
 * to form Operations
 *
 * @param A argument term type
 */
public class Operation<A extends Term> extends Inheritance<Product<A>, Operator> {



    public Operation(String operatorName, A[] args) {
        this(Operator.the(operatorName), Product.make(args));
    }

    public Operation(String operatorName, Product<A> args) {
        this(Operator.the(operatorName), args);
    }

    public Operation(Operator operator, Product<A> args) {
        super(args, operator);
    }



    public final Operation clone(Product args) {
        Operation x = new Operation(getPredicate(), args);
        return x;
    }


    /**
     * gets the term wrapped by the Operator predicate
     */
    public final Term getOperatorTerm() {
        return getOperator().identifier();
    }
    public final Operator getOperator() {
        return getPredicate();
    }

    public final Product<A> arg() {
        return getSubject();
    }

    public final A arg(int i) {
        return arg().term(i);
    }

    @Deprecated public static Inheritance make(final Term subject, final Term predicate) {
        throw new RuntimeException("not what is intended");
    }


    public static Task spawn(Task parent, Compound content, char punctuation, Truth truth, long occ, Budget budget) {
        return spawn(parent, content, punctuation, truth, occ, budget.getPriority(), budget.getDurability(), budget.getQuality());
    }

    public static Task spawn(Task parent, Compound content, char punctuation, Truth truth, long occ, float p, float d, float q) {
        return new MutableTask(content, punctuation)
                .truth(truth)
                .budget(p, d, q)
                .parent(parent)
                .occurr(occ);
    }


//    public Term[] arg(Memory memory) {
//        return arg(memory, false);
//    }
//
//    public Term[] arg(Memory memory, boolean evaluate) {
//        return arg(memory, evaluate, true);
//    }
//
//    public Term[] arg(Memory memory, boolean evaluate, boolean removeSelf) {
//        final Term[] rawArgs = args();
//        int numInputs = rawArgs.length;
//
//        if (removeSelf) {
//            if (numInputs > 0) {
//                if (rawArgs[numInputs - 1].equals(memory.self()))
//                    numInputs--;
//            }
//        }
//
//        if (numInputs > 0) {
//            if (rawArgs[numInputs - 1] instanceof Variable)
//                numInputs--;
//        }
//
//
//        Term[] x;
//
//        if (evaluate) {
//
//            x = new Term[numInputs];
//            for (int i = 0; i < numInputs; i++) {
//                x[i] = eval.eval(rawArgs[i], memory);
//            }
//        } else {
//            x = Arrays.copyOfRange(rawArgs, 0, numInputs);
//        }
//
//        return x;
//    }


//    /**
//     * produces a cloned instance with the replaced args + additional terms in a new argument product
//     */
//    public Operation cloneWithArguments(Term[] args, Term... additional) {
//        return (Operation) cloneReplacingSubterm(0, Product.make(args, additional));
//    }

    /**
     * returns a reference to the raw arguments as contained by the Product subject of this operation
     * avoid using this because it may involve creation of unnecessary array
     * if Product1.terms() is called
     */
    public final A[] args() {
        return arg().terms();
    }

    public final Concept getConcept(Memory m) {
        if (m == null) return null;
        return m.concept(this);//getTerm());
    }

    public final Truth getConceptDesire(Memory m) {
        Concept c = getConcept(m);
        if (c == null) return null;
        return c.getDesire();
    }

    public final float getConceptExpectation(Memory m) {
        Truth tv = getConceptDesire(m);
        if (tv == null) return 0;
        return tv.getExpectation();
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


    public final int numArgs() {
        return arg().size();
    }

    public static boolean isA(Term x, Term someOperatorTerm) {
        if (x instanceof Operation) {
            Operation o = (Operation) x;
            if (o.getOperatorTerm().equals(someOperatorTerm))
                return true;
        }
        return false;
    }



    @Override
    public final byte[] bytes() {

        byte[] op = getOperatorTerm().bytes();
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
        b.append((byte) COMPOUND_TERM_OPENER);


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

        b.append((byte) COMPOUND_TERM_CLOSER);

        return b.toBytes();
    }

    @Override
    public final void append(Appendable p, boolean pretty) throws IOException {

        Term predTerm = getOperatorTerm();

        if ((predTerm.volume() != 1) || (predTerm.hasVar())) {
            //if the predicate (operator) of this operation (inheritance) is not an atom, use Inheritance's append format
            super.append(p, pretty);
            return;
        }


        final Term[] xt = arg().terms();

        predTerm.append(p, pretty); //add the operator name without leading '^'
        p.append(COMPOUND_TERM_OPENER);


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

        p.append(COMPOUND_TERM_CLOSER);

    }




    public final String argString() {
        return Arrays.toString(args());
    }

    /**
     * creates a result term in the conventional format.
     * the final term in the product (x) needs to be a variable,
     * which will be replaced with the result term (y)
     *
     */
    public static Inheritance result(Operation op, Term y) {
        Product x =  op.arg();
        Term t = x.last();
        if (!(t instanceof Variable))
            return null;

        return $.inh(
            y, //SetExt.make(y),
            makeImageExt(x, op.getOperator(), (short) (x.size() - 1) /* position of the variable */)
        );
    }
    /**
     * Try to make an Image from a Product and a relation. Called by the logic rules.
     * @param product The product
     * @param relation The relation (the operator)
     * @param index The index of the place-holder (variable)
     * @return A compound generated or a term it reduced to
     */
    static Term makeImageExt(Product product, Term relation, short index) {
        int pl = product.size();
        if (relation instanceof Product) {
            Product p2 = (Product) relation;
            if ((pl == 2) && (p2.size() == 2)) {
                if ((index == 0) && product.term(1).equals(p2.term(1))) { // (/,_,(*,a,b),b) is reduced to a
                    return p2.term(0);
                }
                if ((index == 1) && product.term(0).equals(p2.term(0))) { // (/,(*,a,b),a,_) is reduced to b
                    return p2.term(1);
                }
            }
        }
        /*Term[] argument =
            Terms.concat(new Term[] { relation }, product.cloneTerms()
        );*/
        Term[] argument = new Term[ pl  ];
        argument[0] = relation;
        System.arraycopy(product.terms(), 0, argument, 1, pl - 1);

        return new ImageExt(argument, index+1);
    }

    /** applies certain data to a feedback task relating to its causing operation's task */
    public static Task asFeedback(MutableTask feedback, Task<Operation> goal, float priMult, float durMult) {
        return feedback.budget(goal.getBudget()).
                budgetScaled(priMult, durMult).
                parent(goal);
    }
}
