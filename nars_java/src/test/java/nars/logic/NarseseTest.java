package nars.logic;

import nars.core.Parameters;
import nars.io.Symbols;
import nars.io.narsese.NarseseParser;
import nars.logic.entity.Task;
import nars.logic.nal1.Inheritance;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by me on 2/2/15.
 */
public class NarseseTest {

    public final static NarseseParser p = NarseseParser.newParser();

    @Test public void testParseCompleteEternalTask() {
        Task t = p.parseTask("$0.99;0.95$ <a --> b>. %1.00;0.95%");


        assertNotNull(t);

        assertEquals(1f, t.sentence.truth.getFrequency(), 0.001);
        assertEquals(0.95f, t.sentence.truth.getConfidence(), 0.001);

        //TODO test other features
    }

    @Test public void testIncompleteTask() {
        Task t = p.parseTask("<a --> b>.");
        assertNotNull(t);
        assertEquals(Symbols.NativeOperator.INHERITANCE, t.sentence.term.operator());
        Inheritance i = (Inheritance)t.getTerm();
        assertEquals("a", i.getSubject().toString());
        assertEquals("b", i.getPredicate().toString());
        assertEquals('.', t.getPunctuation());
        assertEquals(Parameters.DEFAULT_JUDGMENT_PRIORITY, t.budget.getPriority(), 0.001);
        assertEquals(Parameters.DEFAULT_JUDGMENT_DURABILITY, t.budget.getDurability(), 0.001);
        assertEquals(1f, t.sentence.getTruth().getFrequency(), 0.001);
        assertEquals(Parameters.DEFAULT_JUDGMENT_CONFIDENCE, t.sentence.getTruth().getConfidence(), 0.001);
    }

}
