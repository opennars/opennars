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
package objenome.solver.evolve.init;

import objenome.solver.evolve.GPContainer.GPKey;
import objenome.solver.evolve.OrganismBuilder;
import objenome.solver.evolve.TypedOrganism;

/**
 * Initialisation method for <code>STGPIndividual</code>s. It does not define
 * any methods, but initialisation procedures that generate
 * <code>STGPIndividual</code> objects should implement this interface.
 *
 * <p>
 * Where appropriate, implementations should use the
 * <code>MAXIMUM_INITIAL_DEPTH</code> configuration parameter defined in this
 * interface to specify the maximum depth of the program trees they generate.
 * The {@link TypedOrganism#MAXIMUM_DEPTH} parameter should also be enforced.
 *
 * @since 2.0
 */
public interface TypedInitialization extends OrganismBuilder<TypedOrganism> {

    /**
     * The key for setting and retrieving the maximum initial depth setting for
     * program trees
     */
    GPKey<Integer> MAXIMUM_INITIAL_DEPTH = new GPKey<>();

}
