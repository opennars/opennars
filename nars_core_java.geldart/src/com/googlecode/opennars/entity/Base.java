/*
 * Base.java
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

import com.googlecode.opennars.main.Parameters;
import com.googlecode.opennars.parser.Symbols;

/**
 * Each Sentence has a list of serial numbers of a constant length.
 * The input sentence gets a unique number.
 * The derived sentences inherits from its parents.
 */
public class Base implements Cloneable {            // make it part of TruthValue?
    private static final int MAXLENGTH = Parameters.MAXMUM_LABEL_RECORD_LENGTH;
    private static final String opener = Symbols.Base_opener;
    private static final String closer = Symbols.Base_closer;
    private static final String separator = Symbols.Base_separator;
    private static final String separator0 = Symbols.Base_separator0;
    
    // for each base
    private int length;
    private long list[] = new long[MAXLENGTH];
    
    // sequential number, for the whole system
    private static long current = 0;
    
    // generate a new number --- serial number or generation time?
    // called in StringParser
    public Base() {
        current++;
        length = 1;
        list[0] = current;
    }
    
    // the first one is no shorter than the second
    private Base(Base first, Base second) {
        int i1, i2, j;
        i1 = i2 = j = 0;
        while (i2 < second.length() && j < MAXLENGTH) {
            list[j] = first.list(i1);
            i1++;
            j++;
            list[j] = second.list(i2);
            i2++;
            j++;
        }
        while (i1 < first.length() && j < MAXLENGTH) {
            list[j] = first.list(i1);
            i1++;
            j++;
        }
        length = j;
    }
    
    // try to merge two Bases, return null if have overlap
    public static Base make(Base first, Base second) {
        for (int i = 0; i < first.length(); i++)
            for (int j = 0; j < second.length(); j++)
                if (first.list(i) == second.list(j))
                    return null;
        if (first.length() > second.length())
            return new Base(first, second);
        else
            return new Base(second, first);
    }
    
    // Initialize the machenism
    // called in Center
    public static void init() {
        current = 0;
    }
    
    public int length() {
        return length;
    }
    
    public long list(int i) {
        return list[i];
    }

    // to check for subset
    public boolean include(Base s) {
        boolean result = false;
        if (length >= s.length())
            for (int i = 0; i < s.length(); i++) {
                result = false;
                for (int j = 0; j < length; j++)
                    if (list[j] == s.list(i)) {
                        result = true;
                        break;
                    }
                if (result == false)
                    break;
            }
        return result;
    }
    
    public boolean equals(Object that) {
        return ((that instanceof Base) && this.include((Base) that) && ((Base) that).include(this));
    }
    
    // called for update
    public long latest() {
        long v = list[0];
        for (int i = 1; i < length(); i++) 
            if (list[i] > v)
                v = list[i];
        return v;
    }
    
    // for display
    public String toString() {
        String buf = new String(opener + length + separator0);
        for (int i = 0; i < length; i++) {
            buf = buf.concat(Long.toString(list[i]));
            if (i < (length - 1))
                buf = buf.concat(separator);
            else
                buf = buf.concat(closer);
        }
        return buf;
    }
}
