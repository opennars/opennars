/* 
 * The MIT License
 *
 * Copyright 2018 The OpenNARS authors.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.opennars.main;

/**
 * Nar operating parameters.
 * All static values will be removed so that this is an entirely dynamic class.
 */
public class Debug {
    /**
     * ========================================================
     * The following options add additional logging and outputs
     * These do not impact the execution of the system
     * ========================================================
     */

    /** Show errors in reasoning cycle, they are not fatal but ideally should not be hidden, recommended. */
    public static boolean SHOW_REASONING_ERRORS = true;
    /** Show execution errors in operators, they ideally should not be hidden, recommended. */
    public static boolean SHOW_EXECUTION_ERRORS = true;
    /** Show input errors, not recommended as the program that uses NARS should handle them by itself */
    public static boolean SHOW_INPUT_ERRORS = false;
    /** Show premises (parents) of statements */
    public static boolean PARENTS = false;



    /**
     * =======================================================
     * The following options change the behavior of the system
     * Some may slow down the time per cycle or violate AIKR
     * =======================================================
     */

    /** Whether the system tries to continue after occurrence of a reasoning error, recommended as not all cases may be tested */
    public static boolean REASONING_ERRORS_CONTINUE = true;
    /** Whether the system tries to continue after occurrence of an execution error, recommended as these are not always avoidable */
    public static boolean EXECUTION_ERRORS_CONTINUE = true;
    /** Whether the system should continue after an input error, not recommended as it should be handled externally */
    public static boolean INPUT_ERRORS_CONTINUE = false;
    /** Use this for advanced error checking, at the expense of lower performance. */
    public static boolean DETAILED = false;
    /** For thorough sentence debugging (slow), requires DETAILED=true */
    public static final boolean DETAILED_SENTENCES = false;
    /** Set this to generate ancestry tree, not yet implemented */
    public static final boolean ANCESTRY = false;

    /** Set to true by the test system, leave false */
    public static boolean TEST = false;
}
