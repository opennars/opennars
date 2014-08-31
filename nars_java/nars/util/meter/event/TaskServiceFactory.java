package nars.util.meter.event;


/**
 * 
 *
 * @author The Stajistics Project
 */
public class TaskServiceFactory {

    private static final TaskServiceFactory INSTANCE = new TaskServiceFactory(); 

    private volatile TaskService taskService;

    private TaskServiceFactory() {}

    public static TaskServiceFactory getInstance() {
        return INSTANCE;
    }

    public void loadTaskService(final TaskService taskService) {
        this.taskService = taskService;
    }

    public boolean isTaskServiceLoaded() {
        return taskService != null;
    }

    public TaskService getTaskService() {
        if (taskService == null) {
            throw new IllegalStateException("A TaskService has not yet been loaded");
        }
        return taskService;
    }

}
