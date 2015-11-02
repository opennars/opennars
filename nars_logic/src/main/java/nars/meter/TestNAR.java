package nars.meter;

import nars.Global;
import nars.NAR;
import nars.event.CycleReaction;
import nars.meter.condition.EternalTaskCondition;
import nars.meter.condition.TemporalTaskCondition;
import nars.nal.nal7.Tense;
import nars.narsese.InvalidInputException;
import nars.task.Task;
import nars.task.stamp.Stamp;
import nars.util.event.Topic;
import nars.util.graph.TermLinkGraph;
import nars.util.meter.event.HitMeter;

import java.io.PrintStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.*;

import static org.jgroups.util.Util.assertTrue;


/**
* TODO use a countdown latch to provide early termination for successful tests
*/
public class TestNAR  {

    public final Map<Object, HitMeter> eventMeters;
    public final NAR nar;
    boolean showFail = true;
    boolean showSuccess = false;
    boolean showExplanations = false;
    final boolean showOutput = false;

    boolean collectTrace = false;



    /** "must" requirement conditions specification */
    public final List<EternalTaskCondition> requires = new ArrayList();
    //public final List<ExplainableTask> explanations = new ArrayList();
    private Exception error;
    final transient private boolean exitOnAllSuccess = true;
    public List<Task> inputs = new ArrayList();
    private int temporalTolerance = 0;
    protected final static float truthTolerance = Global.TESTS_TRUTH_ERROR_TOLERANCE;
    private StringWriter trace;


    public TestNAR(NAR nar) {
        super();

        this.nar = nar;

        //HACK sends 'answer' events' answers through derived so they can be detected by TaskCondition
        nar.memory.eventAnswer.on(tt -> {
            Task t = tt.getTwo();
            t.log("Answer via Derived");
            nar.memory.eventDerived.emit(t);
        });

        if (exitOnAllSuccess) {
            new EarlyExit(1);
        }

        eventMeters = new EventCount(nar).eventMeters;

    }

    /** returns the "cost", which can be considered the inverse of a "score".
     * it is proportional to the effort (ex: # of cycles) expended by
     * this reasoner in attempts to satisfy success conditions.
     * If the conditions are not successful, the result will be INFINITE,
     * though this can be normalized to a finite value in comparing multiple tests
     * by replacing the INFINITE result with a maximum # of cycles limit,
     * which will be smaller in cases where the success conditions are
     * completed prior to the limit.
     * */
    public double getCost() {
        return EternalTaskCondition.cost(requires);
    }



    public TestNAR debug() {
        Global.DEBUG = true;
        //nar.stdout();
        return this;
    }

    /** asserts that (a snapshot of) the termlink graph is fully connected */
    public TestNAR assertTermLinkGraphConnectivity() {
        TermLinkGraph g = new TermLinkGraph(nar);
        assertTrue("termlinks form a fully connected graph:\n" + g.toString(), g.isConnected());
        return this;
    }

    /** returns a new TestNAR continuing with the current nar */
    public TestNAR next() {
        return new TestNAR(nar);
    }

    public TestNAR input(String s) {
        nar.input(s);
        return this;
    }

    public void inputAt(long time, String s) {
        nar.inputAt(time, s);
    }

    public void believe(String t, Tense tense, float f, float c) {
        nar.believe(t, tense, f, c);
    }


    class EarlyExit extends CycleReaction {

        final int checkResolution; //every # cycles to check for completion
        int cycle = 0;

        public EarlyExit(int checkResolution) {
            super(nar);
            this.checkResolution = checkResolution;
        }

        @Override
        public void onCycle() {
            cycle++;
            if (cycle % checkResolution == 0) {

                if (requires.isEmpty())
                    return;

                boolean finished = true;

                int nr = requires.size();
                for (int i = 0; i < nr; i++) {
                    final EternalTaskCondition oc = requires.get(i);
                    if (!oc.isTrue()) {
                        finished = false;
                        break;
                    }
                }

                if (finished) {
                    stop();

                }

            }
        }
    }


    public void stop() {
        nar.stop();
        /*if (resetOnStop) {
            nar.memory.delete();
        }*/
    }

    //TODO initialize this once in constructor
    Topic<Task>[] outputEvents;


    public TestNAR mustOutput(long cycleStart, long cycleEnd, String sentenceTerm, char punc, float freqMin, float freqMax, float confMin, float confMax, long occTimeAbsolute) throws InvalidInputException {
        if (outputEvents == null) outputEvents = new Topic[] {
            nar.memory().eventDerived, nar.memory().eventTaskRemoved, nar.memory().eventRevision
        };
        mustEmit(outputEvents, cycleStart, cycleEnd, sentenceTerm, punc, freqMin, freqMax, confMin, confMax, occTimeAbsolute);
        return this;
    }


    public TestNAR mustOutput(long withinCycles, String task) throws InvalidInputException {
        if (outputEvents == null) outputEvents = new Topic[] {
                nar.memory().eventDerived, nar.memory().eventTaskRemoved, nar.memory().eventRevision };
        return mustEmit(outputEvents, withinCycles, task);
    }

//    public TestNAR onAnswer(String solution, AtomicBoolean solved /* for detecting outside of this */) throws InvalidInputException {
//
//        solved.set(false);
//
//        final Task expectedSolution = nar.task(solution);
//
//        nar.memory.eventAnswer.on(qa -> {
//             if (!solved.get() && qa.getTwo().equals(expectedSolution)) {
//                 solved.set(true);
//             }
//        });
//
//        return this;
//
//    }

//    public TestNAR mustOutput(Topic<Task> c, long cycleStart, long cycleEnd, String sentenceTerm, char punc, float freqMin, float freqMax, float confMin, float confMax, int ocRelative) throws InvalidInputException {
//        return mustEmit(c, cycleStart, cycleEnd, sentenceTerm, punc, freqMin, freqMax, confMin, confMax, ocRelative );
//    }

    public TestNAR mustEmit(Topic<Task>[] c, long cycleStart, long cycleEnd, String sentenceTerm, char punc, float freqMin, float freqMax, float confMin, float confMax) throws InvalidInputException {
        return mustEmit(c, cycleStart, cycleEnd, sentenceTerm, punc, freqMin, freqMax, confMin, confMax, Stamp.ETERNAL );
    }

    public TestNAR mustEmit(Topic<Task>[] c, long cycleStart, long cycleEnd, String sentenceTerm, char punc, float freqMin, float freqMax, float confMin, float confMax, Tense t) throws InvalidInputException {
        return mustEmit(c, cycleStart, cycleEnd, sentenceTerm, punc, freqMin, freqMax, confMin, confMax, nar.time(t));
    }

    public TestNAR mustEmit(Topic<Task>[] c, long cycleStart, long cycleEnd, String sentenceTerm, char punc, float freqMin, float freqMax, float confMin, float confMax, long occTimeAbsolute) throws InvalidInputException {

        float h = (freqMin!=-1) ? truthTolerance / 2.0f : 0;

        if (freqMin == -1) freqMin = freqMax;

        int tt = getTemporalTolerance();

        cycleStart -= tt;
        cycleEnd += tt;

        EternalTaskCondition tc;
        if (occTimeAbsolute== Stamp.ETERNAL) {
            tc = new EternalTaskCondition(nar,
                    cycleStart, cycleEnd,
                    sentenceTerm, punc, freqMin - h, freqMax + h, confMin - h, confMax + h);
        } else {
            tc = new TemporalTaskCondition(nar,
                    cycleStart, cycleEnd,
                    occTimeAbsolute,occTimeAbsolute,
                    sentenceTerm, punc, freqMin - h, freqMax + h, confMin - h, confMax + h);
        }

        for (Topic<Task> cc : c) {
            cc.on(tc);
        }

        requires.add(tc);

        return this;
//
//        ExplainableTask et = new ExplainableTask(tc);
//        if (showExplanations) {
//            explanations.add(et);
//        }
//        return et;
    }

    /** padding to add to specified time limitations to allow correct answers;
     *  default=0 having no effect  */
    public int getTemporalTolerance() {
        return temporalTolerance;
    }

//    public void setTemporalTolerance(int temporalTolerance) {
//        this.temporalTolerance = temporalTolerance;
//    }

    public Exception getError() {
        return error;
    }

//    public TestNAR mustInput(long withinCycles, String task) {
//        return mustEmit(
//                new Topic[] { nar.memory.eventInput },
//                withinCycles, task);
//    }


    public final long time() { return nar.time(); }

    public TestNAR mustEmit(Topic<Task>[] c, long withinCycles, String task) throws InvalidInputException {
        Task t = nar.task(task);
        //TODO avoid reparsing term from string

        final long now = time();
        final String termString = t.getTerm().toString();
        if (t.getTruth()!=null) {
            final float freq = t.getFrequency();
            final float conf = t.getConfidence();
            long occurrence = t.getOccurrenceTime();
            return mustEmit(c, now, now + withinCycles, termString, t.getPunctuation(), freq, freq, conf, conf, occurrence);
        }
        else {
            return mustEmit(c, now, now + withinCycles, termString, t.getPunctuation(), -1, -1, -1, -1);
        }
    }

    public TestNAR mustOutput(long withinCycles, String term, char punc, float freq, float conf) throws InvalidInputException {
        long now = time();
        return mustOutput(now, now + withinCycles, term, punc, freq, freq, conf, conf, nar.time(Tense.Eternal));
    }

    public TestNAR mustBelieve(long withinCycles, String term, float freqMin, float freqMax, float confMin, float confMax) throws InvalidInputException {
        return mustBelieve(withinCycles, term, freqMin, freqMax, confMin, confMax, Stamp.ETERNAL);
    }
    public TestNAR mustBelieve(long withinCycles, String term, float freqMin, float freqMax, float confMin, float confMax, long tense) throws InvalidInputException {
        long now = time();
        return mustOutput(now, now + withinCycles, term, '.', freqMin, freqMax, confMin, confMax, tense);
    }
//    public TestNAR mustBelievePast(long withinCycles, String term, float freqMin, float freqMax, float confMin, float confMax, int maxPastWindow) throws InvalidInputException {
//        long now = time();
//        return mustOutput(now, now + withinCycles, term, '.', freqMin, freqMax, confMin, confMax);
//    }
//    public ExplainableTask mustBelieve(long cycleStart, long cycleStop, String term, float freq, float confidence) throws InvalidInputException {
//        long now = time();
//        return mustOutput(now + cycleStart, now + cycleStop, term, '.', freq, freq, confidence, confidence);
//    }
    public TestNAR mustBelieve(long withinCycles, String term, float freq, float confidence, Tense t) throws InvalidInputException {
        long ttt = nar.time();
        return mustOutput(ttt, ttt + withinCycles, term, '.', freq, freq, confidence, confidence, nar.time(t));
    }
    public TestNAR mustBelieve(long withinCycles, String term, float freq, float confidence, long occTimeAbsolute) throws InvalidInputException {
        long t = nar.time();
        return mustOutput(t, t + withinCycles, term, '.', freq, freq, confidence, confidence,occTimeAbsolute);
    }

    public TestNAR mustBelieve(long withinCycles, String term, float freq, float confidence) throws InvalidInputException {
        return mustBelieve(withinCycles, term, freq, confidence, Tense.Eternal);
    }
    public TestNAR mustBelieve(long withinCycles, String term, float confidence) throws InvalidInputException {
        return mustBelieve(withinCycles, term, 1.0f, confidence);
    }

    public TestNAR mustDesire(long withinCycles, String goalTerm, float freq, float conf) {
        return mustOutput(withinCycles, goalTerm, '!', freq, conf);
    }
    public TestNAR mustDesire(long withinCycles, String goalTerm, float freq, float conf, long occ) {
        long t = nar.time();
        return mustOutput(t, t + withinCycles, goalTerm, '!', freq, freq, conf, conf, occ);
    }

    public TestNAR ask(String termString) throws InvalidInputException {
        //Override believe to input beliefs that have occurrenceTime set on input
        // "lazy timing" appropriate for test cases that can have delays
        Task t = nar.ask(termString);

        //explainable(t);
        return this;
    }


    public TestNAR believe(String... termString) throws InvalidInputException {
        for (String s : termString)
            nar.believe(s);
        return this;
    }




    public TestNAR believe(String termString, float freq, float conf) throws InvalidInputException {

        nar.believe(termString, freq, conf);
        return this;
    }



    public static class Report implements Serializable {

        public final long time;
        public final HitMeter[] eventMeters;
        protected Serializable error = null;
        protected Task[] inputs;
        protected List<EternalTaskCondition> cond = Global.newArrayList(1);
        transient final int stackElements = 4;

        public Report(TestNAR n) {
            this.time = n.time();

            this.inputs = n.inputs.toArray(new Task[n.inputs.size()]);
            Collection<HitMeter> var = n.eventMeters.values();
            this.eventMeters = var.toArray(new HitMeter[var.size()]);
        }

        public void setError(Exception e) {
            if (e!=null) {
                this.error = new Object[]{e.toString(), Arrays.copyOf(e.getStackTrace(), stackElements)};
            }
        }

        public void add(EternalTaskCondition o) {
            cond.add(o);
        }

        public boolean isSuccess() {
            for (EternalTaskCondition t : cond)
                if (!t.isTrue())
                    return false;
            return true;
        }

        public void toString(PrintStream out) {

            if (error!=null) {
                out.println(error);
            }
            out.print("@" + time + " ");
            out.print(Arrays.toString(eventMeters) + "\n");

            for (Task t : inputs) {
                out.println("IN " + t);
            }

            cond.forEach(c ->
                c.toString(out)
            );
        }
    }

    public TestNAR run() {
        return run(true);
    }

    public TestNAR run(boolean testAndPrintReport /* for use with JUnit */) {
        long finalCycle = 0;
        for (EternalTaskCondition oc : requires) {
            if (oc.creationEnd > finalCycle)
                finalCycle = oc.creationEnd +1;
        }

        if (collectTrace)
            nar.trace(trace = new StringWriter());

        runUntil(finalCycle);


        if (testAndPrintReport) {
            assertTrue("No conditions tested", !requires.isEmpty());

            //assertTrue("No cycles elapsed", tester.nar.memory().time/*SinceLastCycle*/() > 0);

            Report r = getReport();
            report(r, r.isSuccess());

        }


        return this;
    }

    public Report getReport() {
        Report report = new Report(this);

        report.setError(getError());

        requires.forEach(report::add);


        return report;
    }

    protected void report(Report report, boolean success) {

        //String s = //JSONOutput.stringFromFieldsPretty(report);
            //report.toString();

        //explain all validated conditions
        if (requires!=null) {
            requires.forEach(c -> {
                if (c.valid != null)
                    c.valid.forEach(t -> {
                        System.out.println(t.getExplanation()
                    );
                });
            });
        }


        if (success) {
            report.toString(System.out);
        }
        else if (!success) {

            report.toString(System.out);
            if (collectTrace)
                System.err.println(trace.getBuffer());

            assertTrue(false);
        }

        //System.out.println(s);
    }

    public TestNAR run(long extraCycles) {
        return runUntil(time() + extraCycles);
    }

    public TestNAR runUntil(long finalCycle) {

        error = null;

        if (showOutput)
            nar.trace();


        //try {
        nar.frame((int)(finalCycle - time()));
        /*}
        catch (Exception e) {
            error = e;
        }*/

        return this;
    }


//    /** returns null if there is no error, or a non-null String containing report if error */
//    @Deprecated public String evaluate() {
//        //TODO use report(..)
//
//        int conditions = requires.size();
//        int failures = getError()!=null ? 1 : 0;
//
//        for (TaskCondition tc : requires) {
//            if (!tc.isTrue()) {
//                failures++;
//            }
//        }
//
//        int successes = conditions - failures;
//
//
//        if (error!=null || failures > 0) {
//            String result = "";
//
//            if (error!=null) {
//                result += error.toString() + " ";
//            }
//
//            if (failures > 0) {
//                result += successes + "/ " + conditions + " conditions passed";
//            }
//
//            return result;
//        }
//
//        return null;
//
//    }

//    public void report(PrintStream out, boolean showFail, boolean showSuccess, boolean showExplanations) {
//
//        boolean output = false;
//
//        if (showFail || showSuccess) {
//
//            for (TaskCondition tc : requires) {
//
//                if (!tc.isTrue()) {
//                    if (showFail) {
//                        out.println(tc.getFalseReason());
//                        output = true;
//                    }
//                } else {
//                    if (showSuccess) {
//                        out.println(tc.getTrueReasons());
//                        output = true;
//                    }
//                }
//            }
//
//        }
//
//        if (error!=null) {
//            error.printStackTrace();
//            output = true;
//        }
//
//        if (showExplanations) {
//            for (ExplainableTask x : explanations ) {
//                x.printMeaning(out);
//                output = true;
//            }
//        }
//
//        if (output)
//            out.println();
//    }
//
//
//    public void inputTest(String script) {
//
//        if (script == null)
//            throw new RuntimeException("null input");
//
//        nar.input( new TestInput(script) );
//
//    }

//    class TestInput extends TextInput {
//        public TestInput(String script) {
//            super(nar, script);
//        }
//
//        @Override
//        public void accept(Task task) {
//            super.accept(task);
//            inputs.add(task);
//        }
//    }
}
