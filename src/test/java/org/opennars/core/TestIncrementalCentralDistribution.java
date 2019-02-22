package org.opennars.core;

import org.junit.Test;
import org.opennars.entity.Concept;

import static org.junit.Assert.assertTrue;

public class TestIncrementalCentralDistribution {
    @Test
    public void testA() throws Exception {
        Concept.IncrementalCentralDistribution d = new Concept.IncrementalCentralDistribution();
        d.next(10.0);
        d.next(2.0);
        d.next(38.0);
        d.next(23.0);
        d.next(38.0);
        d.next(23.0);
        d.next(21.0);

        assertTrue(Math.abs(d.calcVariance() - 12.298996142875) < 0.01);
        assertTrue(Math.abs(d.mean - 22.142857142857) < 0.01);
    }
}
