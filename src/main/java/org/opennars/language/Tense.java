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

import java.util.HashMap;
import java.util.Map;

public enum Tense {
    
    
    Past(":\\:"),
    Present(":|:"),
    Future(":/:");
    
    
    public final String symbol;

    public static final Tense Eternal = null;
    
    Tense(final String string) {
        this.symbol = string;
    }

    @Override
    public String toString() {
        return symbol;
    }
    
    protected static final Map<String, Tense> stringToTense = new HashMap(Tense.values().length * 2);
    
    static {
        for (final Tense t : Tense.values()) {
            stringToTense.put(t.toString(), t);
        }
    }

    public static Tense tense(final String s) {
        return stringToTense.get(s);
    }
    
}
