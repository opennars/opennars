package nars.scheme.op;


import nars.build.Default;
import nars.core.NAR;
import nars.io.TextOutput;

public class TestEvalScheme {

    public static void main(String[] args) {

        NAR n = new NAR(new Default());

        TextOutput.out(n);

        n.addPlugin(new Scheme());

        n.addInput("scheme((*, car, (*, 3, 3)), #x)!");

        n.run(4);


    }
}
