/*
 * Distributor.java
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
 * along with Open-NARS.  If not, see <http://www.gnu.org/licenses/>.
 */
package nars.util.math;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * A pseudo-random number generator, used in Bag.
 */
public final class Distributor {

    /**
     * Shuffled sequence of index numbers
     */
    public final short[] order;
    /** Capacity of the array */
    public final int capacity;

    private static final Map<Integer,Distributor> distributors = new HashMap(8);
    public static Distributor get(int range) {
        Distributor d = distributors.get(range);
        if (d==null) {
            d = new Distributor(range);
            distributors.put(range, d);
        }
        return d;
    }
    
    /**
     * For any number N < range, there is N+1 copies of it in the array, distributed as evenly as possible
     * @param range Range of valid numbers
     */
    protected Distributor(int range) {
        int index, rank, time;
        capacity = (range * (range + 1)) / 2;
        order = new short[capacity];
        
        Arrays.fill(order, (short)-1);
        index = capacity;
        
        for (rank = range; rank > 0; rank--) {
            int capDivRank = capacity / rank;
            for (time = 0; time < rank; time++) {
                index = (capDivRank + index) % capacity;
                while (order[index] >= 0) {
                    index = (index + 1) % capacity;
                }
                order[index] = (short)(rank - 1);
            }
        }
    }

    /**
     * Get the next number according to the given index
     * @param index The current index
     * @return the random value
     */
    public short pick(int index) {
        return order[index];
    }

    /**
     * Advance the index
     * @param index The current index
     * @return the next index
     */
    public int next(int index) {
        return (index + 1) % capacity;
    }
}
