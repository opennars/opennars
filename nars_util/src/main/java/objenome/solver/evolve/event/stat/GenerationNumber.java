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

import objenome.solver.evolve.event.GenerationEvent.StartGeneration;

/**
 * Stat that provides the generation current number.
 */
public class GenerationNumber extends AbstractStat<StartGeneration> {

    /**
     * The current generation number.
     */
    private int current;

    /**
     * Constructs a <code>GenerationNumber</code>.
     */
    public GenerationNumber() {
        super(NO_DEPENDENCIES);
    }

    /**
     * Determines the current generation number.
     *
     * @param event the <code>StartGeneration</code> event object.
     */
    @Override
    public void refresh(StartGeneration event) {
        current = event.getGeneration();
    }

    /**
     * Returns the current generation number.
     *
     * @return the current generation number.
     */
    public int getCurrent() {
        return current;
    }

    /**
     * Returns a string representation of the generation number.
     *
     * @return a string representation of the generation number.
     */
    @Override
    public String toString() {
        return Integer.toString(current);
    }

}
