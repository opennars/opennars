//package nars.rl.example;
//
//import nars.Events;
//import nars.NAR;
//import nars.Symbols;
//import nars.concept.Concept;
//import nars.event.NARReaction;
//import nars.io.Texts;
//import nars.nal.nal7.TemporalRules;
//
//import nars.task.Task;
//import nars.truth.Truth;
//import nars.util.event.AbstractReaction;
//import nars.util.graph.ConceptGraph;
//
//import java.util.ArrayDeque;
//import java.util.Deque;
//import java.util.LinkedHashMap;
//import java.util.Map;
//
//abstract public class MarkovObservationsGraph extends ConceptGraph<MarkovObservationsGraph.MarkovEdge> {
//
//    int historySize = 10;
//    private long cyclesPerEpisode = 10;
//
//    Deque<Map<Task,Concept>> history = new ArrayDeque();
//    Map<Task,Concept> current;
//
//    float minFreq = 0.75f;
//    float minExp = 0.1f;
//    private long currentEpisode;
//
//    public static class MarkovEdge {
//        double evidence = 0;
//        void add(double e) { evidence += e; }
//
//        public String toString() {
//            return Texts.n4(evidence);
//        }
//    }
//
//    private final AbstractReaction processing;
//
//    public MarkovObservationsGraph(NAR nar) {
//        super(nar);
//
//        current = new LinkedHashMap<>();
//        currentEpisode = nar.time();
//
//        processing = new NARReaction(nar, TaskProcess.class, Events.CycleEnd.class, Events.ResetStart.class) {
//
//            @Override
//            public void event(Class event, Object... args) {
//                if (event == TaskProcess.class) {
//                    Task t = (Task) args[0];
//                    if (/*t.isInput() &&*/ !t.isEternal()) {
//                        long now = nar.time();
//                        long then = t.getOccurrenceTime();
//
//                        if (TemporalRules.concurrent(now, then, nar.memory.duration()))
//                            observe(t);
//                    }
//                }
//                if (event == Events.CycleEnd.class) {
//                    cycle();
//                }
//                if (event == Events.ResetStart.class) {
//                    clear();
//                }
//            }
//        };
//
//        cycle();
//
//    }
//
//    @Override
//    public MarkovEdge createEdge(Concept source, Concept target) {
//        return new MarkovEdge();
//    }
//
//    public void clear() {
//        history.clear();
//        if (current !=null) current.clear();
//    }
//
//    protected void observe(Task task) {
//        if (task.getPunctuation() == Symbols.JUDGMENT) {
//            Truth t = task.getTruth();
//            if (t.getFrequency() < minFreq) return;
//            float exp = t.getExpectation();
//            if (exp < minExp) return;
//
//            Concept c = nar.concept(task.getTerm());
//            if (c == null) return;
//
//            current.put(task, c);
//        }
//    }
//
//    protected void cycle() {
//
//        if (current == null) return;
//
//        if (current.isEmpty()) return; //dont need to add a new history state if empty
//
//        for (Map.Entry<Task,Concept> next : current.entrySet()) {
//
//            int dt = 0;
//            for (Map<Task,Concept> h : history) {
//                dt++;
//                for (Map.Entry<Task,Concept> prev : h.entrySet()) {
//                    transition(prev, next);
//                }
//            }
//
//        }
//
//        if (newEpisode()) {
//            history.addFirst(current);
//
//            if (history.size() == historySize) {
//                current = history.removeLast();
//                current.clear();
//            } else {
//                current = new LinkedHashMap<>();
//            }
//            currentEpisode = nar.time();
//        }
//    }
//
//    public boolean newEpisode() {
//        return (nar.time() - currentEpisode > cyclesPerEpisode);
//    }
//
//    public void setCyclesPerEpisode(long cyclesPerEpisode) {
//        this.cyclesPerEpisode = cyclesPerEpisode;
//    }
//
//    private void transition(Map.Entry<Task, Concept> from, Map.Entry<Task, Concept> to) {
//        Task cause = from.getKey();
//        Task effect = to.getKey();
//
//
//        long dt = effect.getOccurrenceTime() - cause.getOccurrenceTime();
//        if (dt == 0) {
//            //throw new RuntimeException("Tasks occurr at the same time: " + from + " " + to);
//            return;
//        }
//
//        if (cause.getTerm().equals(effect.getTerm()))
//            return; //same term
//
//        MarkovEdge e = addEdge(from.getValue(), to.getValue());
//
//        transition(cause, effect, dt, e);
//    }
//
//    private void transition(Task cause, Task effect, long dt, MarkovEdge e) {
//        float combinedExpect = cause.getTruth().getExpectation() * effect.getTruth().getExpectation();
//        e.add( combinedExpect / dt );
//    }
//
//}
