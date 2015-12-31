package nars.nal.meta;

import nars.Op;
import nars.term.Term;
import nars.term.atom.Atomic;

import java.util.List;

/**
 * each precondition is testesd for equality by its toString() method reprsenting an immutable key.
 * so subclasses must implement a valid toString() identifier containing its components.
 * this will only be used at startup when compiling
 *
 * WARNING: no preconditions should store any state so that their instances may be used by
 * different contexts (ex: NAR's)
 */
public abstract class AtomicBooleanCondition<C> extends Atomic implements BooleanCondition<C> {

    public AtomicBooleanCondition() {
        super();
    }

    public abstract String toString();

    /** add this or subconditions to expand in a rule */
    @Override
    public void addConditions(List<Term> l) {
        //default: just add this
        l.add(this);
    }

    public String toJavaConditionString() {
        return ("(/* TODO: " +
                this.toString() +
                " */ false)\t");
    }


    @Override
    public int complexity() {
        return 1;
    }

    @Override
    public int volume() {
        return 1;
    }

    @Override
    public int vars() {
        return 0;
    }

    @Override
    public int varQuery() {
        return 0;
    }

    @Override
    public int varDep() {
        return 0;
    }

    @Override
    public int varIndep() {
        return 0;
    }

    @Override
    public Op op() {
        return Op.ATOM;
    }

}
