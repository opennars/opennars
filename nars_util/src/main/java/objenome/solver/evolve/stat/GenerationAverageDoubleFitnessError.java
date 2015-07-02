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
package objenome.solver.evolve.stat;

import objenome.goal.DoubleFitness;
import objenome.solver.evolve.Population;
import objenome.solver.evolve.event.GenerationEvent.EndGeneration;
import objenome.solver.evolve.event.stat.AbstractStat;

/**
 * Stat that provides the average fitness error of the population at the end of
 * a generation. This stat can only be used with <code>DoubleFitness</code>.
 *
 * @see DoubleFitness
 */
public class GenerationAverageDoubleFitnessError extends AbstractStat<EndGeneration> {

    /**
     * The average fitness error.
     */
    private double error;

    /**
     * Constructs a <code>GenerationAverageDoubleFitnessError</code>.
     */
    public GenerationAverageDoubleFitnessError() {
        super(GenerationStandardDeviationDoubleFitness.class);
    }

    /**
     * Computes the average fitness error of the population.
     *
     * @param event the <code>EndGeneration</code> event object.
     */
    @Override
    public void refresh(EndGeneration event) {
        double stdev = getConfig().the(GenerationStandardDeviationDoubleFitness.class).getStandardDeviation();
        Population population = event.getPopulation();

        error = stdev / Math.sqrt(population.size());
    }

    /**
     * Returns the average fitness error.
     *
     * @return the average fitness error.
     */
    public double getError() {
        return error;
    }

    /**
     * Returns a string representation of the average fitness error.
     *
     * @return a string representation of the average fitness error.
     */
    @Override
    public String toString() {
        return Double.toString(error);
    }
}
