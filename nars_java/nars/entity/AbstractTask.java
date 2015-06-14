package nars.entity;

/**
 * Base class for all perception commands and Task's
 */
abstract public class AbstractTask extends Item {

    public AbstractTask() {
        super();
    }
    
    public AbstractTask(BudgetValue b) {
        super(b);
    }

    
}
