package nars.entity;

import nars.entity.Item.StringKeyItem;

/**
 * Base class for all perception commands and Task's
 */
abstract public class AbstractTask extends StringKeyItem {

    public AbstractTask() {
        super(null);
    }
    
    public AbstractTask(BudgetValue b) {
        super(b);
    }

    
}
