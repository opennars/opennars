package nars.meta;

import nars.nal.nal4.Product;
import nars.premise.Premise;
import nars.task.Task;
import nars.term.Atom;
import nars.term.Compound;
import nars.term.Term;
import nars.term.Variable;
import nars.term.transform.CompoundTransform;

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


    public static class TaskRuleNormalization implements CompoundTransform<Compound,Term> {

        @Override
        public boolean test(Term term) {
            if (term instanceof Atom)
                return (Character.isUpperCase( ((Atom)term).toString().charAt(0) ));
            return false;
        }

        @Override
        public Term apply(Compound containingCompound, Term v, int depth) {
            return new Variable("%" + v.toString());
        }
    }

    final static TaskRuleNormalization taskRuleNormalization = new TaskRuleNormalization();

    public TaskRule normalize() {
        return this.transform(taskRuleNormalization);
    }
}
