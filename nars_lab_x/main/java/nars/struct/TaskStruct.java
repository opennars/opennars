package nars.struct;

import nars.Memory;
import nars.budget.Budget;
import nars.task.Sentence;
import nars.task.Task;
import nars.task.stamp.Stamp;
import nars.term.Compound;
import nars.truth.Truth;
import nars.util.data.Util;

import java.lang.ref.Reference;
import java.util.List;

/**
 * Created by me on 8/28/15.
 */
public class TaskStruct extends Fuct implements Task {

    final static int STAMP_LENGTH = 4;

    public final Unsigned8 freq = new Unsigned8();
    public final Unsigned8 conf = new Unsigned8();

    public final Unsigned32 creation = new Unsigned32();
    public final Unsigned32 occurrence = new Unsigned32();

    public final Unsigned32[] stamp = array(new Unsigned32[STAMP_LENGTH]);

    public void truth(final float freq, final float conf) {
        this.freq.set( Util.f2b(freq) );
        this.conf.set( Util.f2b(conf) );
    }


    @Override
    public Task getParentTask() {
        return null;
    }

    @Override
    public Reference<Task> getParentTaskRef() {
        return null;
    }

    @Override
    public Task getParentBelief() {
        return null;
    }

    @Override
    public Reference<Task> getParentBeliefRef() {
        return null;
    }

    @Override
    public Sentence getBestSolution() {
        return null;
    }

    @Override
    public Reference<Task> getBestSolutionRef() {
        return null;
    }

    @Override
    public Operation getCause() {
        return null;
    }

    @Override
    public Task setCause(Operation op) {
        return null;
    }

    @Override
    public void delete() {

    }

    @Override
    public Task setTemporalInducting(boolean b) {
        return this;
    }

    @Override
    public boolean isTemporalInductable() {
        return false;
    }

    @Override
    public void log(String reason) {

    }

    @Override
    public List<String> getLog() {
        return null;
    }

    @Override
    public boolean isDouble() {
        return false;
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public boolean isNormalized() {
        return false;
    }

    @Override
    public Task normalized() {
        return this;
    }

    @Override
    public void setTruth(Truth t) {

    }

    @Override
    public void discountConfidence() {

    }

    @Override
    public void setBestSolution(Memory memory, Task belief) {

    }

    @Override
    public boolean isDeleted() {
        return false;
    }

    @Override
    public Task log(List historyToCopy) {
        return null;
    }

    @Override
    public Budget getBudget() {
        return null;
    }

    @Override
    public char getPunctuation() {
        return 0;
    }

    @Override
    public long[] getEvidence() {
        return new long[0];
    }

    @Override
    public Stamp setEvidence(long... evidentialSet) {
        return null;
    }

    @Override
    public long getCreationTime() {
        return 0;
    }

    @Override
    public long getOccurrenceTime() {
        return 0;
    }

    @Override
    public int getDuration() {
        return 0;
    }

    @Override
    public Sentence setCreationTime(long c) {
        return null;
    }

    @Override
    public Sentence setOccurrenceTime(long o) {
        return null;
    }

    @Override
    public boolean equivalentTo(Sentence that, boolean punctuation, boolean term, boolean truth, boolean stamp, boolean creationTime) {
        return false;
    }

    @Override
    public void setTermShared(Compound equivalentInstance) {

    }

    @Override
    public Compound getTerm() {
        return null;
    }

    @Override
    public Truth getTruth() {
        return null;
    }

    @Override
    public boolean isCyclic() {
        return false;
    }

    @Override
    public void setCyclic(boolean cyclic) {

    }


    @Override
    public Stamp setDuration(int duration) {
        return null;
    }

}
