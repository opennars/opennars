package nars.guifx.graph2.layout;

import nars.concept.Concept;
import nars.guifx.graph2.TermNode;
import nars.task.Task;
import nars.util.data.random.XORShiftRandom;

import java.util.Iterator;
import java.util.Random;

/**
 * Displays the short-term history of concept activation
 * in what can only be described as resembling a comet
 */
public class ConceptComet extends HyperassociativeMap2D {

    double axisTheta = 0;

    final double thickness = 250;
    double timeScale = 10f;
    double now = 0; /* center of view */
    double cutoff = 50;

    final Random rng = new XORShiftRandom();

    protected void init() {
        resetLearning();
        setLearningRate(0.4f);
        setRepulsiveWeakness(repulseWeakness.get());
        setAttractionStrength(attractionStrength.get());
        setMaxRepulsionDistance(250);
        setEquilibriumDistance(0.05f);
    }

    @Override
    public void apply(TermNode node, double[] dataRef) {

        if (node.c == null) {
            node.setVisible(false);
            return;
        }


        Iterator<Task> ii = node.c.iterateTasks(true, true, true, true);
        if (!ii.hasNext()) {
            node.setVisible(false);
            return;
        }
        else {
            node.setVisible(true);
        }

        Task x = Concept.taskCreationTime.max(
                ii
        );

        double dt = now - x.getCreationTime();
        if (dt > cutoff) {
            node.setVisible(false);
            return;
        }

        now = Math.max(now, x.getCreationTime());

        double y = dataRef[1];
        if ((y > thickness) || (y< -thickness)) {
            //TODO scale this by a realtime time-amount
            dataRef[1] *= 0.99; //pressure to shrink y-axis to zero
        }

        dataRef[0] = -(dt * timeScale);
        node.move(dataRef[0], dataRef[1], 0.25, 0.01);

    }


}
