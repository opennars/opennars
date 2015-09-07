package spangraph;

import org.infinispan.Cache;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 * Created by me on 9/7/15.
 */
public class InfiniPeerTest {

    @Test
    public void testTmpSave() {
        String key = "_testTmpSave";
        Object standard="xyz", st2;

        {
            Cache<Object, Object> cache = (InfiniPeer.tmp().getCache());
            cache.put(key, standard);
        }
        {
            Cache<Object, Object> cache = (InfiniPeer.tmp().getCache());
            st2 = (Object) cache.get(key);

        }

        assertEquals(standard, st2);
    }
}