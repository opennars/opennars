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
package objenome.solver.evolve.event;

import objenome.solver.evolve.Initialiser;
import objenome.solver.evolve.Population;

/**
 * Base class for initialisation related events.
 * 
 * @see Initialiser
 */
public abstract class InitialisationEvent implements Event {

	/**
	 * An event that indicates the start of the initialisation.
	 */
	public static class StartInitialisation extends InitialisationEvent {
	}

	/**
	 * An event that indicates the end of the initialisation.
	 */
	public static class EndInitialisation extends InitialisationEvent {

		/**
		 * The population at the end of the initialisation.
		 */
		private final Population population;

		/**
		 * Constructs a <code>EndInitialisation</code>.
		 * 
		 * @param population
		 *            the population at the end of the initialisation.
		 */
		public EndInitialisation(Population population) {
			this.population = population;
		}

		/**
		 * Returns the population at the end of the initialisation.
		 * 
		 * @return the population at the end of the initialisation.
		 */
		public Population getPopulation() {
			return population;
		}
	}

}
