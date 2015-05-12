package objenome.solver;

import objenome.solver.evolve.TypedOrganism;

/**
 * Civliziation evolutionary objective
 */
abstract public class EGoal<I> {

    public final String id;

    public EGoal(String id) {
        this.id = id;
    }

    abstract public double cost(I o);

    @Override
    public String toString() {
        return id;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return id.equals(((EGoal)obj).id);
    }
}
