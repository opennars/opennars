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
 * Implementations of this interface supply a stopping condition for
 * evolutionary runs. Different evolutionary strategies may make use of
 * termination criteria in different ways and at different points of execution.
 * 
 * @see GenerationalStrategy
 */
public interface PopulationTermination {

	/**
	 * Indicates whether the given evolutionary run should terminate due to some
	 * stopping condition having been met. The exact point at which this method
	 * is called is determined by the specific evolutionary strategy in use.
	 * 
	 * @return <code>true</code> if the current evolutionary run should
	 *         terminate or <code>false</code> if it should continue.
	 */
	boolean terminate(GPContainer config);

}
