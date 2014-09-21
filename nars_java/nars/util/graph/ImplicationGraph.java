package nars.util.graph;

import nars.core.NAR;
import nars.entity.Sentence;
import nars.io.Symbols;
import nars.language.Statement;

/**
 *
 * @author me
 */


public class ImplicationGraph extends SentenceGraph {

    float minConfidence = 0.1f;
    float minFreq = 0.1f;

    public ImplicationGraph(NAR nar) {
        super(nar);
    }
    
    @Override
    public boolean allow(final Sentence s) {
        float freq = s.truth.getFrequency();
        float conf = s.truth.getConfidence();
        if ((freq > minFreq) && (conf > minConfidence))
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
