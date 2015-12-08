package nars.task;


import nars.term.compound.Compound;

@FunctionalInterface
public interface Tasked<T extends Compound> {

    Task<T> getTask();

    static Task the(Object v) {
        if (v instanceof Tasked)
            return ((Tasked)v).getTask();
        return null;
    }
}
