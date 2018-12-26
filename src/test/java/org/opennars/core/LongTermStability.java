/*
 * The MIT License
 *
 * Copyright 2018 The OpenNARS authors.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.opennars.core;

import org.opennars.interfaces.pub.Reasoner;
import org.opennars.main.Nar;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.Random;

/**
 * Checks for the stability of the system over a long time period (days, weeks, etc)
 *
 * @author Robert Wünsche
 */
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

        feedRelation2(consumer, rng.nextInt(10000), rng.nextInt(10000), "at", false);
        feedRelation2(consumer, rng.nextInt(10000), rng.nextInt(10000), "from", false);
        feedRelation2(consumer, rng.nextInt(10000), rng.nextInt(10000), "a2", false);

        feedRelation2(consumer, objectIdA, placeIdA, "at", false);
        feedRelation2(consumer, objectIdA, placeIdA, "from", false);

        // feed questions
        feedRelation2(consumer, rng.nextInt(3000), rng.nextInt(3000), "at", true);
        feedRelation2(consumer, rng.nextInt(3000), rng.nextInt(3000), "from", true);
        feedRelation2(consumer, rng.nextInt(3000), rng.nextInt(3000), "a2", true);
    }

    public void feedRelation2(Reasoner consumer, long objectId, long placeId, String relation, boolean isQuestion) {
        String taskType = isQuestion ? "?" : ".";

        // we feed a combination of forms
        consumer.addInput(String.format("<(*, %d, %d)--> %s>%s :|:", objectId, placeId, relation, taskType));
        consumer.addInput(String.format("<(*, {%d}, %d)--> %s>%s :|:", objectId, placeId, relation, taskType));
        consumer.addInput(String.format("<(*, {%d}, {%d})--> %s>%s :|:", objectId, placeId, relation, taskType));
        consumer.addInput(String.format("<(*, %d, {%d})--> %s>%s :|:", objectId, placeId, relation, taskType));
        // set
        consumer.addInput(String.format("<(*, {%d, %d}, {%d})--> %s>%s :|:", objectId, objectId + 500000, placeId, relation, taskType));

        // duplicate set
        consumer.addInput(String.format("<(*, {%d, %d}, {%d})--> %s>%s :|:", objectId, objectId, placeId, relation, taskType));


        consumer.addInput(String.format("<%d --> (/, %s, _, %d)>%s :|:", placeId, relation, objectId, taskType));
        consumer.addInput(String.format("<%d --> (/, %s, %d, _)>%s :|:", objectId, relation, placeId, taskType));

        consumer.addInput(String.format("<{%d} --> (/, %s, _, %d)>%s :|:", placeId, relation, objectId, taskType));
        consumer.addInput(String.format("<%d --> (/, %s, {%d}, _)>%s :|:", objectId, relation, placeId, taskType));

        consumer.addInput(String.format("<{%d} --> (/, %s, _, {%d})>%s :|:", placeId, relation, objectId, taskType));
        consumer.addInput(String.format("<{%d} --> (/, %s, {%d}, _)>%s :|:", objectId, relation, placeId, taskType));

        consumer.addInput(String.format("<{%d} --> (/, %s, _, {%d})>%s :|:", placeId, relation, objectId, taskType));
        consumer.addInput(String.format("<{%d} --> (/, %s, {%d}, _)>%s :|:", objectId, relation, placeId, taskType));
    }

    public static void main(String[] args) throws IOException, InstantiationException, InvocationTargetException, NoSuchMethodException, ParserConfigurationException, IllegalAccessException, SAXException, ClassNotFoundException, ParseException {
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
