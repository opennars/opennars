package nars.johkra;

import nars.johkra.Util;
import org.junit.Test;

/**
 * User: Johannes Krampf <johkra@gmail.com>
 * Date: 18.02.11
 */
public class UtilTest {
    @Test
    public void testSplit() throws Exception {
        assert(Util.split("1,2,3",",",true).size() == 3);
        assert(Util.split("pred(args)","(",false).size() == 2);
    }
}
