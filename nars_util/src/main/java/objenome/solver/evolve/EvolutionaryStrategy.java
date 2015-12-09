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

import java.util.List;

/**
 * An <code>EvolutionaryStrategy</code> is a component that is responsible for
 * evolving an existing population. Implementations may perform the evolution in
 * any number of ways, including using a generational or steady-state approach.
 * When used with an Evolver, in a typical setup, the evolutionary strategy will
 * receive a full population of individuals that have already been evaluated.
 * The strategy must progress the population, normally with the use of genetic
 * operators, and provide a stopping condition if appropriate.
 */
public interface EvolutionaryStrategy extends PopulationProcess {

    /**
     * The key for setting and retrieving a list of
     * <code>TerminationCriteria</code>.
     */
    GPKey<List<PopulationTermination>> TERMINATION_CRITERIA = new GPKey<>();

}
