/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.process;

import nars.Global;
import nars.Memory;
import nars.nal.LogicPolicy;
import nars.premise.Premise;
import nars.task.Task;
import nars.task.TaskSeed;
import nars.task.stamp.Stamp;
import nars.term.Compound;
import nars.term.Terms;

import java.util.List;

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
public abstract class NAL implements Runnable, Premise {

    public final Memory memory;
    protected final Task currentTask;


    /** derivation queue (this might also work as a Set) */
    protected List<Task> derived = null;


    /**
     * stores the tasks that this process generates, and adds to memory
     */
    //protected SortedSet<Task> newTasks; //lazily instantiated
    public NAL(Memory mem, Task task) {
        this(mem, -1, task);
    }


    //TODO tasksDicarded

    /**
     * @param nalLevel the NAL level to limit processing of this reasoning context. set to -1 to use Memory's default value
     */
    public NAL(Memory mem, int nalLevel, Task task) {
        super();


        //setKey(getClass());

        memory = mem;

        if ((nalLevel != -1) && (nalLevel != mem.nal()))
            throw new RuntimeException("Different NAL level than Memory not supported yet");

        currentTask = task;

    }


    @Override public void run() {
        onStart();

        process();

        onFinished();

    }

    protected void onStart() {

    }

    protected void onFinished() {
        /** implement if necessary in subclasses */
    }


    protected abstract void process();




    @Override
    public Memory getMemory() {
        return memory;
    }




    /**
     * @return the currentTask
     */
    public Task getTask() {
        return currentTask;
    }


//
//    /**
//     * @return the newStamp
//     */
//    public Stamp getTheNewStamp() {
//        if (newStamp == null) {
//            //if newStamp==null then newStampBuilder must be available. cache it's return value as newStamp
//            newStamp = newStampBuilder.build();
//            newStampBuilder = null;
//        }
//        return newStamp;
//    }
//    public Stamp getTheNewStampForRevision() {
//        if (newStamp == null) {
//            if (newStampBuilder.overlapping()) {
//                newStamp = null;
//            }
//            else {
//                newStamp = newStampBuilder.build();
//            }
//            newStampBuilder = null;
//        }
//        return newStamp;
//    }
//
//    /**
//     * @param newStamp the newStamp to set
//     */
//    public Stamp setNextNewStamp(Stamp newStamp) {
//        this.newStamp = newStamp;
//        this.newStampBuilder = null;
//        return newStamp;
//    }
//
//    /**
//     * creates a lazy/deferred StampBuilder which only constructs the stamp if getTheNewStamp() is actually invoked
//     */
//    public void setNextNewStamp(final Stamp first, final Stamp second, final long time) {
//        newStamp = null;
//        newStampBuilder = new NewStampBuilder(first, second, time);
//    }

//    interface StampBuilder {
//
//        public Stamp build();
//
//        default public Stamp getFirst() { return null; }
//        default public Stamp getSecond(){ return null; }
//
//        default public boolean overlapping() {
//            /*final int stampLength = stamp.baseLength;
//            for (int i = 0; i < stampLength; i++) {
//                final long baseI = stamp.evidentialBase[i];
//                for (int j = 0; j < stampLength; j++) {
//                    if ((i != j) && (baseI == stamp.evidentialBase[j])) {
//                        throw new RuntimeException("Overlapping Revision Evidence: Should have been discovered earlier: " + Arrays.toString(stamp.evidentialBase));
//                    }
//                }
//            }*/
//
//            long[] a = getFirst().toSet();
//            long[] b = getSecond().toSet();
//            for (long ae : a) {
//                for (long be : b) {
//                    if (ae == be) return true;
//                }
//            }
//            return false;
//        }
//    }




    //    /**
//     * create a new stamp builder for a specific occurenceTime
//     */
//
//    public Stamper newStamp(Stamp a, Stamp b, long occurrenceTime) {
//        return new Stamper(a, b, time(), occurrenceTime);
//    }
//
//    public Stamper newStamp(Sentence a, long occurrenceTime) {
//        return newStamp(a, null, occurrenceTime);
//    }


//    /**
//     * create a new stamp builder with an occurenceTime determined by the parent sentence tenses.
//     *
//     * @param t generally the task's sentence
//     * @param b generally the belief's sentence
//     */
//    public Stamper newStamp(Stamp t, Stamp b) {
//
//        final long oc;
//        if (nal(7)) {
//            oc = inferOccurenceTime(t, b);
//        } else {
//            oc = Stamp.ETERNAL;
//        }
//
//        return new Stamper(t, b, time(), oc);
//    }
//
//    /**
//     * returns a new stamp if A and B do not have overlapping evidence; null otherwise
//     */
//    public Stamper newStampIfNotOverlapping(Sentence A, Sentence B) {
//        long[] a = A.getEvidentialSet();
//        long[] b = B.getEvidentialSet();
//        for (long ae : a) {
//            for (long be : b) {
//                if (ae == be) return null;
//                if (be > ae) break; //if be exceeds ae, it will never be equal so go to the next ae
//            }
//        }
//        return newStamp(A, B, A.getOccurrenceTime());
//    }
//public Stamper newStamp(Stamp stamp, long when) {
//
//    return new Stamper(stamp, null, time(), when);
//}
//
//    public Stamper newStamp(Stamp stamp, long when, long[] evidentialBase) {
//        return new Stamper(evidentialBase, time(), when, stamp.getDuration());
//    }
//
//    public Stamper newStamp(Task task, long time) {
//        return newStamp(task.getSentence(), time);
//    }
//
//    /**
//     * new stamp from one parent stamp, with occurence time = now
//     */
//    public Stamper newStampNow(Task task) {
//        return newStamp(task, time());
//    }





    public Task getBelief() {
        return null;
    }

    @Override public void queue(Task derivedTask) {
        if (derived == null)
            derived = Global.newArrayList(1);

        derived.add(derivedTask);
    }

}
