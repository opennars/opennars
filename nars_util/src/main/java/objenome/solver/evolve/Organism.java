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

import java.io.Serializable;

/**
 * An instance of <code>Individual</code> represents one candidate solution to a
 * specific problem. The only responsibility of an individual is to provide a
 * fitness that is an indicator of the solution's quality. A typical
 * implementation would allow a fitness to be set by a {@link FitnessEvaluator}
 * during an evolutionary run.
 */
public interface Organism extends Serializable, Cloneable, Comparable<Organism> {

    /**
     * Returns a fitness which is an indicator of this individual's quality or
     * ability to solve a specific problem.
     *
     * @return the fitness of this individual
     */
    Fitness getFitness();

    /**
     * Returns a copy of this individual.
     *
     * @return a copy of this individual.
     */
    Organism clone();

    void normalize();


}
