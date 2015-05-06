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

import objenome.solver.evolve.event.RunEvent.StartRun;

/**
 * Stat that provides the start time of a run.
 */
public class RunStartTime extends AbstractStat<StartRun> {

    /**
     * The start time of the run.
     */
    private long time;

    /**
     * Constructs a <code>RunStartTime</code>.
     */
    public RunStartTime() {
        super(NO_DEPENDENCIES);
    }

    /**
     * Determines the run start time.
     *
     * @param event the <code>StartRun</code> event object.
     */
    @Override
    public void refresh(StartRun event) {
        time = System.nanoTime();
    }

    /**
     * Returns the generation start time.
     *
     * @return the generation start time.
     */
    public long getTime() {
        return time;
    }

    /**
     * Returns a string representation of the start time of the run.
     *
     * @return a string representation of the start time of the run.
     */
    @Override
    public String toString() {
        return Long.toString(getTime());
    }
}
