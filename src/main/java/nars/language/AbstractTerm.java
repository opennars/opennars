package nars.language;




public interface AbstractTerm extends Cloneable, Comparable<AbstractTerm> {

    /**
     * Whether this compound term contains any variable term
     *
     * @return Whether the name contains a variable
     */
    boolean hasVar();

    /**
     * Check whether the current Term can name a Concept.
     *
     * @return A Term is constant by default
     */
    boolean isConstant();

    /**
     * Reporting the name of the current Term.
     *
     * @return The name of the term as a String
     */
    default CharSequence name() {
        return toString();
    }
    
}
