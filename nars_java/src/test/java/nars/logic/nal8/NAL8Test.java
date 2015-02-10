package nars.logic.nal8;

import nars.build.Curve;
import nars.build.Default;
import nars.core.NewNAR;
import nars.core.Parameters;
import nars.io.narsese.InvalidInputException;
import nars.logic.JavaNALTest;
import org.junit.Test;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;


public class NAL8Test extends JavaNALTest {

    public NAL8Test(NewNAR b) { super(b); }

    @Parameterized.Parameters(name= "{0}")
    public static Collection configurations() {
        return Arrays.asList(new Object[][]{
                {new Default()},
                {new Default().setInternalExperience(null)},
                {new Curve().setInternalExperience(null)}

        });
    }

    @Test public void testQuest() throws InvalidInputException {

        String goal = "<a --> b>";

        nar.goal(Parameters.DEFAULT_GOAL_PRIORITY, Parameters.DEFAULT_GOAL_DURABILITY, goal, 1.0f, 0.9f);

        nar.run(50);

        nar.mustOutput(60, goal, '!', 1.0f, 0.9f);
        nar.quest(goal);
    }
    
}
