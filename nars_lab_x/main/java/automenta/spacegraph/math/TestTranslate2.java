/*
 * gleem -- OpenGL Extremely Easy-To-Use Manipulators.
 * Copyright (C) 1998-2003 Kenneth B. Russell (kbrussel@alum.mit.edu)
 *
 * Copying, distribution and use of this software in source and binary
 * forms, with or without modification, is permitted provided that the
 * following conditions are met:
 *
 * Distributions of source code must reproduce the copyright notice,
 * this list of conditions and the following disclaimer in the source
 * code header files; and Distributions of binary code must reproduce
 * the copyright notice, this list of conditions and the following
 * disclaimer in the documentation, Read me file, license file and/or
 * other materials provided with the software distribution.
 *
 * The names of Sun Microsystems, Inc. ("Sun") and/or the copyright
 * holder may not be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS," WITHOUT A WARRANTY OF ANY
 * KIND. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, NON-INTERFERENCE, ACCURACY OF
 * INFORMATIONAL CONTENT OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. THE
 * COPYRIGHT HOLDER, SUN AND SUN'S LICENSORS SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL THE
 * COPYRIGHT HOLDER, SUN OR SUN'S LICENSORS BE LIABLE FOR ANY LOST
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
 * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
 * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
 * INABILITY TO USE THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGES. YOU ACKNOWLEDGE THAT THIS SOFTWARE IS NOT
 * DESIGNED, LICENSED OR INTENDED FOR USE IN THE DESIGN, CONSTRUCTION,
 * OPERATION OR MAINTENANCE OF ANY NUCLEAR FACILITY. THE COPYRIGHT
 * HOLDER, SUN AND SUN'S LICENSORS DISCLAIM ANY EXPRESS OR IMPLIED
 * WARRANTY OF FITNESS FOR SUCH USES.
 */

package automenta.spacegraph.math;

import automenta.spacegraph.math.linalg.Vec3f;
import com.jogamp.opengl.awt.AWTGLAutoDrawable;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;




/** Tests the Translate2 Manip. */

public class TestTranslate2 {
  private static final int X_SIZE = 400;
  private static final int Y_SIZE = 400;

  static class Listener implements GLEventListener {
    private GLU glu = new GLU();
    private CameraParameters params = new CameraParameters();

    public void init(GLAutoDrawable drawable) {
      GL2 gl = drawable.getGL().getGL2();

      gl.glClearColor(0, 0, 0, 0);
      float[] lightPosition = new float[] {1, 1, 1, 0};
      float[] ambient       = new float[] { 0.0f, 0.0f, 0.0f, 1.0f };
      float[] diffuse       = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
      gl.glLightfv(GL2ES1.GL_LIGHT0, GL2ES1.GL_AMBIENT,  ambient, 0);
      gl.glLightfv(GL2ES1.GL_LIGHT0, GL2ES1.GL_DIFFUSE,  diffuse, 0);
      gl.glLightfv(GL2ES1.GL_LIGHT0, GL2ES1.GL_POSITION, lightPosition, 0);

      gl.glEnable(GL2ES1.GL_LIGHTING);
      gl.glEnable(GL2ES1.GL_LIGHT0);
      gl.glEnable(GL.GL_DEPTH_TEST);

      params.setPosition(new Vec3f(0, 0, 0));
      params.setForwardDirection(Vec3f.NEG_Z_AXIS);
      params.setUpDirection(Vec3f.Y_AXIS);
      params.setVertFOV((float) (Math.PI / 8.0));
      params.setImagePlaneAspectRatio(1);
      params.xSize = X_SIZE;
      params.ySize = Y_SIZE;

      gl.glMatrixMode(GL2ES1.GL_PROJECTION);
      gl.glLoadIdentity();
      glu.gluPerspective(45, 1, 1, 100);
      gl.glMatrixMode(GL2ES1.GL_MODELVIEW);
      gl.glLoadIdentity();

      // Register the window with the ManipManager
      ManipManager manager = ManipManager.getManipManager();
      manager.registerWindow((AWTGLAutoDrawable) drawable);

      // Instantiate a Translate2Manip
      Translate2Manip manip = new Translate2Manip();
      manip.setTranslation(new Vec3f(0, 0, -5));
      manip.setNormal(new Vec3f(-1, 1, 1));
      manager.showManipInWindow(manip, (AWTGLAutoDrawable) drawable);
    }

    public void dispose(GLAutoDrawable drawable) {
    }

    public void display(GLAutoDrawable drawable) {
      GL2 gl = drawable.getGL().getGL2();
      gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
      ManipManager.getManipManager().updateCameraParameters((AWTGLAutoDrawable) drawable, params);
      ManipManager.getManipManager().render((AWTGLAutoDrawable) drawable, gl);
    }

    public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) {
      GL2 gl = drawable.getGL().getGL2();
      float aspect, theta;
      aspect = (float) w / (float) h;
      if (w >= h)
        theta = 45;
      else
        theta = (float) Math.toDegrees(Math.atan(1 / aspect));
      params.setVertFOV((float) Math.toRadians(theta) / 2.0f);
      params.setImagePlaneAspectRatio(aspect);
      params.setXSize(w);
      params.setYSize(h);
      gl.glMatrixMode(GL2ES1.GL_PROJECTION);
      gl.glLoadIdentity();
      glu.gluPerspective(theta, aspect, 1, 100);
      gl.glMatrixMode(GL2ES1.GL_MODELVIEW);
      gl.glLoadIdentity();
    }

    // Unused routines
    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {}
  }

  public static void main(String[] args) {
    Frame frame = new Frame("Translate2 Test");
    frame.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          System.exit(0);
        }
      });
    frame.setLayout(new BorderLayout());
    GLCanvas canvas = new GLCanvas();
    canvas.setSize(400, 400);
    canvas.addGLEventListener(new Listener());
    frame.add(canvas, BorderLayout.CENTER);
    frame.pack();
    frame.setVisible(true);
  }
}
