package org.clockwise;

import org.clockwise.task.TriggerTask;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class SchedulerTest {

    public static void main(String[] args) throws InterruptedException {

        String id = Schedulers.newDefault().schedule(new TriggerTask(new PrintTask(), new PeriodicTrigger(1000)));

        System.out.println(id);

        String id2 = Schedulers.newDefault().schedule(new TriggerTask(new PrintTask(), new PeriodicTrigger(2000)));

        System.out.println(id2);

        System.out.println(Schedulers.getRunningSchedulerCount());

        TimeUnit.SECONDS.sleep(5);

        Schedulers.stop(id2);

        System.out.println(Schedulers.getRunningSchedulerCount());

        TimeUnit.SECONDS.sleep(5);

        Schedulers.stopAll();

        System.out.println("Executing threadpool");
        String id3 = Schedulers.newThreadPoolScheduler(4).schedule(
                new TriggerTask(new PrintTask(), new CronTrigger("0/4 * * * * *")));

        System.out.println(id3);

        TimeUnit.SECONDS.sleep(40);

        Schedulers.stopAll();
        
        Schedulers.taskRegistry().registor(new TriggerTask(new PrintTask(), new PeriodicTrigger(1000)));
        
        List<String> jobIds = Schedulers.taskRegistry().scheduleAll();
    }
}
