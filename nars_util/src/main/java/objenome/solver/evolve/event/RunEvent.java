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

import objenome.solver.evolve.Population;

/**
 * Base class for run related events.
 */
public abstract class RunEvent implements Event {

	/**
	 * The run number.
	 */
	private final int run;

	/**
	 * Constructs a <code>RunEvent</code>.
	 * 
	 * @param run
	 *            the run number.
	 */
	public RunEvent(int run) {
		this.run = run;
	}

	/**
	 * Returns the run number.
	 * 
	 * @return the run number.
	 */
	public int getRun() {
		return run;
	}

	/**
	 * An event that indicates the start of a run.
	 */
	public static class StartRun extends RunEvent {

		/**
		 * Constructs a <code>StartRun</code>.
		 * 
		 * @param run
		 *            the run number.
		 */
		public StartRun(int run) {
			super(run);
		}
	}

	/**
	 * An event that indicates the end of a run.
	 */
	public static class EndRun extends RunEvent {

		/**
		 * The population at the end of the run.
		 */
		private final Population population;

		/**
		 * Constructs a <code>EndRun</code>.
		 * 
		 * @param run
		 *            the run number.
		 * @param population
		 *            the population at the end of the run.
		 */
		public EndRun(int run, Population population) {
			super(run);

			this.population = population;
		}

		/**
		 * Returns the population at the end of the run.
		 * 
		 * @return the population at the end of the run.
		 */
		public Population getPopulation() {
			return population;
		}

	}

}
