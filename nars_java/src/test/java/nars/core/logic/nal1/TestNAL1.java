package nars.core.logic.nal1;


import nars.core.Build;
import nars.core.build.Default;

public class TestNAL1 extends TestNAL1Only  {

    //these need extra time because more NAL levels, including NAL9 InternalExperience are involved
    @Override long backwardInferenceTime() { return 1710;    }
    @Override long multistepTime() { return 350;     }
    @Override long conversionTime() { return 305; }

    @Override
    public Build build() {
        return new Default();
    }
}
