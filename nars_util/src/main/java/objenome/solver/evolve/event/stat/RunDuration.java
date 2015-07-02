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

import objenome.solver.evolve.event.RunEvent.EndRun;

import java.util.concurrent.TimeUnit;

/**
 * Abstract class that provices the duration of the run.
 */
public abstract class RunDuration extends AbstractStat<EndRun> {

    /**
     * The duration of the run.
     */
    private long duration;

    /**
     * Constructs a <code>RunDuration</code>.
     */
    @SuppressWarnings("unchecked")
    public RunDuration() {
        super(RunStartTime.class, RunEndTime.class);
    }

    /**
     * Computes the duration of the run.
     *
     * @param event the <code>EndRun</code> event object.
     */
    @Override
    public void refresh(EndRun event) {
        long start = getConfig().the(RunStartTime.class).getTime();
        long end = getConfig().the(RunEndTime.class).getTime();

        duration = end - start;
    }

    /**
     * Returns the duration of the run.
     *
     * @return the duration of the run.
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
     * Stat that provides the run duration time in nano seconds.
     */
    public class NanoSeconds extends RunDuration {

        @Override
        public long getDuration() {
            return duration;
        }
    }

    /**
     * Stat that provides the run duration time in milli seconds.
     */
    public class MilliSeconds extends RunDuration {

        @Override
        public long getDuration() {
            return TimeUnit.NANOSECONDS.toMillis(duration);
        }
    }

    /**
     * Stat that provides the run duration time in seconds.
     */
    public class Seconds extends RunDuration {

        @Override
        public long getDuration() {
            return TimeUnit.NANOSECONDS.toSeconds(duration);
        }
    }

    /**
     * Stat that provides the run duration time in minutes.
     */
    public class Minutes extends RunDuration {

        @Override
        public long getDuration() {
            return TimeUnit.NANOSECONDS.toMinutes(duration);
        }
    }

    /**
     * Stat that provides the run duration time in hours.
     */
    public class Hours extends RunDuration {

        @Override
        public long getDuration() {
            return TimeUnit.NANOSECONDS.toHours(duration);
        }
    }
}
