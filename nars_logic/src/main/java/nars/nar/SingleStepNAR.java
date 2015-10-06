package nars.nar;

import nars.meta.RuleMatch;
import nars.meter.DerivationGraph;
import nars.nal.SimpleDeriver;
import nars.task.Task;
import nars.util.db.InfiniPeer;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.stream.Stream;

public class SingleStepNAR extends Default {

    static DerivationGraph derivations = new DerivationGraph(false,false);
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {

            try {
                String fn = InfiniPeer.getTempDir() + "/derivations.txt";
                PrintStream out = new PrintStream(new FileOutputStream(
                        fn));

                derivations.premiseResult.forEach((k,v)-> {

                    out.println(k);// + " " v.actual.size() +

                    for (DerivationGraph.TaskResult t : v.actual) {
                        out.println("\t" + t.key);
                    }
                    out.println();

                });

                System.out.println("Derivation results saved to: " + fn);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }));
    }

    public SingleStepNAR() {
        super(1024, 1, 1, 3);


//        memory.eventConceptProcess.on((p) -> {
//           derivations.add(p)
//        });

    }

    protected SimpleDeriver newDeriver() {
        return new SimpleDeriver(SimpleDeriver.standard) {

            @Override
            public Stream<Task> forEachRule(RuleMatch match) {

                //record an empty derivation, in case nothing is returned in the stream
                //allowing us to see what is mising
                derivations.add(match.premise /* none */);

                Stream<Task> s = super.forEachRule(match).peek(t ->
                        derivations.add(match.premise, t)
                );

                return s;
            }
        };
    }

    @Override
    public FIFOTaskPerception initInput() {
        FIFOTaskPerception input = new FIFOTaskPerception(this,
                task -> task.isInput() /* allow only input tasks*/,
                task -> exec(task)
        );
        return input;
    }
}
