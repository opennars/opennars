package nars;

import nars.bag.impl.CacheBag;
import nars.bag.impl.MapCacheBag;
import nars.clock.Clock;
import nars.clock.FrameClock;
import nars.concept.Concept;
import nars.term.Term;
import nars.util.data.map.UnifriedMap;
import nars.util.data.random.XorShift1024StarRandom;
import org.infinispan.marshall.core.JBossMarshaller;

import java.io.IOException;

/** default for single-thread, in-memory processing */
public class LocalMemory extends Memory {


    public LocalMemory(Clock clock, CacheBag<Term,Concept> index) {
        super(clock, new XorShift1024StarRandom(1), index);
    }

    public LocalMemory(Clock clock) {
        this(  clock,
            //new GuavaCacheBag<>()
            new MapCacheBag(
                //new HashMap(256)
                new UnifriedMap(256)
            )
        );
    }

    public LocalMemory() {
        this(new FrameClock());
    }

    public byte[] toBytes() throws IOException, InterruptedException {
        return new JBossMarshaller().objectToByteBuffer(this);
    }
}
