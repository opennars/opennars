package nars.nal;

import com.google.common.collect.Lists;
import nars.Global;
import nars.NAR;
import nars.nar.Default;
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
public abstract class AbstractNALTester {


    //final ThreadLocal<NAR> nars;
    private final Supplier<NAR> nar;
    //private final NAR the;
    private TestNAR created;

    protected AbstractNALTester(NAR nar) {
        Global.DEBUG = true;
        this.nar = () -> nar;
    }

    protected AbstractNALTester(Supplier<NAR> nar) {
        Global.DEBUG = true;
        //this.the = nar.get();
        this.nar = nar;
    }


    public final TestNAR test() {
        return created;
    }


    public NAR nar() {
        //return the
        return nar.get();
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
        created.test();
    }

    @Deprecated public static Iterable<Supplier<NAR>> nars(int level, boolean requireMultistep) {
        return requireMultistep ? nars(level, false, true) : nars(level, true, true);
    }

    public static Iterable<Supplier<NAR>> nars(int level, boolean single, boolean multi) {

        List<Supplier<NAR>> l = Global.newArrayList(2);

        if (multi) {

            for (int c : new int[] {  2/*, 2, 4 */}) { ///concepts fired per cycle
                l.add(supply("Default[NAL<=" + level + ",c=" + c + ']', () -> {
                            Default d = new Default(768, c, 2, 2);
                            d.nal(level);
                            return d;
                        }
                ));
                /*if (level < 8) {
                    l.add(supply("Default[NAL8, c=" + c + "]", () -> {
                                Default d = new Default(512, c, 2, 3);
                                d.nal(8);
                                return d;
                            }
                    ));
                }*/
            }

//            l.add(supply("Alann[NAL<=" + level + ']',
//                () -> {
//                    DefaultAlann d = new DefaultAlann(
//                        new Memory(new FrameClock(), TermIndex.memorySoft(1024)),
//                        8 /* input cycler capacity */,
//                        24 /* derivelets */
//                    );
//                    d.nal(finalLevel);
//                    return d;
//                }
//            ));
        }

        if (single) {
            //throw new RuntimeException("depr");
//            l.add( supply("SingleStep[NAL<=" + level + ']',
//                    () -> new SingleStepNAR(512, 1, 2, 3).nal(finalLevel) ) );
        }

        return l;
    }

}
