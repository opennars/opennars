package nars.nal;

import com.google.common.collect.Lists;
import nars.Global;
import nars.NAR;
import nars.nar.Default2;
import nars.nar.SingleStepNAR;
import nars.nar.Terminal;
import nars.util.meter.TestNAR;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;

import java.util.List;
import java.util.function.Supplier;

import static nars.util.data.LabeledSupplier.supply;

/**
 * Created by me on 2/10/15.
 */
@Ignore
abstract public class AbstractNALTester {


    //final ThreadLocal<NAR> nars;
    //private final Supplier<NAR> nar;
    private final NAR the;
    private TestNAR created;

    protected AbstractNALTester(NAR nar) {
        Global.DEBUG = true;
        this.the = nar;
    }

    protected AbstractNALTester(Supplier<NAR> nar) {
        Global.DEBUG = true;
        this.the = nar.get();
    }


    public final TestNAR test() {
        return created;
    }


    public final NAR nar() {
        return the;
    }

    public static Iterable<Supplier<NAR>> terminal() {
        return Lists.newArrayList(
            new Supplier[] {supply("Terminal", Terminal::new)}
        );
    }

    @Before
    public void start() {
        created = new TestNAR(nar());
    }
    @After
    public void end() {
        created.run2();
    }

    public static Iterable<Supplier<NAR>> nars(int level, boolean requireMultistep) {

        //Level adjustments
        {
            //HACK bump to level 3 is somehow necessary
            if (level == 2) level = 3;
        }



        List<Supplier<NAR>> l = Global.newArrayList();

        final int finalLevel = level;

        /*l.add( supply("Default[NAL<=" + level + "]",
                () -> new Default(new LocalMemory(), 512,1,2,3).nal(finalLevel) ) );*/

        l.add( supply("Default2[NAL<=" + level + "]",
                () -> new Default2(512,2,2,3).nal(finalLevel) ) );

        if (!requireMultistep) {
            l.add( supply("SingleStep[NAL<=" + level + "]",
                    () -> new SingleStepNAR().nal(finalLevel) ) );
        }

        return l;
    }

}
