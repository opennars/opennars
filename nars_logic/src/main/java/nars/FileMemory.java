package nars;

import nars.bag.impl.InfiniCacheBag;
import nars.clock.Clock;
import nars.util.data.random.XorShift1024StarRandom;
import nars.util.db.InfiniPeer;

/** file-persisted memory storage */
public class FileMemory extends Memory {

    final static int MAX_INSTANCES = 128 * 1024;

    public FileMemory(String path, String id, Clock clock) {
        super(  clock,
                new XorShift1024StarRandom(1),
                new InfiniCacheBag(
                        InfiniPeer.file(path, MAX_INSTANCES).the(id)
                ));
    }

}
