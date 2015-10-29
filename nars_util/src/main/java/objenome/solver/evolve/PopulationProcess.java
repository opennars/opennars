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

/**
 * A <code>PopulationProcess</code> defines an object which performs a population wide
 * process.
 */
public interface PopulationProcess<I extends Organism> {

    /**
     * Processes the provided <code>Population</code> and returns the resultant
     * population.
     *
     * @param population a <code>Population</code> to be processed
     * @return a <code>Population</code> which is the result of some form of
     * processing of the provided population
     */
    Population<I> process(Population<I> population);
    
}
