package nars.util.meter;

import nars.NAR;
import nars.concept.Concept;
import nars.concept.util.BeliefTable;
import nars.nal.nal7.Tense;
import nars.term.compound.Compound;
import nars.truth.TruthWave;

/** utility class for analyzing the belief/goal state of a concept */
public class BeliefAnalysis extends EnergyAnalysis {

	public final Compound term;

	public BeliefAnalysis(NAR n, Compound term) {
		super(n);
		this.term = term;
	}

	public BeliefAnalysis(NAR n, String term) {
		this(n, (Compound) n.term(term));
	}

	public BeliefAnalysis believe(float freq, float conf) {
		nar.believe(term, freq, conf);
		return this;
	}

	public BeliefAnalysis believe(float freq, float conf, Tense present) {
		nar.believe(term, present, freq, conf);
		return this;
	}

	public Concept concept() {
		return nar.concept(term);
	}

	public BeliefTable beliefs() {
		Concept c = concept();
		if (c == null)
			return BeliefTable.EMPTY;
		return c.getBeliefs();
	}

	public TruthWave wave() {
		return beliefs().getWave();
	}

	public BeliefAnalysis run(int frames) {
		nar.frame(frames);
		return this;
	}

	public void print() {
		System.out.println("Beliefs[@" + nar.time() + "] " + beliefs().size()
				+ '/' + beliefs().getCapacity());
		beliefs().print(System.out);
		System.out.println();
	}

	public int size() {
		return beliefs().size();
	}

	public void printWave() {
		wave().print();
	}
}
