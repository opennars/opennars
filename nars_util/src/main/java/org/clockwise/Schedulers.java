package org.clockwise;

import org.clockwise.task.TriggerTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;

/**
 * The Class Schedulers.
 * 
 * @author <a href="mailto:janith3000@gmail.com">Janith Bandara</a>
 */
public class Schedulers {

    /** The no of schedullers. */
    private static int noOfSchedullers = 0;

    /** The no of running schedullers. */
    private static int noOfRunningSchedullers = 0;

    /** The current scheduler. */
    private TaskScheduler currentScheduler;

    /** The instance. */
    private static Schedulers instance;

    /** The running schedullers. */
    private final Map<String, ScheduledFuture<?>> runningSchedullers = new ConcurrentHashMap<>();

    
    /**
     * The Enum ExecutorType.
     * 
     * @author <a href="mailto:janith3000@gmail.com">Janith Bandara</a>
     */
    public enum ExecutorType {

        /** The managed. */
        DEFAULT,
        /** The single threaded. */
        THREADED
    }

    /**
     * New default.
     * 
     * @return the schedulers
     */
    public static Schedulers newDefault() {
        return newThreadPoolScheduler(4);
//        createSingleton();
//        instance.increaseNoOfSchedullers();
//        instance.setCurrentScheduler(new ConcurrentTaskScheduler());
//        return instance;
    }

    /**
     * New thread pool scheduler.
     * 
     * @param poolSize
     *            the pool size
     * @return the schedulers
     */
    public static Schedulers newThreadPoolScheduler(int poolSize) {
        createSingleton();
        instance.increaseNoOfSchedullers();
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        CustomizableThreadFactory factory = new CustomizableThreadFactory();
        scheduler.initializeExecutor(factory, (r, executor) -> System.err.println(scheduler + " rejected: " + r));
        scheduler.setPoolSize(poolSize);
        instance.setCurrentScheduler(scheduler);
        return instance;
    }

    /**
     * Stop.
     * 
     * @param schedulerId
     *            the scheduler id
     * @return the schedulers
     */
    public static Schedulers stop(String schedulerId) {
        createSingleton();
        if (instance.runningSchedullers.isEmpty()) {
            throw new IllegalStateException("No schedulers available.");
        }

        ScheduledFuture<?> future = instance.runningSchedullers.get(schedulerId);
        if (future == null) {
            throw new IllegalStateException("No schedulers match with given id.");
        }
        future.cancel(true);
        instance.decreaseNoOfSchedullers();
        instance.decreaseNoOfRunningSchedullers();

        return instance;
    }

    /**
     * Stop all.
     * 
     * @return the schedulers
     */
    public static Schedulers stopAll() {
        createSingleton();
        if (instance.runningSchedullers.isEmpty()) {
            throw new IllegalStateException("No schedulers available.");
        }

        for (Entry<String, ScheduledFuture<?>> entry : instance.runningSchedullers.entrySet()) {
            ScheduledFuture<?> future = entry.getValue();
            future.cancel(true);
            instance.decreaseNoOfSchedullers();
            instance.decreaseNoOfRunningSchedullers();
        }

        return instance;
    }

    /**
     * Schedule.
     * 
     * @param task
     *            the task
     * @return the string
     */
    public String schedule(TriggerTask task) {
        if (getCurrentScheduler() == null) {
            throw new IllegalStateException("New scheduler should be crea");
        }
        ScheduledFuture<?> future = getCurrentScheduler().schedule(task.getRunnable(), task.getTrigger());
        String id = getUUID();
        runningSchedullers.put(id, future);
        increaseNoOfRunningSchedullers();
        setCurrentScheduler(null);
        return id;
    }

    /**
     * Schedule.
     *
     * @param task the task
     * @param trigger the trigger
     * @return the string
     */
    public String schedule(Runnable task, Trigger trigger) {
        ScheduledFuture<?> future = getCurrentScheduler().schedule(task, trigger);
        String id = getUUID();
        runningSchedullers.put(id, future);
        increaseNoOfRunningSchedullers();
        setCurrentScheduler(null);
        return id;
    }
    
    /**
     * Task registry.
     *
     * @return the task registrar
     */
    public static TaskRegistrar taskRegistry(){
        return TaskRegistrar.getInstance();
    }

    /**
     * Increase no of schedullers.
     */
    private synchronized void increaseNoOfSchedullers() {
        noOfSchedullers++;
    }

    /**
     * Increase no of running schedullers.
     */
    private synchronized void increaseNoOfRunningSchedullers() {
        noOfRunningSchedullers++;
    }

    /**
     * Decrease no of schedullers.
     */
    private synchronized void decreaseNoOfSchedullers() {
        noOfSchedullers--;
    }

    /**
     * Decrease no of running schedullers.
     */
    private synchronized void decreaseNoOfRunningSchedullers() {
        noOfRunningSchedullers--;
    }

    /**
     * Gets the uuid.
     * 
     * @return the uuid
     */
    private synchronized String getUUID() {
        return String.valueOf(UUID.randomUUID().getLeastSignificantBits());
    }

    /**
     * Gets the created scheduler count.
     * 
     * @return the created scheduler count
     */
    public static int getCreatedSchedulerCount() {
        return noOfSchedullers;
    }

    /**
     * Gets the running scheduler count.
     * 
     * @return the running scheduler count
     */
    public static int getRunningSchedulerCount() {
        return noOfRunningSchedullers;
    }

    /**
     * Creates the singleton.
     * 
     * @return the schedulers
     */
    private static Schedulers createSingleton() {
        synchronized (Schedulers.class) {
            if (instance == null) {
                instance = new Schedulers();
            }
        }
        return instance;
    }

    /**
     * Gets the current scheduler.
     * 
     * @return the current scheduler
     */
    private TaskScheduler getCurrentScheduler() {
        return currentScheduler;
    }

    /**
     * Sets the current scheduler.
     * 
     * @param currentScheduler
     *            the new current scheduler
     */
    private void setCurrentScheduler(TaskScheduler currentScheduler) {
        this.currentScheduler = currentScheduler;
    }
    
    /**
     * The Class TaskRegistrar.
     *
     * @author <a href="mailto:janith3000@gmail.com">Janith Bandara</a>
     */
    public static class TaskRegistrar {
        
        /** The trigger tasks. */
        final List<TriggerTask> triggerTasks = new CopyOnWriteArrayList<>();
        
        /** The running ids. */
        final List<String> runningIds = new ArrayList<>();
        
        /** The reg instance. */
        private static TaskRegistrar regInstance;
        
        /**
         * Registor.
         *
         * @param task the task
         * @return the task registrar
         */
        public TaskRegistrar registor(TriggerTask task) {
            triggerTasks.add(task);
            return regInstance;
        }
        
        /**
         * Schedule all.
         *
         * @return the list
         */
        public List<String> scheduleAll(){
            for (TriggerTask task : triggerTasks) {
               String id =  newDefault().schedule(task);
               runningIds.add(id);
            }
            triggerTasks.clear();
            return runningIds;
        }
        
        /**
         * Gets the single instance of TaskRegistrar.
         *
         * @return single instance of TaskRegistrar
         */
        public static TaskRegistrar getInstance() {
            synchronized (TaskRegistrar.class) {
                if (regInstance == null) {
                    regInstance = new TaskRegistrar();
                }
            }
            return regInstance;
        }
    }
}
