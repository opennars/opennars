package nars.bag;

import nars.analyze.experimental.NullItem;
import nars.bag.impl.CurveBag;
import nars.util.data.random.XorShift1024StarRandom;
import org.junit.Test;

/**
 */
public class CurveBagBatchUpdateTest {

    @Test
    public void testCurveBag() {

        final int size = 16;

        CurveBag cb = new CurveBag(new XorShift1024StarRandom(1), size);

    }
}
