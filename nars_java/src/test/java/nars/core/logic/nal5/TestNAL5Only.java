package nars.core.logic.nal5;

import nars.core.Build;
import nars.core.build.Default;

/**
 * Created by me on 1/14/15.
 */
public class TestNAL5Only extends NAL5Test {

    @Override
    public Build build() {
        return new Default().level(5);
    }

}
