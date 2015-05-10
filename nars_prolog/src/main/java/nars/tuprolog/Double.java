/*
 * tuProlog - Copyright (C) 2001-2002  aliCE team at deis.unibo.it
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package nars.tuprolog;


import nars.nal.term.*;
import nars.nal.term.TermVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Double class represents the double prolog data type
 *
 */
@SuppressWarnings("serial")
public class Double extends PNum {
    
    private final double value;
    
    public Double(double v) {
        value = v;
    }
    
    /**
     *  Returns the value of the Double as int
     */
    final public int intValue() {
        return (int) value;
    }
    
    /**
     *  Returns the value of the Double as float
     *
     */
    final public float floatValue() {
        return (float) value;
    }
    
    /**
     *  Returns the value of the Double as double
     *
     */
    final public double doubleValue() {
        return value;
    }
    
    /**
     *  Returns the value of the Double as long
     */
    final public long longValue() {
        return (long) value;
    }
    
    
    /** is this term a prolog integer term? */
    final public boolean isInteger() {
        return false;
    }
    
    /** is this term a prolog real term? */
    final public boolean isReal() {
        return true;
    }
    
    /** is an int Integer number? 
     * @deprecated Use <tt>instanceof Int</tt> instead. */
    final public boolean isTypeInt() {
        return false;
    }

    /** is an int Integer number?
     * @deprecated Use <tt>instanceof Int</tt> instead. */
    final public boolean isInt() {
        return false;
    }
    
    /** is a float Real number? 
     * @deprecated Use <tt>instanceof alice.tuprolog.Float</tt> instead. */
    final public boolean isTypeFloat() {
        return false;
    }

    /** is a float Real number?
     * @deprecated Use <tt>instanceof alice.tuprolog.Float</tt> instead. */
    final public boolean isFloat() {
        return false;
    }
    
    /** is a double Real number? 
     * @deprecated Use <tt>instanceof alice.tuprolog.Double</tt> instead. */
    final public boolean isTypeDouble() {
        return true;
    }

    /** is a double Real number?
     * @deprecated Use <tt>instanceof alice.tuprolog.Double</tt> instead. */
    final public boolean isDouble() {
        return true;
    }
    
    /** is a long Integer number? 
     * @deprecated Use <tt>instanceof alice.tuprolog.Long</tt> instead. */
    final public boolean isTypeLong() {
        return false;
    }

    /** is a long Integer number?
     * @deprecated Use <tt>instanceof alice.tuprolog.Long</tt> instead. */
    final public boolean isLong() {
        return false;
    }
    
    /**
     * Returns true if this Double term is grater that the term provided.
     * For number term argument, the int value is considered.
     */
    public boolean isGreater(Term t) {
        t = t.getTerm();
        if (t instanceof PNum) {
            return value>((PNum)t).doubleValue();
        } else if (t instanceof Struct) {
            return false;
        } else return t instanceof Var;
    }
    
    public boolean isGreaterRelink(Term t, ArrayList<String> vorder) {
        t = t.getTerm();
        if (t instanceof PNum) {
            return value>((PNum)t).doubleValue();
        } else if (t instanceof Struct) {
            return false;
        } else return t instanceof Var;
    }
    
    /**
     * Returns true if this Double term is equal to the term provided.
     */
    public boolean isEqual(Term t) {
        t = t.getTerm();
        if (t instanceof PNum) {
            PNum n = (PNum) t;
            if (!n.isReal())
                return false;
            return value == n.doubleValue();
        } else {
            return false;
        }
    }
    
    /**
     * Tries to unify a term with the provided term argument.
     * This service is to be used in demonstration context.
     */
    public boolean unify(List<Var> vl1, List<Var> vl2, Term t) {
        t = t.getTerm();
        if (t instanceof Var) {
            return ((Var)t).unify(vl2, vl1, this);
        } else if (t instanceof PNum && ((PNum) t).isReal()) {
            return value == ((PNum) t).doubleValue();
        } else {
            return false;
        }
    }



    @Override
    public PTerm clone() {
        return new Double(value);
    }

    public String toString() {
        return java.lang.Double.toString(value);
    }
    
    public int resolveVariables(int count) {
        return count;
    }

 


    @Override
    public int hashCode() {
        return java.lang.Double.hashCode(value);
    }


}