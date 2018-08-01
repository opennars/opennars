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
package org.opennars.util;

import java.util.List;
import java.util.function.Predicate;

public class ListUtil {
    /**
     * tries to select the first element where the predicate matches from front (index 0) to the end of the list
     *
     * @param candidates the candidates from which the method may select the first one
     * @param predicate the checked predicate for each element
     * @param <T> generic type
     * @return element which matched first, null if none matched
     */
    public static<T> T findAny(final List<T> candidates, final Predicate<T> predicate) {
        for( final T i : candidates) {
            if(predicate.test(i)) {
                return i;
            }
        }

        return null;
    }
}
