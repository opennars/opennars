/**
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
package org.opennars.language;

import org.opennars.io.Symbols;

/**
 * This stores the magnitude of a time difference, which is the logarithm of the time difference
 * in base D=duration ( @see Param.java ).  The actual printed value is +1 more than the stored
 * magnitude, so for example, it will have name() "+1" if magnitude=0, and "+2" if magnitude=1.
 * 
 * @author peiwang / SeH
 */
public class Interval extends Term {
    
    public static Interval interval(final String i) {
        return new Interval(Long.parseLong(i.substring(1)));
    }
    
    @Override
    public boolean hasInterval() {
        return true;
    }
    
    public final long time;
    
    /** this constructor has an extra unused argument to differentiate it from the other one,
     * for specifying magnitude directly.
     */
    public Interval(final long time) {
        super();
        this.time = time;
        setName(Symbols.INTERVAL_PREFIX + String.valueOf(time));        
    }    
    
    public Interval(final String i) {
        this(Long.parseLong(i.substring(1)) - 1);
    }
    
    @Override
    public Interval clone() {
        //can return this as its own clone since it's immutable.
        //originally: return new Interval(magnitude, true);        
        return this;
    }
}
