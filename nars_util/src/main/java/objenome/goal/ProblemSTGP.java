/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package objenome.goal;

import com.google.common.collect.Lists;
import objenome.solver.evolve.BranchedBreeder;
import objenome.solver.evolve.FitnessEvaluator;
import objenome.solver.evolve.FitnessFunction;
import objenome.solver.evolve.GPContainer;
import objenome.solver.evolve.GenerationalStrategy;
import objenome.solver.evolve.Initialiser;
import objenome.solver.evolve.PopulationProcess;
import objenome.solver.evolve.STGPIndividual;

/**
 * Static-typed Problem solvable by Evolution
 */
public abstract class ProblemSTGP extends GPContainer<STGPIndividual> {
    /**
     * The key for setting <code>Template</code> parameter.
     */
    public static final GPKey<ProblemSTGP> PROBLEM = new GPKey<>();
    /**
     * The key -&gt; value mapping.
     */
    //private final HashMap<GPKey<?>, Object> properties = new HashMap<GPKey<?>, Object>(1);
    
    //private GPContainer config;

    /**
     * Constructs a new <code>Template</code>.
     */
    public ProblemSTGP() {
        super();


        FitnessEvaluator fe = new FitnessEvaluator();

        the(COMPONENTS, Lists.newArrayList(new PopulationProcess[] {
            new Initialiser(),
            new GenerationalStrategy(fe, new BranchedBreeder())
        }));
        
    }
//   public ProblemSTGP(FitnessFunction f) {
//        super();
//
//        the(COMPONENTS, Lists.newArrayList(new PopulationProcess[] {
//            new Initialiser(),
//            new GenerationalStrategy(
//                    new BranchedBreeder(),
//                    new FitnessEvaluator(f))
//        }));
//
//    }

}
