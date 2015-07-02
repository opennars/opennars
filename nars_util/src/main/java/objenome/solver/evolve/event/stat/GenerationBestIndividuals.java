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

import java.util.ArrayList;
import java.util.List;

/**
 * Stat that provides the best individuals of a generation.
 */
public class GenerationBestIndividuals extends AbstractStat<EndGeneration> {

    /**
     * The list of best individuals.
     */
    private List<Organism> best;

    /**
     * Constructs a <code>GenerationBestIndividuals</code>.
     */
    public GenerationBestIndividuals() {
        super(NO_DEPENDENCIES);
    }

    /**
     * Determines the best individuals of a generation.
     *
     * @param event the <code>EndGeneration</code> event object.
     */
    @Override
    public void refresh(EndGeneration event) {
        Population<?> population = event.getPopulation();

        best = new ArrayList<>();

        for (Organism individual : population) {

            if (best.isEmpty()) {
                best.add(individual);
            } else {

                int comparison = individual.compareTo(best.get(0));

                if (comparison > 0) {
                    best.clear();
                }

                if (comparison >= 0) {
                    best.add(individual);
                }
            }
        }
    }

    /**
     * Returns the best individuals.
     *
     * @return the best individuals.
     */
    public Organism[] getBestIndividuals() {
        return best.toArray(new Organism[best.size()]);
    }

    /**
     * Returns an arbitrary best individual.
     *
     * @return an arbitrary best individual.
     */
    public Organism getBest() {
        return (best == null || best.isEmpty()) ? null : best.get(0);
    }

    /**
     * Returns the string representation of an arbitrary best individual.
     *
     * @return the string representation of an arbitrary best individual.
     */
    @Override
    public String toString() {
        Organism individual = getBest();
        return (individual == null) ? "" : individual.toString();
    }

}
