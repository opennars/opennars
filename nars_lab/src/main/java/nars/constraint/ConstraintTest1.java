/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nars.constraint;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.set.SetConstraintsFactory;
import org.chocosolver.solver.search.strategy.SetStrategyFactory;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.VariableFactory;

/**
 *
 * @author me
 */
public class ConstraintTest1 extends AbstractProblem {


    private SetVar x, y, z, universe;
    private IntVar sum;
    private boolean noEmptySet = true;

    public static void main(String[] args) {
        new ConstraintTest1().execute(args);
    }

    @Override
    public void createSolver() {
		solver = new Solver("set union sample");
    }

    @Override
    public void buildModel() {

		///////////////
		// VARIABLES //
		///////////////

		// x initial domain
		int[] x_envelope = new int[]{1,3,2,8}; // not necessarily ordered
		int[] x_kernel = new int[]{1};
		x = VariableFactory.set("x", x_envelope, x_kernel, solver);
		// y initial domain
		int[] y_envelope = new int[]{2,6,7};
		y = VariableFactory.set("y", y_envelope, solver);
		// z initial domain
		int[] z_envelope = new int[]{2,1,3,5,7,12};
		int[] z_kernel = new int[]{2};
		z = VariableFactory.set("z", z_envelope, z_kernel, solver);
		// universe initial domain (note that the universe is a variable)
		int[] universe_envelope = new int[]{1,2,3,5,7,8,42};
		universe = VariableFactory.set("universe", universe_envelope, solver);
		// sum variable
		sum = VariableFactory.bounded("sum of universe", 12, 19, solver);

		/////////////////
		// CONSTRAINTS //
		/////////////////

		// partition constraint
		solver.post(SetConstraintsFactory.partition(new SetVar[]{x, y, z}, universe));
		if (noEmptySet) {
			// forbid empty sets
			solver.post(SetConstraintsFactory.nbEmpty(new SetVar[]{x, y, z, universe}, VariableFactory.fixed(0, solver)));
		}
		// restricts the sum of elements in universe
		solver.post(SetConstraintsFactory.sum(universe, sum, true));
    }

    @Override
    public void configureSearch() {
		// set a search strategy
		solver.set(SetStrategyFactory.force_first(x, y, z, universe));
    }

    @Override
    public void solve() {
		// find the optimum
		solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, sum);
    }

    @Override
    public void prettyOut() {
        System.out.println("best solution found");
        System.out.println(x);
        System.out.println(y);
        System.out.println(z);
		System.out.println(universe);
        System.out.println(sum);
    }

}    

