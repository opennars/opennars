//package nars.temporal;
//
//import com.google.common.collect.Lists;
//import javafx.scene.Scene;
//import nars.Global;
//import nars.NAR;
//import nars.budget.ItemAccumulator;
//import nars.util.event.CycleReaction;
//import nars.guifx.NARide;
//import nars.guifx.NARfx;
//import nars.nal.nal1.Inheritance;
//import nars.nal.nal1.Negation;
//import nars.nal.nal5.Conjunction;
//import nars.nal.nal7.TemporalRules;
//import nars.nal.nal7.Tense;
//import nars.nar.experimental.Equalized;
//import nars.rl.gng.NeuralGasNet;
//import nars.task.Task;
//import nars.term.Atom;
//import nars.term.Compound;
//import nars.term.Term;
//
//import java.util.*;
//import java.util.function.Consumer;
//
///**
// * Created by me on 8/12/15.
// */
//public class TimeClustering extends CycleReaction {
//
//    final NeuralGasNet centroids = new NeuralGasNet(1, 6);
//    private final NAR nar;
//    private long lastUpdateTime;
//    int maxSize = 32;
//    private final ItemAccumulator<Task> pending;
//
//    final TreeSet<Task> timeline = new TreeSet<Task>(new Comparator<Task>() {
//        @Override public int compare(Task o1, Task o2) {
//            if (o1 == o2) return 0;
//            int l = Long.compare(o2.getOccurrenceTime(), o1.getOccurrenceTime());
//            if (l == 0) {
//                if (o1.equals(o2))
//                    return 0;
//                int h1 = o1.hashCode();
//                int h2 = o2.hashCode();
//                int m = Integer.compare(h1, h2);
//                if (m == 0) {
//                    //give up, they have the same time, hashcode, but for some reason are not equal
//                    return 0;
//                }
//                return m;
//            }
//            return l;
//        }
//    });
//
//
//    public TimeClustering(NAR n) {
//        super(n);
//        /*centroids.setEpsN(centroidVelocity);
//        centroids.setEpsW(centroidVelocity);*/
//
//        this.nar = n;
//        this.pending = ((Equalized.EqualizedCycle)n.memory.getCycleProcess()).newTasks;
//        this.lastUpdateTime = n.time();
//    }
//
//
//    @Override
//    public void onCycle() {
//
//        long now = nar.time();
//
//        long dt = now - lastUpdateTime;
//
//        if (dt > 0) {
//            //shift to past, since they are measured relative to now (0)
//            centroids.addToNodes(new double[] { -dt });
//        }
//
//
//        Iterator<Task> ii = pending.items.keySet().iterator();
//        while (ii.hasNext()) {
//            Task t = ii.next();
//            if (add(t, now)) {
//                //pull into this buffer from the pending queue
//                //System.out.println("BUFFER: " + t.toString(nar.memory));
//                ii.remove();
//            }
//        }
//
//        lastUpdateTime = now;
//
//        /*
//        System.out.println(centroids.vertexSet().stream()
//                .map(v -> v.getEntry(0)).collect(Collectors.toList()));
//        System.out.println(timeline.stream().map(x -> x.getOccurrenceTime() - now).collect(Collectors.toList()));
//        */
//
//        if (shouldFlush()) {
//            flush(t -> {
//                nar.believe(t, Tense.Present, 1f, 0.9f);
//            });
//        }
//    }
//
//    private boolean shouldFlush() {
//        return timeline.size() >= maxSize/4;
//    }
//
//    void flush(Consumer<Compound> result) {
//        double[] partitions = centroids.getDimension(0);
//        List<Term> buffer = new ArrayList();
//        Iterator<Task> ii = timeline.iterator();
//
//        int segment = partitions.length;
//        double m = Double.NaN;
//        while (true) {
//
//            Task next;
//            if (!ii.hasNext())
//                next = null;
//            else
//                next = ii.next();
//
//            long relTime = next!=null ? next.getOccurrenceTime() - lastUpdateTime : 0;
//
//            if (next == null || (segment == partitions.length) || relTime < m) {
//                if (segment < 2)
//                    m = Double.NEGATIVE_INFINITY;
//                else {
//                    m = 0.5 * (partitions[segment - 1] + partitions[segment - 2]);
//                }
//
//
//                int bsize = buffer.size();
//                if (bsize > 0) {
//                    Lists.reverse(buffer); //so earliest is first
//                    result.accept(batch(buffer));
//                    buffer.clear();
//                }
//
//                segment--;
//            }
//
//            if (next == null)
//                break;
//
//            Compound nextTerm = next.getTerm();
//
//            if (!next.getTruth().isNegative())
//                buffer.add(nextTerm);
//            else
//                buffer.add(Negation.make(nextTerm));
//
//        }
//
//        timeline.clear();
//
//    }
//
//    private Compound batch(List<Term> buffer) {
//        return (Compound) Conjunction.make(buffer, TemporalRules.ORDER_CONCURRENT);
//    }
//
//
//    public boolean add(Task input, long now) {
//        if (input.isEternal()) return false;
//
//        if (!input.isJudgment()) return false;
//
//        //only allow fully polarized beliefs, so that they can either be represneted as true or negated in a conjunction among others
//        float freq = input.getTruth().getFrequency();
//        float conf = input.getTruth().getConfidence();
//        if ((freq > 0.25f) && (freq < 0.75f))
//            return false;
//        if (conf < 0.75f)
//            return false;
//
//        //exclude all parallel sequential conjunctions for now, preventing recycling of terms created here
//        if ((input.getTerm() instanceof Conjunction) && (input.getTemporalOrder()!= TemporalRules.ORDER_NONE)) {
//            return false;
//        }
//
//        final long tt = input.getOccurrenceTime();
//
//        if (timeline.size() >= maxSize) {
//            long latest = timeline.first().getOccurrenceTime();
//            long oldest = timeline.last().getOccurrenceTime();
//
//            if (Math.abs(latest - now) > Math.abs(now - oldest)) {
////                if (tt < oldest)
////                    return true; //discard it. returns true here to do that
////                else
//                    timeline.pollFirst();
//            }
//            else {
////                if (tt > latest)
////                    return true; //discard it. returns true here to do that
////                else
//                    timeline.pollLast();
//            }
//        }
//
//        timeline.add(input);
//        centroids.learn(tt - now);
//
//
//        return true;
//    }
//
//    public static class RandomEventGenerator extends CycleReaction {
//
//
//        private final Atom id;
//        int uniqueTerms = 8;
//        int tasksPerCycle = 3;
//        private final NAR nar;
//        Random rng = new Random();
//        int timeVariance = 60 * 1000; //milliseconds around now into past and future
//
//        public RandomEventGenerator(String id, NAR nar) {
//            super(nar);
//            this.nar = nar;
//            this.id = Atom.the(id);
//        }
//
//        @Override public void onCycle() {
//            long now = nar.time();
//            for (int i = 0; i < tasksPerCycle; i++) {
//                nar.believe(
//
//                        Global.DEFAULT_JUDGMENT_PRIORITY,
//                        Global.DEFAULT_JUDGMENT_DURABILITY,
//
//                        Inheritance.make(
//                                Atom.the("" + (char)(rng.nextInt(uniqueTerms) + 'a')),
//                                id
//                        ),
//
//                        now + rng.nextInt(timeVariance*2) - timeVariance,
//                        rng.nextBoolean() ? 1f : 0f,
//                        0.9f
//                );
//            }
//        }
//    }
//
//    public static void main(String[] args) {
//
//        Equalized e = new Equalized(1024, 1, 3) {
//
//
//        };
//        e.realTime();
//        e.duration.set(250);
//        e.conceptForgetDurations.set(10);
//        e.taskLinkForgetDurations.set(10);
//        e.termLinkForgetDurations.set(10);
//
//        NAR n = new NAR(e);
//
//        new RandomEventGenerator("x", n);
//        new TimeClustering(n);
//
//
//        NARfx.run( (app, stage) -> {
//
//            NARide p = new NARide(n);
//
//
//            stage.setWidth(900);
//            stage.setHeight(900);
//
//            Scene scene = new Scene(p);
//            stage.setScene(scene);
//
//            stage.show();
//
//            //TextOutput.out(n);
//            new Thread( () ->  n.loop(100)  ).start();
//        });
//
//
//
//
//
//    }
// }
