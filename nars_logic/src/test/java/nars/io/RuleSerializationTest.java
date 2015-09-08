package nars.io;

import nars.meta.TaskRule;
import nars.nar.NewDefault;
import nars.term.Compound;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

import static org.junit.Assert.assertArrayEquals;

/**
 * Created by me on 9/7/15.
 */
@RunWith(Parameterized.class)
public class RuleSerializationTest extends AbstractSerializationTest<TaskRule,TaskRule> {


    public RuleSerializationTest(TaskRule input) {
        super(input);
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection configurations() {
        return NewDefault.standard;
    }


    @Override
    TaskRule parse(TaskRule input) {
        return input;
    }

    @Override
    protected TaskRule post(TaskRule deserialized) {
        deserialized.rehash();
        return deserialized;
    }

    @Override
    public void testEquality(TaskRule a, TaskRule b)  {

        if (!a.equals(b)) {
            System.out.println(a +  "\t\t" + b);

            /*try {
                System.out.println(JSON.omDeep.writeValueAsString(a));
                System.out.println(JSON.omDeep.writeValueAsString(b));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }*/
        }

        Assert.assertEquals(a.toString(), b.toString());
        Assert.assertEquals(a.hashCode(), b.hashCode());
        Assert.assertEquals(
                ((Compound)a.getTerm()).term(0),
                ((Compound)a.getTerm()).term(0)
        );
        assertArrayEquals(a.preconditions, b.preconditions);
        //postconditions will eventually be backed by proper terms, until then, it is enough for preconditions to match
        //assertArrayEquals(a.postconditions, b.postconditions);
        Assert.assertEquals(a.postconditions.length, b.postconditions.length);


        Assert.assertEquals(a, b);


    }


}