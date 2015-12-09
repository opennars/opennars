package nars.guifx.graph2.layout;

import nars.NAR;
import nars.concept.Concept;
import nars.guifx.graph2.TermNode;
import nars.nal.nal7.Tense;
import nars.task.Task;
import nars.term.Term;

import java.util.Iterator;

/**
 * Displays the short-term history of concept activation
 * in what can only be described as resembling a comet
 */
public class ConceptComet extends HyperassociativeMap2D {

    private final NAR nar;
    double axisTheta = 0;

    final double thickness = 250;
    double timeScale = 10.0f;
    double now = 0; /* center of view */
    double cutoff = 50;

    public ConceptComet(NAR nar /* TODO take any clock */) {
        this.nar = nar;
        nar.onEachFrame(n -> {
            now = nar.time();

            //reset? update?
        });
    }
    //final Random rng = new XORShiftRandom();

//    @Override
//    protected void init() {
//        resetLearning();
//        setLearningRate(0.4f);
//        setRepulsiveWeakness(repulseWeakness.get());
//        setAttractionStrength(attractionStrength.get());
//        setMaxRepulsionDistance(250);
//        setEquilibriumDistance(0.05f);
//    }



    @Override
    public void apply(TermNode node, double[] dataRef) {



        Task x;
        if (node.term instanceof Task) {
            x = (Task)(node.term);
        }
        else if (node.term instanceof Term) {
            if (node.c == null) {
                node.setVisible(false);
                return;
            }


            Iterator<Task> ii = node.c.iterateTasks(true, true, true, true);
            if (!ii.hasNext()) {
                node.setVisible(false);
                return;
            }
            node.setVisible(true);

            x = Concept.taskCreationTime.max(ii);
        }
        else {
            System.out.println("Wtf is " + node);
            node.setVisible(false);
            return;
        }


        long xCreation = x.getCreationTime();
        if (xCreation <= Tense.TIMELESS) {
            xCreation = (long) now;
        }

        double dt = now - xCreation;
        now = nar.time(); //Math.max(now, xCreation);

        System.out.println(dt + " " + now);

        if (dt > cutoff) {
            node.setVisible(false);
            return;
        }




        double y = dataRef[1];
        if ((y > thickness) || (y< -thickness)) {
            //TODO scale this by a realtime time-amount
            dataRef[1] *= 0.99; //pressure to shrink y-axis to zero
        }

        dataRef[0] = -(dt * timeScale);
        node.move(dataRef[0], dataRef[1], 0.5, 0.01);

    }


}
