package nars.util.graph;

import nars.NAR;
import nars.nal.NALOperator;
import nars.nal.term.Compound;

/** Maintains a directed grpah of Inheritance and Similiarty statements */
public class ImplicationGraph2 extends StatementGraph {


    public ImplicationGraph2(NAR nar) {
        super(nar.memory);
    }
    


    @Override
    public boolean allow(final Compound st) {
        NALOperator o = st.operator();

        if ((o == NALOperator.IMPLICATION))
            return true;

        return false;
    }

    
    
}
