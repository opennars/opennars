package nars.nal;

import com.google.common.collect.Lists;
import nars.NAR;
import nars.meter.TestNAR;
import nars.nar.Default;
import org.junit.Ignore;

import java.util.List;
import java.util.function.Supplier;

/**
 * Created by me on 2/10/15.
 */
@Ignore
abstract public class AbstractNALTest  {

    public static final List<Supplier<NAR>> core =Lists.newArrayList(
        //() -> new Default().nal(1),
        //() -> new Default().nal(2),
        () -> new Default().nal(6)
        //() -> new DefaultAlann(48)
    );


    private final Supplier<NAR> nar;

    protected AbstractNALTest(Supplier<NAR> nar) {
        this.nar = nar;
    }

    public final TestNAR test() {
        return new TestNAR(nar());
    }
    public final NAR nar() {
        return nar.get();
    }

}
