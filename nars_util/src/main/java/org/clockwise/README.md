# Clockwise
Lightweight Java task scheduler with fluent API, inspired by Spring framework scheduler.

Clockwise is initially developed for [Audit4j](http://audit4j.org) - *An open source auditing framework*.

Please visit the [FAQ](https://github.com/janithb/Clockwise/wiki/FAQ) page for more information.

Quick Guide:

```java

String id = Schedulers.newDefault().schedule(new PrintTask(), new PeriodicTrigger(1000)); // (1)

System.out.println(id); // (2)

String id2 = Schedulers.newDefault().schedule(new PrintTask(), new CronTrigger("0/2 * * * * *")); // (3)

System.out.println(id2); // (4)

System.out.println(Schedulers.getRunningSchedulerCount()); // (5)

TimeUnit.SECONDS.sleep(5); // (6)

Schedulers.stop(id2); // (7)

System.out.println(Schedulers.getRunningSchedulerCount()); // (8)

TimeUnit.SECONDS.sleep(5); // (9)

Schedulers.stopAll(); // (10)

System.out.println("Executing threadpool scheduler");
String id3 = Schedulers.newThreadPoolScheduler(4).schedule(new PrintTask(), new PeriodicTrigger(4000)); // (11)

System.out.println(id3); // (12)

TimeUnit.SECONDS.sleep(40); // (13)

Schedulers.stopAll(); // (14)

//Registor tasks.
Schedulers.taskRegistry().registor(new TriggerTask(new PrintTask(), new PeriodicTrigger(1000))).registor(
    new TriggerTask(new PrintTask(), new PeriodicTrigger(3000))); // (15)
        
List<String> jobIds = Schedulers.taskRegistry().scheduleAll(); // (16)
        


public class PrintTask implements Runnable {
    @Override
    public void run() {
        System.out.println("Executed");
    }
}

```

1. Scheduler 1: Schedules a task for every one second.
2. Prints the scheduler id.
3. Scheduler 2 Schedules an another task for every two second. This uses Cron expression.
4. Prints the scheduler 2 id.
5. Prints the running scheduler counts. This will prints '2'
6. Sleep for five seconds
7. Stops the scheduler 2
8. Prints the running scheduler counts. This will prints '1'
9. Sleep for five seconds
10. Stop all running schedulers.
11. Scheduler 3: Schedules an another task for every four seconds. The executor is created using thread pool and 4 threads used.
12. Prints the scheduler 3 id.
13. Sleep for fourty seconds.
14. Stop all running schedulers.
15. Register trigger tasks.
16. Schedule all registered tasks.
