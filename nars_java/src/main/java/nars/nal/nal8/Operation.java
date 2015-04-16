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

import nars.Global;
import nars.Memory;
import nars.budget.Budget;
import nars.io.Symbols;
import nars.nal.Concept;
import nars.nal.NALOperator;
import nars.nal.Sentence;
import nars.nal.Task;
import nars.nal.nal1.Inheritance;
import nars.nal.nal4.Product;
import nars.nal.term.Compound;
import nars.nal.term.Term;
import nars.nal.term.Variable;

import java.util.Arrays;

import static nars.nal.NALOperator.COMPOUND_TERM_CLOSER;
import static nars.nal.NALOperator.COMPOUND_TERM_OPENER;

/**
 * An operation is interpreted as an Inheritance relation.
 */
public class Operation extends Inheritance {
    private Task task;
    
    
    //public final static Term[] SELF_TERM_ARRAY = new Term[] { SELF };

    /**
     * Constructor with partial values, called by make
     *
     */
    protected Operation(Term argProduct, Term operator) {
        super(argProduct, operator);
    }
    
    protected Operation(Term[] t) {
        super(t);
    }
    protected Operation() {
        super(null);
    }

    
    /**
     * Clone an object
     *
     * @return A new object, to be casted into a SetExt
     */
    @Override
    public Operation clone() {        
        return new Operation(term);
    }
 
   
    /**
     * Try to make a new compound from two components. Called by the logic
     * rules.
     *
     * @param self specify a SELF, or null to use memory's current self
     * @return A compound generated or null
     */
    public static Operation make(final Operator oper, Term[] arg, Term self) {

//        if (Variables.containVar(arg)) {
//            throw new RuntimeException("Operator contains variable: " + oper + " with arguments " + Arrays.toString(arg) );
//        }

        if (self == null) {
            self = oper.getMemory().getSelf();
        }

        if((arg.length == 0) || ( !arg[arg.length-1].equals(self)) ) {
            Term[] arg2=new Term[arg.length+1];
            System.arraycopy(arg, 0, arg2, 0, arg.length);
            arg2[arg.length] = self;
            arg=arg2;
        }
        
        return new Operation( new Product(arg), oper  );        
    }

    public Operator getOperator() {
        return (Operator)getPredicate();
    }
    
    @Override
    protected CharSequence makeName() {
        if (Global.DEBUG)
            if (!(getSubject() instanceof Product && getPredicate() instanceof Operator))
                throw new RuntimeException("Invalid Operation contents: " + this); //should never happen

       return makeName(getPredicate().name(), ((Product)getSubject()).term);
    }

    
    public static CharSequence makeName(final CharSequence op, final Term[] arg) {
        final StringBuilder nameBuilder = new StringBuilder(16) //estimate
                .append(COMPOUND_TERM_OPENER.ch).append(op);
        
        int n=0;
        for (final Term t : arg) {
            /*if(n==arg.length-1) {
                break;
            }*/
            nameBuilder.append(Symbols.ARGUMENT_SEPARATOR);
            nameBuilder.append(t.name());
            n++;
        }
        
        nameBuilder.append(COMPOUND_TERM_CLOSER.ch);
        return nameBuilder.toString();
    }
    
    
    /*public Operator getOperator() {
        return (Operator) getPredicate();
    }
    
    public Term[] getArguments() {
        return ((CompoundTerm) getSubject()).term;
    }*/

    /** stores the currently executed task, which can be accessed by Operator execution */
    public void setTask(final Task task) {
        this.task = task;
    }

    public Task getTask() {
        return task;
    }

    public Product getArguments() {
        return (Product)getSubject();
    }

    public static Term make(Memory m, Term[] raw) {
        if (raw.length < 1) {
            //must include at least the operate as the first term in raw[]
            return null;
        }

        String operator = raw[0].name().toString();
        if (operator.charAt(0)!= NALOperator.OPERATION.ch) {
            //prepend '^' if missing
            operator = NALOperator.OPERATION.symbol + operator;
        }

        Term[] args = Arrays.copyOfRange(raw, 1, raw.length);

        Operator o = m.operator(operator);
        if (o == null)
            throw new RuntimeException("Unknown operate: " + operator); //TODO use a 'UnknownOperatorException' so this case can be detected by callers

        return make(o, args, null);
    }

    public Term getArgument(int i) {
        return getArguments().term[i];
    }

    @Override
    public boolean isExecutable(Memory mem) {
        //don't allow ^want and ^believe to be active/have an effect,
        //which means its only used as monitor
        return getOperator().isExecutable(mem);
    }


    public static Operation make(Operator opTerm, Term... arg) {
        return make(opTerm, arg, null);
    }

    public Task newSubTask(Sentence sentence, Budget budget) {
        return new Task(sentence, budget, getTask());
    }

    public Term[] getArgumentTerms(boolean evaluate) {
        final Term[] rawArgs = getArgumentsRaw();
        int numInputs = rawArgs.length;

        if (rawArgs[numInputs - 1].equals(getOperator().getMemory().getSelf()))
            numInputs--;

        if (rawArgs[numInputs - 1] instanceof Variable)
            numInputs--;

        Term[] x;

        if (evaluate) {
            x = new Term[numInputs];
            for (int i = 0; i < numInputs; i++) {
                x[i] = evaluate(getOperator().getMemory(), rawArgs[i]);
            }
        }
        else {
            x = Arrays.copyOfRange(rawArgs, 0, numInputs);
        }

        return x;
    }

    protected static Term evaluate(final Memory m, final Term x) {
        if (x instanceof Operation) {
            final Operation o = (Operation)x;
            final Operator op = o.getOperator();
            if (op instanceof TermEval) {
//                CompoundTerm[] ee = ((TermFunction) op).executeTerm(o);
//                if ((ee!=null) && (ee[0]!=null))
//                    return ee[0];
                return ((TermFunction)op).function(o.getArgumentTerms(true));
            }
        }

        if (x instanceof Compound) {
            Compound ct = (Compound)x;
            Term[] r = new Term[ct.size()];
            boolean modified = false;
            int j = 0;
            for (final Term w : ct.term) {
                Term v = evaluate(m, w);
                if ((v!=null) && (v!=w)) {
                    r[j] = v;
                    modified = true;
                }
                else {
                    r[j] = w;
                }
                j++;
            }
            if (modified)
                return ct.clone(r);
        }

        return x; //return as-is
    }


    /** produces a cloned instance with the replaced args + additional terms in a new argument product */
    public Operation cloneWithArguments(Term[] args, Term... additional) {
        return (Operation)setComponent(0, Product.make(args, additional));
    }

    /** returns a reference to the raw arguments as contained by the Product subject of this operation */
    public Term[] getArgumentsRaw() {
        return getArguments().term;
    }

    public float getDesire(Memory m) {
        Concept c = m.concept(getTerm());
        if (c == null) return 0;
        return c.getDesire().getExpectation();
    }
}
