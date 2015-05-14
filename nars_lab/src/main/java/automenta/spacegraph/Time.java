package automenta.spacegraph;

public interface Time {
  /** Updates this Time object. Call update() each frame before
      calling the accessor routines. */
  public void  update();
  /** Time in seconds since beginning of application. */
  public double time();
  /** Time in seconds since last update. */
  public double deltaT();
}
