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
    /** Show errors in reasoning cycle, they are not fatal but ideally should not be hidden, recommended.*/
    public static final boolean SHOW_REASONING_ERRORS = true;
    /** Whether the system should try to continue after the occurrence of a reasoning error, recommended as not all cases might be tested*/
    public static final boolean REASONING_ERRORS_CONTINUE = true;
    
    /** Show execution errors in operators, they ideally should not be hidden, recommended.*/
    public static final boolean SHOW_EXECUTION_ERRORS = true;
    /** Whether the system should try to continue after the occurrence of an execution error, recommended as they are not always avoidable*/
    public static final boolean EXECUTION_ERRORS_CONTINUE = true;
    
    /** Show input errors, not recommended as the program that uses NARS should handle them by itself */
    public static final boolean SHOW_INPUT_ERRORS = false;
    /** Whether the system should continue after an input error, not recommended as it should be handled from outside*/
    public static boolean INPUT_ERRORS_CONTINUE = false;
    

    /** use this for advanced error checking, at the expense of lower performance.*/
    public static boolean DEBUG = false;
    /** Set to true by the test system, leave false */
    public static boolean TEST = false;

    /** for thorough bag debugging (slow) */
    public static final boolean DEBUG_BAG = false;
    
    /* for thorough sentence debugging (slow) */
    public static final boolean DEBUG_INVALID_SENTENCES = true;
    
    /** equivalency based on Term contents; experimental mode - not ready yet, leave FALSE */
    public static final boolean TERM_ELEMENT_EQUIVALENCY = false; //TODO check potential, if can't work, remove
    
    /** enables the parsing of functional input format for operation terms: function(a,b,...) */
    public static boolean FUNCTIONAL_OPERATIONAL_FORMAT = true;
}
