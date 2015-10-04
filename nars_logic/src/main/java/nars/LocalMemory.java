package nars;

import nars.bag.impl.CacheBag;
import nars.bag.impl.GuavaCacheBag;
import nars.clock.Clock;
import nars.clock.FrameClock;
import nars.concept.Concept;
import nars.term.Term;
import nars.util.data.random.XorShift1024StarRandom;
import org.infinispan.marshall.core.JBossMarshaller;

import java.io.IOException;

/** default for single-thread, in-memory processing */
public class LocalMemory extends Memory {


    public LocalMemory(Clock clock, CacheBag<Term,Concept> index) {
        super(clock, new XorShift1024StarRandom(1), index);
    }

    public LocalMemory(Clock clock) {
        this(  clock, new GuavaCacheBag<>());
    }

    public LocalMemory() {
        this(new FrameClock());
    }

    public byte[] toBytes() throws IOException, InterruptedException {
        return new JBossMarshaller().objectToByteBuffer(this);
    }
}
