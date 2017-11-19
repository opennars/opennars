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

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import nars.storage.Memory;
import nars.io.Symbols;

/**
 * This stores the magnitude of a time difference, which is the logarithm of the time difference
 * in base D=duration ( @see Param.java ).  The actual printed value is +1 more than the stored
 * magnitude, so for example, it will have name() "+1" if magnitude=0, and "+2" if magnitude=1.
 * 
 * @author peiwang / SeH
 */
public class Interval extends Term {

    //Because AtomicInteger/Double ot supported by teavm
    public static class PortableInteger {
        public PortableInteger(){}
        Object lock = new Object();
        int VAL = 0;
        public PortableInteger(int VAL){synchronized(lock){this.VAL = VAL;}}
        public void set(int VAL){synchronized(lock){this.VAL = VAL;}}
        public int get() {return this.VAL;}
        public float floatValue() {return (float)this.VAL;}
        public float doubleValue() {return (float)this.VAL;}
        public int intValue() {return (int)this.VAL;}
        public int incrementAndGet(){synchronized(lock){this.VAL++;} return VAL;}
    }
    public static class PortableDouble {
        Object lock = new Object();
        public PortableDouble(){}
        double VAL = 0;
        public PortableDouble(double VAL){synchronized(lock){this.VAL = VAL;}}
        public void set(double VAL){synchronized(lock){this.VAL = VAL;}}
        public double get() {return this.VAL;}
        public float floatValue() {return (float)this.VAL;}
        public float doubleValue() {return (float)this.VAL;}
        public int intValue() {return (int)this.VAL;}
    }
    
    public static class AtomicDuration extends PortableInteger {
        
        /** this represents the amount of time in proportion to a duration in which
         *  Interval resolution calculates.  originally, NARS had a hardcoded duration of 5
         *  and an equivalent Interval scaling factor of ~1/2 (since ln(E) ~= 1/2 * 5).
         *  Since duration is now adjustable, this factor approximates the same result
         *  with regard to a "normalized" interval scale determined by duration.
         */
        double linear = 0.5;
        
        transient double log; //caches the value here
        transient int lastValue = -1;

        public AtomicDuration() {
            super();
        }
        
        
        public AtomicDuration(int v) {
            super(v);            
        }        

        public void setLinear(double linear) {
            this.linear = linear;
            update();
        }
        
        protected void update() {
            int val = get();
            lastValue = val;
            this.log = Math.log(val * linear );            
        }
        
        public double getSubDurationLog() {
            int val = get();
            if (lastValue != val) {
                update();
            }
            return log;
        }
        
    }
    
    static final int INTERVAL_POOL_SIZE = 16;
    static Interval[] INTERVAL = new Interval[INTERVAL_POOL_SIZE];
    
    public static Interval interval(final String i) {
        return interval( Integer.parseInt(i.substring(1)) - 1);
    }
    
    public static Interval interval(final long time, final Memory memory) {
        return interval( timeToMagnitude( time, memory.param.duration ) );
    }
    
    public static Interval interval(final long time, final AtomicDuration duration) {
        return interval( timeToMagnitude( time, duration ) );
    }
    
    public static Interval interval(int magnitude) {
        if (magnitude >= INTERVAL_POOL_SIZE)
            return new Interval(magnitude, true);
        else if (magnitude < 0)
            magnitude = 0;
            
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
    protected Interval(final int magnitude, final boolean yesMagnitude) {
        super();
        this.magnitude = magnitude;
        setName(Symbols.INTERVAL_PREFIX + String.valueOf(1+magnitude));        
    }

    public static int timeToMagnitude(final long timeDiff, final AtomicDuration duration) {
        int m = (int) timeDiff; //Math.round(Math.log(timeDiff) / duration.getSubDurationLog());
        if (m < 0) return 0;
        return m;
    }
    
    public static double magnitudeToTime(final double magnitude, final AtomicDuration duration) {
        if (magnitude <= 0)
            return 1;
        return magnitude; //Math.exp(magnitude * duration.getSubDurationLog());
    }
    
    public static long magnitudeToTime(final int magnitude, final AtomicDuration duration) {
        return (long)Math.round(magnitudeToTime((double)magnitude, duration));
    }
    
    public final long getTime(final AtomicDuration duration) {
        //TODO use a lookup table for this
        return magnitudeToTime(magnitude, duration);
    }
    
    public final long getTime(final Memory memory) {        
        return getTime(memory.param.duration);
    }
    
    @Override
    public Interval clone() {
        //can return this as its own clone since it's immutable.
        //originally: return new Interval(magnitude, true);        
        return this;
    }

    /** returns a sequence of intervals which approximate a time period with a maximum number of consecutive Interval terms */
    public static List<Interval> intervalTimeSequence(final long t, final int maxTerms, final Memory memory) {
        return Lists.newArrayList(interval(t, memory));
    }

}