package nars.core.logic.nal4;

import nars.core.Build;
import nars.core.build.Default;

/**
 * Created by me on 1/14/15.
 */
public class NAL4onNAL6Only extends NAL4Test {

    @Override
    public Build build() {
        return new Default().level(6);
    }

}
