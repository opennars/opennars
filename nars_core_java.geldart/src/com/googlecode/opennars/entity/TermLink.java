/*
 * TermLink.java
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
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.googlecode.opennars.entity;

import com.googlecode.opennars.language.CompoundTerm;
import com.googlecode.opennars.language.Term;
import com.googlecode.opennars.main.Memory;
import com.googlecode.opennars.parser.Symbols;

/**
 * A link between a compound term and a component term
 * <p>
 * A TermLink links the current Term to a target Term, which is 
 * either a component of, or compound made from, the current term.
 * <p>
 * Both terms are constant.
 * <p>
 * The index value(s) indicates the location of the component in the compound.
 */
public class TermLink extends Item {

    public static final short SELF = 0;                 // TaskLink only
    public static final short COMPONENT = 1;
    public static final short COMPOUND = 2;
    public static final short COMPONENT_STATEMENT = 3;
    public static final short COMPOUND_STATEMENT = 4;
    public static final short COMPONENT_CONDITION = 5;
    public static final short COMPOUND_CONDITION = 6;
    public static final short TRANSFORM = 7;            // TaskLink only
        
    private Term target;
    protected short type;
    protected short[] index;
    
    public TermLink(Memory memory) {
    	super(memory);
    }
    
    /**
     * Simplest constructor, called in CompoundTerm and Implication
     * @param t target Term
     * @param p link type
     * @param i component index in compound
     */
    public TermLink(Term t, short p, int i, Memory memory) {
    	super(memory);
        target = t;
        type = p;
        index = new short[1];
        index[0] = (short) i;
        setKey();
    }

    public TermLink(Term t, short p, int i, int j, Memory memory) {
    	super(memory);
        target = t;
        type = p;
        index = new short[2];
        index[0] = (short) i;
        index[1] = (short) j;
        setKey();
    }

    public TermLink(Term t, short p, int i, int j, int k, Memory memory) {
    	super(memory);
        target = t;
        type = p;
        index = new short[3];
        index[0] = (short) i;
        index[1] = (short) j;
        index[2] = (short) k;
        setKey();
    }

    protected TermLink(BudgetValue v, Memory memory) {
        super(v, memory);
    }
    
    // the CompotionLink that is actually inserted
    public TermLink(Term t, TermLink template, BudgetValue v, Memory memory) {
        super(v, memory);
        target = t;
        type = template.getType();
        if (template.getTarget().equals(target)) 
            type = reverse(type);
        index = template.getIndices();
        setKey();
    }

    protected void setKey() {
        String at1, at2;
        if (toComponent()) {
            at1 = Symbols.LinkToComponent_at1;
            at2 = Symbols.LinkToComponent_at2;
        } else {
            at1 = Symbols.LinkToCompound_at1;
            at2 = Symbols.LinkToCompound_at2;            
        }
        String in = "T" + type;
        if (index != null)
            for (int i = 0; i < index.length; i++) {
                in += "-" + (index[i]+1);
            }
        key = at1 + in + at2;
        if (target != null)
            key += target;
    }
    
    public Term getTarget() {
        return target;
    }

    public short getType() {
        return type;
    }
    
    public boolean toComponent() {
        return ((type % 2) > 0);
    }

    public int indexLength() {
        return (index == null)? 0 : index.length;
    }
    
    public short[] getIndices() {
        return index;
    }
    
    public short getIndex(int i) {
        if ((index != null) && (i < index.length))
            return index[i];
        else
            return -1;
    }
    
    protected short reverse(short i) {
        if ((i % 2) == 0)
            return (short) (i - 1);
        else
            return (short) (i + 1);
    }
    
    public String toString() {
        return (super.toString() + " " + key);
    }

    public String toString2() {
        return (super.toString2() + " " + key);
    }
}

