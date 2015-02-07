package org.drools;

import org.drools.spi.Consequence;
import org.drools.spi.KnowledgeHelper;

import java.util.function.Consumer;

/**
 * Created by me on 2/7/15.
 */
abstract public class AbstractConsequence<X> implements Consequence, Consumer<X> {

    @Override
    public void evaluate(KnowledgeHelper knowledgeHelper, WorkingMemory workingMemory) {

        for (FactHandle fh : knowledgeHelper.getTuple().getFactHandles()) {
            accept( (X)workingMemory.getObject(fh) );
        }

    }

}
