package nars.budget;

/**
 * Created by me on 3/24/15.
 */
public interface BudgetTarget {

    /** returns "change" that was reimbursed */
    public float receive(float amount);

}
