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

import objenome.solver.evolve.GPContainer.GPKey;
import objenome.solver.evolve.Individual;
import objenome.solver.evolve.IndividualSelector;
import objenome.solver.evolve.Population;

/**
 * This class represents an {@link IndividualSelector} that selects individuals
 * through a tournament performed on a subset of the population. In tournament
 * selection, <code>n</code> individuals are randomly selected and the
 * individual with the highest fitness is considered the winner of the
 * tournament and becomes the selected individual.
 */
public class TournamentSelector implements IndividualSelector {

    /**
     * The key for setting and retrieving the tournament size.
     */
    public static final GPKey<Integer> TOURNAMENT_SIZE = new GPKey<>();

    /**
     * The random selector used to select the individuals participating in the
     * tournament.
     */
    private final RandomSelector randomSelector;

    /**
     * The tournament size.
     */
    private int size;

    /**
     * Constructs a <code>TournamentSelector</code>.
     */
    public TournamentSelector() {
        randomSelector = new RandomSelector();
    }

    @Override
    public void setup(Population population) {
        randomSelector.setup(population);
        size = population.getConfig().get(TOURNAMENT_SIZE);
    }

    /**
     * Returns the selected individual using a tournament.
     *
     * @return the selected individual using a tournament.
     */
    @Override
    public Individual select() {
        Individual best = null;

        // choose and compare randomly selected programs.
        for (int i = 0; i < size; i++) {
            Individual individual = randomSelector.select();
            if ((best == null) || (individual.compareTo(best) > 0)) {
                best = individual;
            }
        }

        return best;
    }

}
