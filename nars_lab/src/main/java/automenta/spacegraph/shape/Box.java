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
public class Box extends Spatial implements Drawable {
    public final boolean drawingSide[] = new boolean[] { true, true, true, true, true, true };


    public Box() {
        //this(new Vec3f(0,0,0), new Vec3f(1,1,1), new Rotf(new Vec3f(0,0,1), 0));
        super(new Vec3f(0,0,0), new Vec3f(1,1,1), new Vec3f(0,0,0));
    }



    public void draw(GL2 gl) {
        gl.glPushMatrix();

        transform(gl);


        final float w = 1f;
        final float h = 1f;
        final float d = 1f;

        // Six faces of cube
        // Top face
        gl.glPushMatrix();
        float r = 1.0f;
        float g = 0.0f;
        float b = 0.5f;
        gl.glColor3f(r, g, b);
        gl.glBegin(GL2.GL_QUADS);
        {
            //Front
            if (drawingSide[0]) {
                gl.glNormal3f(0, 0, 1); {
                    gl.glVertex3f(-w, -h, d);
                    gl.glVertex3f(w, -h, d);
                    gl.glVertex3f(w, h, d);
                    gl.glVertex3f(-w, h, d);
                }
            }

            //Back
            if (drawingSide[1]) {
                gl.glNormal3f(0, 0, -1); {
                    gl.glVertex3f(-w, -h, -d);
                    gl.glVertex3f(w, -h, -d);
                    gl.glVertex3f(w, h, -d);
                    gl.glVertex3f(-w, h, -d);
                }
            }

            //Top
            if (drawingSide[2]) {
                gl.glNormal3f(0, 1, 0); {
                    gl.glVertex3f(-w, h, -d);
                    gl.glVertex3f(-w, h, d);
                    gl.glVertex3f(w, h, d);
                    gl.glVertex3f(w, h, -d);
                }
            }

            //Down
            if (drawingSide[3]) {
                gl.glNormal3f(0, -1, 0); {
                    gl.glVertex3f(-w, -h, -d);
                    gl.glVertex3f(w, -h, -d);
                    gl.glVertex3f(w, -h, d);
                    gl.glVertex3f(-w, -h, d);
                }
            }

            //Right
            if (drawingSide[4]) {
                gl.glNormal3f(0, 1, 0); {
                    gl.glVertex3f(w, -h, -d);
                    gl.glVertex3f(w, h, -d);
                    gl.glVertex3f(w, h, d);
                    gl.glVertex3f(w, -h, d);
                }
            }

            //Left
            if (drawingSide[5]) {
                gl.glNormal3f(0, 1, 0); {
                    gl.glVertex3f(-w, -h, -d);
                    gl.glVertex3f(-w, -h, d);
                    gl.glVertex3f(-w, h, d);
                    gl.glVertex3f(-w, h, -d);
                }
            }
        }
        gl.glEnd();



        gl.glPopMatrix();
        
    }
    
    protected void drawFace(GL2 gl, float width, float height, float outZ, float r, float g, float b, float nx, float ny, float nz, String text) {
        final float halfWidth = width / 2;
        final float halfHeight = height / 2;
        final float halfOutZ = outZ / 2;
        // Face is centered around the local coordinate system's z axis,
        // at a z depth of faceSize / 2
        gl.glColor3f(r, g, b);
        gl.glBegin(GL2.GL_QUADS);
        gl.glNormal3f(nx, ny, nz);
        gl.glVertex3f(-halfWidth, -halfHeight, halfOutZ);
        gl.glVertex3f(halfWidth, -halfHeight, halfOutZ);
        gl.glVertex3f(halfWidth, halfHeight, halfOutZ);
        gl.glVertex3f(-halfWidth, halfHeight, halfOutZ);
        gl.glEnd();

//        // Now draw the overlaid text. In this setting, we don't want the
//        // text on the backward-facing faces to be visible, so we enable
//        // back-face culling; and since we're drawing the text over other
//        // geometry, to avoid z-fighting we disable the depth test. We
//        // could plausibly also use glPolygonOffset but this is simpler.
//        // Note that because the TextRenderer pushes the enable state
//        // internally we don't have to reset the depth test or cull face
//        // bits after we're done.
//        textRenderer.begin3DRendering();
//        gl.glEnable(GL2.GL_DEPTH_TEST);
//        //gl.glEnable(GL2.GL_CULL_FACE);
//
//        // Note that the defaults for glCullFace and glFrontFace are
//        // GL_BACK and GL_CCW, which match the TextRenderer's definition
//        // of front-facing text.
//        Rectangle2D bounds = textRenderer.getBounds(text);
//        float w = (float) bounds.getWidth();
//        float h = (float) bounds.getHeight();
//        textRenderer.draw3D(text,
//            w / -2.0f * textScaleFactor,
//            h / -2.0f * textScaleFactor,
//            halfFaceSize + 0.1f,
//            textScaleFactor);
//        textRenderer.end3DRendering();
    }
    

    

}
