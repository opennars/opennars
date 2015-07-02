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
 * A stat that returns the number of terminals in all program trees in the
 * population from the previous generation. All individuals in the population
 * must be instances of <code>STGPIndividual</code>.
 *
 * @since 2.0
 */
public class GenerationTerminals extends AbstractStat<EndGeneration> {

    private int[] terminals;

    /**
     * Constructs a <code>GenerationTerminals</code> stat and registers its
     * dependencies
     */
    public GenerationTerminals() {
        super(NO_DEPENDENCIES);
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
        Population<?> population = event.getPopulation();
        terminals = new int[population.size()];
        int index = 0;

        for (Organism individual : population) {
            if (individual instanceof TypedOrganism) {
                terminals[index++] = ((TypedOrganism) individual).getRoot().countTerminals();
            }
        }
    }

    /**
     * Returns an array containing the number of terminal nodes in each program
     * tree in the population
     *
     * @return the number of terminal nodes in each program tree in the previous
     * generation
     */
    public int[] getTerminals() {
        return terminals;
    }

    /**
     * Returns a string representation of the value of this stat
     *
     * @return a <code>String</code> that represents the value of this stat
     */
    @Override
    public String toString() {
        return Arrays.toString(terminals);
    }
}
