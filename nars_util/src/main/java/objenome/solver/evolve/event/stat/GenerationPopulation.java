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

import objenome.solver.evolve.Organism;
import objenome.solver.evolve.Population;
import objenome.solver.evolve.event.GenerationEvent.EndGeneration;

/**
 * Stat that provides the population of the current generation.
 */
public class GenerationPopulation extends AbstractStat<EndGeneration> {

    /**
     * The current population.
     */
    private Population<?> population;

    /**
     * Constructs a <code>GenerationPopulation</code>.
     */
    public GenerationPopulation() {
        super(NO_DEPENDENCIES);
    }

    /**
     * Stores the population of the current generation.
     *
     * @param event the <code>EndGeneration</code> event object.
     */
    @Override
    public void refresh(EndGeneration event) {
        population = event.getPopulation();
    }

    /**
     * Returns the population.
     *
     * @return the population.
     */
    public Population getPopulation() {
        return population;
    }

    /**
     * Returns a string representation of the generation population.
     *
     * @return a string representation of the generation population.
     */
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();

        boolean sep = false;
        for (Organism individual : population) {
            if (sep) {
                buffer.append(", ");
            }
            buffer.append(individual);
            sep = true;
        }

        return buffer.toString();
    }

    /**
     * Stat that provides the population (sorted into ascending order) of the
     * current generation.
     */
    public class Sorted extends GenerationFitnesses {

        @Override
        public void refresh(EndGeneration event) {
            super.refresh(event);

            population = population.clone();
            population.sort();
        }
    }

}
