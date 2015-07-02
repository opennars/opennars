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

import objenome.solver.evolve.Organism;
import objenome.solver.evolve.event.ElitismEvent.EndElitism;

import java.util.Arrays;

/**
 * Stat that provides the information about the elitist individuals.
 */
public class ElitismElites extends AbstractStat<EndElitism> {

    /**
     * The elitist individuals.
     */
    private Organism[] elites;

    /**
     * Constructs a <code>ElitismElites</code>.
     */
    public ElitismElites() {
        super(NO_DEPENDENCIES);
    }

    /**
     * Updates the elitist individuals.
     *
     * @param event the <code>EndElitism</code> event object.
     */
    @Override
    public void refresh(EndElitism event) {
        elites = event.getElites();
    }

    /**
     * Returns the elitist individuals.
     *
     * @return the elitist individuals.
     */
    public Organism[] getElites() {
        return elites;
    }

    /**
     * Returns a string representation of the elitist individuals.
     *
     * @return a string representation of the elitist individuals.
     */
    @Override
    public String toString() {
        return Arrays.toString(elites);
    }
}
