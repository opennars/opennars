package org.opennars.metrics;

import org.opennars.entity.Sentence;
import org.opennars.interfaces.pub.Reasoner;
import org.opennars.io.Parser;
import org.opennars.io.events.AnswerHandler;
import org.opennars.main.Nar;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * temporal metric to test and quantify the capability of a NARS implementation to retain a temporal relationship it had learned a long time ago with events.
 */
public class TemporalOneShotPseudoMetric extends AnswerHandler {
    public Reasoner reasonerUnderTest;

    public int numberOfTermNames = 500;

    public int numberOfRandomEventsBeforeTest = 14;

    private List<String> termNames = new ArrayList<>();

    private Random rng = new Random();

    private boolean wasAnswered = false;

    public static void main(String[] args) throws IOException, InstantiationException, InvocationTargetException, NoSuchMethodException, ParserConfigurationException, IllegalAccessException, SAXException, ClassNotFoundException, ParseException, Parser.InvalidInputException {
        TemporalOneShotPseudoMetric metric = new TemporalOneShotPseudoMetric();
        metric.reasonerUnderTest = new Nar();

        int numberOfRandomEventsBeforeTest=10;
        for (;numberOfRandomEventsBeforeTest<30; numberOfRandomEventsBeforeTest++) {
            System.out.println("checking # of events=" + Integer.toString(numberOfRandomEventsBeforeTest));


            metric.numberOfRandomEventsBeforeTest = numberOfRandomEventsBeforeTest;

            boolean successes = false;
            for (int try_=0; try_<8; try_++) {
                if (metric.check() ) {
                    successes = true;
                    break;
                }
            }

            if (!successes) {
                break;
            }
        }

        System.out.println("metric of passed # events = " + Integer.toString(numberOfRandomEventsBeforeTest-1));

        int debugMeHere = 5;
    }

    public boolean check() throws Parser.InvalidInputException {
        reasonerUnderTest.reset();

        wasAnswered = false;



        // generate set of random term names
        for (int i=0;i<numberOfTermNames;i++) {
            termNames.add(createRandomString(7, rng));
        }

        // one shot learned knowledge
        reasonerUnderTest.addInput("<flash --> [seen]>. :|:");
        reasonerUnderTest.addInput("<b --> B>. :|:");
        reasonerUnderTest.addInput("<spam --> [observed]>. :|:");
        reasonerUnderTest.addInput("<thunder --> [heard]>. :|:");

        // feed the reasoner with random events

        for (int i=0;i<numberOfRandomEventsBeforeTest;i++) {
            final String chosenTermName = termNames.get(rng.nextInt(termNames.size()));

            reasonerUnderTest.addInput(String.format("<%s-->[%s_]>. :|:", chosenTermName, chosenTermName));
            reasonerUnderTest.cycles(1);
        }


        // check if reasoner still knows the one shot knowledge
        reasonerUnderTest.ask("<(&/,<flash --> [seen]>,?1,<thunder --> [heard]>,?2) =/> ?3>", this);

        /// give reasoner enough time to reason
        reasonerUnderTest.cycles(50000);

        return wasAnswered;

    }

    private static String createRandomString(final int length, Random rng) {
        String res = "";

        for (int i=0;i<length;i++) {
            res += (char)(0x41 + rng.nextInt(26));
        }

        return res;
    }

    @Override
    public void onSolution(Sentence belief) {
        wasAnswered = true;
    }
}
