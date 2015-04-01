package nars.util.graph;

import nars.NAR;
import nars.nal.NALOperator;
import nars.nal.term.Compound;

/** Maintains a directed grpah of Inheritance and Similiarty statements */
public class InheritanceGraph extends StatementGraph {

    private final boolean includeInheritance;
    private final boolean includeSimilarity;

    public InheritanceGraph(NAR nar, boolean includeInheritance, boolean includeSimilarity) {
        super(nar.memory);
        this.includeInheritance = includeInheritance;
        this.includeSimilarity = includeSimilarity;
    }
    


    @Override
    public boolean allow(final Compound st) {
        NALOperator o = st.operator();
        
        
        
        if ((o == NALOperator.INHERITANCE) && includeInheritance)
            return true;
        if ((o == NALOperator.SIMILARITY) && includeSimilarity)
            return true;

        return false;
    }

    
    
}
