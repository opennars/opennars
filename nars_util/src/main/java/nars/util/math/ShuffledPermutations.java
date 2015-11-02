package nars.util.math;

import nars.util.data.array.Arrays;

import java.util.Random;

/**
 * Created by me on 11/1/15.
 */
public class ShuffledPermutations extends Permutations {

    /** starting index of the current shuffle */
    int[] shuffle;

    @Override
    public ShuffledPermutations restart(int size) {
        return restart(size, new Random());
    }

    public ShuffledPermutations restart(int size, Random random) {
        super.restart(size);

        //shuffle = random.nextInt(num);
        int[] shuffle = this.shuffle;
        if (shuffle == null || shuffle.length < size)
            this.shuffle = shuffle = new int[size];

        for (int i = 0; i < size; i++)
            shuffle[i] = i;
        Arrays.shuffle(shuffle, size, random);

        return this;
    }

    @Override
    public final int get(int index) {
        return super.get(this.shuffle[index]);
    }

    public int[] nextShuffled(int[] n) {
        next();
        final int l = this.size;
        for (int i = 0; i < l; i++)
            n[i] = get(i);
        return n;
    }
}
