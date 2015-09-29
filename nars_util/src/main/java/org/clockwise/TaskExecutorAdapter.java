package org.clockwise;

import java.util.concurrent.*;


/**
 * Adapter that takes a JDK {@code java.util.concurrent.Executor} and exposes a
 * Spring {@link org.audit4j.schedule.springframework.core.task.TaskExecutor} for it. Also
 * detects an extended {@code java.util.concurrent.ExecutorService}, adapting
 * the {@link org.audit4j.schedule.springframework.core.task.AsyncTaskExecutor} interface
 * accordingly.
 * 
 * @author Juergen Hoeller
 * @since 3.0
 * @see java.util.concurrent.Executor
 * @see java.util.concurrent.ExecutorService
 * @see java.util.concurrent.Executors
 */
public class TaskExecutorAdapter implements AsyncTaskExecutor {
    private final Executor concurrentExecutor;

    /**
     * Create a new TaskExecutorAdapter, using the given JDK concurrent
     * executor.
     * 
     * @param concurrentExecutor
     *            the JDK concurrent executor to delegate to
     */
    public TaskExecutorAdapter(Executor concurrentExecutor) {
        // Assert.notNull(concurrentExecutor, "Executor must not be null");
        this.concurrentExecutor = concurrentExecutor;
    }

    /**
     * Delegates to the specified JDK concurrent executor.
     * 
     * @see java.util.concurrent.Executor#execute(Runnable)
     */
    @Override
    public void execute(Runnable task) {
        try {
            this.concurrentExecutor.execute(task);
        } catch (RejectedExecutionException ex) {
            throw new TaskRejectedException("Executor [" + this.concurrentExecutor + "] did not accept task: " + task,
                    ex);
        }
    }

    @Override
    public void execute(Runnable task, long startTimeout) {
        execute(task);
    }

    @Override
    public Future<?> submit(Runnable task) {
        try {
            if (this.concurrentExecutor instanceof ExecutorService) {
                return ((ExecutorService) this.concurrentExecutor).submit(task);
            } else {
                FutureTask<Object> future = new FutureTask<>(task, null);
                this.concurrentExecutor.execute(future);
                return future;
            }
        } catch (RejectedExecutionException ex) {
            throw new TaskRejectedException("Executor [" + this.concurrentExecutor + "] did not accept task: " + task,
                    ex);
        }
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        try {
            if (this.concurrentExecutor instanceof ExecutorService) {
                return ((ExecutorService) this.concurrentExecutor).submit(task);
            } else {
                FutureTask<T> future = new FutureTask<>(task);
                this.concurrentExecutor.execute(future);
                return future;
            }
        } catch (RejectedExecutionException ex) {
            throw new TaskRejectedException("Executor [" + this.concurrentExecutor + "] did not accept task: " + task,
                    ex);
        }
    }
}
