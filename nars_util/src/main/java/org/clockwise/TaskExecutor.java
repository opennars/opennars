package org.clockwise;

import java.util.concurrent.Executor;

/**
 * Simple task executor interface that abstracts the execution of a
 * {@link Runnable}.
 * 
 * <p>
 * Implementations can use all sorts of different execution strategies, such as:
 * synchronous, asynchronous, using a thread pool, and more.
 * 
 * <p>
 * Equivalent to JDK 1.5's {@link java.util.concurrent.Executor} interface;
 * extending it now in Spring 3.0, so that clients may declare a dependency on
 * an Executor and receive any TaskExecutor implementation. This interface
 * remains separate from the standard Executor interface mainly for backwards
 * compatibility with JDK 1.4 in Spring 2.x.
 */
interface TaskExecutor extends Executor {

    /**
     * Execute the given {@code task}.
     * <p>
     * The call might return immediately if the implementation uses an
     * asynchronous execution strategy, or might block in the case of
     * synchronous execution.
     * 
     * @param task
     *            the {@code Runnable} to execute (never {@code null})
     * @throws TaskRejectedException
     *             if the given task was not accepted
     */
    @Override
    void execute(Runnable task);
}
