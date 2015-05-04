package nars.rl.horde;


import nars.rl.horde.demons.Demon;
import nars.rl.horde.functions.HordeUpdatable;
import org.apache.commons.math3.linear.RealVector;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Horde<A> implements Serializable {


    final List<HordeUpdatable<A>> beforeFunctions = new ArrayList();
    final List<Demon<A>> demons = new ArrayList();
    final List<HordeUpdatable<A>> afterFunctions = new ArrayList();
    private final HordeScheduler scheduler;

    public Horde(int threads) {
        this(new HordeScheduler(threads));
    }

    public Horde(HordeScheduler scheduler) {
        this.scheduler = scheduler;
    }

//  public String demonLabel(int i) {
//    return Labels.label(demons.get(i));
//  }
//
//  public String beforeFunctionLabel(int i) {
//    return Labels.label(beforeFunctions.get(i));
//  }
//
//  public String afterFunctionLabel(int i) {
//    return Labels.label(afterFunctions.get(i));
//  }

    public void update(final RealVector o_tp1, final RealVector x_t, final A a_t, final RealVector x_tp1) {
        scheduler.update(new HordeScheduler.Context() {
            @Override
            public void updateElement(int index) {
                beforeFunctions.get(index).update(o_tp1, x_t, a_t, x_tp1);
            }

            @Override
            public int nbElements() {
                return beforeFunctions.size();
            }
        });
        scheduler.update(new HordeScheduler.Context() {
            @Override
            public void updateElement(int index) {
                demons.get(index).update(x_t, a_t, x_tp1);
            }

            @Override
            public int nbElements() {
                return demons.size();
            }
        });
        scheduler.update(new HordeScheduler.Context() {
            @Override
            public void updateElement(int index) {
                afterFunctions.get(index).update(o_tp1, x_t, a_t, x_tp1);
            }

            @Override
            public int nbElements() {
                return afterFunctions.size();
            }
        });
    }

    public List<HordeUpdatable<A>> beforeFunctions() {
        return beforeFunctions;
    }

    public List<HordeUpdatable<A>> afterFunctions() {
        return afterFunctions;
    }

    public List<Demon<A>> demons() {
        return demons;
    }

    public boolean addBeforeFunction(HordeUpdatable<A> function) {
        return beforeFunctions.add(function);
    }

    public boolean addAfterFunction(HordeUpdatable<A> function) {
        return afterFunctions.add(function);
    }

    public Horde<A> addDemon(Demon<A> demon) {
        demons.add(demon);
        return this;
    }
}
