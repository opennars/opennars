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
    /** for thorough bag debugging (slow), requires DEBUG=true */
    public static final boolean DEBUG_BAG = false;
    /* for thorough sentence debugging (slow), requires DEBUG=true */
    public static final boolean DEBUG_SENTENCES = false;
    
    /** Set to true by the test system, leave false */
    public static boolean TEST = false;
}
