/*
 * Copyright (c) 2006 Sun Microsystems, Inc. All Rights Reserved.
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
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.awt.TextRenderer;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.geom.Rectangle2D;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



/** Illustrates both the TextRenderer's capability for handling
    relatively large amounts of text (more than drawn on the screen --
    showing the least recently used capabilities of its internal
    cache) as well as using the Java 2D text layout mechanisms in
    conjunction with the TextRenderer to flow text across the
    screen. */

public class DemoTextFlow extends SG {

  public static void main(String[] args) {

    Frame frame = new Frame("Text Flow");
    frame.setLayout(new BorderLayout());

    GLCapabilities caps = new GLCapabilities(GLProfile.get(GLProfile.GL2));
    GLCanvas canvas = new GLCanvas(caps);
    final DemoTextFlow demo = new DemoTextFlow();

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

  private List/*<String>*/ lines = new ArrayList();
  private Time time;
  private TextRenderer renderer;
  private int curParagraph;
  private float x = 30;
  private float y;
  private float velocity = 100;  // pixels/sec
  private int lineSpacing;
  private int EXTRA_LINE_SPACING = 5;

  private void reflow(float width) {
    lines.clear();
    lineSpacing = 0;
    int numLines = 0;
    FontRenderContext frc = renderer.getFontRenderContext();
    for (int i = 0; i < text.length; i++) {
      String paragraph = text[i];
      Map attrs = new HashMap();
      attrs.put(TextAttribute.FONT, renderer.getFont());
      AttributedString str = new AttributedString(paragraph, attrs);
      LineBreakMeasurer measurer = new LineBreakMeasurer(str.getIterator(), frc);
      int curPos = 0;
      while (measurer.getPosition() < paragraph.length()) {
        int nextPos = measurer.nextOffset(width);
        String line = paragraph.substring(curPos, nextPos);
        Rectangle2D bounds = renderer.getBounds(line);
        lines.add(line);
        lineSpacing += (int) bounds.getHeight();
        ++numLines;
        curPos = nextPos;
        measurer.setPosition(curPos);
      }
      // Indicate end of paragraph with a null LineInfo
      lines.add(null);
    }
    lineSpacing = (int) ((float) lineSpacing / (float) numLines) + EXTRA_LINE_SPACING;
  }

  public void init(GLAutoDrawable drawable) {
    renderer = new TextRenderer(new Font("SansSerif", Font.PLAIN, 72), true, false);
    time = new SystemTime();
    ((SystemTime) time).rebase();
  }

  public void dispose(GLAutoDrawable drawable) {
    renderer = null;
    time = null;
  }

  public void display(GLAutoDrawable drawable) {
    time.update();

    GL gl = drawable.getGL();
    gl.glClear(GL.GL_COLOR_BUFFER_BIT);

    float deltaT = (float) time.deltaT();
    y += velocity * deltaT;

    // Draw text starting at the specified paragraph
    int paragraph = 0;
    float curY = y;
    renderer.beginRendering(drawable.getSurfaceWidth(), drawable.getSurfaceHeight());
    boolean renderedOne = false;
    for (int i = 0; i < lines.size(); i++) {
      String line = (String) lines.get(i);
      if (line == null) {
        ++paragraph;
        if (paragraph >= curParagraph) {
          // If this paragraph has scrolled off the top of the screen,
          // don't draw it the next frame
          if (paragraph > curParagraph && curY > drawable.getSurfaceHeight()) {
            ++curParagraph;
            y = curY;
          }
          curY -= 2 * lineSpacing;
        }
      } else {
        if (paragraph >= curParagraph) {
          curY -= lineSpacing;
          if (curY < drawable.getSurfaceHeight() + lineSpacing) {
            renderer.draw(line, (int) x, (int) curY);
            renderedOne = true;
          }
          if (curY < 0) {
            // Done rendering all visible lines
            break;
          }
        }
      }
    }
    renderer.endRendering();
    if (!renderedOne) {
      // Start over
      curParagraph = 0;
      y = 0;
    }
  }

  public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
    reflow(Math.max(100, width - 60));
  }

  public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {}

  // Use some nonsense Lorem Ipsum text generated from www.lipsum.com
  private static final String[] text = {
    "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Nulla in mi ut augue laoreet gravida. Quisque sodales vehicula ligula. Donec posuere. Morbi aliquet, odio vitae tempus mattis, odio dolor vestibulum leo, congue laoreet risus felis vitae dolor. Nulla arcu. Morbi non quam. Vestibulum pretium dolor fermentum erat. Proin dictum volutpat nibh. Morbi egestas mauris a diam. Vestibulum mauris eros, porttitor at, fermentum a, varius eu, mauris. Cras rutrum felis ut diam. Aenean porttitor risus a nunc. Aliquam et ante eu dolor pretium adipiscing. Sed fermentum, eros in dapibus lacinia, augue nunc fermentum tellus, eu egestas justo elit at mauris. Sed leo nisl, fermentum in, pretium vitae, tincidunt at, lacus. Curabitur non diam.",
    "Etiam varius sagittis lorem. Vivamus iaculis condimentum tortor. Nunc sollicitudin scelerisque dolor. Nunc condimentum fringilla nisl. Fusce purus mauris, blandit eu, lacinia eget, vestibulum nec, massa. Nulla vitae libero. Suspendisse potenti. Aliquam iaculis, lorem eu adipiscing tempor, ipsum dui aliquam sem, eu vehicula leo leo eu ipsum. Pellentesque faucibus. Nullam porttitor ligula eget nibh. Cras elementum mi ac libero. Praesent pellentesque pede vitae quam. Sed nec arcu id ante cursus mollis. Suspendisse quis ipsum. Maecenas feugiat interdum neque. Nullam dui diam, convallis at, condimentum vitae, mattis vitae, metus. Integer sollicitudin, diam id lacinia posuere, quam velit fringilla dolor, eu semper sapien felis ac elit.",
    "Ut a magna vitae lectus euismod hendrerit. Quisque varius consectetuer sapien. Suspendisse ligula. Nullam feugiat venenatis mauris. In consequat lorem at neque. Pellentesque libero. In eget lectus in velit auctor facilisis. Donec nec metus. Aliquam facilisis eros vel dui. Integer a diam. Donec interdum, eros faucibus blandit venenatis, ante ante ornare enim, a gravida ante lectus id metus. Ut sem.",
    "Duis consectetuer leo quis elit. Suspendisse pretium nunc ac dolor. Quisque eleifend fringilla nisl. Suspendisse potenti. Duis vel ipsum at enim tincidunt consectetuer. Aliquam tempor justo nec metus. Nunc ac velit id nibh consequat vulputate. Cras vel dolor eu massa lacinia volutpat. Curabitur nibh nisi, auctor et, tincidunt eget, molestie vel, neque. Sed semper viverra neque. Nullam rhoncus hendrerit libero. Nulla adipiscing. Fusce pede nibh, lacinia a, malesuada a, dictum nec, pede. Etiam ut lorem. Donec quis massa vitae est pharetra mattis.",
    "Nullam dui. Morbi nulla quam, imperdiet iaculis, consectetuer a, porttitor eu, sem. Donec id ipsum vitae nisi viverra porta. In hac habitasse platea dictumst. In ligula libero, dapibus eleifend, eleifend vel, accumsan sit amet, felis. Morbi tortor. Donec mattis ultricies arcu. Ut eget leo. Sed vel quam at ipsum sodales semper. Curabitur tincidunt quam id odio. Quisque porta, magna vel nonummy pulvinar, ligula tellus fringilla tellus, ut pharetra turpis velit ac eros. Cras eu enim vel mi suscipit malesuada. Phasellus ut orci. Aenean vitae turpis vitae lectus malesuada aliquet."
  };
}