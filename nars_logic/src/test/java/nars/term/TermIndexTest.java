package nars.term;

import javassist.scopedpool.SoftValueHashMap;
import nars.NAR;
import nars.index.GuavaIndex;
import nars.index.MapIndex;
import nars.index.MapIndex2;
import nars.nal.nal7.Sequence;
import nars.nar.Default;
import nars.nar.Terminal;
import nars.task.Task;
import nars.term.compile.TermIndex;
import nars.term.compound.Compound;
import nars.time.FrameClock;
import nars.util.data.map.UnifriedMap;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.Assert.*;


public class TermIndexTest {

    @Test
    public void testTaskTermSharing1() {

        NAR t = new Terminal();

        String term = "<a --> b>.";

        Task t1 = t.inputTask(term);
        Task t2 = t.inputTask(term);

        testShared(t1, t2);

    }

    void testIndex(TermIndex i) {
        testTermSharing(i);
        testSequenceNotShared(i);
    }

    @Test public void testTermSharing1() {
        testIndex(new MapIndex(new HashMap(), new HashMap()));
    }
    @Test public void testTermSharing2() {
        testIndex(new MapIndex(new UnifriedMap(), new UnifriedMap()));
    }
    @Test public void testTermSharing3() {
        testIndex(new MapIndex(new SoftValueHashMap(), new SoftValueHashMap()));
    }
    @Test public void testTermSharing5() {
        testIndex(new MapIndex2(new HashMap()));
    }

//    @Test public void testTermSharing4() {
//        testIndex(new MapIndex(new WeakHashMap(), new WeakHashMap()));
//    }
    @Test public void testTermSharingGuava() {
        testIndex(new GuavaIndex());
    }


    void testTermSharing(TermIndex tt) {
        NAR n = new Terminal(tt, new FrameClock());

        testShared(n, "<<x-->w> --> <y-->z>>");
        testShared(n, "<a --> b>");
        testShared(n, "(c, d)");
        testShared(n, "<e <=> f>");
        testShared(n, "g");

        //tt.print(System.out);
        //System.out.println();

    }


    public void testSequenceNotShared(TermIndex i) {
        NAR n = new Terminal(i);

        Termed a = n.term("(&/, 1, /2)");
        assertNull(n.memory.index.getTermIfPresent(a));

        Termed b = n.term("(&/, 1, /3)");
        assertNull(n.memory.index.getTermIfPresent(b));

        assertFalse(((Sequence)a).equals2((Sequence) b.term()));

    }

    private void testNotShared(NAR n, String s) {
        Term t1 = n.term(s); //create by parsing
        Term t2 = n.term(s); //create by parsing again
        assertEquals(t1, t2);
        assertTrue(t1 != t2);
    }

    private void testShared(NAR n, String s) {
        TermIndex i = n.memory.index;
        int t0 = i.size();
        int s0 = i.subtermsCount();

        Term a = n.term(s); //create by parsing

        int t1 = i.size();
        int s1 = i.subtermsCount();

        //some terms and subterms were added
        if (a.isCompound()) {
            assertTrue(t0 < t1);
            assertTrue(s1 + " subterms indexed for " + t0 + " terms", s0 < s1);
        }

        Term a2 = n.term(s); //create by parsing again
        testShared(a, a2);

        assertEquals(i.size(), t1 /* unchanged */);
        assertEquals(i.subtermsCount(), s1 /* unchanged */);

        //i.print(System.out); System.out.println();

        //create by composition
        Compound b = n.term('(' + s + ')');
        testShared(a, b.term(0));

        assertEquals(i.size(), t1 + 1 /* one more for the product container */);

        //i.print(System.out); System.out.println();

        assertEquals(i.subtermsCount(), s1 + 1 /* unchanged */);

        //create by transformation (substitution)
        //testShared(a, n.term(..).substMap(..
    }

    static void testShared(Termed t1, Termed t2) {
        //t.memory.terms.forEachTerm(System.out::println);

        assertEquals(t1.term(), t2.term());
        if (t1 != t2)
            System.err.println("share failed: " + t1 + " " + t1.getClass() + " " + t2 + " "+ t2.getClass());

        assertTrue(t1 == t2);

        if (t1 instanceof Compound) {
            //test all subterms are shared
            for (int i = 0; i < t1.term().size(); i++)
                testShared(((Compound) t1).term(i), ((Compound) t2).term(i));
        }
    }

    @Ignore
    @Test
    public void testRuleTermsAddedToMemoryTermIndex() {
        NAR d = new Default(100, 1, 1, 1);
        Set<Term> t = new TreeSet();
        d.memory.index.forEach(x -> t.add(x.term()));

        assertTrue(t.size() > 100); //approximate

        //t.forEach(System.out::println);

    }
}