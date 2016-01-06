package nars.nar.experimental;

import nars.Memory;
import nars.nar.AbstractNAR;
import nars.op.app.Commander;
import nars.util.data.random.XorShift128PlusRandom;

import java.util.Random;

/**
 * Base class for ALANN impls
 */
public abstract class AbstractAlann extends AbstractNAR {

	final Random rng = new XorShift128PlusRandom(1);
	final Commander commander;

	protected AbstractAlann(Memory m, int commanderCapacity) {
		super(m);

		commander = new Commander(this, commanderCapacity);
	}

	protected abstract void processConcepts();

	// final ItemAccumulator<Task> newTasks = new
	// ItemAccumulator<>(Budget.plus);

	// public float tlinkToConceptExchangeRatio = 0.1f;

	// final static Deriver deriver;

	// final static Procedure2<Budget,Budget> budgetMerge = Budget.plus;

	// @Deprecated
	// public final Default param = new NewDefault() {
	//
	// @Override
	// protected DerivationFilter[] getDerivationFilters() {
	// return new DerivationFilter[]{
	// new FilterBelowConfidence(0.01),
	// new FilterDuplicateExistingBelief(),
	// new LimitDerivationPriority()
	// //param.getDefaultDerivationFilters().add(new BeRational());
	// };
	// }
	//
	// }; // shadow defaults, will replace once refactored

	// protected final List<Task> sorted = Global.newArrayList();

	// protected void processNewTasks(int maxNewTaskHistory, int
	// maxNewTasksPerCycle) {
	// final int size = newTasks.size();
	// if (size!=0) {
	//
	// int toDiscard = Math.max(0, size - maxNewTaskHistory);
	// int remaining = newTasks.update(maxNewTaskHistory, sorted);
	//
	// if (size > 0) {
	//
	// int toRun = Math.min( maxNewTasksPerCycle, remaining);
	//
	// TaskProcess.run(memory, sorted, toRun, toDiscard);
	//
	// //System.out.print("newTasks size=" + size + " run=" + toRun + "=(" +
	// x.length + "), discarded=" + toDiscard + "  ");
	// }
	// }
	// }
	// protected void processNewTasks() {
	// final int size = newTasks.size();
	// if (size!=0) {
	// newTasks.forEach(t -> process(t));
	// newTasks.clear();
	// }
	// }

	// @Override
	// public void cycle() {
	// processNewTasks();
	// processConcepts();
	// }

	// @Override final public boolean accept(final Task t) {
	// return newTasks.add(t);
	// }
	//
	// @Deprecated @Override public Concept nextConcept() {
	// throw new
	// RuntimeException("should not be called, this method will be deprecated");
	// }
	//

	// public Concept conceptualize(final Termed termed, final Budget budget,
	// final boolean createIfMissing) {
	// final Term term = termed.term();
	//
	// final float activationFactor;
	// // if ((termed instanceof TermLinkBuilder) ||
	// // (termed instanceof TaskLink) || (termed instanceof TermLinkTemplate))
	// {
	// //activationFactor = tlinkToConceptExchangeRatio;
	// // }
	// // else {
	// // //task seed
	// // activationFactor = 1f;
	// // }
	//
	// return null;
	//
	// // return
	// ((MapCacheBag<Term,Concept,?>)(memory.getConcepts())).data.compute(term,
	// (k, existing) -> {
	// // if (existing!=null) {
	// // existing.getBudget().mergePlus(budget, activationFactor);
	// // return existing;
	// // }
	// // else {
	// // Concept c = newConcept(term, memory);
	// // c.getBudget().budget(budget).mulPriority(activationFactor);
	// // return c;
	// // }
	// // });
	// }

	// @Override
	// public boolean reprioritize(Term term, float newPriority) {
	// throw new RuntimeException("N/A");
	// }
	//
	// @Override
	// public Concept remove(Concept c) {
	// Itemized removed = memory().remove(c.getTerm());
	// if ((removed==null) || (removed!=c))
	// throw new RuntimeException("concept unknown");
	//
	// return c;
	// }

	// @Override
	// public Param getParam() {
	// param.the(Deriver.class, NewDefault.der);
	// param.setTermLinkBagSize(32);
	// return param;
	// }

	// @Override
	// public PremiseProcessor getPremiseProcessor(final Param p) {
	// return param.getPremiseProcessor(p);
	// }
	//
	// public Concept newConcept(Term t, Budget b, Bag<Sentence, TaskLink>
	// taskLinks, Bag<TermLinkKey, TermLink> termLinks) {
	//
	// final Concept c;
	// if (t instanceof Atom) {
	// c = new AtomConcept(t,
	// termLinks, taskLinks,
	// memory
	// );
	// }
	// else {
	// c = new DefaultConcept(t,
	// taskLinks, termLinks,
	// memory
	// );
	// }
	//
	//
	// return c;
	// }

	// public Concept newConcept(final Term t, final Memory m) {
	//
	// Bag<Task, TaskLink> taskLinks =
	// new CurveBag<>(rng, /*sentenceNodes,*/ getConceptTaskLinks());
	// taskLinks.mergePlus();
	//
	// Bag<TermLinkKey, TermLink> termLinks =
	// new CurveBag<>(rng, /*termlinkKeyNodes,*/ getConceptTermLinks());
	// termLinks.mergePlus();
	//
	// final Concept c;
	// if (t instanceof Atom) {
	// c = new AtomConcept(t, termLinks, taskLinks);
	// } else {
	// c = new DefaultConcept(t, taskLinks, termLinks, memory);
	// }
	//
	// return c;
	// }

	// private int getConceptTaskLinks() {
	// return -1;
	// }
	//
	// private int getConceptTermLinks() {
	// return -1;
	// }

}
