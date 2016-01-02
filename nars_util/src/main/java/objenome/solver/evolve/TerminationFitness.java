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

import objenome.solver.evolve.GPContainer.GPContainerAware;
import objenome.solver.evolve.event.GenerationEvent.EndGeneration;
import objenome.solver.evolve.event.Listener;

/**
 * This class represents a termination criteria based on a desired fitness
 * value. Given a threshold fitness value, the run is terminated when the
 * current fitness is equal to or greater than the threshold fitness.
 */
public class TerminationFitness implements PopulationTermination, Listener<EndGeneration>, GPContainerAware {

    /**
     * The threshold fitness value.
     */
    private final Fitness threshold;

    /**
     * The current best fitness value.
     */
    private Fitness fittest;

    /**
     * Constructs a <code>TerminationFitness</code>.
     *
     * @param threshold the threshold fitness value.
     */
    public TerminationFitness(Fitness threshold) {
        this.threshold = threshold;
    
    }

    @Override
    public void setConfig(GPContainer c) {
        c.on(EndGeneration.class, this);
    }

    /**
     * Returns <code>true</code> the current fitness is equal to or greater than
     * the threshold fitness.
     *
     * @return <code>true</code> if the current fitness is equal to or greater
     * than the threshold fitness; <code>false</code> otherwise.
     */
    @Override
    public boolean terminate(GPContainer config) {
        return fittest != null && fittest.compareTo(threshold) >= 0;
    }

    /**
     * Retireves the current best fitness from the population.
     *
     * @param event the event to get the popuation from.
     */
    @Override
    public void onEvent(EndGeneration event) {
        fittest = event.getPopulation().best().getFitness();
    }


}
