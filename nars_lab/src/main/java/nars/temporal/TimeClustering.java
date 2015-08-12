package nars.temporal;

import nars.Global;
import nars.NAR;
import nars.budget.ItemAccumulator;
import nars.clock.RealtimeClock;
import nars.event.CycleReaction;
import nars.io.out.TextOutput;
import nars.nal.nal1.Inheritance;
import nars.nal.nal7.Tense;
import nars.nar.experimental.Equalized;
import nars.rl.gng.NeuralGasNet;
import nars.task.Task;
import nars.term.Atom;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Created by me on 8/12/15.
 */
public class TimeClustering extends CycleReaction {

    final NeuralGasNet centroids = new NeuralGasNet(1, 3);
    private final NAR nar;
    int maxSize = 32;


    final TreeSet<Task> timeline = new TreeSet<Task>(new Comparator<Task>() {
        @Override public int compare(Task o1, Task o2) {
            if (o1 == o2) return 0;
            int l = Long.compare(o2.getOccurrenceTime(), o1.getOccurrenceTime());
            if (l == 0) {
                if (o1.equals(o2))
                    return 0;
                int h1 = o1.hashCode();
                int h2 = o2.hashCode();
                int m = Integer.compare(h1, h2);
                if (m == 0) {
                    //give up, they have the same time, hashcode, but for some reason are not equal
                    return 0;
                }
                return m;
            }
            return l;
        }
    });
    private final ItemAccumulator<Task> pending;

    public TimeClustering(NAR n) {
        super(n);
        /*centroids.setEpsN(centroidVelocity);
        centroids.setEpsW(centroidVelocity);*/

        this.nar = n;
        this.pending = ((Equalized.EqualizedCycle)n.memory.getControl()).newTasks;
    }


    @Override
    public void onCycle() {

        long now = nar.time();

        Iterator<Task> ii = pending.items.iterator();
        while (ii.hasNext()) {
            Task t = ii.next();
            if (add(t, now)) {
                //pull into this buffer from the pending queue
                ii.remove();
            }
        }

        System.out.println(centroids.vertexSet().stream()
                .map(v -> v.getEntry(0)).collect(Collectors.toList()));
        System.out.println(timeline.stream().map(x -> x.getOccurrenceTime() - now).collect(Collectors.toList()));
    }

    public boolean add(Task input, long now) {
        if (input.isEternal()) return false;


        final long tt = input.getOccurrenceTime();

        if (timeline.size() >= maxSize) {
            long latest = timeline.first().getOccurrenceTime();
            long oldest = timeline.last().getOccurrenceTime();

            if (Math.abs(latest - now) > Math.abs(now - oldest)) {
//                if (tt < oldest)
//                    return true; //discard it. returns true here to do that
//                else
                    timeline.pollFirst();
            }
            else {
//                if (tt > latest)
//                    return true; //discard it. returns true here to do that
//                else
                    timeline.pollLast();
            }
        }

        timeline.add(input);
        centroids.learn(tt - now);


        return true;
    }

    public static class RandomEventGenerator extends CycleReaction {


        private final Atom id;
        int uniqueTerms = 5;
        int tasksPerCycle = 4;
        private final NAR nar;
        Random rng = new Random();
        int timeVariance = 60 * 1000; //milliseconds around now into past and future

        public RandomEventGenerator(String id, NAR nar) {
            super(nar);
            this.nar = nar;
            this.id = Atom.the(id);
        }

        @Override public void onCycle() {
            long now = nar.time();
            for (int i = 0; i < tasksPerCycle; i++) {
                nar.believe(

                        Global.DEFAULT_JUDGMENT_PRIORITY,
                        Global.DEFAULT_JUDGMENT_DURABILITY,

                        Inheritance.make(
                                Atom.the("" + (char)(rng.nextInt(uniqueTerms) + 'a')),
                                id
                        ),

                        now + rng.nextInt(timeVariance*2) - timeVariance,
                        1f,
                        0.9f
                );
            }
        }
    }

    public static void main(String[] args) {

        Equalized e = new Equalized(1024, 1, 1) {


        };
        e.realTime();
        NAR n = new NAR(e);

        new RandomEventGenerator("x", n);
        new TimeClustering(n);

        TextOutput.out(n);
        n.loop(1000);


    }
}
