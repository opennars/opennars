/*
 * Copyright (C) 2014 peiwang
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package nars.language;

import nars.io.Symbols;

/**
 *
 * @author peiwang
 */
/**
 *
 * @author peiwang
 */
public class Interval extends Term {
    
    static final int INTERVAL_POOL_SIZE = 16;
    static Interval[] INTERVAL = new Interval[INTERVAL_POOL_SIZE];
    static long[] MAGNITUDE_TIMES = new long[INTERVAL_POOL_SIZE];    
    static {
        for (int i = 0; i < INTERVAL_POOL_SIZE; i++)
            MAGNITUDE_TIMES[i] = magnitudeToTime(i);
    }
    
    public static Interval interval(String i) {
        return intervalMagnitude( Integer.parseInt(i.substring(1)) );
    }
    
    public static Interval intervalTime(final long time, final int duration) {
        return intervalMagnitude( timeToMagnitude( time, duration ) );
    }
    
    protected static Interval intervalMagnitude(final int magnitude) {
        if (magnitude >= INTERVAL_POOL_SIZE)
            return new Interval(magnitude, true);
        
        Interval existing = INTERVAL[magnitude];
        if (existing == null) {
            existing = new Interval(magnitude, true);
            INTERVAL[magnitude] = existing;
        }
        return existing;            
    }
    
    
    public final int magnitude;

    // time is a positive integer
    protected Interval(final long timeDiff, final int duration) {
        this(timeToMagnitude(timeDiff, duration), true);
    }
    
    
    /** this constructor has an extra unused argument to differentiate it from the other one,
     * for specifying magnitude directly.
     */
    protected Interval(final int magnitude, boolean yesMagnitude) {
        super();
        this.magnitude = magnitude;
        setName(Symbols.INTERVAL_PREFIX + String.valueOf(magnitude));        
    }
    
//    protected Interval(final String s) {
//        magnitude = Integer.parseInt(s.substring(1));
//        setName(s);
//    }

    public static int timeToMagnitude(long timeDiff, int duration) {
        return (int) Math.round(Math.log(  ((double)timeDiff)/((double)duration)  ));
    }
    
    public static long magnitudeToTime(int magnitude, int duration) {
        return magnitudeToTime(magnitude) * duration;
    }
    
    public static long magnitudeToTime(int magnitude) {
        return (long) Math.ceil(Math.exp(magnitude));
    }
    
    public long getTime(int duration) {
        //use a lookup table for this
        if (magnitude < INTERVAL_POOL_SIZE)
            return MAGNITUDE_TIMES[magnitude] * duration;
        return magnitudeToTime(magnitude, duration);
    }
    
    @Override
    public Interval clone() {
        //can return this as its own clone since it's immutable.
        //originally: return new Interval(magnitude, true);        
        return this;
    }
}