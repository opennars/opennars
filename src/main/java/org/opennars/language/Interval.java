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
package org.opennars.language;

import org.opennars.io.Symbols;

/**
 * This stores the magnitude of a time difference, which is the logarithm of the time difference
 * in base D=duration ( @see Param.java ).  The actual printed value is +1 more than the stored
 * magnitude, so for example, it will have name() "+1" if magnitude=0, and "+2" if magnitude=1.
 * 
 * @author Pei Wang
 * @author Patrick Hammer
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
