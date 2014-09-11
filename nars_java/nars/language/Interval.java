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

import java.util.concurrent.atomic.AtomicInteger;
import nars.io.Symbols;

/**
 * This stores the magnitude of a time difference, which is the logarithm of the 
 * @author peiwang
 */
public class Interval extends Term {
    
    public static class AtomicDuration extends AtomicInteger {
                
        double log; //caches the value here
        int lastValue = -1;

        public AtomicDuration() {
            super();
        }
        public AtomicDuration(int v) {
            super(v);            
        }
        
        
        public double getLog() {
            int val = get();
            if (lastValue != val) {
                lastValue = val;
                this.log = Math.log(val);
            }
            return log;
        }
        
    }
    
    static final int INTERVAL_POOL_SIZE = 16;
    static Interval[] INTERVAL = new Interval[INTERVAL_POOL_SIZE];
    
    public static Interval interval(String i) {
        return intervalMagnitude( Integer.parseInt(i.substring(1)) );
    }
    
    public static Interval intervalTime(final long time, AtomicDuration duration) {
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
    protected Interval(final long timeDiff, final AtomicDuration duration) {
        this(timeToMagnitude(timeDiff, duration), true);
    }
    
    
    /** this constructor has an extra unused argument to differentiate it from the other one,
     * for specifying magnitude directly.
     */
    protected Interval(final int magnitude, boolean yesMagnitude) {
        super();
        this.magnitude = magnitude;
        setName(Symbols.INTERVAL_PREFIX + String.valueOf(1+magnitude));        
    }
    
//    protected Interval(final String s) {
//        magnitude = Integer.parseInt(s.substring(1));
//        setName(s);
//    }

    public static int timeToMagnitude(long timeDiff, AtomicDuration duration) {
        return (int) Math.round(Math.log(timeDiff) / duration.getLog());
    }
    
    public static long magnitudeToTime(int magnitude, AtomicDuration duration) {
        return (long)( Math.round(Math.exp(magnitude * duration.getLog())));
    }
    
    @Deprecated public static long magnitudeToTime(int magnitude) {
        return (long) Math.ceil(Math.exp(magnitude));
    }
    
    public long getTime(final AtomicDuration duration) {
        //TODO use a lookup table for this
        return magnitudeToTime(magnitude, duration);
    }
    
    @Override
    public Interval clone() {
        //can return this as its own clone since it's immutable.
        //originally: return new Interval(magnitude, true);        
        return this;
    }
}