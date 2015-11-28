/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package automenta.spacegraph.shape;

import automenta.spacegraph.math.linalg.Vec3f;
import com.jogamp.opengl.GL2;

/**
 * maps [0..getWidth()], [0..getHeight()] to a value
 * @author seh
 */
abstract public class HeightMap extends Spatial implements Drawable {

    boolean bRender = true;


    public HeightMap() {
        super(new Vec3f(0,0,0), new Vec3f(1,1,1), new Vec3f(0,0,0));
    }

    float c[] = new float[4];
    Vec3f n = new Vec3f();

    public void draw(GL2 gl) {
        transform(gl);
        
        float X = 0, Y = 0;                                    // Create Some Variables To Walk The Array With.
        float x, z;
        float y;// Create Some Variables For Readability

        if (bRender) // What We Want To Render
        {
            gl.glBegin(gl.GL_QUADS);                          // Render Polygons
        } else {
            gl.glBegin(gl.GL_LINES);                          // Render Lines Instead
        }

        final float STEP_SIZE = getStepSize();

        
        for (X = getMinX(); X <= (getMaxX()); X += STEP_SIZE) {
            for (Y = getMinY(); Y <= (getMaxY()); Y += STEP_SIZE) {
                {
                    float px = X + STEP_SIZE - X;
                    float py = 0;
                    float pz = getValue(X+STEP_SIZE, Y) - getValue(X, Y);

                    float qx = 0;
                    float qy = Y + STEP_SIZE - Y;
                    float qz = getValue(X, Y+STEP_SIZE) - getValue(X, Y);

                    float nx = py * qz - pz * qy;
                    float ny = pz * qx - px * qz;
                    float nz = px * qy - py * qx;

                    //calculate normal of a triangle
                    //px#=vx1-vx0
                    //py#=vy1-vy0
                    //pz#=vz1-vz0
                    //
                    //qx#=vx2-vx0
                    //qy#=vy2-vy0
                    //qz#=vz2-vz0
                    //
                    //nx#=(py*qz)-(pz*qy)
                    //ny#=(pz*qx)-(px*qz)
                    //nz#=(px*qy)-(py*qx)


                    n.set(nx, ny, nz);
                    n.normalize();

                    gl.glNormal3f(n.x(), n.y(), n.z());
                }


                // Get The (X, Y, Z) Value For The Bottom Left Vertex
                x = X;
                y = Y;
                z = getValue(x, y);

                getVertexColor(x, y, c);
                gl.glColor3f(c[0], c[1], c[2]);
                gl.glVertex3f(x, y, z);                          // Send This Vertex To OpenGL To Be Rendered (Integer Points Are Faster)

                // Get The (X, Y, Z) Value For The Top Left Vertex
                x = X;
                y = Y + STEP_SIZE;
                z = getValue(x, y);

                getVertexColor(x, y, c);
                gl.glColor3f(c[0], c[1], c[2]);
                gl.glVertex3f(x, y, z);	                         // Send This Vertex To OpenGL To Be Rendered

                // Get The (X, Y, Z) Value For The Top Right Vertex
                x = X + STEP_SIZE;
                y = Y + STEP_SIZE;
                z = getValue(x, y);

                getVertexColor(x, y, c);
                gl.glColor3f(c[0], c[1], c[2]);
                gl.glVertex3f(x, y, z);                          // Send This Vertex To OpenGL To Be Rendered

                // Get The (X, Y, Z) Value For The Bottom Right Vertex
                x = X + STEP_SIZE;
                y = Y;
                z = getValue(x, y);

                // Set The Color Value Of The Current Vertex
                getVertexColor(x, y, c);
                gl.glColor3f(c[0], c[1], c[2]);
                gl.glVertex3f(x, y, z);                          // Send This Vertex To OpenGL To Be Rendered
            }
        }
        gl.glEnd();
        //gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);                // Reset The Color
    }

    abstract public float getMinX();
    abstract public float getMinY();
    abstract public float getMaxX();
    abstract public float getMaxY();

    abstract public float getStepSize();

    abstract public float getValue(float x, float y);
    abstract public void getVertexColor(float x, float y, float[] color);
}
