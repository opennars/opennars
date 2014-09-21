package nars.util.graph;

import nars.core.Memory;
import nars.core.NAR;
import nars.entity.Sentence;
import nars.io.Symbols;
import nars.language.Statement;

/**
 *
 * @author me
 */


public class ImplicationGraph extends SentenceGraph {

    float minConfidence = 0.01f;

    public ImplicationGraph(NAR nar) {
        super(nar.memory);
    }
    public ImplicationGraph(Memory memory) {
        super(memory);
    }
    
    @Override
    public boolean allow(final Sentence s) {        
        float conf = s.truth.getConfidence();
        if (conf > minConfidence)
            return true;
        return false;
    }


    @Override
    public boolean allow(final Statement st) {
        Symbols.NativeOperator o = st.operator();
        if ((o == Symbols.NativeOperator.IMPLICATION_WHEN) || (o == Symbols.NativeOperator.IMPLICATION_BEFORE)) {
            return true;
        }
        return false;
    }
    
    
}
