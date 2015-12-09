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

/**
 * An instance of <code>Operator</code> represents a genetic operator used to
 * generate new individuals. In general, a genetic operator takes <it>n</it>
 * individuals as input and performs random modifications to generate new
 * individuals.
 */
public interface OrganismOperator extends GPContainerAware {

    /**
     * Returns the number of individuals expected by the operator.
     *
     * @return the number of individuals expected by the operator.
     */
    int inputSize();

    /**
     * Performs the operator on the specified individuals. If the operator is
     * not successful, the specified individuals will not be changed and
     * <code>null</code> is returned.
     *
     * @param individuals the individuals undergoing the operator.
     *
     * @return the modified individuals; <code>null</code> when the operator
     * could not be applied.
     */
    Organism[] apply(Population population, Organism... individuals);

    /**
     * Returns the probability of the operator (the higher the value, the more
     * frequent the operator is used).
     *
     * @return the probability of the operator.
     */
    double probability();

}
