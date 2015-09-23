package nars.nal;

import com.google.common.collect.Lists;
import nars.NAR;
import nars.meter.TestNAR;
import nars.nar.Default;
import nars.nar.SingleStepNAR;
import org.junit.Ignore;

import java.util.List;
import java.util.function.Supplier;

/**
 * Created by me on 2/10/15.
 */
@Ignore
abstract public class AbstractNALTest  {

    public static final List<Supplier<NAR>> core1 =Lists.newArrayList(
        //() -> new Default().nal(1),
        //() -> new Default().nal(2),
      //  () -> new Default().nal(6), //set to maximum now also because now its as follows, the testcases are tested whether the rules match at all
            () -> new SingleStepNAR().nal(6) //and secondly they are tried with full control mechanism on with all rules,
        //() -> new DefaultAlann(48) //it doesn't make sense to test with a subset of the rules here since the system has to run with all at the end
    );                               //however I see its appealing to try rules according to NAL layers, so we should let this optiopn
                                     //open so that such suppliers can be tried even that we won't need them for now - Patrick

                                    //TODO: Once single step testcases all work, control related ones can be added to the testcases again
                                    //but its too early for that

    public static final List<Supplier<NAR>> core2 =Lists.newArrayList(
            //() -> new Default().nal(1),
            //() -> new Default().nal(2),
            //() -> new Default().nal(6),
            () -> new SingleStepNAR().nal(6)
            //() -> new DefaultAlann(48)
    );
    public static final List<Supplier<NAR>> core3 =Lists.newArrayList(
            //() -> new Default().nal(1),
            //() -> new Default().nal(2),
           // () -> new Default().nal(6),
            () -> new SingleStepNAR().nal(6)
            //() -> new DefaultAlann(48)
    );
    public static final List<Supplier<NAR>> core6 =Lists.newArrayList(
            //() -> new Default().nal(1),
            //() -> new Default().nal(2),
            //() -> new Default().nal(6),
            () -> new SingleStepNAR().nal(6)
            //() -> new DefaultAlann(48)
    );

    public static final List<Supplier<NAR>> fullTest =Lists.newArrayList(
            //() -> new Default().nal(1),
            //() -> new Default().nal(2),
            () -> new Default().nal(6),
            () -> new SingleStepNAR().nal(6)
            //() -> new DefaultAlann(48)
    );

    //final ThreadLocal<NAR> nars;
    //private final Supplier<NAR> nar;
    private final NAR the;

    protected AbstractNALTest(NAR nar) {
        this.the = nar;
    }

    protected AbstractNALTest(Supplier<NAR> nar) {
        //this.nar = nar;
        this.the = nar.get();
        //this.nars = ThreadLocal.withInitial( nar );
    }


    public final TestNAR test() {
        return new TestNAR(nar());
    }


    public final NAR nar() {
        return the;
    }

}
