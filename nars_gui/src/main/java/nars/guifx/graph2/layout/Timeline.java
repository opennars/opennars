package nars.guifx.graph2.layout;

import nars.concept.Concept;
import nars.guifx.graph2.TermNode;
import nars.task.Task;
import nars.util.data.random.XORShiftRandom;

import java.util.Iterator;
import java.util.Random;

/**
 * Created by me on 10/9/15.
 */
public class Timeline extends HyperassociativeMap2D {

    double axisTheta = 0;

    final double thickness = 250;
    double timeScale = 10f;
    double now = 0; /* center of view */
    double cutoff = 50;

    final Random rng = new XORShiftRandom();

    @Override
    public void init(TermNode n) {
        super.init(n);
        n.move(0, rng.nextDouble() * thickness);
    }

    @Override
    public void apply(TermNode node, double[] dataRef) {
        if (!node.isVisible())
            return;

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
