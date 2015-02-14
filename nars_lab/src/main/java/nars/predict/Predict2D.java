package nars.predict;

import automenta.vivisect.swing.NWindow;
import nars.build.Default;
import nars.core.Events;
import nars.core.NAR;
import nars.core.Parameters;
import nars.event.AbstractReaction;
import nars.logic.entity.Concept;
import nars.logic.entity.Sentence;
import nars.logic.entity.Term;
import nars.logic.nal1.Inheritance;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by me on 2/13/15.
 */
public class Predict2D extends JPanel {


    private float time;

    public static class Point {
        public final Color color;
        public float x, y;

        public Point(Color c) {
            this.color = c;
        }

        public void update(float t) {

        }

    }

    public List<Point> points = new ArrayList();

    @Override public void paint(Graphics g) {

        g.clearRect(0,0,getWidth(), getHeight());
        int w = (int)(getWidth()/20f);
        int h = (int)(getHeight()/20f);
        for (Point p : points) {
            p.update(time);
            int x = (int)((p.x*0.9f + 1f) * getWidth()/2f);
            int y = (int)((p.y*0.9f + 1f) * getHeight()/2f);
            g.setColor(p.color);
            g.fillOval(x, y, w, h);
        }
    }

    public void update(float t) {
        this.time = t;
        repaint();
    }

    public static class DiscretizedInput  {

        private final float max;
        private final float min;
        private final String term;
        private final NAR nar;
        Discretize discretize;
        private String current, prev;

        public DiscretizedInput(NAR n, String term, int levels, float min, float max) {


            this.nar = n;
            this.term = term;
            this.discretize = new Discretize(n, levels);
            this.min = min;
            this.max = max;

        }


        public void setValue(float v) {
            v = (v - min) / (max-min);
            int i = discretize.i(v);
            setValue(i);
        }
        
        protected void setValue(int i) {
            current = "<" + term + " --> n" + i + ">";

            String input = current + ". :|: \n";

            if (prev!=null) {
                if (!prev.equals(current))
                    input += ("<" + prev + " =/> " + current + ">. :|: \n");
                input += ("<" + current+ " =/> ?>?\n");
            }

            nar.addInput(input);

            prev = current;
        }

    }

    public static class ValuePrediction extends AbstractReaction {

        private final String term;
        private final String prefix, suffix;
        private final NAR nar;
        float v = 0;

        float expect[];


        public ValuePrediction(NAR n, String term, int levels) {
            super(n, Events.ConceptBeliefAdd.class);
            this.nar = n;
            this.term = term;
            this.prefix = "<" + term + " --> ";
            this.suffix = ">";
            this.expect = new float[levels];
        }

        @Override
        public void event(Class event, Object[] args) {
            if (event == Events.ConceptBeliefAdd.class) {
                Concept c = (Concept)args[0];
                int level = match(c);

                if (level!=-1)
                    updateBelief(c, level);
            }
        }

        protected int match(Concept c) {
            Term t = c.getTerm();
            if (t instanceof Inheritance) {
                Inheritance i = (Inheritance)t;
                if (i.getSubject().toString().equals(term)) {
                    Term p = i.getPredicate();
                    if (p.getClass() == Term.class) {
                        String x = p.toString();
                        if (x.startsWith("n"))
                            return Integer.parseInt(x.substring(1));
                    }
                }
            }

            return -1;
        }

        protected void updateBelief(Concept c, int level) {

            expect[level] = 0;
            for (Sentence s : c.beliefs) {
                if (s.isEternal()) continue;
                long o = s.getOccurenceTime();
                if (o <= nar.time()) continue;

                //future belief:
                expect[level] = s.truth.getExpectation();
                System.out.println("PREDICT: " + Arrays.toString(expect));
            }
            //System.out.println(nar.time() + " " + c.beliefs);


        }

        float value() {
            return v;
        }
    }

    public static void main(String[] args) throws InterruptedException {

        NAR n = new NAR(new Default().setInternalExperience(null).simulationTime());

        Predict2D p = new Predict2D();
        new NWindow("Predicting", p).show(400,400,true);

        Point target, belief;

        Parameters.DEBUG = true;

        int levels = 8;
        DiscretizedInput ix = new DiscretizedInput(n, "x", levels, -1f, 1f);
        DiscretizedInput iy = new DiscretizedInput(n, "y", levels, -1f, 1f);

        ValuePrediction px = new ValuePrediction(n, "x", levels);
        ValuePrediction py = new ValuePrediction(n, "y", levels);

        p.points.add(target = new Point(Color.RED) {
            @Override
            public void update(float ms) {
                this.x = (float)Math.sin(ms / 1000f);
                this.y = (float)Math.cos(ms / 1000f);
            }
        });
        p.points.add(belief = new Point(Color.BLUE) {
            @Override
            public void update(float ms) {
                this.x = px.value();
                this.y = py.value();
            }
        });

        //TextOutput.out(n);

        float t = 0;
        while (true) {

            ix.setValue(target.x);
            iy.setValue(target.y);

            p.update(t);

            n.memory.addSimulationTime(10);
            n.step(100);

            t += 100;

            Thread.sleep(100);
        }

    }
}
