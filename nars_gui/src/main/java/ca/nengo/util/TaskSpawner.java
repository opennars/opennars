package ca.nengo.util;

/**
 * A node that uses ThreadTasks.
 * Provides a way to easily collect every task defined
 *
 * @author Jonathan Lai
 */
public interface TaskSpawner {

    /**
     * @return The ThreadTasks used by this Node
     */
    public ThreadTask[] getTasks();

    /**
     * @param tasks Sets the tasks of the spawner to this
     */
    public void setTasks(ThreadTask[] tasks);

    /**
     * @param tasks Adds the this to the tasks of the spawner
     */
    public void addTasks(ThreadTask[] tasks);
}
