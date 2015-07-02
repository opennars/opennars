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
import objenome.solver.evolve.Organism;
import objenome.solver.evolve.Population;
import objenome.solver.evolve.event.GenerationEvent.EndGeneration;

import java.util.Arrays;

/**
 * Stat that provides the fitness values of a generation.
 */
public class GenerationFitnesses extends AbstractStat<EndGeneration> {

    /**
     * The fitness values.
     */
    private Fitness[] fitnesses;

    /**
     * Constructs a <code>GenerationFitnesses</code>.
     */
    public GenerationFitnesses() {
        super(NO_DEPENDENCIES);
    }

    /**
     * Determines the fitness values of the generation.
     *
     * @param event the <code>EndGeneration</code> event object.
     */
    @Override
    public void refresh(EndGeneration event) {
        Population<?> population = event.getPopulation();
        fitnesses = new Fitness[population.size()];
        int index = 0;

        for (Organism individual : population) {
            fitnesses[index++] = individual.getFitness();
        }
    }

    /**
     * Returns the fitness values.
     *
     * @return the fitness values.
     */
    public Fitness[] getFitnesses() {
        return fitnesses;
    }

    /**
     * Returns a string representation of the fitness values of a generation.
     *
     * @return a string representation of the fitness values of a generation.
     */
    @Override
    public String toString() {
        return Arrays.toString(fitnesses);
    }

    /**
     * Stat that provides the sorted (ascending order) fitness values of a
     * generation.
     */
    public class Sorted extends GenerationFitnesses {

        @Override
        public void refresh(EndGeneration event) {
            super.refresh(event);
            Arrays.sort(fitnesses);
        }
    }
}
