package nars.rl.horde;

import nars.rl.horde.demons.Demon;
import nars.rl.horde.math.MovingMeanVarNormalizer;

import java.util.List;


public class Surprise {
  private final MovingMeanVarNormalizer[] errorNormalizers;
  private final Demon[] demons;
  private final double[] errors;
  private double surpriseMeasure;

  public Surprise(List<Demon> demons, int trackingSpeed) {
    this.demons = new Demon[demons.size()];
    demons.toArray(this.demons);
    errors = new double[demons.size()];
    errorNormalizers = new MovingMeanVarNormalizer[demons.size()];
    for (int i = 0; i < errorNormalizers.length; i++)
      errorNormalizers[i] = new MovingMeanVarNormalizer(trackingSpeed);
  }

  public double updateSurpriseMeasure() {
    surpriseMeasure = 0;
    for (int i = 0; i < demons.length; i++) {
      double error = demons[i].learner().error();
      errorNormalizers[i].update(error);
      double scaledError = errorNormalizers[i].normalize(error);
      errors[i] = scaledError;
      surpriseMeasure = Math.max(surpriseMeasure, Math.abs(scaledError));
    }
    return surpriseMeasure;
  }

  /*String labelOf(int index) {
    return Labels.label(demons[index]);
  }*/
}
