package nars.language;

import java.util.Objects;


abstract public class RelIndexCompoundTerm extends CompoundTerm {

    /** The index of relation in the component list */
    public final short relationIndex;
    
    public RelIndexCompoundTerm(Term[] t, short index) {
        super(t);
        this.relationIndex = index;
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
    
    @Override
    public boolean validSize(int num) {
        return num >= 1;
    }


    @Override
    protected int calcHash() {
        return Objects.hash(super.hashCode(), relationIndex);
    }

    @Override
    public int compareTo(Term that) {
        if (that instanceof RelIndexCompoundTerm) {
            RelIndexCompoundTerm r = (RelIndexCompoundTerm)that;
            if (r.relationIndex != relationIndex)
                return relationIndex - r.relationIndex;
        }
        return super.compareTo(that);
    }

    @Override
    public boolean equals(Object that) {
        if (that instanceof RelIndexCompoundTerm) {
            RelIndexCompoundTerm r = (RelIndexCompoundTerm)that;
            if (r.relationIndex != relationIndex)
                return false;
        }
        return super.equals(that);
    }

    
    
    
}
