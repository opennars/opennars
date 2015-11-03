package nars.io;

import nars.util.data.Util;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.Test;

import java.net.SocketException;

import static org.junit.Assert.assertTrue;

/**
 * Created by me on 10/7/15.
 */
public class StreamOperatorsTest {


    @Test
    public void testUDP() throws SocketException, InterruptedException {
        UDPNetwork<String> a = new UDPNetwork("a",10001);
        UDPNetwork<String> b = new UDPNetwork("b",10002);
        b.peer("localhost", 10001, 1.0f);

        System.out.println("Testing UDP pair at 60hz");
        a.setFrequency(60);
        b.setFrequency(60);

        DescriptiveStatistics stat = new DescriptiveStatistics();

        Thread test = Thread.currentThread();

        a.onIn(received -> {
            //System.out.println(aIn);
            a.out(received.getTwo());
        });
        b.onIn(m -> {
            long start = Long.valueOf(m.getTwo()).longValue();
            long now = System.currentTimeMillis();

            stat.addValue(now-start);

            test.interrupt();
        });


        int bursts = 5;
        int burstSize = 8;

        for (int i = 0; i < bursts; i++) {
            final long now = System.currentTimeMillis();
            for (int j = 0; j < burstSize; j++) {
                b.out(String.valueOf(now));

                Util.pause(((long) ( 1000.0 / 60.0)));
            }

            //System.out.println(stat);

        }


        System.out.println(stat);
        assertTrue(stat.getN() > 1);


        System.out.println("average lag time: " +
                (stat.getMean() - (1000.0/60.0)) + "ms"
        );

        a.stop();
        b.stop();

    }

}
