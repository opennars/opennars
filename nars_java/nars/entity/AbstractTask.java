package nars.entity;

/**
 * Base class for all perception commands and Task's
 */
abstract public class AbstractTask<K> extends Item<K> {

    public AbstractTask() {
        super(null);
    }
    
    public AbstractTask(BudgetValue b) {
        super(b);
    }

    
}
