package nars.core.logic.nal1;


import nars.core.Build;
import nars.core.build.Default;

public class TestNAL1 extends TestNAL1Only  {

    @Override
    public Build build() {
        return new Default();
    }
}
