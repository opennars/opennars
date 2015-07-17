package nars.meta;

import nars.premise.Premise;
import nars.nal.nal4.Product;
import nars.task.Task;

/**
 * A rule which produces a Task
 */
public class TaskRule extends Rule<Premise,Task> {


    public TaskRule(Product premises, Product result) {
        super(premises, result);
    }

    @Override
    public Task generate(Premise context) {
        //TODO
        return null;
    }


    public Product premise() {
        return (Product)term(0);
    }

    public Product result() {
        return (Product) term(1);
    }

    public int premiseCount() {
        return premise().length();
    }


}
