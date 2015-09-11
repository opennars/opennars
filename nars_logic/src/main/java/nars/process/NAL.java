/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.process;

import nars.Global;
import nars.NAR;
import nars.task.Task;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * NAL Reasoner Process.  Includes all reasoning process state and common utility methods that utilize it.
 * <p>
 * https://code.google.com/p/open-nars/wiki/SingleStepTestingCases
 * according to derived Task: if it contains a mental operate it is NAL9, if it contains a operation it is NAL8, if it contains temporal information it is NAL7, if it contains in/dependent vars it is NAL6, if it contains higher order copulas like &&, ==> or negation it is NAL5
 * <p>
 * if it contains product or image it is NAL4, if it contains sets or set operations like &, -, | it is NAL3
 * <p>
 * if it contains similarity or instances or properties it is NAL2
 * and if it only contains inheritance
 */
public abstract class NAL extends AbstractPremise implements Supplier<Collection<Task>>, Consumer<Task> {

    /** derivation queue (this might also work as a Set) */
    protected List<Task> derived = null;

    public NAL(final NAR n) {
        super(n);
    }

    @Override public Collection<Task> get() {

        beforeDerive();

        derive();

        if (derived == null)
            derived = Collections.EMPTY_LIST;

        return afterDerive(derived);
    }

    /** implement if necessary in subclasses */
    protected void beforeDerive() {

    }

    /** implement if necessary in subclasses */
    protected Collection<Task> afterDerive(Collection<Task> c) {
        return c;
    }

    /** run the actual derivation process */
    protected abstract void derive();





    @Override public void accept(Task derivedTask) {
        if (derived == null)
            derived = Global.newArrayList(1);

        if (!derived.add(derivedTask)) {
            if (Global.DEBUG && Global.PRINT_DUPLICATE_DERIVATIONS) {
                System.err.println(
                        new RuntimeException("duplicate derivation: " + derivedTask)
                );
            }
        }
    }

    public void input(NAR nar) {
        get().forEach(nar::input);
    }

}
