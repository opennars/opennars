/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.process;

import nars.NAR;
import nars.premise.Premise;
import nars.task.Task;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Stream;

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
public abstract class NAL extends AbstractPremise  {

    /** derivation queue (this might also work as a Set) */
    protected Collection<Task> derived = null;

    public NAL(final NAR n) {
        super(n);
    }


    abstract public Stream<Task> derive(Function<Premise,Stream<Task>> processor);



//    @Override public void accept(Task derivedTask) {
//        if (derived == null)
//            derived = Global.newArrayList();
//                    //Global.newHashSet(1);
//
//        if (!derived.add(derivedTask)) {
//            if (Global.DEBUG && Global.PRINT_DUPLICATE_DERIVATIONS) {
//                System.err.println(
//                        new RuntimeException("duplicate derivation: " + derivedTask)
//                );
//            }
//        }
//    }

//    public void input(NAR nar, Consumer<Premise> premiseProcessor) {
//        apply(premiseProcessor).forEach(nar::input);
//    }
//    public void input(NAR nar, Consumer<Premise> premiseProcessor, Function<Task,Task> postfilter) {
//        apply(premiseProcessor).stream().map(postfilter).forEach(nar::input);
//    }
//    public Collection<Task> getDerived(Function<Premise,Stream<Task>> premiseProcessor) {
//        return apply(premiseProcessor);
//    }

}
