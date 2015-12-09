/** (C) Copyright 2010 Hal Hildebrand, All Rights Reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */
package hellblazer.gossip;

import junit.framework.TestCase;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Basic testing of the system view
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 * 
 */
public class SystemViewTest extends TestCase {

    public void testLiveMembers() throws Exception {
        Random random = mock(Random.class);
        when(random.nextInt(3)).thenReturn(2);

        InetSocketAddress local = new InetSocketAddress("127.0.0.1", 1);

        InetSocketAddress seed1 = new InetSocketAddress("127.0.0.1", 2);
        InetSocketAddress seed2 = new InetSocketAddress("127.0.0.1", 3);
        InetSocketAddress seed3 = new InetSocketAddress("127.0.0.1", 4);
        InetSocketAddress seed4 = new InetSocketAddress("127.0.0.1", 5);

        InetSocketAddress live1 = new InetSocketAddress("127.0.0.1", 10);
        InetSocketAddress live2 = new InetSocketAddress("127.0.0.1", 11);
        InetSocketAddress live3 = new InetSocketAddress("127.0.0.1", 12);

        List<InetSocketAddress> seedHosts = Arrays.asList(seed1, seed2, seed3,
                                                          seed4);
        int quarantineDelay = 30;
        int unreachableDelay = 400;
        SystemView view = new SystemView(random, local, seedHosts,
                                         quarantineDelay, unreachableDelay);

        List<InetSocketAddress> liveSet = new ArrayList<>();
        liveSet.add(live1);
        liveSet.add(live2);
        liveSet.add(live3);

        view.markAlive(live1);
        view.markAlive(live2);
        view.markAlive(live3);

        assertEquals(live3, view.getRandomMember(liveSet));
    }

    public void testQuarantined() throws Exception {
        Random random = mock(Random.class);

        InetSocketAddress local = new InetSocketAddress("127.0.0.1", 1);

        InetSocketAddress seed1 = new InetSocketAddress("127.0.0.1", 2);
        InetSocketAddress seed2 = new InetSocketAddress("127.0.0.1", 3);
        InetSocketAddress seed3 = new InetSocketAddress("127.0.0.1", 4);
        InetSocketAddress seed4 = new InetSocketAddress("127.0.0.1", 5);

        InetSocketAddress live1 = new InetSocketAddress("127.0.0.1", 10);
        InetSocketAddress live2 = new InetSocketAddress("127.0.0.1", 11);
        InetSocketAddress live3 = new InetSocketAddress("127.0.0.1", 12);
        InetSocketAddress live4 = new InetSocketAddress("127.0.0.1", 20);
        InetSocketAddress live5 = new InetSocketAddress("127.0.0.1", 21);
        InetSocketAddress live6 = new InetSocketAddress("127.0.0.1", 22);
        InetSocketAddress live7 = new InetSocketAddress("127.0.0.1", 23);
        InetSocketAddress live8 = new InetSocketAddress("127.0.0.1", 24);

        List<InetSocketAddress> seedHosts = Arrays.asList(seed1, seed2, seed3,
                                                          seed4);
        int quarantineDelay = 30;
        int unreachableDelay = 400;
        SystemView view = new SystemView(random, local, seedHosts,
                                         quarantineDelay, unreachableDelay);

        view.markAlive(live1);
        view.markAlive(live2);
        view.markAlive(live3);
        view.markAlive(live4);
        view.markAlive(live5);
        view.markAlive(live6);
        view.markAlive(live7);
        view.markAlive(live8);

        assertFalse(view.isQuarantined(live4));

        view.markDead(live1, 0);
        assertTrue(view.isQuarantined(live1));
        view.cullQuarantined(quarantineDelay + 10);
        assertFalse(view.isQuarantined(live1));
    }

    public void testSeedMembers() throws Exception {
        Random random = mock(Random.class);
        when(random.nextInt(4)).thenReturn(2);

        InetSocketAddress local = new InetSocketAddress("127.0.0.1", 1);

        InetSocketAddress seed1 = new InetSocketAddress("127.0.0.1", 2);
        InetSocketAddress seed2 = new InetSocketAddress("127.0.0.1", 3);
        InetSocketAddress seed3 = new InetSocketAddress("127.0.0.1", 4);
        InetSocketAddress seed4 = new InetSocketAddress("127.0.0.1", 5);

        InetSocketAddress live1 = new InetSocketAddress("127.0.0.1", 10);
        InetSocketAddress live2 = new InetSocketAddress("127.0.0.1", 11);
        InetSocketAddress live3 = new InetSocketAddress("127.0.0.1", 12);
        InetSocketAddress live4 = new InetSocketAddress("127.0.0.1", 20);
        InetSocketAddress live5 = new InetSocketAddress("127.0.0.1", 21);
        InetSocketAddress unreachable1 = new InetSocketAddress("127.0.0.1", 13);
        InetSocketAddress unreachable2 = new InetSocketAddress("127.0.0.1", 14);
        InetSocketAddress unreachable3 = new InetSocketAddress("127.0.0.1", 15);
        InetSocketAddress unreachable4 = new InetSocketAddress("127.0.0.1", 16);
        InetSocketAddress unreachable5 = new InetSocketAddress("127.0.0.1", 17);

        List<InetSocketAddress> seedHosts = Arrays.asList(seed1, seed2, seed3,
                                                          seed4);
        int quarantineDelay = 30;
        int unreachableDelay = 400;
        SystemView view = new SystemView(random, local, seedHosts,
                                         quarantineDelay, unreachableDelay);
        assertNull(view.getRandomSeedMember(Arrays.asList(seed1)));
        assertEquals(seed3, view.getRandomSeedMember(Arrays.asList(local)));

        view.markAlive(live1);
        view.markAlive(live2);
        view.markAlive(live3);
        view.markAlive(unreachable1);
        view.markAlive(unreachable2);
        view.markAlive(unreachable3);
        view.markAlive(unreachable4);
        view.markAlive(unreachable5);
        view.markDead(unreachable1, 0);
        view.markDead(unreachable2, 0);
        view.markDead(unreachable3, 0);
        view.markDead(unreachable4, 0);
        view.markDead(unreachable5, 0);
        view.cullQuarantined(quarantineDelay + 10);

        when(random.nextDouble()).thenReturn(0.75, 0.45, 0.0);

        assertEquals(seed3, view.getRandomSeedMember(Arrays.asList(local)));

        view.markAlive(live4);
        view.markAlive(live5);
        assertNotNull(view.getRandomSeedMember(Arrays.asList(local)));
    }

    public void testUnreachableMembers() throws Exception {
        Random random = mock(Random.class);

        InetSocketAddress local = new InetSocketAddress("127.0.0.1", 1);

        InetSocketAddress seed1 = new InetSocketAddress("127.0.0.1", 2);
        InetSocketAddress seed2 = new InetSocketAddress("127.0.0.1", 3);
        InetSocketAddress seed3 = new InetSocketAddress("127.0.0.1", 4);
        InetSocketAddress seed4 = new InetSocketAddress("127.0.0.1", 5);

        InetSocketAddress live1 = new InetSocketAddress("127.0.0.1", 10);
        InetSocketAddress live2 = new InetSocketAddress("127.0.0.1", 11);
        InetSocketAddress live3 = new InetSocketAddress("127.0.0.1", 12);
        InetSocketAddress live4 = new InetSocketAddress("127.0.0.1", 20);
        InetSocketAddress live5 = new InetSocketAddress("127.0.0.1", 21);
        InetSocketAddress live6 = new InetSocketAddress("127.0.0.1", 22);
        InetSocketAddress live7 = new InetSocketAddress("127.0.0.1", 23);
        InetSocketAddress live8 = new InetSocketAddress("127.0.0.1", 24);

        InetSocketAddress unreachable1 = new InetSocketAddress("127.0.0.1", 13);
        InetSocketAddress unreachable2 = new InetSocketAddress("127.0.0.1", 14);
        InetSocketAddress unreachable3 = new InetSocketAddress("127.0.0.1", 15);
        InetSocketAddress unreachable4 = new InetSocketAddress("127.0.0.1", 16);

        List<InetSocketAddress> seedHosts = Arrays.asList(seed1, seed2, seed3,
                                                          seed4);
        int quarantineDelay = 30;
        int unreachableDelay = 400;
        SystemView view = new SystemView(random, local, seedHosts,
                                         quarantineDelay, unreachableDelay);

        view.markAlive(live1);
        view.markAlive(live2);
        view.markAlive(live3);
        view.markAlive(live4);
        view.markAlive(live5);
        view.markAlive(live6);
        view.markAlive(live7);
        view.markAlive(live8);
        view.markAlive(unreachable1);
        view.markAlive(unreachable2);
        view.markAlive(unreachable3);
        view.markAlive(unreachable4);
        view.markDead(unreachable1, 0);
        view.markDead(unreachable2, 0);
        view.markDead(unreachable3, 0);
        view.markDead(unreachable4, 0);
        view.cullQuarantined(quarantineDelay + 10);

        when(random.nextInt(4)).thenReturn(2, 3);
        when(random.nextDouble()).thenReturn(0.75, 0.40);

        assertNull(view.getRandomUnreachableMember(8));
        assertEquals(unreachable3, view.getRandomUnreachableMember(8));
    }
}
