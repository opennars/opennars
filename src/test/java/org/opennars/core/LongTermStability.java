package org.opennars.core;

import org.opennars.interfaces.pub.Reasoner;
import org.opennars.main.Nar;

import java.util.Random;

public class LongTermStability {
    public final Reasoner reasoner;

    public final ObjectIdCounter counter = new ObjectIdCounter();

    public final Random rng = new Random();

    public LongTermStability(Reasoner reasoner) {
        this.reasoner = reasoner;
    }

    public void run(long timeToRunInMilliseconds) {
        // TODO< countdown time >
        long remainingTimeToRunInMilliseconds = timeToRunInMilliseconds;

        while (true) {
            feed(reasoner);
            cycleReasoner(100000);
        }
    }

    public void cycleReasoner(int numberOfCycles) {
        reasoner.cycles(numberOfCycles);
    }

    public void feed(Reasoner consumer) {
        int objectIdA = rng.nextInt(10000);
        int placeIdA = rng.nextInt(10000);

        feedRelation2(consumer, rng.nextInt(10000), rng.nextInt(10000), "at");
        feedRelation2(consumer, rng.nextInt(10000), rng.nextInt(10000), "from");
        feedRelation2(consumer, rng.nextInt(10000), rng.nextInt(10000), "a2");

        feedRelation2(consumer, objectIdA, placeIdA, "at");
        feedRelation2(consumer, objectIdA, placeIdA, "from");
    }

    public void feedRelation2(Reasoner consumer, long objectId, long placeId, String relation) {
        // we feed a combination of forms
        consumer.addInput(String.format("<(*, %d, %d)--> %s>. :|:", objectId, placeId, relation));
        consumer.addInput(String.format("<(*, {%d}, %d)--> %s>. :|:", objectId, placeId, relation));
        consumer.addInput(String.format("<(*, {%d}, {%d})--> %s>. :|:", objectId, placeId, relation));
        consumer.addInput(String.format("<(*, %d, {%d})--> %s>. :|:", objectId, placeId, relation));
        // set
        consumer.addInput(String.format("<(*, {%d, %d}, {%d})--> %s>. :|:", objectId, objectId + 500000, placeId, relation));

        // duplicate set
        consumer.addInput(String.format("<(*, {%d, %d}, {%d})--> %s>. :|:", objectId, objectId, placeId, relation));


        consumer.addInput(String.format("<%d --> (/, %s, _, %d)>. :|:", placeId, relation, objectId));
        consumer.addInput(String.format("<%d --> (/, %s, %d, _)>. :|:", objectId, relation, placeId));

        consumer.addInput(String.format("<{%d} --> (/, %s, _, %d)>. :|:", placeId, relation, objectId));
        consumer.addInput(String.format("<%d --> (/, %s, {%d}, _)>. :|:", objectId, relation, placeId));

        consumer.addInput(String.format("<{%d} --> (/, %s, _, {%d})>. :|:", placeId, relation, objectId));
        consumer.addInput(String.format("<{%d} --> (/, %s, {%d}, _)>. :|:", objectId, relation, placeId));

        consumer.addInput(String.format("<{%d} --> (/, %s, _, {%d})>. :|:", placeId, relation, objectId));
        consumer.addInput(String.format("<{%d} --> (/, %s, {%d}, _)>. :|:", objectId, relation, placeId));
    }

    public static void main(String[] args) {
        Reasoner reasonerUnderTest = new Nar();
        final LongTermStability test = new LongTermStability(reasonerUnderTest);

        long timeToRunInMilliseconds = 7 * 24 * 3600 * 1000;

        reasonerUnderTest.addInput("*volume=0");

        test.run(timeToRunInMilliseconds);
    }

    private static class ObjectIdCounter {
        long counter = 0;

        long retNext() {
            return counter++;
        }
    }
}
