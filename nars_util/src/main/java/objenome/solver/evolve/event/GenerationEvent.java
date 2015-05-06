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

import objenome.solver.evolve.GenerationalStrategy;
import objenome.solver.evolve.Population;

/**
 * Base class for generation related events.
 *
 * @see GenerationalStrategy
 */
public abstract class GenerationEvent implements Event {

    /**
     * The current generation number.
     */
    private final int generation;

    /**
     * The current population.
     */
    private final Population<?> population;

    /**
     * Constructs a <code>GenerationEvent</code>.
     *
     * @param generation the current generation number.
     * @param population the current population.
     */
    public GenerationEvent(int generation, Population population) {
        this.generation = generation;
        this.population = population;
    }

    /**
     * Returns the generation of this event.
     *
     * @return the generation of this event.
     */
    public int getGeneration() {
        return generation;
    }

    /**
     * Returns the population of this event.
     *
     * @return the population of this event.
     */
    public Population<?> getPopulation() {
        return population;
    }

    /**
     * An event that indicates the start of a generation.
     */
    public static class StartGeneration extends GenerationEvent {

        /**
         * Constructs a <code>StartGeneration</code>.
         *
         * @param generation the current generation number.
         * @param population the current population.
         */
        public StartGeneration(int generation, Population population) {
            super(generation, population);
        }
    }

    /**
     * An event that indicates the end of a generation.
     */
    public static class EndGeneration extends GenerationEvent {

        /**
         * Constructs an <code>EndGeneration</code>.
         *
         * @param generation the current generation number.
         * @param population the current population.
         */
        public EndGeneration(int generation, Population population) {
            super(generation, population);
        }
    }
}
