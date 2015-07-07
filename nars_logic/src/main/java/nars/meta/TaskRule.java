package nars.meta;

import nars.nal.Premise;
import nars.nal.nal4.Product;
import nars.task.Task;
import nars.term.Term;

/**
 * A rule which produces a Task
 */
public class TaskRule extends Rule<Premise,Task> {


    public TaskRule(Product premises, Term result) {
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

    public Term result() {
        return term(1);
    }

    public int premiseCount() {
        return premise().length();
    }


}
