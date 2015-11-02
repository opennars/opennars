package nars.util.math;

import org.apache.commons.math3.util.ArithmeticUtils;

import java.util.NoSuchElementException;

/** * from http://stackoverflow.com/questions/2920315/permutation-of-array */
public class Permutations  {

    int size = 0;

    /** total possible */
    int num;

    /** current iteration */
    int count;

    //private E[] arr;
    private int[] ind;

    //public E[] output;//next() returns this array, make it public

    public Permutations() {
        super();
    }

    public Permutations restart(int size) {


        //this.arr = arr; //TODO clone option: arr.clone();
        this.size = size; //size = arr.length;

        if (ind==null || ind.length<size) {
            ind = new int[size];
        }

        for(int i = 0; i < size; i++){
            ind[i] = i;
        }
        count = 0;
        num = (int) ArithmeticUtils.factorial(size);

        return this;
    }

    public final boolean hasNext() {
        return count < num;
    }

    /**
     * Computes next permutations. Same array instance is returned every time!
     * @return
     */
    public final int[] next() {
        final int size = this.size;
        if (count++ == (1+num) || size == 0)
            throw new NoSuchElementException();

        final int[] ind = this.ind;

        if (count == 1) {
            //first access since restart()
            return ind;
        }

        //final E[] output = this.output;
        //final E[] arr = this.arr;

        //get next permutation
        for(int tail = size - 1;tail > 0;tail--){

            final int tailMin1 = tail - 1;

            if (ind[(tailMin1)] < ind[tail]){//still increasing

                //find last element which does not exceed ind[tail-1]
                int s = size - 1;
                while(ind[(tailMin1)] >= ind[s])
                    s--;

                swap(ind, tailMin1, s);

                //reverse order of elements in the tail
                for(int i = tail, j = size - 1; i < j; i++, j--){
                    swap(ind, i, j);
                }
                break;
            }

        }
        return ind;
    }

    public int get(int index) {
        return ind[index];
    }

    private static final void swap(int[] arr, int i, int j){
        int t = arr[i];
        arr[i] = arr[j];
        arr[j] = t;
    }


}