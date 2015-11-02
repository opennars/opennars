package nars.util.math;

import org.junit.Test;

import java.util.Arrays;

import static junit.framework.TestCase.assertEquals;

/**
 * from http://stackoverflow.com/questions/2920315/permutation-of-array
 */
public class PermutationsTest {

    @Test
    public void testPerm1() {
        Permutations<Integer> perm = new Permutations<Integer>(new Integer[]{0,1,2});
        int count = 0;
        while(perm.hasNext()){
            System.out.println(Arrays.toString(perm.next()));
            count++;
        }
        System.out.println("total: " + count);
        assertEquals(6, count);
    }
}