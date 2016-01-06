package nars.util.meter;

import nars.NAR;
import nars.Narsese;
import nars.nal.PremiseRule;
import nars.nar.Default;
import nars.task.Task;

/**
 * test an invididual premise
 */
public class RuleTest extends TestNAR {

	public static RuleTest from(PremiseRule r) {
		// TODO eventually make this handle all of r's postconditions and
		// modifiers, etc
		String task = r.task().toString();
		task = task.replace("%", "p"); // TODO do proper term replacement

		String belief = r.belief().toString();
		belief = belief.replace("%", "p"); // TODO do proper term replacement

		String conc = r.getConclusion().term(0).toString();
		conc = conc.replace("%", "p");
		char concPunc = '.';

		char beliefPunc = '.';
		char taskPunc = '.';
		return new RuleTest(task + taskPunc, belief + beliefPunc, conc
				+ concPunc);

	}

	public RuleTest(String task, String belief, String result) {
		this(task, belief, result, 0, 1, 0, 1);
	}
	public RuleTest(String task, String belief, String result, float minFreq,
			float maxFreq, float minConf, float maxConf) {
		this(
				// new SingleStepNAR(),
				new Default(), task, belief, result, minFreq, maxFreq, minConf,
				maxConf);
	}

	static final Narsese p = Narsese.the();

	public RuleTest(NAR nar, String task, String belief, String result,
			float minFreq, float maxFreq, float minConf, float maxConf) {
		this(nar, nar.task(task), nar.task(belief), result, minFreq, maxFreq,
				minConf, maxConf);

	}
	public RuleTest(NAR nar, Task task, Task belief, String result,
			float minFreq, float maxFreq, float minConf, float maxConf) {
		super(nar);

		nar.input(task);
		nar.input(belief);

		mustBelieve(25, result, minFreq, maxFreq, minConf, maxConf);

	}

}
