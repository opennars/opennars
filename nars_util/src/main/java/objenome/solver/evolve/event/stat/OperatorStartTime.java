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

import objenome.solver.evolve.event.OperatorEvent.StartOperator;

/**
 * Stat that provides the start time of a genetic operator.
 */
public class OperatorStartTime extends AbstractStat<StartOperator> {

    /**
     * The start time of the genetic operator.
     */
    private long time;

    /**
     * Constructs a <code>OperatorStartTime</code>.
     */
    public OperatorStartTime() {
        super(NO_DEPENDENCIES);
    }

    /**
     * Determines the genetic operator start time.
     *
     * @param event the <code>StartOperator</code> event object.
     */
    @Override
    public void refresh(StartOperator event) {
        time = System.nanoTime();
    }

    /**
     * Returns the genetic operator start time.
     *
     * @return the genetic operator start time.
     */
    public long getTime() {
        return time;
    }

    /**
     * Returns a string representation of the genetic operator start time.
     *
     * @return a string representation of the genetic operator start time.
     */
    @Override
    public String toString() {
        return Long.toString(getTime());
    }
}
