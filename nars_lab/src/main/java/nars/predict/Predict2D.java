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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

            String input = current + ". :|: %1.00;0.95%\n";

            if (prev!=null) {
                if (!prev.equals(current)) {
                    //input += ("<" + prev + " =/> " + current + ">. :|: \n");
                    //input += (prev + ". %0.00;0.90% :/: \n");
                }
                //input += ("<" + current+ " =/> ?>?\n");
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
        private final int levels;

        Map<Concept, Integer> level = new ConcurrentHashMap();



        public ValuePrediction(NAR n, String term, int levels) {
            super(n, Events.ConceptBeliefAdd.class);
            this.nar = n;
            this.term = term;
            this.levels = levels;
            this.prefix = "<" + term + " --> ";
            this.suffix = ">";
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
            Integer e = level.get(c);
            if (e!=null) return e.intValue();

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

        protected void updateBelief(Concept c, int l) {

            level.put(c, l);
        }


        /** v[0] = scalar value, v[1] = confidence */
        public float[] predictBest(long when, float[] v) {

            v[0] = v[1] = 0;

            //TODO examine all the beliefs of all the concepts each cycle
            //to calculate a smoothed believe according to the current time
            //and the occurrence time, not just when it is updated

            float expect[] = new float[levels];

            for (Map.Entry<Concept,Integer> e : level.entrySet()) {
                Concept c = e.getKey();
                int level = e.getValue();

                if (!c.beliefs.isEmpty()) {
                    for (Sentence s : c.beliefs) {
                        if (s == null) {
                            throw new RuntimeException(c.beliefs.toString());
                        }

                        if (s.isEternal()) continue;
                        long o = s.getOccurrenceTime();
                        if (o <= nar.time()) continue;
                        if (o < when) continue;

                        float futureFactor = 1.0f / (1.0f + Math.abs(o - when));
                        expect[level] += s.truth.getFrequency() * s.truth.getConfidence() * futureFactor;
                    }
                }

            }

            //winner take all

            float best = 0;
            int b = -1;
            float total = 0;
            for (int i = 0; i < expect.length; i++) {
                if (expect[i] > best) {
                    best = expect[i];
                    b = i;
                }
                total += expect[i];
            }
            float avg = total / expect.length;

            if (b!=-1)
                v[0] = ((float) disc.continuous(b) - 0.5f) * 2f;
            else
                v[0] = 0;
            v[1] = avg; //temporary
            return v;

//            float b = 0;
//            float total = 0;
//            for (int i = 0; i < expect.length; i++) {
//                b += expect[i] * i;
//                total += expect[i];
//            }
//            if (total!=0) {
//                b /= total;
//
//
//                v = 0;




        }
    }

    float tx, ty;

    public Predict2D() throws InterruptedException {
        super();
        Parameters.IMMEDIATE_ETERNALIZATION = false;

        NAR n = new NAR(new Default().setInternalExperience(null).simulationTime());
        n.param.shortTermMemoryHistory.set(2);
        n.param.duration.set(5);
        n.param.duration.setLinear(16f);
        n.param.conceptBeliefsMax.set(32);

        int levels = 3;

        float freq = 2f;

        new NWindow("Predicting", this).show(400,400,true);

        Point target, belief;

        Parameters.DEBUG = true;

        DiscretizedInput ix = new DiscretizedInput(n, "x", levels, -1f, 1f);
        DiscretizedInput iy = new DiscretizedInput(n, "y", levels, -1f, 1f);

        ValuePrediction px = new ValuePrediction(n, "x", levels);
        ValuePrediction py = new ValuePrediction(n, "y", levels);

        points.add(target = new Point() {
            @Override
            public void update(float ms, Graphics g) {
                tx = (float)Math.sin(ms / 1000f * freq);
                ty = (float)Math.cos(ms / 1000f * freq);
                draw(g, tx, ty, Color.RED);
            }
        });

        points.add(belief = new Point() {

            float v[] = new float[2];

            @Override
            public void update(float ms, Graphics g) {

                try {
                    for (int j = 0; j < 5; j += 100) {
                        float x, y, c;

                        px.predictBest(n.time() + j, v);
                        x = v[0];
                        c = v[1];

                        py.predictBest(n.time() + j, v);
                        y = v[0];
                        c += v[1];
                        c /= 2f;

                        draw(g, x, y, new Color(j / 256f, j / 256.0f, c));
                    }
                }
                catch (Exception e) { System.out.println(e); } //TODO use correct threading
            }
        });

        //TextOutput.out(n);

        float t = 0;
        while (true) {

            ix.setValue(tx);
            iy.setValue(ty);


            update(t);

            n.memory.addSimulationTime(1);
            n.step(10);

            t += 1;

            Thread.sleep(1);
        }


    }
    public static void main(String[] args) throws InterruptedException {
        new Predict2D();
    }
}
