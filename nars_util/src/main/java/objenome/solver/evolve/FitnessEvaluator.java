/*
 * Copyright 2007-2013
 * Licensed under GNU Lesser General Public License
 * 
 * This file is part of EpochX
 * 
 * EpochX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * EpochX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with EpochX. If not, see <http://www.gnu.org/licenses/>.
 * 
 * The latest version is available from: http://www.epochx.org
 */
package objenome.solver.evolve;

import objenome.solver.evolve.GPContainer.GPKey;

/**
 * Implementations of <code>FitnessEvaluator</code> are components that are
 * responsible for assigning fitnesses to individuals in a population. Typically
 * this will be performed by evaluating the quality of each individual against
 * problem specific requirements.
 */
public class FitnessEvaluator<I extends Organism> extends ProxyComponent<FitnessFunction> implements PopulationProcess<I> {

    /**
     * The key for setting and retrieving the <code>FitnessFunction</code> used
     * by this component.
     */
    public static final GPKey<FitnessFunction> FUNCTION = new GPKey<>();
    private FitnessFunction function;

    /**
     * Constructs a <code>FitnessEvaluator</code>.
     */
    public FitnessEvaluator() {
        super(FUNCTION);
    }
    
    /** constructs an evaluator with a specific fitness function */
    public FitnessEvaluator(FitnessFunction f) {
        super(FUNCTION);
        function = f;
    }
    
    /**
     * Delegates the evaluation of the population to the
     * <code>FitnessFunction</code> object.
     */
    @Override
    public Population<I> process(Population<I> population) {
        if (config == null)
            setConfig(population.getConfig());
        
        if (function !=null) {
            function.evaluate(population);
        }
        else {
            FitnessFunction handler = getHandler();
            if (handler == null) {
                throw new IllegalStateException("The fitness function has not been set.");
            }
            handler.evaluate(population);
        }

        return population;
    }

}
