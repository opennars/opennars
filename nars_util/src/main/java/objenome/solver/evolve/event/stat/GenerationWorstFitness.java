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
package objenome.solver.evolve.event.stat;

import objenome.solver.evolve.Fitness;
import objenome.solver.evolve.event.GenerationEvent.EndGeneration;

/**
 * Stat that provides the information of the worst fitness of a generation.
 */
public class GenerationWorstFitness extends AbstractStat<EndGeneration> {

    /**
     * The worst fitness value of a generation.
     */
    private Fitness worst;

    /**
     * Constructs a <code>GenerationWorstFitness</code>.
     */
    public GenerationWorstFitness() {
        super(GenerationFitnesses.class);
    }

    /**
     * Determines the worst fitness value of a generation.
     *
     * @param event the <code>EndGeneration</code> event object.
     */
    @Override
    public void refresh(EndGeneration event) {
        Fitness[] fitnesses = getConfig().the(GenerationFitnesses.class).getFitnesses();
        worst = null;

        for (Fitness fitness : fitnesses) {
            if (worst == null || fitness.compareTo(worst) < 0) {
                worst = fitness;
            }
        }
    }

    /**
     * Returns the worst fitness value.
     *
     * @return the worst fitness value.
     */
    public Fitness getWorst() {
        return worst;
    }

    /**
     * Returns a string representation of the worst fitness value.
     *
     * @return a string representation of the worst fitness value.
     */
    @Override
    public String toString() {
        return worst.toString();
    }

}
