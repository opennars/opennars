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
package org.opennars.storage;

/**
 * A pseudo-random number generator, used in Bag.
 */
public class Distributor {

    /** Shuffled sequence of index numbers */
    public int order[];
    /** Capacity of the array */
    private int capacity;

    /**
     * For any number N &lt; range, there is N+1 copies of it in the array, distributed as evenly as possible
     * @param range Range of valid numbers
     */
    public Distributor(int range) {
        int index, rank, time;
        capacity = (range * (range + 1)) / 2;
        order = new int[capacity];
        for (index = 0; index < capacity; index++) {
            order[index] = -1;
        }
        for (rank = range; rank > 0; rank--) {
            for (time = 0; time < rank; time++) {
                index = ((capacity / rank) + index) % capacity;
                while (order[index] >= 0) {
                    index = (index + 1) % capacity;
                }
                order[index] = rank - 1;
            }
        }
    }

    /**
     * Get the next number according to the given index
     * @param index The current index
     * @return the random value
     */
    public int pick(int index) {
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