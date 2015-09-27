package nars.io;

import nars.Memory;
import nars.nar.Default;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

/**
 * Created by me on 9/7/15.
 */
@RunWith(Parameterized.class)
public class MemorySerializationTest extends AbstractSerializationTest<Memory,Memory> {


    @Parameterized.Parameters(name = "{0}")
    public static Collection configurations() {
        return Arrays.asList(new Object[][]{
                { new Default().memory },

                { new Default()
                .believe("b:a")
                .believe("c:b.")
                .run(4).memory }
        });
    }

    public MemorySerializationTest(Memory mem) {
        super(mem);
    }


    @Override
    public void testEquality(Memory a, Memory b) {

        Assert.assertTrue( Memory.equals(a,b) );
    }

    @Override
    Memory parse(Memory input) {
        return input;
    }
}
