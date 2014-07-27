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
    public Interval(long time) {
        magnitude = (int) Math.log(time);
        name = "" + Symbols.INTERVAL_PREFIX + magnitude;
    }
    
    public Interval(String s) {
        magnitude = Integer.parseInt(s.substring(1));
        name = s;
    }
    
    public long getTime() {
        return (long) Math.ceil(Math.exp(magnitude));
    }
    
    @Override
    public Object clone() {
        return new Interval(name);
    }
}