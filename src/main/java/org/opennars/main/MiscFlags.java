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

/**
 * Nar operating parameters.
 * All static values will be removed so that this is an entirely dynamic class.
 */
public class MiscFlags {
    public static final boolean SHOW_REASONING_ERRORS=false; //currently false because the sentence constructor is the only one
                                                       //who creates them but is not doing it because of an error.

    /** use this for advanced error checking, at the expense of lower performance.
     it is enabled for unit tests automatically regardless of the value here.    */
    public static boolean DEBUG = false;
    public static boolean TEST_RUNNING = false;

    /** for thorough bag debugging (slow) */
    public static final boolean DEBUG_BAG = false;
    public static final boolean DEBUG_INVALID_SENTENCES = true;
    
    /** equivalency based on Term contents; experimental mode - not ready yet, leave FALSE */
    public static final boolean TERM_ELEMENT_EQUIVALENCY = false; //TODO check potential, if can't work, remove
    
    /** enables the parsing of functional input format for operation terms: function(a,b,...) */
    public static boolean FUNCTIONAL_OPERATIONAL_FORMAT = true;
}
