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
package org.opennars.main;

public class NarParameters {
    /** what this value represents was originally equal to the termlink record length (10), but we may want to adjust it or make it scaled according to duration since it has more to do with time than # of records.  it can probably be increased several times larger since each item should remain in the recording queue for longer than 1 cycle */
    public int NOVELTY_HORIZON = 100000;
}
