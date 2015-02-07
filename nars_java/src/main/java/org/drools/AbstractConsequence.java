package org.drools;

import org.drools.spi.Consequence;
import org.drools.spi.KnowledgeHelper;

import java.util.function.Consumer;


public interface AbstractConsequence<X> extends Consequence, Consumer<X> {

    @Override
    default public void evaluate(KnowledgeHelper knowledgeHelper, WorkingMemory workingMemory) {

        for (FactHandle fh : knowledgeHelper.getTuple().getFactHandles()) {
            accept( (X)workingMemory.getObject(fh) );
        }

    }

}
