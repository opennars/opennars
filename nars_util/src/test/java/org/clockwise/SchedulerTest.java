package org.clockwise;

import org.clockwise.task.TriggerTask;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class SchedulerTest {

    @Test
    public void test1() throws InterruptedException {

        String id = Schedulers.newDefault().schedule(new TriggerTask(new NullTask(), new PeriodicTrigger(17)));

        //System.out.println(id);

        String id2 = Schedulers.newDefault().schedule(new TriggerTask(new NullTask(), new PeriodicTrigger(34)));
        assertNotEquals(id, id2);

        //System.out.println(Schedulers.getRunningSchedulerCount());
        assertEquals(2, Schedulers.getRunningSchedulerCount());

        TimeUnit.SECONDS.sleep(1);

        Schedulers.stop(id2);

        assertEquals(1, Schedulers.getRunningSchedulerCount());

        TimeUnit.SECONDS.sleep(1);

        Schedulers.stopAll();

        //System.out.println("Executing threadpool");
        String id3 = Schedulers.newDefault().schedule(
                new TriggerTask(new NullTask(), new CronTrigger("0/2 * * * * *")));

        //System.out.println(id3);

        TimeUnit.SECONDS.sleep(1);

        Schedulers.stopAll();
        
        Schedulers.taskRegistry().registor(new TriggerTask(new NullTask(), new PeriodicTrigger(17)));
        
        List<String> jobIds = Schedulers.taskRegistry().scheduleAll();

        //System.out.println(jobIds);

        //TODO add better test conditions
        assertTrue(true);
    }

    static class NullTask implements Runnable {

        @Override
        public void run() {
            Thread.yield();
            //System.out.println("Executed");
        }

    }
}
