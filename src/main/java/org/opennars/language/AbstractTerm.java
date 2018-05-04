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




public interface AbstractTerm extends Cloneable, Comparable<AbstractTerm> {

    /**
     * Whether this compound term contains any variable term
     *
     * @return Whether the name contains a variable
     */
    boolean hasVar();

    /**
     * Check whether the current Term can name a Concept.
     *
     * @return A Term is constant by default
     */
    boolean isConstant();

    /**
     * Reporting the name of the current Term.
     *
     * @return The name of the term as a String
     */
    default CharSequence name() {
        return toString();
    }
    
}
