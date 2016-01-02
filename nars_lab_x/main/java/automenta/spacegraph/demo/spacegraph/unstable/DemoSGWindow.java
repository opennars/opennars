/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package automenta.spacegraph.demo.spacegraph.unstable;


import automenta.spacegraph.SG;
import automenta.spacegraph.SystemTime;
import automenta.spacegraph.Time;
import automenta.spacegraph.demo.spacegraph.old.FPSCounter;
import automenta.spacegraph.video.SGWindow;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.awt.TextRenderer;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Rectangle2D;

/**
 *
 * @author seh
 */
public class DemoSGWindow {

    public static void main(String[] args) {
        new SGWindow("Text Cube", new SG() {

            private float xAng;
            private float yAng;
            private GLU glu = new GLU();
            private Time time;
            private TextRenderer textRenderer;
            private FPSCounter fps;
            private float textScaleFactor;

            public void init(GLAutoDrawable drawable) {
                textRenderer = new TextRenderer(new Font("SansSerif", Font.PLAIN, 72));
                //textRenderer.setSmoothing(true);
                textRenderer.setUseVertexArrays(true);

                GL2 gl = drawable.getGL().getGL2();
                gl.glEnable(GL2.GL_DEPTH_TEST);

                fps = new FPSCounter(drawable, 36);

                time = new SystemTime();
                ((SystemTime) time).rebase();
                //    gl.setSwapInterval(0);
            }

            public void dispose(GLAutoDrawable glad) {
            }

            public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
                GL2 gl = drawable.getGL().getGL2();
                gl.glMatrixMode(GL2.GL_PROJECTION);
                gl.glLoadIdentity();
                glu.gluPerspective(15, (float) width / (float) height, 5, 15);
            }

            public void display(GLAutoDrawable drawable) {
                GL2 gl = drawable.getGL().getGL2();
                gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

                gl.glMatrixMode(GL2.GL_MODELVIEW);
                gl.glLoadIdentity();
                glu.gluLookAt(0, 0, 10,
                    0, 0, 0,
                    0, 1, 0);

                // Base rotation of cube

                fps.draw();

                time.update();
            }

            private void drawFace(GL2 gl, float faceSize, float r, float g, float b, String text) {
                float halfFaceSize = faceSize / 2;
                // Face is centered around the local coordinate system's z axis,
                // at a z depth of faceSize / 2
                gl.glColor3f(r, g, b);
                gl.glBegin(GL2.GL_QUADS);
                gl.glVertex3f(-halfFaceSize, -halfFaceSize, halfFaceSize);
                gl.glVertex3f(halfFaceSize, -halfFaceSize, halfFaceSize);
                gl.glVertex3f(halfFaceSize, halfFaceSize, halfFaceSize);
                gl.glVertex3f(-halfFaceSize, halfFaceSize, halfFaceSize);
                gl.glEnd();

                // Now draw the overlaid text. In this setting, we don't want the
                // text on the backward-facing faces to be visible, so we enable
                // back-face culling; and since we're drawing the text over other
                // geometry, to avoid z-fighting we disable the depth test. We
                // could plausibly also use glPolygonOffset but this is simpler.
                // Note that because the TextRenderer pushes the enable state
                // internally we don't have to reset the depth test or cull face
                // bits after we're done.
                textRenderer.begin3DRendering();
                gl.glEnable(GL2.GL_DEPTH_TEST);
                //gl.glEnable(GL2.GL_CULL_FACE);

                // Note that the defaults for glCullFace and glFrontFace are
                // GL_BACK and GL_CCW, which match the TextRenderer's definition
                // of front-facing text.
                Rectangle2D bounds = textRenderer.getBounds(text);
                float w = (float) bounds.getWidth();
                float h = (float) bounds.getHeight();
                textRenderer.draw3D(text,
                    w / -2.0f * textScaleFactor,
                    h / -2.0f * textScaleFactor,
                    halfFaceSize + 0.1f,
                    textScaleFactor);
                textRenderer.end3DRendering();
            }

            public void drawCube(GL2 gl, float x, float y, float z) {

                gl.glPushMatrix();
                gl.glRotatef(xAng, 1, 0, 0);
                gl.glRotatef(yAng, 0, 1, 0);
                gl.glTranslatef(x, y, z);

                // Six faces of cube
                // Top face
                gl.glPushMatrix();
                gl.glRotatef(-90, 1, 0, 0);
                drawFace(gl, 1.0f, 0.2f, 0.2f, 0.8f, "Top");
                gl.glPopMatrix();
                // Front face
                drawFace(gl, 1.0f, 0.8f, 0.2f, 0.2f, "Front");
                // Right face
                gl.glPushMatrix();
                gl.glRotatef(90, 0, 1, 0);
                drawFace(gl, 1.0f, 0.2f, 0.8f, 0.2f, "Right");
                // Back face
                gl.glRotatef(90, 0, 1, 0);
                drawFace(gl, 1.0f, 0.8f, 0.8f, 0.2f, "Back");
                // Left face
                gl.glRotatef(90, 0, 1, 0);
                drawFace(gl, 1.0f, 0.2f, 0.8f, 0.8f, "Left");
                gl.glPopMatrix();
                // Bottom face
                gl.glPushMatrix();
                gl.glRotatef(90, 1, 0, 0);
                drawFace(gl, 1.0f, 0.8f, 0.2f, 0.8f, "Bottom");
                gl.glPopMatrix();

                gl.glPopMatrix();

            }

            public void mouseDragged(MouseEvent e) {
            }

            public void mouseMoved(MouseEvent e) {
            }

            public void mouseClicked(MouseEvent e) {
            }

            public void mousePressed(MouseEvent e) {
            }

            public void mouseReleased(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
            }

            public void keyTyped(KeyEvent e) {
            }

            public void keyPressed(KeyEvent e) {
            }

            public void keyReleased(KeyEvent e) {
            }

            public void mouseWheelMoved(MouseWheelEvent e) {
            }
        });
    }
}
