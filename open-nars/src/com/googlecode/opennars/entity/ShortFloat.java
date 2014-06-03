/*
 * ShortFloat.java
 *
 * Copyright (C) 2008  Pei Wang
 *
 * This file is part of Open-NARS.
 *
 * Open-NARS is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * Open-NARS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.googlecode.opennars.entity;

/**
 * A float value in [0, 1], with 4 digits accuracy.
 */
public class ShortFloat implements Cloneable {
    
    // the values are saved as short integers (-32768 to 32767, only 0 to 10000 used),
    // but used as float
    private short value;
    
    public ShortFloat(float v) {
        setValue(v);
    }
    
    // access value
    public float getValue() {
        return (float) (value * 0.0001);
    }

    public short getShortValue() {
        return value;
    }
    
    // set new value, rounded, with validity checking
    public void setValue(float v) {
        if ((v < 0) || (v > 1))
            System.err.println("!!! Wrong value: " + v);
        else
            value = (short) (v * 10000.0 + 0.5);
    }
    
    public boolean equals(Object that) {
        return ((that instanceof ShortFloat) && (value == ((ShortFloat) that).getShortValue()));
    }
    
    // full output
    public String toString() {
        if (value == 10000)
            return "1.0000";
        else {
            String s = String.valueOf(value);
            while (s.length() < 4)
                s = "0" + s;
            return "0." + s;
        }
    }
    
    // output with 2 digits, rounded
    public String toString2() {
        String s = toString();
        if (s.length() > 4)
            return s.substring(0, 4);
        else
            return s;
    }
}
