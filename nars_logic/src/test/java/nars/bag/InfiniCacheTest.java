package nars.bag;

import nars.NAR;
import nars.bag.impl.CacheBag;
import nars.bag.impl.InfiniCacheBag;
import nars.concept.Concept;
import nars.io.out.TextOutput;
import nars.nar.Default;
import nars.term.Term;

/**
 * Created by me on 6/3/15.
 */
public class InfiniCacheTest {

    public static class P2PDefault extends Default {

        private final String uid;

        final static String subconChannel = "subcon";

        protected InfiniCacheBag<Term, Concept> internar;

        public P2PDefault(String uid) {
            this.uid = uid;
        }

        @Override
        public void init(NAR n) {
            super.init(n);
            n.input("schizo(" + uid + ")!");
            n.runWhileInputting(1);
        }

        @Override
        public CacheBag<Term, Concept> newIndex() {
            return (this.internar = InfiniCacheBag.local(uid, subconChannel));
        }

        public InfiniCacheBag<Term, Concept> getInterNAR() {
            return internar;
        }

        @Override
        public String toString() {
            return super.toString() + ":" + uid;
        }
    }


    public static void printInterNAR(InfiniCacheBag<Term, Concept>... x) {

        for (InfiniCacheBag<Term,Concept> y : x) {
            System.out.println(y);
            y.forEach(c -> System.out.println("\t" + c));
        }
        System.out.println();
    }

    public static void main(String[] args) throws InterruptedException {
        P2PDefault ap, bp;

        NAR a = new NAR((ap = new P2PDefault("a")).setActiveConcepts(4));
        NAR b = new NAR((bp = new P2PDefault("b")).setActiveConcepts(4));

        TextOutput.out(a);
        TextOutput.out(b);

        a.input("<a --> b>. %0.75;0.74%");
        a.input("<c &/ d>.");
        a.input("<(d,c,b) ==> a>!");
        a.input("<(a||b) &| c>.");
        a.input("<b --> c>.");

        for (int i = 0; i < 16; i++) {
            a.frame(1);
            b.frame(1);
        }

        Thread.sleep(1000);

        printInterNAR(ap.getInterNAR(), ap.getInterNAR());
    }

}
