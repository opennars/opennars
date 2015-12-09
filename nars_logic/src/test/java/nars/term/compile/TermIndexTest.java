package nars.term.compile;

import nars.MapIndex;
import nars.NAR;
import nars.nar.Default;
import nars.nar.Terminal;
import nars.task.Task;
import nars.term.Term;
import nars.term.compound.Compound;
import nars.util.data.map.UnifriedMap;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class TermIndexTest {

    @Test public void testTaskTermSharing1() {

        final NAR t = new Terminal();

        String term = "<a --> b>.";

        Task t1 = t.inputTask(term);
        Task t2 = t.inputTask(term);

        testShared(t1.getTerm(), t2.getTerm());

    }

    @Test public void testTermSharing1() {
        testTermSharing(new MapIndex(new HashMap()));
        testTermSharing(new MapIndex(new UnifriedMap()));
    }

    public void testTermSharing(TermIndex tt) {
        NAR n = new Terminal(tt);

        testShared(n, "<<x-->w> --> <y-->z>>");
        testShared(n, "<a --> b>");
        testShared(n, "(x, y)");
        testShared(n, "<a <=> b>");

        tt.print(System.out);
        System.out.println();

    }

    @Test public void testSequenceNotShared() {
        NAR n = new Terminal();
        testNotShared(n, "(&/, 1, 2, /2, 3, /4)");

    }

    private void testNotShared(NAR n, String s) {
        Term t1 = n.term(s); //create by parsing
        Term t2 = n.term(s); //create by parsing again
        assertEquals(t1, t2);
        assertTrue(t1 != t2);
    }

    private void testShared(NAR n, String s) {
        Term a = n.term(s); //create by parsing
        Term a2 = n.term(s); //create by parsing again
        testShared(a, a2);

        //create by composition
        Compound b = n.term("(" + s + ")");
        testShared(a, b.term(0));

        //create by transformation (substitution)
        //testShared(a, n.term(..).substMap(..
    }

    static void testShared(Term t1, Term t2) {
        //t.memory.terms.forEachTerm(System.out::println);

        assertEquals(t1, t2);
        if (t1!=t2)
            System.err.println("share failed: " + t1);

        assertTrue(t1 == t2);

        if (t1 instanceof Compound) {
            //test all subterms are shared
            for (int i = 0; i < t1.getTerm().size(); i++)
                testShared(((Compound)t1).term(i), ((Compound)t2).term(i));
        }
    }

    @Ignore
    @Test public void testRuleTermsAddedToMemoryTermIndex() {
        final NAR d = new Default(100,1,1,1);
        Set<Term> t = new TreeSet();
        d.memory.index.forEach(x -> t.add(x.getTerm()));

        assertTrue(t.size() > 100); //approximate

        //t.forEach(System.out::println);

    }
}