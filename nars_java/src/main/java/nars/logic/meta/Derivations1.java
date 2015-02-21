package nars.logic.meta;

import nars.build.Default;
import nars.core.NAR;
import nars.io.ExampleFileInput;
import nars.io.test.TestNAR;
import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.DirectedMultigraph;

public class Derivations1 extends DirectedMultigraph {


    public Derivations1() {

        super((EdgeFactory)null);
    }


    public static void main(String[] args) {


        Derivations d = new Derivations(false, false);

        for (String s : ExampleFileInput.getPaths("test2") ) {
            NAR n = new TestNAR(new Default().setInternalExperience(null).level(3));
            d.record(n);
            n.addInput(ExampleFileInput.getExample(s));
            n.run(400);
        }

        d.print(System.out);
    }
}
