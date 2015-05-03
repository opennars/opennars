package nars.rl.horde.test;

import com.google.common.collect.Lists;
import nars.rl.horde.Horde;
import nars.rl.horde.HordeScheduler;
import nars.rl.horde.LinearLearner;
import nars.rl.horde.demons.Demon;
import nars.rl.horde.functions.HordeUpdatable;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;


@SuppressWarnings("serial")
public class HordeSchedulerTest {


    static class FakeDemon implements Demon<Integer> {
        RealVector x_tp1;
        Integer a_t;
        RealVector x_t;
        public boolean updated = false;

        @Override
        public void update(RealVector x_t, Integer a_t, RealVector x_tp1) {
            this.x_t = x_t;
            this.a_t = a_t;
            this.x_tp1 = x_tp1;
            updated = true;
        }

        @Override
        public LinearLearner learner() {
            return null;
        }
    }

    static class FakeFunction implements HordeUpdatable<Integer> {
        RealVector x_tp1 = null;
        Integer a_t = null;
        RealVector x_t = null;
        RealVector o_tp1 = null;
        boolean demonState;
        final FakeDemon fakeDemon;

        public FakeFunction(FakeDemon fakeDemon) {
            this.fakeDemon = fakeDemon;
            demonState = fakeDemon.updated;
        }

        @Override
        public void update(RealVector o_tp1, RealVector x_t, Integer a_t, RealVector x_tp1) {
            this.o_tp1 = o_tp1;
            this.x_t = x_t;
            this.a_t = a_t;
            this.x_tp1 = x_tp1;
            this.demonState = fakeDemon.updated;
        }
    }

    @Test
    public void testScheduler() {
        RealVector o_tp1 = new ArrayRealVector(1);

        FakeDemon d1 = new FakeDemon(), d2 = new FakeDemon();
        final List<FakeDemon> demons = Lists.newArrayList(d1, d2);
        final FakeFunction[] beforeFunctions = {new FakeFunction(d1), new FakeFunction(d2)};
        final FakeFunction[] afterFunctions = {new FakeFunction(d1), new FakeFunction(d2)};
        Horde horde = new Horde(new HordeScheduler(3));
        horde.beforeFunctions().addAll(Lists.newArrayList(beforeFunctions));
        horde.demons().addAll(demons);
        horde.afterFunctions().addAll(Lists.newArrayList(afterFunctions));
        final RealVector x0 = new ArrayRealVector(1), x1 = new ArrayRealVector(1);

        final Integer a0 = 0;

        checkFunction(beforeFunctions, null, null, null, null, false);
        checkFunction(afterFunctions, null, null, null, null, false);
        horde.update(o_tp1, x0, a0, x1);
        checkFunction(beforeFunctions, x0, a0, o_tp1, x1, false);
        checkFunction(afterFunctions, x0, a0, o_tp1, x1, true);
        checkDemon(d1, x0, a0, x1);
        checkDemon(d2, x0, a0, x1);
    }

    public void checkDemon(FakeDemon d, final RealVector x_t, final Integer a_t, final RealVector x_tp1) {
        Assert.assertEquals(d.x_t, x_t);
        Assert.assertEquals(d.a_t, a_t);
        Assert.assertEquals(d.x_tp1, x_tp1);
    }

    public void checkFunction(FakeFunction[] fs, final RealVector x_t, final Integer a_t, RealVector o_tp1,
                              final RealVector x_tp1, boolean state) {
        for (FakeFunction f : fs) {
            Assert.assertEquals(f.o_tp1, o_tp1);
            Assert.assertEquals(f.x_t, x_t);
            Assert.assertEquals(f.a_t, a_t);
            Assert.assertEquals(f.x_tp1, x_tp1);
            Assert.assertEquals(f.demonState, state);
        }
    }
}
