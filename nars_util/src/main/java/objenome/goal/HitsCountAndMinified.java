package objenome.goal;

import objenome.solver.evolve.TypedOrganism;

/**
 * Created by me on 5/6/15.
 */
public class HitsCountAndMinified extends HitsCount {

    @Override
    public double getCost(TypedOrganism program) {
        double d = super.getCost(program);

        //if fully matched, then apply a bonus for shorter programs
        //longer programs will be reduced by a lesser amount, favoring shorter ones:
        if (d == 0) {
            d -= 1.0 / (1.0 + program.size());
        }

        return d;
    }
}
