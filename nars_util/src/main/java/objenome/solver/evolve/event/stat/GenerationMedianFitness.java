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
package objenome.solver.evolve.event.stat;

import objenome.solver.evolve.Fitness;
import objenome.solver.evolve.event.GenerationEvent.EndGeneration;

/**
 * Stat that provides the median fitness. If there are an even number of
 * programs in the population then there are two median values, the first will
 * be returned.
 */
public class GenerationMedianFitness extends AbstractStat<EndGeneration> {

    /**
     * The median fitness value.
     */
    private Fitness median;

    /**
     * Constructs a <code>GenerationMedianFitness</code>.
     */
    public GenerationMedianFitness() {
        super(GenerationFitnesses.Sorted.class);
    }

    /**
     * Determines the median fitness value.
     *
     * @param event the <code>EndGeneration</code> event object.
     */
    @Override
    public void refresh(EndGeneration event) {
        Fitness[] fitnesses = getConfig().the(GenerationFitnesses.Sorted.class).getFitnesses();

        int medianIndex = (int) Math.floor(fitnesses.length / 2.0f);
        median = fitnesses[medianIndex - 1];
    }

    /**
     * Returns the median fitness value.
     *
     * @return the median fitness value.
     */
    public Fitness getMedian() {
        return median;
    }

    /**
     * Returns a string representation of the median fitness value.
     *
     * @return a string representation of the median fitness value.
     */
    @Override
    public String toString() {
        return median.toString();
    }
}
