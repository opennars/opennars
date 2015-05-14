/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package automenta.spacegraph.shape;

import automenta.spacegraph.math.linalg.Vec2f;
import automenta.spacegraph.math.linalg.Vec3f;
import automenta.spacegraph.math.linalg.Vec4f;
import com.jogamp.opengl.GL2;

import java.awt.geom.Path2D;

/**
 *
 * @author seh
 */
public class Rect extends Spatial implements Drawable {

    private Vec4f backgroundColor = new Vec4f(0.5f, 0.5f, 0.5f, 1.0f);
    boolean filled = true;
    final private Vec2f a = new Vec2f();
    final private Vec2f b = new Vec2f();
    final private Vec2f c = new Vec2f();
    final private Vec2f d = new Vec2f();
    final private Path2D.Double shapePath = new Path2D.Double();

    public Rect() {
        super();
        updateGeometry();
    }

    public Rect(Vec3f pos, Vec3f scale) {
        super(pos, scale);
        updateGeometry();
    }
    
    public Rect scale(float sx, float sy) {
        return scale(sx, sy, 1.0f);        
    }

    public Rect center(float cx, float cy) {
        return center(cx, cy, 0);        
    }
    
    
    public Rect tilt(float newTilt) {
        rotation.setZ(newTilt);
        updateGeometry();
        return this;
    }

    public float getTilt() {
        return rotation.z();
    }

    public void setFilled(boolean filled) {
        this.filled = filled;
    }

    public boolean isFilled() {
        return filled;
    }

    public Rect corner(float blX, float blY, float urX, float urY) {
        float w = Math.abs(blX - urX);
        float h = Math.abs(blY - urY);
        float x = ( urX + blX) * 0.5f;
        float y = ( urY + blY) * 0.5f;
        center(x, y, 0);
        scale(w, h, 1);
        return this;
    }
    
    public Rect color(float r, float g, float b) {
        return color(r, g, b, 1.0f);
    }

    public Rect color(float r, float g, float b, float a) {
        return color(new Vec4f(r, g, b, a));
    }

    public Rect color(Vec4f c) {
        setBackgroundColor(c);
        return this;
    }

    public void setBackgroundColor(Vec4f backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public Vec4f getBackgroundColor() {
        return backgroundColor;
    }

    public void draw(GL2 gl) {
        if (isFilled()) {
            gl.glPushMatrix();

            transform(gl);


            final float w = 0.5f;
            final float h = 0.5f;

            // Six faces of cube
            // Top face
            Vec4f bc = getBackgroundColor();
            gl.glColor4f(bc.x(), bc.y(), bc.z(), bc.w());
            gl.glBegin(GL2.GL_QUADS);
            {
                //Front
                //gl.glNormal3f(0, 0, 1); {
                gl.glVertex3f(-w, -h, 0);
                gl.glVertex3f(w, -h, 0);
                gl.glVertex3f(w, h, 0);
                gl.glVertex3f(-w, h, 0);
                //}
            }
            gl.glEnd();

            drawFront(gl);

            gl.glPopMatrix();
        }
    }

    /** draw within -1..+1 for x, y */
    protected void drawFront(GL2 gl) {
    }

    @Override
    public Rect scale(float sx, float sy, float sz) {
        super.scale(sx, sy, sz);
        updateGeometry();
        return this;
    }

    @Override public Rect center(float dx, float dy, float dz) {
        super.center(dx, dy, dz);
        updateGeometry();
        return this;
    }
    
    public Rect move(float x, float y, float z) {
        super.move(x, y, z);
        updateGeometry();
        return this;
    }

    public Rect move(float x, float y) {
        return move(x, y, 0);
    }


//    @Deprecated
//    public static boolean CircleIntersection(float cx, float cy, float radius, float px, float py) {
//        double d = (px - cx) * (px - cx) + (py - cy) * (py - cy);
//        return (d < radius * radius);
//    }
    public void updateGeometry() {
        float ux = (float) (scale.x() * Math.cos(rotation.z())) / 2.0f;
        float uy = (float) (scale.x() * Math.sin(rotation.z())) / 2.0f;

        float vx = (float) (scale.y() * Math.cos(rotation.z() + Math.PI / 2.0)) / 2.0f;
        float vy = (float) (scale.y() * Math.sin(rotation.z() + Math.PI / 2.0)) / 2.0f;

        a.set(center.x() + ux + vx, center.y() + uy + vy);
        b.set(center.x() - ux + vx, center.y() - uy + vy);
        c.set(center.x() - ux - vx, center.y() - uy - vy);
        d.set(center.x() + ux - vx, center.y() + uy - vy);

        shapePath.reset();
        shapePath.moveTo(a.x(), a.y());
        shapePath.lineTo(b.x(), b.y());
        shapePath.lineTo(c.x(), c.y());
        shapePath.lineTo(d.x(), d.y());
    }

    public boolean intersects(Vec2f p) {
        return (shapePath.contains(p.x(), p.y()));
    }

    public void setCenter(Vec3f newCenter) {
        this.center = newCenter;
        updateGeometry();
    }
    public void setScale(Vec3f newScale) {
        this.scale = newScale;
        updateGeometry();
    }

//    public boolean is_inside_triangle(Float x, Float y, Float x1, Float y1, Float x2, Float y2, Float x3, Float y3) {
//        Float fAB;
//        Float fBC;
//        Float fCA;
//
//        fAB = (y - y1) * (x2 - x1) - (x - x1) * (y2 - y1);
//        fCA = (y - y3) * (x1 - x3) - (x - x3) * (y1 - y3);
//        fBC = (y - y2) * (x3 - x2) - (x - x2) * (y3 - y2);
//
//        if (fAB * fBC > 0 && fBC * fCA > 0) {
//            return true;
//        } else {
//            return false;
//        }
//    }
//
//    public static boolean pointInTriangle(Vec2f p, Vec2f a, Vec2f b, Vec2f c) {
//        //see: http://www.blackpawn.com/texts/pointinpoly/default.html
//
//        // Compute vectors        
//        Vec2f v0 = new Vec2f(c.x() - a.x(), c.y() - a.y());
//        Vec2f v1 = new Vec2f(b.x() - a.x(), b.y() - a.y());
//        Vec2f v2 = new Vec2f(p.x() - a.x(), p.y() - a.y());
//
//        // Compute dot products
//        float dot00 = v0.dot(v0);
//        float dot01 = v0.dot(v1);
//        float dot02 = v0.dot(v2);
//        float dot11 = v1.dot(v1);
//        float dot12 = v1.dot(v2);
//
//        // Compute barycentric coordinates
//        float invDenom = 1f / (dot00 * dot11 - dot01 * dot01);
//        float u = (dot11 * dot02 - dot01 * dot12) * invDenom;
//        float v = (dot00 * dot12 - dot01 * dot02) * invDenom;
//
//        // Check if point is in triangle
//        return (u > 0) && (v > 0) && (u + v < 1);
//    }
}
