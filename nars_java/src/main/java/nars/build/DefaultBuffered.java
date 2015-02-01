package nars.build;

import nars.control.DefaultCore;
import nars.control.experimental.BufferCore;
import nars.core.Core;

/**
 * Created by me on 2/1/15.
 */
public class DefaultBuffered extends Default {

    @Override
    public Core newCore() {
        return new BufferCore(newConceptBag(), newSubconceptBag(), getConceptBuilder());
    }

}
