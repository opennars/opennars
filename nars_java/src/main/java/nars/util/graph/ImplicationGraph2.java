package nars.util.graph;

import nars.core.NAR;
import nars.logic.NALOperator;
import nars.logic.entity.CompoundTerm;

/** Maintains a directed grpah of Inheritance and Similiarty statements */
public class ImplicationGraph2 extends StatementGraph {


    public ImplicationGraph2(NAR nar) {
        super(nar.memory);
    }
    


    @Override
    public boolean allow(final CompoundTerm st) {
        NALOperator o = st.operator();

        if ((o == NALOperator.IMPLICATION))
            return true;

        return false;
    }

    
    
}
