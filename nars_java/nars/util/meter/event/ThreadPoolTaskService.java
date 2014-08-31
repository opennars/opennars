/* Copyright 2009 - 2010 The Stajistics Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nars.util.meter.event;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import nars.util.meter.key.StatsProperties;
import nars.util.meter.util.ServiceLifeCycle.Support;

/**
 * @author The Stajistics Project
 */
public class ThreadPoolTaskService implements TaskService {

    private static final String PROP_CORE_POOL_SIZE = ThreadPoolTaskService.class.getName() + ".corePoolSize";

    private static final String PROP_MAX_POOL_SIZE = ThreadPoolTaskService.class.getName() + ".maxPoolSize";
    private static final int DEFAULT_MAX_POOL_SIZE = 20;

    private static final String PROP_KEEP_ALIVE_TIME_SECONDS = ThreadPoolTaskService.class.getName() + ".keepAliveTimeSeconds";
    private static final int DEFAULT_KEEP_ALIVE_TIME_SECONDS = 300;

    private static final String PROP_QUEUE_SIZE = ThreadPoolTaskService.class.getName() + ".queueSize";
    private static final int DEFAULT_QUEUE_SIZE = 10;

    // TODO: persist and restore upon de/serialization
    private transient final ThreadPoolExecutor executor;

    private final Support lifeCycleSupport = new Support();

    public ThreadPoolTaskService() {
        int noCPUs = Runtime.getRuntime().availableProcessors();
        int corePoolSize = StatsProperties.getIntegerProperty(PROP_CORE_POOL_SIZE,
                                                              noCPUs + 1);
        int maxPoolSize = StatsProperties.getIntegerProperty(PROP_MAX_POOL_SIZE,
                                                             Math.max(corePoolSize, DEFAULT_MAX_POOL_SIZE));

        long keepAliveTime = StatsProperties.getIntegerProperty(PROP_KEEP_ALIVE_TIME_SECONDS,
                                                                DEFAULT_KEEP_ALIVE_TIME_SECONDS);
        TimeUnit keepAliveTimeUnit = TimeUnit.SECONDS;

        if (keepAliveTime < 0) {
            keepAliveTime = Long.MAX_VALUE;
            keepAliveTimeUnit = TimeUnit.NANOSECONDS;
        }

        executor = new ThreadPoolExecutor(corePoolSize,
                                          maxPoolSize,
                                          keepAliveTime,
                                          keepAliveTimeUnit,
                                          createWorkQueue(),
                                          createThreadFactory());
    }

    public ThreadPoolTaskService(final ThreadPoolExecutor executor) {
        //assertNotNull(executor, "executor");
        this.executor = executor;
    }

    protected BlockingQueue<Runnable> createWorkQueue() {
        BlockingQueue<Runnable> queue;

        int queueSize = StatsProperties.getIntegerProperty(PROP_QUEUE_SIZE, DEFAULT_QUEUE_SIZE);
        if (queueSize < 1) {
            queue = new LinkedBlockingQueue<Runnable>();
        } else {
            queue = new ArrayBlockingQueue<Runnable>(queueSize);
        }

        return queue;
    }

    protected ThreadFactory createThreadFactory() {
        return new TaskServiceThreadFactory();
    }

    @Override
    public void initialize() {
        lifeCycleSupport.initialize(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                executor.prestartCoreThread();
                return null;
            }
        });
    }

    @Override
    public boolean isRunning() {
        return lifeCycleSupport.isRunning();
    }

    @Override
    public void shutdown() {
        lifeCycleSupport.shutdown(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                executor.shutdown();
                return null;
            }
        });
    }

    @Override
    public <T> Future<T> submit(final Class<?> source,
                                final Callable<T> task) {
        return executor.submit(task);
    }

    @Override
    public void execute(Class<?> source, Runnable task) {
        executor.execute(task);
    }
    
/**
 * @author The Stajistics Project
 */
protected static class TaskServiceThreadFactory implements ThreadFactory {

    private static final Logger logger = Logger.getLogger(TaskServiceThreadFactory.class.toString());

    private static final String PROP_THREAD_PRIORITY = TaskServiceThreadFactory.class.getName() + ".threadPriority";
    private static final int DEFAULT_THREAD_PRIORITY = Thread.NORM_PRIORITY;

    private static final AtomicInteger nextPoolId = new AtomicInteger(0);
    private final AtomicInteger nextThreadId = new AtomicInteger(0);

    private final String namePrefix;
    private final ThreadGroup threadGroup;

    public TaskServiceThreadFactory() {
        namePrefix = TaskService.class.getSimpleName() + "-" + nextPoolId.incrementAndGet();
        threadGroup = new ThreadGroup(namePrefix);
    }

    public ThreadGroup getThreadGroup() {
        return threadGroup;
    }

    @Override
    public Thread newThread(final Runnable r) {

        //final String threadName = namePrefix + "-worker-" + nextThreadId.incrementAndGet();

        Runnable wrapper = new Runnable() {
            @Override
            public void run() {
                try {
                    //logger.debug("Thread started: {}", Thread.currentThread().getName());

                    r.run();
                } finally {
                    //logger.debug("Thread destroyed: {}", Thread.currentThread().getName());
                }
            }
        };

        Thread thread = new Thread(threadGroup, wrapper);//, threadName);

        int priority = StatsProperties.getIntegerProperty(PROP_THREAD_PRIORITY,
                                                          DEFAULT_THREAD_PRIORITY);
        thread.setPriority(priority);
        thread.setDaemon(true);

        return thread;
    }
}    
}
