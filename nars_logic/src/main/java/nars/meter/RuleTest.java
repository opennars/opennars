package nars.meter;

import nars.meta.TaskRule;
import nars.nar.SingleStepNAR;
import nars.narsese.NarseseParser;
import nars.task.Task;

/**
 * test an invididual premise
 */
public class RuleTest extends TestNAR {



    public static RuleTest from(TaskRule r) {
        //TODO eventually make this handle all of r's postconditions and modifiers, etc
        String task = r.task().toString();
        task = task.replace("%", "p"); //TODO do proper term replacement
        char taskPunc = '.';

        String belief = r.belief().toString();
        belief = belief.replace("%", "p"); //TODO do proper term replacement
        char beliefPunc = '.';

        String conc = r.conclusion().term(0).toString();
        conc = conc.replace("%", "p");
        char concPunc = '.';

        return new RuleTest(task + taskPunc, belief + beliefPunc, conc + concPunc);

    }

    public RuleTest(String task, String belief, String result) {
        this(task, belief, result, 0, 1, 0, 1);
    }
    public RuleTest(String task, String belief, String result, float minFreq, float maxFreq, float minConf, float maxConf) {
        this(new SingleStepNAR(), task, belief, result, minFreq, maxFreq, minConf, maxConf);
    }


    static final NarseseParser p = NarseseParser.the();

    public RuleTest(SingleStepNAR nar, String task, String belief, String result, float minFreq, float maxFreq, float minConf, float maxConf) {
        this(nar, nar.task(task), nar.task(belief), result, minFreq, maxFreq, minConf, maxConf);

    }
    public RuleTest(SingleStepNAR nar, Task task, Task belief, String result, float minFreq, float maxFreq, float minConf, float maxConf) {
        super(nar);

        nar.input(task);
        nar.input(belief);

        mustBelieve(25, result, minFreq, maxFreq, minConf, maxConf);

    }


}
