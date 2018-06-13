/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
