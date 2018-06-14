/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.opennars.language;

import org.opennars.io.Symbols;
import org.opennars.io.Symbols.NativeOperator;
import org.opennars.main.MiscFlags;

import java.util.Objects;

import static org.opennars.io.Symbols.NativeOperator.COMPOUND_TERM_CLOSER;
import static org.opennars.io.Symbols.NativeOperator.COMPOUND_TERM_OPENER;

/**
 *
 * @author me
 */
abstract public class Image extends CompoundTerm {
    /** The index of relation in the component list */
    public final short relationIndex;

    protected Image(final Term[] components, final short relationIndex) {
        super(components);
        
        this.relationIndex = relationIndex;
                
        init(components);
    }

    @Override
    protected void init(final Term[] components) {
        super.init(components);
        this.hash = Objects.hash(super.hashCode(), relationIndex); 
    }
    
    @Override
    public int hashCode() {
        if (MiscFlags.TERM_ELEMENT_EQUIVALENCY)
            return hash;        
        else
            return super.hashCode();
    }

    @Override
    public boolean equals2(final CompoundTerm other) {
        return relationIndex == ((Image)other).relationIndex;           
    }
    
    @Override
    public int compareTo(final AbstractTerm that) {
        if (that instanceof Image) {
            final int r = relationIndex - ((Image)that).relationIndex;
            if (r!=0)
                return r;            
        }
        return super.compareTo(that);
    }

    //TODO replace with a special Term type
    public static boolean isPlaceHolder(final Term t) {
        if (t.getClass() != Term.class) return false;
        final CharSequence n = t.name();
        if (n.length() != 1) return false;
        return n.charAt(0) == Symbols.IMAGE_PLACE_HOLDER;
    }    
    
   /**
     * default method to make the oldName of an image term from given fields
     *
     * @param op the term operator
     * @param arg the list of term
     * @param relationIndex the location of the place holder
     * @return the oldName of the term
     */
    protected static String makeImageName(final NativeOperator op, final Term[] arg, final int relationIndex) {
        final int sizeEstimate = 12 * arg.length + 2;
        
        final StringBuilder name = new StringBuilder(sizeEstimate)
            .append(COMPOUND_TERM_OPENER.ch)
            .append(op)
            .append(Symbols.ARGUMENT_SEPARATOR)
            .append(arg[relationIndex].name());
        
        for (int i = 0; i < arg.length; i++) {
            name.append(Symbols.ARGUMENT_SEPARATOR);
            if (i == relationIndex) {
                name.append(Symbols.IMAGE_PLACE_HOLDER);                
            } else {
                name.append(arg[i].name());
            }
        }
        name.append(COMPOUND_TERM_CLOSER.ch);
        return name.toString();
    }    
    
    /**
     * Get the other term in the Image
     * @return The term relaterom existing fields
     * @return the name of the term
     */
    @Override
    public CharSequence makeName() {
        return makeImageName(operator(), term, relationIndex);
    }

    /**
     * Get the relation term in the Image
     * @return The term representing a relation
     */
    public Term getRelation() {
        return term[relationIndex];
    }

    /**
     * Get the other term in the Image
     * @return The term related
     */
    public Term getTheOtherComponent() {
        if (term.length != 2) {
            return null;
        }
        return (relationIndex == 0) ? term[1] : term[0];
    }
}

