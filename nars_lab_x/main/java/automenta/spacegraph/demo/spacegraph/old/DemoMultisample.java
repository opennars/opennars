/*
 * Copyright (c) 2003 Sun Microsystems, Inc. All Rights Reserved.
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

import com.jogamp.opengl.awt.GLCanvas;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;


public class DemoMultisample {
  private GLCanvas canvas;

  // Simple class to warn if results are not going to be as expected
  static class MultisampleChooser extends DefaultGLCapabilitiesChooser {
    public int chooseCapabilities(GLCapabilities desired,
                                  List<? extends GLCapabilities> available,
                                  int windowSystemRecommendedChoice) {
      boolean anyHaveSampleBuffers = false;
      for (GLCapabilities caps : available) {
        if (caps != null && ((GLCapabilities)caps).getSampleBuffers()) {
          anyHaveSampleBuffers = true;
          break;
        }
      }
      int selection = super.chooseCapabilities(desired, available, windowSystemRecommendedChoice);
      if (!anyHaveSampleBuffers) {
        System.err.println("WARNING: antialiasing will be disabled because none of the available pixel formats had it to offer");
      } else {
        if (!available.get(selection).getSampleBuffers()) {
          System.err.println("WARNING: antialiasing will be disabled because the DefaultGLCapabilitiesChooser didn't supply it");
        }
      }
      return selection;
    }
  }

  public static void main(String[] args) {
    new DemoMultisample().run(args);
  }

  public void run(String[] args) {
    GLCapabilities caps = new GLCapabilities(null);
    GLCapabilitiesChooser chooser = new MultisampleChooser();

    caps.setSampleBuffers(true);
    caps.setNumSamples(4);
    canvas = new GLCanvas(caps, chooser, null);
    canvas.addGLEventListener(new Listener());
    
    Frame frame = new Frame("Full-scene antialiasing");
    frame.setLayout(new BorderLayout());
    canvas.setSize(512, 512);
    frame.add(canvas, BorderLayout.CENTER);
    frame.pack();
    frame.setVisible(true);
    frame.setLocation(0, 0);
    canvas.requestFocus();

    frame.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          runExit();
        }
      });

    // No antialiasing (for comparison)
    caps.setSampleBuffers(false);
    canvas = new GLCanvas(caps);
    canvas.addGLEventListener(new Listener());
    
    frame = new Frame("No antialiasing");
    frame.setLayout(new BorderLayout());
    canvas.setSize(512, 512);
    frame.add(canvas, BorderLayout.CENTER);
    frame.pack();
    frame.setVisible(true);
    frame.setLocation(512, 0);
    canvas.requestFocus();

    frame.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          runExit();
        }
      });
  }

  class Listener implements GLEventListener {
    public void init(GLAutoDrawable drawable) {
      GL2 gl = drawable.getGL().getGL2();

      gl.glClearColor(0, 0, 0, 0);
      //      gl.glEnable(GL.GL_DEPTH_TEST);
      //      gl.glDepthFunc(GL.GL_LESS);

      gl.glMatrixMode(GL2ES1.GL_MODELVIEW);
      gl.glLoadIdentity();
      gl.glMatrixMode(GL2ES1.GL_PROJECTION);
      gl.glLoadIdentity();
      gl.glOrtho(-1, 1, -1, 1, -1, 1);
    }

    public void dispose(GLAutoDrawable drawable) {
    }

    public void display(GLAutoDrawable drawable) {
      GL2 gl = drawable.getGL().getGL2();

      gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

      int numSteps = 20;
      double increment = Math.PI / numSteps;
      double radius = 1;

      gl.glBegin(GL.GL_LINES);
      for (int i = numSteps - 1; i >= 0; i--) {
        gl.glVertex3d(radius * Math.cos(i * increment),
                      radius * Math.sin(i * increment),
                      0);
        gl.glVertex3d(-1.0 * radius * Math.cos(i * increment),
                      -1.0 * radius * Math.sin(i * increment),
                      0);
      }
      gl.glEnd();
    }

    // Unused routines
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {}
    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {}
  }

  private void runExit() {
    // Note: calling System.exit() synchronously inside the draw,
    // reshape or init callbacks can lead to deadlocks on certain
    // platforms (in particular, X11) because the JAWT's locking
    // routines cause a global AWT lock to be grabbed. Instead run
    // the exit routine in another thread.
    new Thread(new Runnable() {
        public void run() {
          System.exit(0);
        }
      }).start();
  }
}
