package nars.struct;

import nars.Memory;
import nars.Op;
import nars.bag.Bag;
import nars.budget.Budget;
import nars.concept.BeliefTable;
import nars.concept.Concept;
import nars.concept.TaskTable;
import nars.link.*;
import nars.premise.Premise;
import nars.premise.PremiseGenerator;
import nars.task.Sentence;
import nars.task.Task;
import nars.term.Term;
import nars.term.transform.TermVisitor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;

/** represents one particular Term "view" of a TermCept, at a time.
 *  a view is parameterized by an Op type
 */
public class TermSpect extends TermCept implements Term, Concept {

    private Op op = null;

    public TermSpect() {
        super();
    }

    public TermSpect(TermCept t, Op o) {
        this();
        the(t, o);
    }

    public void believe(final float f, final float c) {
        believe(f, c, op);
    }


    public TermSpect the(ByteBuffer b, int offset, Op o) {
        set(b, offset);
        the(o);
        return this;
    }

    public TermSpect the(TermCept t, Op o) {
        set(t.getByteBuffer(), t.outerOffset);
        this.op = o;
        return this;
    }

    public TermSpect the(TermCore core, int a, Op o) {
        set(core, a);
        the(o);
        return this;
    }


    public TermSpect the(Op op) {
        this.op = op;
        return this;
    }

    @Override
    public Op op() {
        return op;
    }

    @Override
    public int volume() {
        return 0;
    }

    @Override
    public int complexity() {
        return 0;
    }

    @Override
    public void recurseTerms(TermVisitor v, Term parent) {

    }

    @Override
    public void recurseSubtermsContainingVariables(TermVisitor v, Term parent) {

    }

    @Override
    public int containedTemporalRelations() {
        return 0;
    }

    @Override
    public int length() {
        return 0;
    }

    @Override
    public boolean isNormalized() {
        return false;
    }

    @Override
    public boolean containsTerm(Term target) {
        return false;
    }

    @Override
    public boolean containsTermRecursivelyOrEquals(Term target) {
        return false;
    }

    @Override
    public Term clone() {
        return null;
    }

    @Override
    public Term cloneDeep() {
        return null;
    }

    @Override
    public boolean hasVar() {
        return false;
    }

    @Override
    public boolean hasVar(Op type) {
        return false;
    }

    @Override
    public int varIndep() {
        return 0;
    }

    @Override
    public int varDep() {
        return 0;
    }

    @Override
    public int varQuery() {
        return 0;
    }

    @Override
    public int vars() {
        return 0;
    }

    @Override
    public byte[] bytes() {
        return new byte[0];
    }


    @Override
    public int structure() {
        return 0;
    }

    @Override
    public void append(Appendable w, boolean pretty) throws IOException {

    }

    @Override
    public StringBuilder toStringBuilder(boolean pretty) {
        return null;
    }

    @Override
    public boolean impossibleStructure(int possibleSubtermStructure) {
        return false;
    }

    @Override
    public boolean impossibleSubTermVolume(int otherTermVolume) {
        return false;
    }

    @Override
    public void rehash() {

    }

    @Override
    public int compareTo(Object o) {
        return 0;
    }

    @Override
    public Bag<Sentence, TaskLink> getTaskLinks() {
        return null;
    }

    @Override
    public Bag<TermLinkKey, TermLink> getTermLinks() {
        return null;
    }

    @Override
    public Map<Object, Object> getMeta() {
        return null;
    }

    @Override
    public void setMeta(Map<Object, Object> meta) {

    }

    @Override
    public Memory getMemory() {
        return null;
    }



    @Override
    public boolean linkTerms(Budget budgetRef, boolean b) {
        return false;
    }

    @Override
    public TermLink activateTermLink(TermLinkBuilder termLinkBuilder) {
        return null;
    }

    @Override
    public void updateLinks() {

    }

    @Override
    public Budget getBudget() {
        return null;
    }

    @Override
    public float getPriority() {
        return 0;
    }

    @Override
    public boolean link(Task currentTask) {
        return false;
    }

    public boolean isConstant() {
        return false;
    }

    @Override
    public boolean setConstant(boolean b) {
        return false;
    }

    @Override
    public TermLinkBuilder getTermLinkBuilder() {
        return null;
    }

    @Override
    public PremiseGenerator getPremiseGenerator() {
        return null;
    }

    @Override
    public BeliefTable getBeliefs() {
        return null;
    }

    @Override
    public BeliefTable getGoals() {
        return null;
    }

    @Override
    public TaskTable getQuestions() {
        return null;
    }

    @Override
    public TaskTable getQuests() {
        return null;
    }

    @Override
    public long getCreationTime() {
        return 0;
    }

    @Override
    public void delete() {

    }

    @Override
    public boolean processBelief(Premise nal, Task task) {
        return false;
    }

    @Override
    public boolean processGoal(Premise nal, Task task) {
        return false;
    }

    @Override
    public Task processQuestion(Premise nal, Task task) {
        return null;
    }

    @Override
    public Term name() {
        return this;
    }

    @Override
    public Term getTerm() {
        return this;
    }
}
