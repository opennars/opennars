/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package automenta.spacegraph.ui;

import automenta.spacegraph.control.Pointer;
import automenta.spacegraph.control.Touchable;
import automenta.spacegraph.math.linalg.Vec3f;
import automenta.spacegraph.math.linalg.Vec4f;
import automenta.spacegraph.shape.Rect;
import com.jogamp.opengl.GL2;

/**
 *
 * @author me
 */
public class Panel extends Rect implements Touchable {

    private Vec4f leftColor = new Vec4f(0.75f, 0.75f, 0.75f, 0.5f);
    private Vec4f upColor = new Vec4f(0.75f, 0.75f, 0.75f, 0.5f);
    private Vec4f rightColor = new Vec4f(0.15f, 0.15f, 0.15f, 0.5f);
    private Vec4f downColor = new Vec4f(0.15f, 0.15f, 0.15f, 0.5f);
    
    public Panel() {
        super();
    }
    public Panel(Vec3f center, Vec3f scale) {
        super(center, scale);
    }
    
    public Vec4f getLeftColor() {
        return leftColor;
    }

    public Vec4f getRightColor() {
        return rightColor;
    }

    public Vec4f getUpColor() {
        return upColor;
    }

    public Vec4f getDownColor() {
        return downColor;
    }
    
    
    @Override
    public void draw(GL2 gl) {
        if (isFilled()) {
            gl.glPushMatrix();

            transform(gl);


            final float w = 0.5f;
            final float h = 0.5f;

            float bxh = 0.03f;
            float byh = 0.03f;
            
            Vec4f bc = getBackgroundColor();
            Vec4f bl = getLeftColor();
            Vec4f br = getRightColor();
            Vec4f bu = getUpColor();
            Vec4f bd = getDownColor();
            
            // Six faces of cube
            // Top face
            gl.glBegin(GL2.GL_QUADS);
            {
                gl.glColor4f(bc.x(), bc.y(), bc.z(), bc.w());
                //Front
                //gl.glNormal3f(0, 0, 1); {
                gl.glVertex3f(-w+bxh, -h+byh, 0);
                gl.glVertex3f(w-bxh, -h+byh, 0);
                gl.glVertex3f(w-bxh, h-byh, 0);
                gl.glVertex3f(-w+bxh, h-byh, 0);
                //}
                

                //Left Side
                gl.glColor4f(bl.x(), bl.y(), bl.z(), bl.w());
                gl.glVertex3f(-w, -h, 0);
                gl.glVertex3f(-w, h, 0);
                gl.glVertex3f(-w+bxh, h-byh, 0);
                gl.glVertex3f(-w+bxh, -h+byh, 0);


                //RightSide
                gl.glColor4f(br.x(), br.y(), br.z(), br.w());
                gl.glVertex3f(w, -h, 0);
                gl.glVertex3f(w, h, 0);
                gl.glVertex3f(w-bxh, h-byh, 0);
                gl.glVertex3f(w-bxh, -h+byh, 0);

                //UpSide
                gl.glColor4f(bu.x(), bu.y(), bu.z(), bu.w());
                gl.glVertex3f(-w, h, 0);
                gl.glVertex3f(w, h, 0);
                gl.glVertex3f(w-bxh, h-byh, 0);
                gl.glVertex3f(-w+bxh, h-byh, 0);
                
                //DownSide
                gl.glColor4f(bd.x(), bd.y(), bd.z(), bd.w());
                gl.glVertex3f(-w, -h, 0);
                gl.glVertex3f(w, -h, 0);
                gl.glVertex3f(w-bxh, -h+byh, 0);
                gl.glVertex3f(-w+bxh, -h+byh, 0);
            }
            gl.glEnd();
            
            drawFront(gl);

            gl.glPopMatrix();
        }
    }

    @Override
    public boolean isTouchable() {
        return true;
    }

    @Override
    public void onTouchChange(Pointer pointer, boolean touched) {
    }

    
}
