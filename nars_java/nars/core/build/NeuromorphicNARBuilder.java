package nars.core.build;

import nars.core.Attention;
import nars.core.Param;
import nars.core.control.DefaultAttention;
import nars.entity.Concept;
import nars.entity.ConceptBuilder;
import nars.language.Term;
import nars.storage.Bag;
import nars.storage.DelayBag;

/**
 *
 * https://en.wikipedia.org/wiki/Neuromorphic_engineering
 */
public class NeuromorphicNARBuilder extends DefaultNARBuilder {


    @Override
    protected Bag<Concept, Term> newConceptBag(Param p) {
        return new DelayBag(getConceptBagSize());
    }

    
    
}