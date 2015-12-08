package nars.nal.nal8.decide;

import nars.task.Task;

import java.util.function.Predicate;

/**
 * A method of deciding if an execution should proceed.
 */
public interface Decider extends Predicate<Task> {
}
