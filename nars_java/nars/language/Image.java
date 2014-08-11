package nars.language;

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

