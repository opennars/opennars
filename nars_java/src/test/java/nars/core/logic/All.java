package nars.core.logic;

import nars.core.logic.nal1.NAL1Test;
import nars.core.logic.nal4.NAL4Test;
import nars.core.logic.nal5.NAL5Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        NAL1Test.class,
        NAL4Test.class,
        NAL5Test.class,
        NALTest.class
    })
public class All {



}
