package nars.term.compile;

import nars.NAR;
import nars.nar.Default;
import nars.nar.Terminal;
import nars.task.Task;
import nars.term.Term;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Set;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class TermIndexTest {

    @Test public void testTermSharing1() {
        final NAR t = new Terminal();

        String term = "<a --> b>.";

        Task t1 = t.inputTask(term);
        Task t2 = t.inputTask(term);

        //t.memory.terms.forEachTerm(System.out::println);

        assertEquals(t1.getTerm(), t2.getTerm());
        assertTrue(t1.getTerm() == t2.getTerm());
        for (int i = 0; i < t1.getTerm().size(); i++)
            assertTrue(t1.getTerm().term(i) == t2.getTerm().term(i));

    }

    @Ignore
    @Test public void testRuleTermsAddedToMemoryTermIndex() {
        final NAR d = new Default(100,1,1,1);
        Set<Term> t = new TreeSet();
        d.memory.index.forEachTerm(x -> t.add(x.getTerm()));

        assertTrue(t.size() > 100); //approximate

        //t.forEach(System.out::println);

    }
}