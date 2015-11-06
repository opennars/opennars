package nars.io;

import nars.nal.SimpleDeriver;
import nars.nal.TaskRule;
import nars.term.Compound;
import org.junit.Assert;

import java.util.Collection;
import java.util.Iterator;

import static org.junit.Assert.assertTrue;

/**
 * Created by me on 9/7/15.
 */
public class RuleSerializationTest extends AbstractSerializationTest<Collection<TaskRule>,Collection<TaskRule>> {


    public RuleSerializationTest() {
        super(SimpleDeriver.standard);
    }



    @Override
    Collection<TaskRule> parse(Collection<TaskRule> input) {
        return input;
    }

    @Override
    protected Collection<TaskRule> post(Collection<TaskRule> deserialized) {
        for (TaskRule rr: deserialized)
            rr.rehash();
        return deserialized;
    }

    @Override
    public void testEquality(Collection<TaskRule> aa, Collection<TaskRule> bb)  {

        assertTrue(aa!=bb);

        Iterator xa = aa.iterator();
        Iterator xb = bb.iterator();

        Assert.assertEquals(aa.size(), bb.size());


        while (xa.hasNext()) {
            TaskRule a = (TaskRule) xa.next();
            TaskRule b = (TaskRule) xb.next();

            assertTrue(a!=b);

            if (!a.equals(b)) {
                System.out.println(a +  "\t\t" + b);

                /*try {
                    System.out.println(JSON.omDeep.writeValueAsString(a));
                    System.out.println(JSON.omDeep.writeValueAsString(b));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }*/
            }

            //Assert.assertEquals(a.toString(), b.toString());
            Assert.assertEquals(a.hashCode(), b.hashCode());
            Assert.assertEquals(
                    ((Compound)a.getTerm()).term(0),
                    ((Compound)a.getTerm()).term(0)
            );
            Assert.assertArrayEquals(a.preconditions, b.preconditions);
            //postconditions will eventually be backed by proper terms, until then, it is enough for preconditions to match
            //assertArrayEquals(a.postconditions, b.postconditions);
            Assert.assertEquals(a.postconditions.length, b.postconditions.length);


            Assert.assertEquals(a, b);
        }


    }


}