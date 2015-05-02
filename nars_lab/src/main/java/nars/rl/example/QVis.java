package nars.rl.example;

import automenta.vivisect.swing.NWindow;
import com.google.common.collect.Iterables;
import jurls.core.utils.MatrixImage;
import nars.nal.concept.Concept;
import nars.nal.nal8.Operation;
import nars.nal.term.Term;
import nars.rl.QLAgent;

import java.util.ArrayList;

/**
 * Created by me on 5/2/15.
 */
public class QVis extends MatrixImage implements Runnable {

    final java.util.List<Term> xstates = new ArrayList();
    final java.util.List<Operation> xactions = new ArrayList();
    private final QLAgent agent;
    private final NWindow nmi;

    Data2D mid = new Data2D() {

        @Override
        public double getValue(final int y, final int x) {
            return agent.q(xstates.get(x), xactions.get(y));
        }

    };


    public QVis(QLAgent agent) {
        super(400, 400);
        this.agent = agent;

        nmi = new NWindow("Q", this).show(400, 400);
    }

    @Override
    public void run() {
        repaint();


        //if (xstates.size() != states.size()) {
        xstates.clear();
        Iterables.addAll(xstates, agent.rows);
        //}
        //if (xactions.size() != actions.size()) {
        xactions.clear();
        Iterables.addAll(xactions, agent.cols);
        //}

        repaint();


        draw(mid, xstates.size(), xactions.size(), -1, 1);

        nmi.setTitle(xstates.size() + " states, " + xactions.size() + " actions");

//                    mi.draw(new Data2D() {
//                        @Override
//                        public double getValue(int x, int y) {
//                            return q(y, x);
//                        }
//                    }, nstates, nactions, -1, 1);
    }

    @Override
    public void draw(Data2D d, int cw, int ch, double minValue, double maxValue) {
        super.draw(d, cw, ch, minValue, maxValue);

        for (int i = 0; i < ch; i++) {
            for (int j = 0; j < cw; j++) {
                final double value = d.getValue(i, j);

                float pri = 0;

                Concept c = (Concept) agent.table.get(xstates.get(j), xactions.get(i));
                if (c != null) {
                    pri = c.getPriority();
                }

                int ipri = (int) (127 * pri) + 127;
                int p = image.getRGB(j, i);

                //dim
                p &= (ipri << 24) | 0xffffff; // | (ipri << 16) | (ipri << 8) | (ipri);

                image.setRGB(j, i, p);
            }
        }
    }

}
