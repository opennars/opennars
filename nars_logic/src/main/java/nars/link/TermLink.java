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
package nars.link;

import nars.Symbols;
import nars.budget.Budget;
import nars.budget.Item;
import nars.term.Term;
import nars.term.Termed;
import nars.util.utf8.Utf8;

/**
 * A tlink between a compound term and a component term
 * <p>
 * A TermLink links the current Term to a target Term, which is 
 * either a component of, or compound made from, the current term.
 * <p>
 * Neither of the two terms contain variable shared with other terms.
 * <p>
 * The index value(s) indicates the location of the component in the compound.
 * <p>
 * This class is mainly used in logic.RuleTable to dispatch premises to logic rules
 */
public class TermLink extends Item<TermLinkKey> implements TermLinkKey, TLink<Term>, Termed {



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


    /** The index of the component in the component list of the compound, may have up to 4 levels */
    public final short[] index;
    public final short type;
    private final int hash;
    private byte[] prefix;


    /**
     * Constructor to make actual TermLink from a template
     * <p>
     * called in Concept.buildTermLinks only
     * @param
     * @param incoming or outgoing
     * @param template TermLink template previously prepared
     * @param v Budget value of the tlink
     */
    public TermLink(final Term t, final TermLinkTemplate template, final Budget v, final byte[] prefix, final int hash) {
        super(v);

        if (!t.isNormalized()) {
            throw new RuntimeException("not normalized: "+ t);
        }
        this.target = t;

        this.type = template.getType(t); /* whether this points to subterm */

        this.index = template.index;

        this.prefix = prefix;

        this.hash = hash;
    }

    @Override
    public byte[] bytes() {
        return prefix;
    }

    @Override
    public void setBytes(byte[] b) {
        this.prefix = b;
    }

    @Override
    public TermLinkKey name() {
        return this;
    }

    public boolean toSelfOrTransform() {
        return ((type == SELF) || (type == TRANSFORM));
    }
    public boolean toSuperTerm() {
        if (toSelfOrTransform()) return false;
        return type % 2 == 1;
    }
    public boolean toSubTerm() {
        if (toSelfOrTransform()) return false;
        return type % 2 == 0;
    }


    @Override
    public int hashCode() {
        return hash;
    }


    @Override
    public boolean equals(final Object obj) {
        return termLinkEquals(obj);

//        if (this != obj) {
//            if (termLinkEquals(obj)) {
//                System.err.println("identity inequal but " + this + "(" + getClass() + ") actually equals " + obj + " (" + obj.getClass() );
//                return true;
//            }
//        }
//        return false;



        //return termLinkEquals(obj);
    }

    
    @Override
    public String toString() {
        return new StringBuilder()
                .append(Utf8.fromUtf8(TermLinkTemplate.prefix(type, index, false)))
                .append(Symbols.TLinkSeparator)
                .append(getTerm().toString()).toString();
    }





    /**
     * Get one index by level
     * @param i The index level
     * @return The index value
     */
    @Override
    public final short getIndex(final int i) {
        /*if ((i < 0) || ( i >= index.length))
            throw new RuntimeException(this + " index fault: " + i);*/
        //if (/*(index != null) &&*/ (i < index.length)) {

        return index[i];
        //} else {
            //return -1;
        //}
    }

    @Override
    public Term getTerm() {
        return target.getTerm();
    }

    @Override public void delete() {
        
    }





//    /** the original prefix code, verbose. see TermLinkTemplate.prefix(..) */
//    @Deprecated protected CharSequence newPrefix() {
//
//        final String at1, at2;
//        if ((type % 2) == 1) {  // to component
//            at1 = Symbols.TO_COMPONENT_1;
//            at2 = Symbols.TO_COMPONENT_2;
//        } else {                // to compound
//            at1 = Symbols.TO_COMPOUND_1;
//            at2 = Symbols.TO_COMPOUND_2;
//        }
//
//        final int MAX_INDEX_DIGITS = 2;
//
//        final CharSequence targetName = target.name();
//
//        final int estimatedLength = 2+2+1+MAX_INDEX_DIGITS*( (index!=null ? index.length : 0) + 1);// + targetName.length();
//
//        final StringBuilder n = new StringBuilder(estimatedLength);
//        n.append(at1).append('T').append(type);
//        if (index != null) {
//            for (final short i : index) {
//                //prefix.append('-').append( Integer.toString(i + 1, 16 /** hexadecimal */)  );
//
//                n.append('-');
//
//                final char ii = (char)(i + 1);
//                if (ii < 10)
//                    n.append((char)(ii+'0') );
//                else
//                    n.append( Texts.n2(ii) );
//            }
//
//        }
//        return n.append(at2);//.append(targetName);
//    }
}
