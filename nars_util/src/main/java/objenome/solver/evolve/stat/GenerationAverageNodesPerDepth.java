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

import objenome.solver.evolve.Organism;
import objenome.solver.evolve.Population;
import objenome.solver.evolve.TypedOrganism;
import objenome.solver.evolve.event.GenerationEvent.EndGeneration;
import objenome.solver.evolve.event.stat.AbstractStat;

import java.util.Arrays;

/**
 * A stat that returns the average number of nodes at each depth level of the
 * program trees in the population from the previous generation. All individuals
 * in the population must be instances of <code>STGPIndividual</code>.
 *
 * @since 2.0
 */
public class GenerationAverageNodesPerDepth extends AbstractStat<EndGeneration> {

    private double[] averages;

    /**
     * Constructs a <code>GenerationAverageNodesPerDepth</code> stat and
     * registers its dependencies
     */
    public GenerationAverageNodesPerDepth() {
        super(GenerationAverageDepth.class);
    }

    /**
     * Triggers the generation of an updated value for this stat. Once this stat
     * has been registered, this method will be called on each
     * <code>EndGeneration</code> event.
     *
     * @param event an object that encapsulates information about the event that
     * occurred
     */
    @Override
    public void refresh(EndGeneration event) {
        int maxDepth = getConfig().the(GenerationMaximumDepth.class).getMaximum();
        Population<?> population = event.getPopulation();

        averages = new double[maxDepth];

        // For each depth calculate an average
        for (int d = 0; d < maxDepth; d++) {
            // Get number of nodes for each program
            int noNodes = 0;
            for (Organism individual : population) {
                if (individual instanceof TypedOrganism) {
                    noNodes += ((TypedOrganism) individual).getRoot().nodesAtDepth(d).size();
                }
            }
            averages[d] = noNodes / (double) population.size();
        }
    }

    /**
     * Returns an array containing the average number of nodes at each depth
     * level across all the programs in the previous generation
     *
     * @return the error of the mean depth of the program trees
     */
    public double[] getAverageNodesPerDepth() {
        return averages;
    }

    /**
     * Returns a string representation of the value of this stat
     *
     * @return a <code>String</code> that represents the value of this stat
     */
    @Override
    public String toString() {
        return Arrays.toString(averages);
    }
}
