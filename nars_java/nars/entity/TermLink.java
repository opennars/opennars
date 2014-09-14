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
 * along with Open-NARS.  If not, see <http://www.gnu.org/licenses/>.
 */
package nars.entity;

import nars.core.Parameters;
import nars.io.Symbols;
import nars.io.Texts;
import nars.language.Term;
import nars.util.rope.Rope;

/**
 * A link between a compound term and a component term
 * <p>
 * A TermLink links the current Term to a target Term, which is 
 * either a component of, or compound made from, the current term.
 * <p>
 * Neither of the two terms contain variable shared with other terms.
 * <p>
 * The index value(s) indicates the location of the component in the compound.
 * <p>
 * This class is mainly used in inference.RuleTable to dispatch premises to inference rules
 */
public class TermLink extends Item {
    /** At C, point to C; TaskLink only */
    public static final short SELF = 0;
    /** At (&&, A, C), point to C */
    public static final short COMPONENT = 1;
    /** At C, point to (&&, A, C) */
    public static final short COMPOUND = 2;
    /** At <C --> A>, point to C */
    public static final short COMPONENT_STATEMENT = 3;
    /** At C, point to <C --> A> */
    public static final short COMPOUND_STATEMENT = 4;
    /** At <(&&, C, B) ==> A>, point to C */
    public static final short COMPONENT_CONDITION = 5;
    /** At C, point to <(&&, C, B) ==> A> */
    public static final short COMPOUND_CONDITION = 6;
    /** At C, point to <(*, C, B) --> A>; TaskLink only */
    public static final short TRANSFORM = 8;
    
    
    /** The linked Term */
    public final Term target;
    
    /** The type of link, one of the above */
    
    public final short type;
    /** The index of the component in the component list of the compound, may have up to 4 levels */
    public final short[] index;
    private CharSequence key;
    
   

            
    /**
     * Constructor for TermLink template
     * <p>
     * called in CompoundTerm.prepareComponentLinks only
     * @param target Target Term
     * @param type Link type
     * @param indices Component indices in compound, may be 1 to 4
     */
    public TermLink(final Term target, final short type, final short... indices) {
        super();
        this.target = target;
        this.type = type;
        assert (type % 2 == 0); // template types all point to compound, though the target is component
        if (type == TermLink.COMPOUND_CONDITION) {  // the first index is 0 by default
            
            index = new short[indices.length + 1];
            //index[0] = 0; //first index is zero, but not necessary to set since index[] was just created
            
            System.arraycopy(indices, 0, index, 1, indices.length);
            /* for (int i = 0; i < indices.length; i++)
                index[i + 1] = (short) indices[i]; */
        } else {
            index = indices;
            /* index = new short[indices.length];
            for (int i = 0; i < index.length; i++)
                index[i] = (short) indices[i]; */
        }
        //setKey();        
    }

    /** called from TaskLink
     * @param s The key of the TaskLink
     * @param v The budget value of the TaskLink
     */
    protected TermLink(final BudgetValue v, final short type, short[] indices) {
        super(v);
        this.type = type;
        this.index = indices;
        this.target = null;
        //setKey();
    }

    /**
     * Constructor to make actual TermLink from a template
     * <p>
     * called in Concept.buildTermLinks only
     * @param t Target Term
     * @param template TermLink template previously prepared
     * @param v Budget value of the link
     */
    public TermLink(final Term t, final TermLink template, final BudgetValue v) {
        super(v);
        target = t;
        type = (template.target.equals(t)) 
                ? (short)(template.type - 1) //// point to component
                : template.type;
        index = template.index;
        //setKey();
    }

   @Override
    public boolean equals(final Object obj) {
        if (obj == this) return true;
        
        if (!(obj.getClass() == getClass()))
            return false;
        
        TermLink that = (TermLink)obj;
        
        if (getKey() instanceof Rope) {
            //some quick comparisons to fail fast, because rope equals() may be slow
            if (that.type != type)
                return false;

            final int indexLen = index!=null ? index.length : -1;
            final int thatIndexLen = that.index!=null ? that.index.length : -1;
            if (indexLen != thatIndexLen)
                return false;
        }
                    
        return getKey().equals(that.getKey());
    }

    @Override
    public int hashCode() {
        return getKey().hashCode();
    }

    @Override
    public CharSequence getKey() {
        if (key == null)
            setKey();
        return key;
    }

    
    protected final void setKey() {
        setKey(null);        
    }

    
    /**
     * Set the key of the link
     * @param suffix optional suffix, may be null
     */    
    protected final void setKey(final CharSequence suffix) {
        this.key = Texts.yarn(Parameters.ROPE_TERMLINK_TERM_SIZE_THRESHOLD,
                        newKeyPrefix(), 
                        target!=null ? target.name() : null, 
                        suffix);        
    }
    
//    protected final Rope newKeyRope(String suffix) {
//        Rope r = Rope.catFast();
//        
//        //Rope.visualize(r, System.err);
//        
//        return r;
//    }
//    
//    protected final StringBuilder newKeyStringBuilder(String suffix) {
//        int estimatedLength = 0;
//        
//        CharSequence prefix = newKeyPrefix();
//        estimatedLength += prefix.length();
//        
//        String targetString = null;
//        if (target!=null) {
//            targetString = target.toString();
//            estimatedLength += targetString.length();
//        }
//        
//        if (suffix!=null)
//            estimatedLength += suffix.length();
//        
//        
//        StringBuilder sb = new StringBuilder(estimatedLength).append(prefix);
//        
//        if (target != null)
//            sb.append(targetString);        
//        
//        if (suffix!=null)
//            sb.append(suffix);        
//        
//        return sb;
//    }
       
    public CharSequence newKeyPrefix() {
        final String at1, at2;
        if ((type % 2) == 1) {  // to component
            at1 = Symbols.TO_COMPONENT_1;
            at2 = Symbols.TO_COMPONENT_2;
        } else {                // to compound
            at1 = Symbols.TO_COMPOUND_1;
            at2 = Symbols.TO_COMPOUND_2;
        }
        
        final int MAX_INDEX_DIGITS = 2;
        
        int estimatedLength = 2+2+1+MAX_INDEX_DIGITS*( (index!=null ? index.length : 0) + 1);
        
        
        final StringBuilder prefix = new StringBuilder(estimatedLength);
        
        prefix.append(at1).append('T').append(type);
        
        if (index != null) {
            for (int i : index) {
                prefix.append('-').append( Integer.toString(i + 1, 16 /** hexadecimal */)  );
            }
        }
        prefix.append(at2);
        
        return prefix;
    }
    
    /**
     * Get one index by level
     * @param i The index level
     * @return The index value
     */
    public final short getIndex(final int i) {
        if ((index != null) && (i < index.length)) {
            return index[i];
        } else {
            return -1;
        }
    }

    public TermLink(final short type, final Term target, final int i0) {
        this(target, type, (short)i0);
    }
    
    public TermLink(final short type, final Term target, final int i0, final int i1) {
        this(target, type, (short)i0, (short)i1);
    }
    
    public TermLink(final short type, final Term target, final int i0, final int i1, final int i2) {
        this(target, type, (short)i0, (short)i1, (short)i2);
    }

    public TermLink(final short type, final Term target, final int i0, final int i1, final int i2, final int i3) {
        this(target, type, (short)i0, (short)i1, (short)i2, (short)i3);
    }

}
