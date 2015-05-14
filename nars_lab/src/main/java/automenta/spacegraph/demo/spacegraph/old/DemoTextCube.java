/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author seh
 */
/*
 * Copyright (c) 2007 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * - Redistribution of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES,
 * INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN
 * MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR
 * ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR
 * DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE
 * DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY,
 * ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF
 * SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed or intended for use
 * in the design, construction, operation or maintenance of any nuclear
 * facility.
 *
 * Sun gratefully acknowledges that this software was originally authored
 * and developed by Kenneth Bradley Russell and Christopher John Kline.
 */

package automenta.spacegraph.demo.spacegraph.old;

import automenta.spacegraph.SG;
import automenta.spacegraph.SystemTime;
import automenta.spacegraph.Time;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.awt.TextRenderer;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;



/** Shows how to place 2D text in 3D using the TextRenderer. */

public class DemoTextCube extends SG {
  private float xAng;
  private float yAng;
  private GLU glu = new GLU();
  private Time time;
  private TextRenderer renderer;
  private FPSCounter fps;
  private float textScaleFactor;

  public static void main(String[] args) {
    Frame frame = new Frame("Text Cube");
    frame.setLayout(new BorderLayout());

    GLCapabilities caps = new GLCapabilities(GLProfile.get(GLProfile.GL2));
    GLCanvas canvas = new GLCanvas(caps);
    final DemoTextCube demo = new DemoTextCube();

    canvas.addGLEventListener(demo);
    frame.add(canvas, BorderLayout.CENTER);

    frame.setSize(512, 512);
    final Animator animator = new Animator(canvas);
    frame.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          // Run this on another thread than the AWT event queue to
          // make sure the call to Animator.stop() completes before
          // exiting
          new Thread(new Runnable() {
              public void run() {
                animator.stop();
                System.exit(0);
              }
            }).start();
        }
      });
    frame.setVisible(true);
    animator.start();
  }

  public void init(GLAutoDrawable drawable) {
    renderer = new TextRenderer(new Font("SansSerif", Font.PLAIN, 72));
    GL2 gl = drawable.getGL().getGL2();
    gl.glEnable(GL2.GL_DEPTH_TEST);

    // Compute the scale factor of the largest string which will make
    // them all fit on the faces of the cube
    Rectangle2D bounds = renderer.getBounds("Bottom");
    float w = (float) bounds.getWidth();
    float h = (float) bounds.getHeight();
    textScaleFactor = 1.0f / (w * 1.1f);
    fps = new FPSCounter(drawable, 36);

    time = new SystemTime();
    ((SystemTime) time).rebase();
//    gl.setSwapInterval(0);
  }

  public void dispose(GLAutoDrawable drawable) {
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
    gl.glRotatef(xAng, 1, 0, 0);
    gl.glRotatef(yAng, 0, 1, 0);

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

    fps.draw();

    time.update();
    xAng += 200 * (float) time.deltaT();
    yAng += 150 * (float) time.deltaT();
  }

  public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
    GL2 gl = drawable.getGL().getGL2();
    gl.glMatrixMode(GL2.GL_PROJECTION);
    gl.glLoadIdentity();
    glu.gluPerspective(15, (float) width / (float) height, 5, 15);
  }

  public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {}

  private void drawFace(GL2 gl,
                        float faceSize,
                        float r, float g, float b,
                        String text) {
    float halfFaceSize = faceSize / 2;
    // Face is centered around the local coordinate system's z axis,
    // at a z depth of faceSize / 2
    gl.glColor3f(r, g, b);
    gl.glBegin(GL2.GL_QUADS);
    gl.glVertex3f(-halfFaceSize, -halfFaceSize, halfFaceSize);
    gl.glVertex3f( halfFaceSize, -halfFaceSize, halfFaceSize);
    gl.glVertex3f( halfFaceSize,  halfFaceSize, halfFaceSize);
    gl.glVertex3f(-halfFaceSize,  halfFaceSize, halfFaceSize);
    gl.glEnd();

    // Now draw the overlaid text. In this setting, we don't want the
    // text on the backward-facing faces to be visible, so we enable
    // back-face culling; and since we're drawing the text over other
    // geometry, to avoid z-fighting we disable the depth test. We
    // could plausibly also use glPolygonOffset but this is simpler.
    // Note that because the TextRenderer pushes the enable state
    // internally we don't have to reset the depth test or cull face
    // bits after we're done.
    renderer.begin3DRendering();
    gl.glDisable(GL2.GL_DEPTH_TEST);
    gl.glEnable(GL2.GL_CULL_FACE);
    // Note that the defaults for glCullFace and glFrontFace are
    // GL_BACK and GL_CCW, which match the TextRenderer's definition
    // of front-facing text.
    Rectangle2D bounds = renderer.getBounds(text);
    float w = (float) bounds.getWidth();
    float h = (float) bounds.getHeight();
    renderer.draw3D(text,
                    w / -2.0f * textScaleFactor,
                    h / -2.0f * textScaleFactor,
                    halfFaceSize,
                    textScaleFactor);
    renderer.end3DRendering();
  }
}