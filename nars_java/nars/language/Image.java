package nars.language;

import nars.io.Symbols;
import nars.io.Symbols.NativeOperator;
import static nars.io.Symbols.NativeOperator.COMPOUND_TERM_CLOSER;
import static nars.io.Symbols.NativeOperator.COMPOUND_TERM_OPENER;

/**
 *
 * @author me
 */


abstract public class Image extends CompoundTerm {
    /** The index of relation in the component list */
    public final short relationIndex;

    protected Image(CharSequence name, Term[] components, short relationIndex) {
        super(name, components);
        this.relationIndex = relationIndex;
    }
    
    protected Image(CharSequence n, Term[] cs, boolean con, short complexity, short index) {
        super(n, cs, con, complexity);
        this.relationIndex = index;
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
        
        StringBuilder name = new StringBuilder(sizeEstimate)
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


    
    @Override
    public int getMinimumRequiredComponents() {
        return 1;
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
    
    @Override
    public int compareTo(Term that) {
        if (that.getClass() == getClass()) {
            int c = super.compareTo(that);
            if (c==0)
                return Integer.compare(relationIndex, ((Image)that).relationIndex);
            return c;
        }
        else
            return super.compareTo(that);    
    }
   
    
}

