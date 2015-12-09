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
import objenome.solver.evolve.Fitness;
import objenome.solver.evolve.event.GenerationEvent.EndGeneration;
import objenome.solver.evolve.event.stat.AbstractStat;
import objenome.solver.evolve.event.stat.GenerationFitnesses;

/**
 * Stat that provides the standard deviation fitness value of the population at
 * the end of a generation. This stat can only be used with
 * <code>DoubleFitness</code>.
 *
 * @see DoubleFitness
 */
public class GenerationStandardDeviationDoubleFitness extends AbstractStat<EndGeneration> {

    /**
     * The standard deviation fitness value.
     */
    private double stdev;

    /**
     * Constructs a <code>GenerationStandardDeviationDoubleFitness</code>.
     */
    @SuppressWarnings("unchecked")
    public GenerationStandardDeviationDoubleFitness() {
        super(GenerationFitnesses.class, GenerationAverageDoubleFitness.class);
    }

    /**
     * Computes the standard deviation fitness value.
     *
     * @param event the <code>EndGeneration</code> event object.
     */
    @Override
    public void refresh(EndGeneration event) {
        Fitness[] fitnesses = getConfig().the(GenerationFitnesses.class).getFitnesses();
        double average = getConfig().the(GenerationAverageDoubleFitness.class).getAverage();

        // Sum the squared differences.
        double sqDiff = 0;
        for (Fitness fitness : fitnesses) {
            sqDiff += Math.pow(((DoubleFitness) fitness).getValue() - average, 2);
        }

        // Take the square root of the average.
        stdev = Math.sqrt(sqDiff / fitnesses.length);
    }

    /**
     * Returns the standard deviation fitness value.
     *
     * @return the standard deviation fitness value.
     */
    public double getStandardDeviation() {
        return stdev;
    }

    /**
     * Returns a string representation of the standard deviation fitness value.
     *
     * @return a string representation of the standard deviation fitness value.
     */
    @Override
    public String toString() {
        return Double.toString(stdev);
    }
}
