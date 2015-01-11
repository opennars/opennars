package nars.jwam.datastructures;

import java.util.Arrays;

/**
 * Just an array list, but then with primitive integers. This is
 * compacter/faster because one does not need wrappings in Integer objects.
 *
 * @author Bas Testerink, Utrecht University, The Netherlands
 *
 */
public class IntArrayList {

    public int[] data;
    private int size = 0;

    /**
     * Constructor. Use an existing integer array as a basis for an array list.
     *
     * @param data Integer array that serves as the initial data.
     * @param size The amount of integers that are already present in the array
     * list.
     */
    public IntArrayList(int[] data, int size) {
        this.data = data;
        this.size = size;
    }

    /**
     * Constructor. Initializes with capacity 8.
     */
    public IntArrayList() {
        data = new int[8];
    }

    /**
     * Get the integer from a given index.
     *
     * @param index The index of the integer.
     * @return
     */
    public int get(int index) {
        //RangeCheck(index);
        return data[index];
    }

    /**
     * Search and remove the first occurrence of the given integer.
     *
     * @param element The element to remove.
     */
    public void remove(int element) {
        for (int i = 0; i < size; i++) // Search through the array
        {
            if (data[i] == element) {
                removeAt(i);						// Found it, so remove the integer
                return;
            }
        }
    }

    /**
     * Add an integer to the end of the array list.
     *
     * @param element
     */
    public void add(int element) {
        data[size] = element;					// Add the integer
        size++;
        if (size == data.length) // Expand if necessary
        {
            data = Arrays.copyOf(data, data.length * 2);
        }
    }

    /**
     * Add an integer in the array on a given index.
     *
     * @param index Index to add the integer.
     * @param element The integer to add.
     */
    public void add(int index, int element) {
        //RangeCheck(index - 1);						// Check if the index is legal (-1 for the case where the index equals size())
        for (int i = size; i > index; i--) // Shove all consecutive integers backwards
        {
            data[i] = data[i - 1];
        }
        data[index] = element; 					// Add the new element
        size++; 									// Update the size
        if (size == data.length) // Expand if necessary 
        {
            data = Arrays.copyOf(data, data.length * 2);
        }
    }

    /**
     * Get the last integer from the array.
     *
     * @return The last integer.
     */
    public int getLast() {
        return data[size - 1];
    }

    /**
     * Removes and returns the last integer. This is faster than
     * removeIntAt(size()-1).
     *
     * @return The last integer prior to removing the last.
     */
    public int removeLast() {
        // Remove the last integer 
        return data[--size];
    }

    /**
     * Remove the integer at the given index (indices start at 0).
     *
     * @param index The position from which to remove the integer.
     * @return The removed integer.
     */
    public int removeAt(int index) {
        //RangeCheck(index);
        size--;
        if (index != size) { 							// It was not the last integer that was removed
            int r = data[index];					// Get the integer's value
            System.arraycopy(data, index, data, index+1, size-index);
            /*
            for (int i = index; i < size; i++) // Shove all integers forwards
                data[i] = data[i + 1];             */
            return r;			// Return the removed element
        }
        return data[index];					// Last was removed so return that integer
    }

    /**
     * Converts the array list to a String representation.
     */
    @Override
    public String toString() {
        String s = "[";
        for (int i = 0; i < size; i++) {
            s += data[i];
            if (i < size - 1) {
                s += ",";
            }
        }
        return s + "]";
    }

    /**
     * Add the content of another array list in this one.
     *
     * @param other
     */
    public void addAll(IntArrayList other) {
        while (data.length <= (size() + other.size)) // Expand if necessary
        {
            data = Arrays.copyOf(data, data.length * 2);
        }
        System.arraycopy(other.data, 0, data, size(), other.size());
        size += other.size;
    }

    /**
     * Remove the entries from the list.
     */
    public void clear() {
        size = 0;
    }

    /**
     * Check whether the array is empty.
     *
     * @return True if the size equals 0, false otherwise.
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Get the size of the array list, which is equal to the number of elements
     * in it.
     *
     * @return The array list's size.
     */
    public int size() {
        return size;
    }

    /**
     * Taken from the ArrayList class. Checks whether an index is appropriate
     * given the size of the array list.
     *
     * @param index The index to check.
     */
    private void RangeCheck(int index) {
        if (index >= size) {
            throw new IndexOutOfBoundsException(
                    "Index: " + index + ", Size: " + size);
        }
    }
}
