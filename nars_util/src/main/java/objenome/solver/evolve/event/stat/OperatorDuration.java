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

import objenome.solver.evolve.event.OperatorEvent.EndOperator;

import java.util.concurrent.TimeUnit;

/**
 * Stat that provides the duration of a genetic operator.
 */
public abstract class OperatorDuration extends AbstractStat<EndOperator> {

    /**
     * The operator duration.
     */
    private long duration;

    /**
     * Constructs a <code>OperatorDuration</code>.
     */
    public OperatorDuration() {
        super(OperatorEndTime.class);
    }

    /**
     * Computes the duration of a genetic operator.
     *
     * @param event the <code>EndOperator</code> event object.
     */
    @Override
    public void refresh(EndOperator event) {
        long start = getConfig().the(OperatorStartTime.class).getTime();
        long end = getConfig().the(OperatorEndTime.class).getTime();

        duration = end - start;
    }

    /**
     * Returns the duration of a genetic operator.
     *
     * @return the duration of a genetic operator.
     */
    public abstract long getDuration();

    /**
     * Returns a string representation of the duration.
     *
     * @return a string representation of the duration.
     */
    @Override
    public String toString() {
        return Long.toString(getDuration());
    }

    /**
     * Stat that provides the generation duration time in nano seconds.
     */
    public class NanoSeconds extends OperatorDuration {

        @Override
        public long getDuration() {
            return duration;
        }
    }

    /**
     * Stat that provides the generation duration time in millis seconds.
     */
    public class MilliSeconds extends OperatorDuration {

        @Override
        public long getDuration() {
            return TimeUnit.NANOSECONDS.toMillis(duration);
        }
    }

    /**
     * Stat that provides the generation duration time in seconds.
     */
    public class Seconds extends OperatorDuration {

        @Override
        public long getDuration() {
            return TimeUnit.NANOSECONDS.toSeconds(duration);
        }
    }

    /**
     * Stat that provides the generation duration time in minutes.
     */
    public class Minutes extends OperatorDuration {

        @Override
        public long getDuration() {
            return TimeUnit.NANOSECONDS.toMinutes(duration);
        }
    }

    /**
     * Stat that provides the generation duration time in hours.
     */
    public class Hours extends OperatorDuration {

        @Override
        public long getDuration() {
            return TimeUnit.NANOSECONDS.toHours(duration);
        }
    }
}
