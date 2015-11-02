package nars.meta;

import java.io.Serializable;
import java.util.function.Predicate;

/**
 * each precondition is testesd for equality by its toString() method reprsenting an immutable key.
 * so subclasses must implement a valid toString() identifier containing its components.
 * this will only be used at startup when compiling
 *
 * WARNING: no preconditions should store any state so that their instances may be used by
 * different contexts (ex: NAR's)
 */
abstract public class PreCondition implements Predicate<RuleMatch>, Serializable {

    abstract public String toString();

    @Override
    public final int hashCode() {
        return toString().hashCode();
    }

    @Override
    public final boolean equals(Object obj) {
        return toString().equals(obj.toString());
    }

}
