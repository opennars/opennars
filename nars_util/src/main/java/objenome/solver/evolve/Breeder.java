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
 * A <code>Breeder</code> is a component that is responsible for applying
 * genetic operators to a population. The operators that are applied should be
 * chosen from those provided by the <code>OPERATORS</code> configuration
 * parameter. The individuals that undergo the operations should be selected
 * from the received population using the individual selector set with the
 * <code>SELECTOR</code> configuration parameter. The method for selecting
 * between the operators and their order of application are implementation
 * specific details.
 */
public interface Breeder extends PopulationProcess {

    /**
     * The key for setting and retrieving the genetic operators.
     */
    GPKey<List<OrganismOperator>> OPERATORS = new GPKey<>();

    /**
     * The key for setting and retrieving the <code>IndividualSelector</code>.
     */
    GPKey<IndividualSelector> SELECTOR = new GPKey<>();

}
