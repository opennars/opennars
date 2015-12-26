package nars.nal.nal8;

import nars.$;
import nars.Global;
import nars.NAR;
import nars.Narsese;
import nars.nal.nal7.Tense;
import nars.nar.Default;
import nars.term.Term;
import nars.util.event.On;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertTrue;

/**
 * Created by me on 12/26/15.
 */
public class QuestTest {

    int exeCount = 0;

    @Test
    public void testQuest() throws Narsese.NarseseException {

        Global.DEBUG = true;

        String term = "<a --> b>";

        NAR nar = new Default(1, 1, 1, 1);

        On exeFunc = nar.onExecTerm("exe", (Term[] t) -> {
            exeCount++;
            return $.the("a");
        });

        //nar.log();

        nar.goal(nar.term(term), Tense.Eternal, 1.0f, 0.9f);
        nar.frame();

        AtomicBoolean valid = new AtomicBoolean(false);

        nar.answer(nar.task(term + '@'), a -> {
            //System.out.println("answer: " + a);
            //System.out.println(" " + a.getLog());
            if (a.toString().contains("<a-->b>!"))
                valid.set(true);
        });

        nar.frame(1);

        assertTrue(valid.get());
    }


}
