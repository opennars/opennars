package nars.meta;

import java.io.Serializable;
import java.util.function.Predicate;

/**
 * each precondition is testesd for equality by its toString() method reprsenting an immutable key.
 * so subclasses must implement a valid toString() identifier containing its components.
 * this will only be used at startup when compiling
 */
abstract public class PreCondition implements Predicate<RuleMatch>, Serializable {

    //abstract public boolean test(RuleMatch m);

    abstract public String toString();

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return toString().equals(obj.toString());
    }

    /** can this precondition be tested in the early phase */
    public boolean isEarly() { return false; }
    
}
