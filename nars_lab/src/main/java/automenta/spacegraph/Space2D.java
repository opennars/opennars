package automenta.spacegraph;

import automenta.spacegraph.shape.Drawable;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;

import java.util.ArrayList;
import java.util.List;


public class Space2D implements Drawable {

     List<Drawable> drawables = new ArrayList<Drawable>();

    public Space2D() {
        super();
    }

    public Space2D(Drawable d) {
        this();
        drawables.add(d);
    }

    public List<Drawable> getDrawables() {
        return drawables;
    }

    @Override
    public void draw(GL2 gl) {
        int id = 0;

        gl.glEnable(GL.GL_BLEND);			// Turn Blending On
        gl.glDisable(GL.GL_DEPTH_TEST);	// Turn Depth Testing Off
        //gl.glColor4f(1.0f, 1.0f, 1.0f, 0.5f);					// Full Brightness.  50% Alpha (new )
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE);					// Set The Blending Function For Translucency (new )

        synchronized (drawables) {
            for (Drawable d : drawables) {
                //gl.glPushName(id++);
                d.draw(gl);
                //gl.glPopName();
            }
        }
    }

    public void removeAll() {
        synchronized (drawables) {
            drawables.clear();
        }
    }

    public <D extends Drawable> D add(D d) {
        synchronized (drawables) {
            drawables.add(d);
        }
        return d;
    }

    public boolean remove(Drawable d) {
        synchronized (drawables) {
            return drawables.add(d);
        }
    }
}
