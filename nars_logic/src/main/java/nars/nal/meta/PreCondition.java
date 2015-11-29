package nars.nal.meta;

import nars.nal.RuleMatch;

import java.io.Serializable;
import java.util.List;
import java.util.function.Predicate;

/**
 * each precondition is testesd for equality by its toString() method reprsenting an immutable key.
 * so subclasses must implement a valid toString() identifier containing its components.
 * this will only be used at startup when compiling
 *
 * WARNING: no preconditions should store any state so that their instances may be used by
 * different contexts (ex: NAR's)
 */
abstract public class PreCondition implements Predicate<RuleMatch>, Comparable<PreCondition>, Serializable {

    abstract public String toString();

    @Override
    public final int hashCode() {
        return toString().hashCode();
    }

    @Override
    public final boolean equals(Object obj) {
        if (toString().equals(obj.toString())) {
//            if (getClass()!=obj.getClass())
//                throw new RuntimeException(this + " (" + getClass() + ") and " + obj + " (" + obj.getClass() + ") have equal string but different class");

            return true;
        }
        return false;
    }

    public void addConditions(List<PreCondition> l) {
        l.add(this);
    }

    @Override
    public final int compareTo(PreCondition p) {
        return toString().compareTo(p.toString());
    }
}
