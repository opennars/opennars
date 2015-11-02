package nars.util.math;

import nars.util.data.random.XorShift1024StarRandom;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import static com.google.common.math.IntMath.factorial;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * from http://stackoverflow.com/questions/2920315/permutation-of-array
 */
public class PermutationsTest {

    @Test
    public void testPerm1() {
        Permutations perm = new Permutations().restart(3);
        int count = 0;
        while(perm.hasNext()){
            System.out.println(Arrays.toString(perm.next()));
            count++;
        }
        System.out.println("total: " + count);
        assertEquals(6, count);
    }

    @Test public void testShuffleReset2() {
        testShuffleReset(2, 2);
        testShuffleReset(2, 1);
    }
    @Test public void testShuffleReset3() {
        testShuffleReset(3, 2);
        testShuffleReset(3, 1);
    }
    @Test public void testShuffleReset4() {
        testShuffleReset(4, 5);
        testShuffleReset(4, 4);
        testShuffleReset(4, 3);
        testShuffleReset(4, 2);
        testShuffleReset(4, 1);
    }
    @Test public void testShuffleReset5() {
        testShuffleReset(5, 5);
        testShuffleReset(5, 4);
        testShuffleReset(5, 3);
        testShuffleReset(5, 2);
        testShuffleReset(5, 1);
    }

    public void testShuffleReset(int size, int selected) {

        int expected = factorial(size);
        int attempts = (1+expected/selected)*(1+expected/selected); //just to be safe

        Set<String> sequences = new HashSet();
        Set<String> arrays = new TreeSet();

        XorShift1024StarRandom rng =
                new XorShift1024StarRandom(2);

        int[] n = new int[size];

        ShuffledPermutations perm = new ShuffledPermutations();

        int attempt;
        for (attempt = 1; attempt < attempts; attempt++) {
            perm.restart(size, rng);

            StringBuilder sb = new StringBuilder();
            int x;
            for (x = 0; perm.hasNext() && x < selected; x++) {
                String aa = Arrays.toString(perm.nextShuffled(n));
                arrays.add(aa);
                sb.append(aa).append(' ');
            }

            /*System.out.println(//perm.shuffle +
                    " " + sb.toString());*/
            sequences.add(sb.toString());
            if (arrays.size() == expected) break;
        }

        System.out.println(size + " exhausted all " + expected + " permutations after " + attempt + " attempts when sets of " + selected + " are selected");

        //by this point there should be at least > 1
        assertTrue(sequences.size() >= 1);

        //arrays.forEach(a -> System.out.println(a));

        assertTrue(expected >= arrays.size());
    }

}