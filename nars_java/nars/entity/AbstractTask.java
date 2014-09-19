package nars.entity;

/**
 * Base class for all perception commands and Task's
 */
abstract public class AbstractTask extends Item {

    public AbstractTask() {
        super(null);
    }
    
    public AbstractTask(BudgetValue b) {
        super(b);
    }

    
}
