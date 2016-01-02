/*
 * Copyright (c) 2005 Sun Microsystems, Inc. All Rights Reserved.
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
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureCoords;
import com.jogamp.opengl.util.texture.TextureIO;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;



/** Demonstrates simple use of the TextureIO texture loader. */

public class DemoTexture implements GLEventListener {
  public static void main(String[] args) {
    new DemoTexture().run(args);
  }

  private File curDir;

  private void run(String[] args) {
    JPopupMenu.setDefaultLightWeightPopupEnabled(false);

    JFrame frame = new JFrame("Texture Loader Demo");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    final GLCanvas canvas = new GLCanvas();

    JMenuBar menuBar = new JMenuBar();
    JMenu menu = new JMenu("File");
    JMenuItem item = new JMenuItem("Open texture...");
    item.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          JFileChooser chooser = new JFileChooser(curDir);
          int res = chooser.showOpenDialog(null);
          if (res == JFileChooser.APPROVE_OPTION) {
            File chosen = chooser.getSelectedFile();
            if (chosen != null) {
              curDir = chosen.getParentFile();
              setTextureFile(chosen);
              canvas.repaint();
            }
          }
        }
      });
    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
    menu.add(item);

    item = new JMenuItem("Flush texture");
    item.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          flushTexture();
          canvas.repaint();
        }
      });
    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK));
    menu.add(item);

    item = new JMenuItem("Exit");
    item.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          System.exit(0);
        }
      });
    item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK));
    menu.add(item);

    menuBar.add(menu);

    canvas.addGLEventListener(this);
    frame.getContentPane().add(canvas);
    frame.setJMenuBar(menuBar);
    frame.setSize(800, 600);
    frame.setVisible(true);
  }

  private boolean newTexture;
  private boolean flushTexture;
  private File file;
  private Texture texture;
  private GLU glu = new GLU();

  public void setTextureFile(File file) {
    this.file = file;
    newTexture = true;
  }

  public void flushTexture() {
    flushTexture = true;
  }

  public void init(GLAutoDrawable drawable) {

    GL2 gl = drawable.getGL().getGL2();
    drawable.setGL(new DebugGL2(gl.getGL2()));

    gl.glClearColor(0, 0, 0, 0);
    gl.glEnable(GL.GL_DEPTH_TEST);
  }

  public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
    GL2 gl = drawable.getGL().getGL2();
    gl.glMatrixMode(GL2ES1.GL_PROJECTION);
    gl.glLoadIdentity();
    glu.gluOrtho2D(0, 1, 0, 1);
    gl.glMatrixMode(GL2ES1.GL_MODELVIEW);
    gl.glLoadIdentity();
  }

  public void dispose(GLAutoDrawable drawable) {
  }

  public void display(GLAutoDrawable drawable) {
    GL2 gl = drawable.getGL().getGL2();
    gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

    if (flushTexture) {
      flushTexture = false;
      if (texture != null) {
        texture.destroy(gl);
        texture = null;
      }
    }

    if (newTexture) {
      newTexture = false;

      if (texture != null) {
        texture.destroy(gl);
        texture = null;
      }

      try {
        System.err.println("Loading texture...");
        texture = TextureIO.newTexture(file, true);
        System.err.println("Texture estimated memory size = " + texture.getEstimatedMemorySize());
      } catch (IOException e) {
        e.printStackTrace();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        e.printStackTrace(new PrintStream(bos));
        JOptionPane.showMessageDialog(null,
                                      bos.toString(),
                                      "Error loading texture",
                                      JOptionPane.ERROR_MESSAGE);
        return;
      }
    }

    if (texture != null) {
      texture.enable(gl);
      texture.bind(gl);
      gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL.GL_REPLACE);
      TextureCoords coords = texture.getImageTexCoords();

      gl.glBegin(GL2.GL_QUADS);
      gl.glTexCoord2f(coords.left(), coords.bottom());
      gl.glVertex3f(0, 0, 0);
      gl.glTexCoord2f(coords.right(), coords.bottom());
      gl.glVertex3f(1, 0, 0);
      gl.glTexCoord2f(coords.right(), coords.top());
      gl.glVertex3f(1, 1, 0);
      gl.glTexCoord2f(coords.left(), coords.top());
      gl.glVertex3f(0, 1, 0);
      gl.glEnd();
      texture.disable(gl);
    }
  }

  public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {}
}
