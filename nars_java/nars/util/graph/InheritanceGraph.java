package nars.util.graph;

import nars.core.NAR;
import nars.entity.Sentence;
import nars.io.Symbols;
import nars.language.CompoundTerm;



public class InheritanceGraph extends SentenceGraph {

    float minConfidence = 0.01f;
    

    public InheritanceGraph(NAR nar) {
        super(nar.memory);
    }
    
    @Override
    public boolean allow(final Sentence s) {        
        float conf = s.truth.getConfidence();
        if (conf > minConfidence)
            return true;
        return false;
    }

    @Override
    public boolean allow(final CompoundTerm st) {
        Symbols.NativeOperator o = st.operator();
        if ((o == Symbols.NativeOperator.INHERITANCE) || (o == Symbols.NativeOperator.SIMILARITY)) {
            return true;
        }
        return false;
    }
    
    
}
