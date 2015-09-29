package org.clockwise;

import java.util.concurrent.RejectedExecutionException;

/**
 * Exception thrown when a {@link TaskExecutor} rejects to accept a given task
 * for execution.
 * 
 * @author Juergen Hoeller
 * @since 2.0.1
 * @see TaskExecutor#execute(Runnable)
 * @see TaskTimeoutException
 */
@SuppressWarnings("serial")
public class TaskRejectedException extends RejectedExecutionException {
    /**
     * Create a new {@code TaskRejectedException} with the specified detail
     * message and no root cause.
     * 
     * @param msg
     *            the detail message
     */
    public TaskRejectedException(String msg) {
        super(msg);
    }

    /**
     * Create a new {@code TaskRejectedException} with the specified detail
     * message and the given root cause.
     * 
     * @param msg
     *            the detail message
     * @param cause
     *            the root cause (usually from using an underlying API such as
     *            the {@code java.util.concurrent} package)
     * @see java.util.concurrent.RejectedExecutionException
     */
    public TaskRejectedException(String msg, Throwable cause) {
        super(msg, cause);
    }
}