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
    private final int magnitude;

    // time is a positive integer
    public Interval(final long time) {
        this((int) Math.log(time), true);
    }
    
    /** this constructor has an extra unused argument to differentiate it from the other one,
     * for specifying magnitude directly.
     */
    public Interval(final int magnitude, boolean yesMagnitude) {
        super();
        this.magnitude = magnitude;
        setName(Symbols.INTERVAL_PREFIX + String.valueOf(magnitude));        
    }
    
    public Interval(final String s) {
        magnitude = Integer.parseInt(s.substring(1));
        setName(s);
    }
    
    public long getTime() {
        return (long) Math.ceil(Math.exp(magnitude));
    }
    
    public long getMagnitude() {
        return magnitude;
    }
    
    @Override
    public Interval clone() {
        return new Interval(magnitude, true);
    }
}