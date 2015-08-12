package nars.temporal;

import nars.NAR;
import nars.event.CycleReaction;
import nars.io.out.TextOutput;
import nars.nal.nal1.Inheritance;
import nars.nal.nal7.Tense;
import nars.nar.experimental.Equalized;
import nars.rl.gng.NeuralGasNet;
import nars.task.Task;
import nars.term.Atom;

import java.util.Random;

/**
 * Created by me on 8/12/15.
 */
public class TimeClustering {

    final NeuralGasNet centroids = new NeuralGasNet(1, 4);

    public boolean add(Task input) {
        if (input.isEternal()) return false;
        centroids.learn( input.getOccurrenceTime() );

        return true;
    }

    public static class RandomEventGenerator extends CycleReaction {


        private final Atom id;
        int uniqueTerms = 4;
        int tasksPerCycle = 16;
        private final NAR nar;
        Random rng = new Random();

        public RandomEventGenerator(String id, NAR nar) {
            super(nar);
            this.nar = nar;
            this.id = Atom.the(id);
        }

        @Override public void onCycle() {
            for (int i = 0; i < tasksPerCycle; i++) {
                nar.believe(

                        Inheritance.make(
                                Atom.the("" + (rng.nextInt(uniqueTerms) + 'a')),
                                id
                        ),
                        Tense.Present,
                        1f,
                        0.9f
                );
            }
        }
    }

    public static void main(String[] args) {

        Equalized e = new Equalized(1024, 4, 4) {

        };
        NAR n = new NAR(e);

        new RandomEventGenerator("x", n);

        TextOutput.out(n);
        n.loop(100);


    }
}
