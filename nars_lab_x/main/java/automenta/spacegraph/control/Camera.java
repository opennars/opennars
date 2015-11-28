package automenta.spacegraph.control;

import automenta.spacegraph.math.linalg.Vec3f;

public class Camera {

    public final Vec3f camPos = new Vec3f(0, 0, 10);
    public final Vec3f camTarget = new Vec3f(0, 0, 0);
    public final Vec3f camUp = new Vec3f(0, 1, 0);
}
