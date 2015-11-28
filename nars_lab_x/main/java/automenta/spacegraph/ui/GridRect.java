/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package automenta.spacegraph.ui;

import automenta.spacegraph.math.linalg.Vec4f;
import automenta.spacegraph.shape.Rect;
import com.jogamp.opengl.GL2;

/**
 *
 * @author seh
 */
public class GridRect extends Rect {

    float lineWidth;
    Vec4f lineColor;
    float dx = 0.1f;
    float dy = 0.1f;

    public GridRect(float sx, float sy, float lineWidth, Vec4f lineColor) {
        super();
        this.lineWidth = lineWidth;
        this.lineColor = lineColor;
        scale(sx, sy, 1.0f);
        color(0,0,0);
        setFilled(false);        
    }
    
    public GridRect(float sx, float sy) {
        this(sx, sy, 3.0f, new Vec4f(0.5f, 0.5f, 0.5f, 1f));
    }
    
       
    @Override public void draw(GL2 gl) {
        super.draw(gl);

        gl.glPushMatrix();

        transform(gl);
        
        gl.glEnable(gl.GL_LINE_SMOOTH);
        gl.glLineWidth(lineWidth);

        gl.glColor4f(lineColor.x(), lineColor.y(), lineColor.z(), lineColor.w());

        gl.glBegin(gl.GL_LINES);
        {
            for (float x = -0.5f; x < 0.5f ; x+=dx) {
                gl.glVertex3f(x, -0.5f, 0);
                gl.glVertex3f(x, 0.5f, 0);
            }
            for (float y = -0.5f; y < 0.5f ; y+=dy) {
                gl.glVertex3f(-0.5f, y, 0);
                gl.glVertex3f(0.5f, y, 0);
            }
        }
        gl.glEnd();
        gl.glPopMatrix();

    }

}
