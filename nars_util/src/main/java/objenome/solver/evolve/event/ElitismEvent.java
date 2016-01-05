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

import objenome.solver.evolve.Organism;
import objenome.solver.evolve.Population;

/**
 * Base class for elitism related events.
 */
public abstract class ElitismEvent implements Event {

	/**
	 * The current population.
	 */
	private final Population population;

	/**
	 * Constructs a <code>ElitismEvent</code>.
	 * 
	 * @param population
	 *            the current population.
	 */
	public ElitismEvent(Population population) {
		this.population = population;
	}

	/**
	 * Returns the population associated with this event.
	 * 
	 * @return the population associated with this event.
	 */
	public Population getPopulation() {
		return population;
	}

	/**
	 * An event that indicates the start of the elitism process.
	 */
	public static class StartElitism extends ElitismEvent {

		/**
		 * Constructs a <code>StartElitism</code>.
		 * 
		 * @param population
		 *            the current population.
		 */
		public StartElitism(Population population) {
			super(population);
		}
	}

	/**
	 * An event that indicates the end of the elitism process.
	 */
	public static class EndElitism extends ElitismEvent {

		/**
		 * The elitist individuals.
		 */
		private final Organism[] elites;

		/**
		 * Constructs an <code>EndElitism</code>.
		 * 
		 * @param population
		 *            the current population.
		 * @param elites
		 *            the elitist individuals.
		 */
		public EndElitism(Population population, Organism[] elites) {
			super(population);

			this.elites = elites;
		}

		/**
		 * Returns the elitist individuals.
		 * 
		 * @return the elitist individuals.
		 */
		public Organism[] getElites() {
			return elites;
		}
	}

}
