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

import objenome.solver.evolve.event.ElitismEvent.EndElitism;

import java.util.concurrent.TimeUnit;

/**
 * Abstract class that provices the duration of the elitism process.
 */
public abstract class ElitismDuration extends AbstractStat<EndElitism> {

    /**
     * The duration of the elitism process.
     */
    private long duration;

    /**
     * Constructs an <code>ElitismDuration</code>.
     */
    @SuppressWarnings("unchecked")
    public ElitismDuration() {
        super(ElitismStartTime.class, ElitismEndTime.class);
    }

    /**
     * Computes the duration of the elitism process.
     *
     * @param event the <code>EndElitism</code> event object.
     */
    @Override
    public void refresh(EndElitism event) {
        long start = getConfig().the(ElitismStartTime.class).getTime();
        long end = getConfig().the(ElitismEndTime.class).getTime();

        duration = end - start;
    }

    /**
     * Returns the duration of the elitism process.
     *
     * @return the duration of the elitism process.
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
     * Stat that provides the elitism duration time in nano seconds.
     */
    public class NanoSeconds extends ElitismDuration {

        @Override
        public long getDuration() {
            return duration;
        }
    }

    /**
     * Stat that provides the elitism duration time in milli seconds.
     */
    public class MilliSeconds extends ElitismDuration {

        @Override
        public long getDuration() {
            return TimeUnit.NANOSECONDS.toMillis(duration);
        }
    }

    /**
     * Stat that provides the elitism duration time in seconds.
     */
    public class Seconds extends ElitismDuration {

        @Override
        public long getDuration() {
            return TimeUnit.NANOSECONDS.toSeconds(duration);
        }
    }

    /**
     * Stat that provides the elitism duration time in minutes.
     */
    public class Minutes extends ElitismDuration {

        @Override
        public long getDuration() {
            return TimeUnit.NANOSECONDS.toMinutes(duration);
        }
    }

    /**
     * Stat that provides the elitism duration time in hours.
     */
    public class Hours extends ElitismDuration {

        @Override
        public long getDuration() {
            return TimeUnit.NANOSECONDS.toHours(duration);
        }
    }
}
