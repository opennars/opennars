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
    private boolean hasPlaceHolder;

    protected Image(Term[] components, short relationIndex) {
        super();
        
        this.relationIndex = relationIndex;
        init(components);
    }
    

    public static boolean isPlaceHolder(Term t) {
        CharSequence n = t.name();
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
        CharSequence c = makeImageName(operator(), term, relationIndex);
        this.hasPlaceHolder = false;
        for (Term t : term) 
            if (isPlaceHolder(t)) {
                hasPlaceHolder = true;
                break;
            }                
        return c;
    }

    public boolean containsPlaceHolder() {
        return hasPlaceHolder;
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
    public int compareTo(AbstractTerm that) {
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

