package nars.io;

import nars.NAR;
import nars.concept.Concept;
import nars.nar.Default;
import nars.task.Task;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

/**
 * Created by me on 9/7/15.
 */
@RunWith(Parameterized.class)
public class ConceptSerializationTest extends AbstractSerializationTest<String,Concept> {


    final NAR nar = new Default();

    public ConceptSerializationTest(String input) {
        super(input);
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection configurations() {
        return Arrays.asList(new Object[][]{

                //TODO add all term types here

                {"<x-->y>."},
                {"<#1-->y>."},
                {"$0.12;0.41;0.31$ (--, x). %1.00|0.50%"},
                {"(x, y, z)?"},
                {"<a --> b>!"},
                {"(x && y)."},
                {"(x =/> y)."},
                {"(x ==> y)."},
                {"(x ==> y)@"},
                {"((x,y) ==> z)@"},
                {"((x,y) ==> z)."},
                {"<a --> (b, c)>."},
                {"<a --> (b, c)>. :/:"},
                {"$0.5$ (&/, a, /3, b)."},


                //TODO images
                //Intervals
                //Immediate Operations (Command)


        });
    }


    @Override
    Concept parse(String input) {

        nar.reset();
        Task t = nar.inputTask(input);
        nar.frame(1);

        if (t!=null)
            return nar.concept(t.getTerm());
        return null;
    }

    @Override
    protected Concept post(Concept deserialized) {
        return deserialized;
    }

    @Override
    public void testEquality(Concept a, Concept b)  {

        Assert.assertEquals(a.hashCode(), b.hashCode());

        Assert.assertEquals(a.getTerm(), b.getTerm());
        Assert.assertEquals(a.getBudget(), b.getBudget());

        Assert.assertEquals(a.getBeliefs(), b.getBeliefs());
        Assert.assertEquals(a.getGoals(), b.getGoals());
        Assert.assertEquals(a.getQuestions(), b.getQuestions());

        if (!a.getTermLinks().equals(b.getTermLinks())) {
            System.err.println("inequal termlinks: ");
            a.getTermLinks().printAll();
            b.getTermLinks().printAll();
        }
        Assert.assertEquals(a.getTermLinks(), b.getTermLinks());
        Assert.assertEquals(a.getTaskLinks(), b.getTaskLinks());

        //Assert.assertEquals(a.getTermLinkBuilder().templates(), b.getTermLinkBuilder().templates());


        if (!a.equals(b)) {
            System.out.println(a +  "\t\t" + b);

            /*try {
                System.out.println(JSON.omDeep.writeValueAsString(a));
                System.out.println(JSON.omDeep.writeValueAsString(b));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }*/
        }

        a.equals(b);

        Assert.assertEquals(a, b);

        Assert.assertEquals(a.toString(), b.toString());

    }


}