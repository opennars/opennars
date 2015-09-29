package org.clockwise;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * Extended interface for asynchronous {@link TaskExecutor} implementations,
 * offering an overloaded {@link #execute(Runnable, long)} variant with a start
 * timeout parameter as well support for {@link java.util.concurrent.Callable}.
 * 
 * <p>
 * Note: The {@link java.util.concurrent.Executors} class includes a set of
 * methods that can convert some other common closure-like objects, for example,
 * {@link java.security.PrivilegedAction} to {@link Callable} before executing
 * them.
 * 
 * <p>
 * Implementing this interface also indicates that the
 * {@link #execute(Runnable)} method will not execute its Runnable in the
 * caller's thread but rather asynchronously in some other thread.
 * 
 * @author Juergen Hoeller
 * @since 2.0.3
 * @see SimpleAsyncTaskExecutor
 * @see org.springframework.scheduling.SchedulingTaskExecutor
 * @see java.util.concurrent.Callable
 * @see java.util.concurrent.Executors
 */
public interface AsyncTaskExecutor extends TaskExecutor {
    /** Constant that indicates immediate execution */
    long TIMEOUT_IMMEDIATE = 0;
    /** Constant that indicates no time limit */
    long TIMEOUT_INDEFINITE = Long.MAX_VALUE;

    /**
     * Execute the given {@code task}.
     * 
     * @param task
     *            the {@code Runnable} to execute (never {@code null})
     * @param startTimeout
     *            the time duration (milliseconds) within which the task is
     *            supposed to start. This is intended as a hint to the executor,
     *            allowing for preferred handling of immediate tasks. Typical
     *            values are {@link #TIMEOUT_IMMEDIATE} or
     *            {@link #TIMEOUT_INDEFINITE} (the default as used by
     *            {@link #execute(Runnable)}).
     * @throws TaskTimeoutException
     *             in case of the task being rejected because of the timeout
     *             (i.e. it cannot be started in time)
     * @throws TaskRejectedException
     *             if the given task was not accepted
     */
    void execute(Runnable task, long startTimeout);

    /**
     * Submit a Runnable task for execution, receiving a Future representing
     * that task. The Future will return a {@code null} result upon completion.
     * 
     * @param task
     *            the {@code Runnable} to execute (never {@code null})
     * @return a Future representing pending completion of the task
     * @throws TaskRejectedException
     *             if the given task was not accepted
     * @since 3.0
     */
    Future<?> submit(Runnable task);

    /**
     * Submit a Callable task for execution, receiving a Future representing
     * that task. The Future will return the Callable's result upon completion.
     * 
     * @param task
     *            the {@code Callable} to execute (never {@code null})
     * @return a Future representing pending completion of the task
     * @throws TaskRejectedException
     *             if the given task was not accepted
     * @since 3.0
     */
    <T> Future<T> submit(Callable<T> task);
}