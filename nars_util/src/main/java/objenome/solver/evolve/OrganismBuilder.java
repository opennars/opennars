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
 * An instance of <code>Initialiser</code> is responsible for creating the
 * individuals.
 */
public interface OrganismBuilder<I extends Organism> {

    /**
     * The key for setting and retrieving whether the initialisation method
     * should allow duplicate individuals or not
     */
    GPKey<Boolean> ALLOW_DUPLICATES = new GPKey<>();

    /**
     * Returns a newly created individual.
     *
     * @return a newly created individual.
     */
    I newOrganism();

    /**
     * Returns a population of new individuals.
     *
     * @return a population of new individuals.
     */
    Population<I> createPopulation(Population<I> survivors, GPContainer config);

    void populate(Population<I> population, int populationSize);

}
