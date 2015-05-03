package nars.rl.horde;


import nars.rl.horde.demons.Demon;
import nars.rl.horde.functions.HordeUpdatable;
import org.apache.commons.math3.linear.RealVector;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Horde<A> implements Serializable {


  final List<HordeUpdatable> beforeFunctions = new ArrayList<HordeUpdatable>();
  final List<Demon> demons = new ArrayList<Demon>();
  final List<HordeUpdatable> afterFunctions = new ArrayList<HordeUpdatable>();
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

  public List<HordeUpdatable> beforeFunctions() {
    return beforeFunctions;
  }

  public List<HordeUpdatable> afterFunctions() {
    return afterFunctions;
  }

  public List<Demon> demons() {
    return demons;
  }

  public boolean addBeforeFunction(HordeUpdatable function) {
    return beforeFunctions.add(function);
  }

  public boolean addAfterFunction(HordeUpdatable function) {
    return afterFunctions.add(function);
  }

  public boolean addDemon(Demon demon) {
    return demons.add(demon);
  }
}
