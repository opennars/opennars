/*
 * Stamp.java
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
 * GNU General Pbulic License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Open-NARS.  If not, see <http://www.gnu.org/licenses/>.
 */
package nars.truth;

import nars.Global;

import java.util.Arrays;

public interface Stamp {

    /***
     * zips two evidentialBase arrays into a new one
     */
    static long[] zip(long[] a, long[] b) {

        int baseLength = Math.min(a.length + b.length, Global.MAXIMUM_EVIDENTAL_BASE_LENGTH);

        long[] c = new long[baseLength];

        int firstLength = a.length;
        int secondLength = b.length;

        int i2 = 0, j = 0;
        //https://code.google.com/p/open-nars/source/browse/trunk/nars_core_java/nars/entity/Stamp.java#143
        while (i2 < secondLength && j < baseLength) {
            c[j++] = b[i2++];
        }
        int i1 = 0;
        while (i1 < firstLength && j < baseLength) {
            c[j++] = a[i1++];
        }
        return c;
    }

    static long[] toSetArray(long[] x) {
        int l = x.length;

        if (l < 2)
            return x;


        //1. copy evidentialBase and sort it
        long[] sorted = Arrays.copyOf(x, l);
        Arrays.sort(sorted);

        //2. count unique elements
        long lastValue = -1;
        int uniques = 0; //# of unique items
        int sLen = sorted.length;

        for (long v : sorted) {
            if (lastValue != v)
                uniques++;
            lastValue = v;
        }

        if (uniques == sLen) {
            //if no duplicates, just return it
            return sorted;
        }

        //3. de-duplicate
        long[] deduplicated = new long[uniques];
        uniques = 0;
        long lastValue2 = -1;
        for (long v : sorted) {
            if (lastValue2 != v)
                deduplicated[uniques++] = v;
            lastValue2 = v;
        }
        return deduplicated;
    }


    long getCreationTime();

    Stamp setCreationTime(long t);

    default float getOriginality() {
        if (getEvidence() == null)
            throw new RuntimeException(this + " has null evidence");
        return 1.0f / (getEvidence().length + 1);
    }

    /**
     * deduplicated and sorted version of the evidentialBase.
     * this can always be calculated deterministically from the evidentialBAse
     * since it is the deduplicated and sorted form of it.
     */
    long[] getEvidence();

    Stamp setEvidence(long... evidentialSet);

}