/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package automenta.spacegraph.shape;

import automenta.spacegraph.math.linalg.Vec3f;
import com.jogamp.opengl.GL2;

/**
 *
 * @author seh
 */
class Spatial {
    protected Vec3f center;
    protected Vec3f scale;
    protected Vec3f rotation;

    public Spatial() {
        this(new Vec3f(0,0,0), new Vec3f(1,1,1), new Vec3f(0,0,0));
    }

    public Spatial(Vec3f center, Vec3f size) {
        this(center, size, new Vec3f(0,0,0));        
    }

    public Spatial(Vec3f center, Vec3f size, Vec3f rotation) {
        super();
        this.center = center;
        this.scale = size;
        this.rotation = rotation;
    }    
    

    public Spatial scale(float sx, float sy, float sz) {
        getScale().set(sx, sy, sz);
        return this;
    }
    
    
    public Spatial center(float cx, float cy, float cz) {
        getCenter().set(cx, cy, cz);        
        return this;
    }

    public Spatial move(float dx, float dy, float dz) {
        float cx = getCenter().x();
        float cy = getCenter().y();
        float cz = getCenter().z();
        getCenter().set(cx + dx, cy + dy, cz + dz);
        return this;
    }

    public Vec3f getCenter() {
        return center;
    }

    public Vec3f getScale() {
        return scale;
    }

//    public Rotf getRotation() {
//        return rotation;
//    }

    public Vec3f getRotation() {
        return rotation;
    }
    
    protected void transform(GL2 gl) {
//        transform.makeIdent();
//        transform.setScale(getSize());
//        transform.setTranslation(getCenter());
//        transform.setRotation(getRotation());
//        gl.glLoadMatrixf(transform.data, 0);


        gl.glTranslatef(getCenter().x(), getCenter().y(), getCenter().z());


        gl.glRotatef((float)Math.toDegrees( getRotation().x() ), 1, 0, 0);
        gl.glRotatef((float)Math.toDegrees( getRotation().y() ), 0, 1, 0);
        gl.glRotatef((float)Math.toDegrees( getRotation().z() ), 0, 0, 1);

        gl.glScalef(getScale().x(), getScale().y(), getScale().z());

    }

}
