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
package objenome.solver.evolve.selection;

import objenome.solver.evolve.AbstractSelector;
import objenome.solver.evolve.IndividualSelector;
import objenome.solver.evolve.Organism;
import objenome.solver.evolve.RandomSequence;

import static objenome.solver.evolve.RandomSequence.RANDOM_SEQUENCE;

/**
 * This class represents an {@link IndividualSelector} that selects individuals
 * at random.
 */
public class RandomSelector extends AbstractSelector {

    /**
     * Returns a random individual from the current population.
     *
     * @return a random individual from the current population.
     */
    @Override
    public Organism select() {
        int index = ((RandomSequence)population.getConfig().get(RANDOM_SEQUENCE)).nextInt(population.size());
        return population.get(index);
    }

}
