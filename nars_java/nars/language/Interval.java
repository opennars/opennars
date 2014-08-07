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

import java.util.Objects;
import nars.io.Symbols;


public class Interval extends Term {
    
    private final long time;
    
    /** magnitude is what is used for comparing, hash, and equality.  time is saved for reference */
    public final int magnitude;
    
    transient private final int hash;

    // time is a positive integer
    public Interval(final long time) {
        super();
        this.time = time;
        this.magnitude = (int)Math.log(time);        
        this.hash = Objects.hash(Term.class.getSimpleName(), magnitude);
    }
    public Interval(final String magnitudeString) {
        super();
        this.magnitude = Integer.parseInt(magnitudeString.substring(1));
        this.time = (long) Math.ceil(Math.exp(magnitude));
        this.hash = Objects.hash(Term.class.getSimpleName(), magnitude);
    }
    
    @Override
    public String toString() {
        return Symbols.INTERVAL_PREFIX + String.valueOf(magnitude);
    }
            
    @Override
    public Interval clone() {
        return new Interval(time);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public int compareTo(Term o) {
        if (o instanceof Interval) {
            return Integer.compare(magnitude, ((Interval)o).magnitude);
        }
        return -1;
    }
    
    @Override
    public boolean equals(Object that) {
        if (that instanceof Interval) {
            return magnitude == ((Interval)that).magnitude;
        }
        return false;    
    }

    @Override
    public boolean isConstant() {
        return true;
    }

    @Override
    public short getComplexity() {
        return 0;
    }

    @Override
    public boolean containsVar() {
        return false;
    }

 
}