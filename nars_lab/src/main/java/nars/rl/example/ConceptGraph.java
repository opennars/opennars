package nars.rl.example;

import nars.NAR;
import nars.util.index.ConceptMatrix;

/**
 * Abstract graph model of inter-Concept dynamics
 */
abstract public class ConceptGraph extends ConceptMatrix {

    public ConceptGraph(NAR nar) {
        super(nar);
    }


}
