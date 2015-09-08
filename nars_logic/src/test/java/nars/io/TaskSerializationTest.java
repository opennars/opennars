package nars.io;

import nars.Memory;
import nars.NAR;
import nars.nar.Default;
import nars.task.DefaultTask;
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
public class TaskSerializationTest  extends AbstractSerializationTest<String,DefaultTask> {


    final static Memory memory = new NAR(new Default()).memory;

    public TaskSerializationTest(String input) {
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
                {"(x ==> y)@"},
                {"(&&, x, y);"},
                {"<a --> (b, c)>. :/:"},
                {"$0.5$ (&/, a, /3, b)."},


                //TODO images
                //Intervals
                //Immediate Operations (Command)


        });
    }


    @Override
    DefaultTask parse(String input) {
        DefaultTask t = (DefaultTask)p.task(input, memory);
        if (t!=null)
            return (DefaultTask) t.normalized();
        return null;
    }

    @Override
    protected DefaultTask post(DefaultTask deserialized) {
        DefaultTask t = super.post(deserialized);
        t.invalidate();
        return (DefaultTask) t.normalized();
    }

    @Override
    public void testEquality(DefaultTask a, DefaultTask b)  {


        Assert.assertEquals(a.getTerm(), b.getTerm());
        Assert.assertEquals(a.getPunctuation(), b.getPunctuation());
        Assert.assertEquals(a.getEvidence(), b.getEvidence());
        Assert.assertEquals(a.getOccurrenceTime(), b.getOccurrenceTime());
        Assert.assertEquals(a.hashCode(), b.hashCode());

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