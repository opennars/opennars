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
package org.opennars.interfaces;

/**
 * Something which can work with Narsese as a string representation
 */
public interface NarseseConsumer {
    // TODO< split this and refactor to interface which can be used by the parser too >

    /**
     * feeds narsese input to the consumer
     * @param narsese the narsese text
     */
    void addInput(String narsese);
}
