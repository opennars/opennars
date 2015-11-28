package automenta.spacegraph.control;

import automenta.spacegraph.math.linalg.Vec2f;
import automenta.spacegraph.math.linalg.Vec3f;
import automenta.spacegraph.shape.Rect;

import java.util.HashSet;
import java.util.Set;

public class Pointer {

    public final Vec2f pixel = new Vec2f(0, 0);
    public final Vec3f world = new Vec3f(0, 0, 0);
    public final Set<Touchable> touching = new HashSet();
    public boolean[] buttons = new boolean[3];

    public final Vec3f dragStartworld = new Vec3f(0, 0, 0);
    public boolean dragging = false;

    public Touchable getSmallestTouched() {
        //TODO actually implement it, but for now just return the first entry in 'touching'
        if (touching.isEmpty())
            return null;
        
        Touchable smallest = null;
        float smallestArea = Float.POSITIVE_INFINITY;
        
        for (Touchable t : touching) {
            if (t instanceof Rect) {
                Rect r = (Rect)t;
                float a = r.getScale().x() * r.getScale().y();
                if (a < smallestArea) {
                    smallestArea = a;
                    smallest = t;
                }
            }
            else {
                //....??
            }
        }
        return smallest;
    }
}
