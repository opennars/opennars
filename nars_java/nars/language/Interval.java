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

/**
 *
 * @author peiwang
 */
public class Interval extends Term {
    private int magnitude;

    // time is a positive integer
    public Interval(long time) {
        name = "[" + magnitude + "]";
        magnitude = (int) Math.floor(Math.log(time));
    }
    
    public long getTime() {
        return (long) Math.exp(magnitude);
    }
    
    @Override
    public Object clone() {
        return new Interval(getTime());
    }
}
