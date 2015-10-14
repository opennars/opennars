package nars.meter.experiment;

import nars.Global;
import nars.NAR;
import nars.nal.nal2.Instance;
import nars.nal.nal4.Product;
import nars.nal.nal5.Conjunction;
import nars.nal.nal7.Temporal;
import nars.nal.nal7.Tense;
import nars.nar.Default;
import nars.task.Task;
import nars.term.Atom;
import nars.term.Compound;
import nars.term.Statement;
import nars.term.Term;
import nars.util.java.NALObjects;

import java.util.Collection;

/**
 * Created by me on 10/13/15.
 */
public class BitmapRecognition {

    public static class TermBitmap {
        private Term id;
        int w;
        int h;
        private float[][] on;
        private Compound[][] pixels;
        private Term pixelTerms;

        public TermBitmap() {

        }

        public TermBitmap(String id, int w, int h) {
            this.id = Atom.the(id);
            resize(w, h);

        }
        public final void resize(int w, int h) {
            this.w = w;
            this.h = h;

            on = new float[w][];
            pixels = new Compound[w][];
            Collection<Term> p = Global.newArrayList(w*h);
            for (int x = 0; x < w; x++) {
                on[x] = new float[h];
                pixels[x] = new Compound[h];
                for (int y = 0; y < h; y++) {
                    //on[x][y] = 0;
                    p.add(
                        pixels[x][y] = Instance.make(
                            Product.make(Integer.toString(x), Integer.toString(y)),
                            id
                        )
                    );
                }
            }
            this.pixelTerms = Conjunction.make(p, Temporal.ORDER_CONCURRENT);
        }

        public void askWhich(NAR n, Term... possibilities) {
            applyAndGetPixelState(n);



            for (Term po : possibilities) {
                Statement tt = Instance.make(
                        id, po
                );
                Task q = n.task(tt + "?");// :|:");
                System.err.println("ASK: " + q);
                n.input(q);
            }

//            new AnswerReaction(n, q) {
//
//                @Override
//                public void onSolution(Task belief) {
//                    onSolution.accept(belief);
//                }
//            };
        }



        public Term applyAndGetPixelState(NAR n) {
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    updatePixel(n, x, y);
                }
            }
            return pixelTerms;
        }

        private void updatePixel(NAR n, int x, int y) {
            Compound p = pixels[x][y];
            float on = this.on[x][y];
            n.believe(
                p,
                Tense.Present, on, 0.75f /* conf */);
        }

        public void tell(NAR n, Term similaritage) {

            applyAndGetPixelState(n);
            n.believe(
                Instance.make(
                    pixelTerms,
                    similaritage
                ), Tense.Present, 1.0f, 0.95f
            );
        }

        public void fill(float v) {
            fill(0, 0, w, h, (x,y) -> v);
        }

        @FunctionalInterface interface PixelValue {
            float value(int px, int py);
        }

        public void fill(int x0, int y0, int x1, int y1, PixelValue p) {
            for (int x = x0; x < x1; x++)
                for (int y = y0; y< y1; y++)
                    on[x][y] = p.value(x, y);
        }

    }


    public static void main(String[] args) throws Exception {

        Global.DEBUG = true;
        int size = 2;

        NAR n = new Default();

        TermBitmap tb = new TermBitmap("i",size,size);

        tb = new NALObjects(n).build("i", tb);

        int exposureCycles = 1000;

        for (int i = 0; i < 5; i++) {
            tb.fill(0f);
            tb.tell(n, Atom.the("black"));
            n.frame(exposureCycles);

            tb.fill(1f);
            tb.tell(n, Atom.the("white"));
            n.frame(exposureCycles);
        }

        ///n.log();


        n.memory.eventAnswer.on(tt -> {
            //if (tt.getOne().isInput()) //if question is from input
                System.err.println("\tANS: " + tt.getOne() + ", " + tt.getTwo() );
        });


        for (int i = 0; i < 10; i++) {
            System.out.println(n.concepts().size());
            n.input("(--,<white <-> black>).");

            tb.askWhich(n,
                    Atom.the("white"),
                    Atom.the("black")
                    //n.term("?x"),
                    //n.term("$x")
                    );
            n.frame(exposureCycles);
        }

    }
}
