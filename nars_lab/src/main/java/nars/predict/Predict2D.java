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
import java.util.List;

/**
 * Created by me on 2/13/15.
 */
public class Predict2D extends JPanel {


    private float time;

    abstract public class Point {

        public Point() {

        }

        abstract public void update(float t, Graphics g);

        public void draw(Graphics g, float x, float y, Color c) {
            int w = (int)(getWidth()/20f);
            int h = (int)(getHeight()/20f);
            int px = (int)((x*0.9f + 1f) * getWidth()/2f);
            int py = (int)((y*0.9f + 1f) * getHeight()/2f);
            g.setColor(c);
            g.fillOval(px, py, w, h);
        }
    }

    public List<Point> points = new ArrayList();

    @Override public void paint(Graphics g) {

        g.clearRect(0,0,getWidth(), getHeight());

        for (Point p : points) {
            p.update(time, g);
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
        int lastI = -1;

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
            if (lastI == i) return; //prevent duplicate repeats

            current = "<" + term + " --> n" + i + ">";

            String input = current + ". :|: \n";

            if (prev!=null) {
                if (!prev.equals(current)) {
                    input += ("<" + prev + " =/> " + current + ">. :|: \n");
                    input += (prev + ". %0.00;0.90% :/: \n");
                }
                input += ("<" + current+ " =/> ?>?\n");
            }

            nar.addInput(input);

            prev = current;
            lastI = i;
        }

    }

    public static class ValuePrediction extends AbstractReaction {

        private final String term;
        private final String prefix, suffix;
        private final NAR nar;
        private final Discretize disc;
        float v = 0;

        float expect[];


        public ValuePrediction(NAR n, String term, int levels) {
            super(n, Events.ConceptBeliefAdd.class);
            this.nar = n;
            this.term = term;
            this.prefix = "<" + term + " --> ";
            this.suffix = ">";
            this.expect = new float[levels];
            this.disc = new Discretize(n, levels);
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

            //TODO examine all the beliefs of all the concepts each cycle
            //to calculate a smoothed believe according to the current time
            //and the occurrence time, not just when it is updated

            expect[level] = 0;
            for (Sentence s : c.beliefs) {
                if (s.isEternal()) continue;
                long o = s.getOccurenceTime();
                if (o <= nar.time()) continue;



                //future belief:
                float futureFactor = 1.0f / ( o - nar.time());
                expect[level] += Math.max(expect[level], s.truth.getExpectation()) * futureFactor;


                //System.out.println("PREDICT: " + Arrays.toString(expect));
            }


            //System.out.println(c.beliefs);

            //winner take all
            /*
            float best = 0;
            int b = -1;
            for (int i = 0; i < expect.length; i++) {
                if (expect[i] > best) {
                    best = expect[i];
                    b = i;
                }
            }*/
            float b = 0;
            float total = 0;
            for (int i = 0; i < expect.length; i++) {
                b += expect[i] * i;
                total += expect[i];
            }
            if (total!=0) {
                b /= total;


                v = 0;
            /*if (b!=-1)*/
                {
                    v = ((float) disc.continuous(b) - 0.5f) * 2f;
                    //System.out.println(term + " " + b + " " + " " + v);
                }
            }


        }

        float value() {
            return v;
        }
    }

    float tx, ty;

    public Predict2D() throws InterruptedException {
        Parameters.IMMEDIATE_ETERNALIZATION = false;

        NAR n = new NAR(new Default().setInternalExperience(null).simulationTime());
        n.param.shortTermMemoryHistory.set(4);

        Predict2D p = new Predict2D();
        new NWindow("Predicting", p).show(400,400,true);

        Point target, belief;

        Parameters.DEBUG = true;

        int levels = 15;
        DiscretizedInput ix = new DiscretizedInput(n, "x", levels, -1f, 1f);
        DiscretizedInput iy = new DiscretizedInput(n, "y", levels, -1f, 1f);

        ValuePrediction px = new ValuePrediction(n, "x", levels);
        ValuePrediction py = new ValuePrediction(n, "y", levels);

        p.points.add(target = new Point() {
            @Override
            public void update(float ms, Graphics g) {
                tx = (float)Math.sin(ms / 1000f);
                ty = (float)Math.cos(ms / 1000f);
                draw(g, tx, ty, Color.RED);
            }
        });

        p.points.add(belief = new Point() {

            @Override
            public void update(float ms, Graphics g) {
                float x = (float)Math.sin(ms / 1000f);
                float y = (float)Math.cos(ms / 1000f);
                draw(g, x, y, Color.RED);
            }
        });

        //TextOutput.out(n);

        float t = 0;
        while (true) {

            ix.setValue(tx);
            iy.setValue(ty);


            p.update(t);

            n.memory.addSimulationTime(1);
            n.step(50);

            t += 1;

            Thread.sleep(5);
        }


    }
    public static void main(String[] args) throws InterruptedException {
        new Predict2D();
    }
}
