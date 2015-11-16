package nars.task;


public interface Tasked {
    public Task getTask();

    static Task the(Object v) {
        if (v instanceof Tasked)
            return ((Tasked)v).getTask();
        return null;
    }
}
