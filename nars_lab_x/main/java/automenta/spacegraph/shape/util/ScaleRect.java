package automenta.spacegraph.shape.util;

import automenta.spacegraph.math.linalg.Vec3f;
import automenta.spacegraph.shape.Rect;

public class ScaleRect extends Vec3f {

    private final Rect rect;
    private final float sx;
    private final float sy;

    public ScaleRect(Rect r, float sx, float sy) {
        super();
        this.rect = r;
        this.sx = sx;
        this.sy = sy;
    }

    @Override
    public float x() {
        return rect.getScale().x() * sx;
    }

    @Override
    public float y() {
        return rect.getScale().y() * sy;
    }

    @Override
    public float z() {
        return rect.getScale().z();
    }
}
