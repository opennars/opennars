/**
 * Project : JHelpSceneGraph<br>
 * Package : jhelp.gui<br>
 * Class : ComponentView3D<br>
 * Date : 18 janv. 2009<br>
 * By JHelp
 */
package jhelp.engine.gui;

import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import jhelp.engine.JHelpSceneRenderer;
import jhelp.engine.util.CanvasOpenGLMaker;

import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;

/**
 * Component for view 3D <br>
 * <br>
 * Last modification : 22 janv. 2009<br>
 * Version 0.0.1<br>
 * 
 * @author JHelp
 */
public class ComponentView3D
      extends JPanel
{
   /** Canvas for 3D */
   private final GLCanvas canvas;
   /** Scene renderer */
   private final JHelpSceneRenderer sceneRenderer;

   /**
    * Constructs ComponentView3D<br>
    * Component dimension must be >0
    * 
    * @param width
    *           Component width
    * @param height
    *           Component height
    */
   public ComponentView3D(final int width, final int height)
   {
      // Create the renderer
      this.sceneRenderer = new JHelpSceneRenderer();
      // Create OpenGL context
      this.canvas = CanvasOpenGLMaker.CANVAS_OPENGL_MAKER.newGLCanvas();
      this.canvas.setAutoSwapBufferMode(false);
      this.canvas.addGLEventListener(this.sceneRenderer);
      // Set the dimension
      final Dimension dimension = new Dimension(width, height);
      this.canvas.setSize(dimension);
      this.canvas.setPreferredSize(dimension);
      this.canvas.setMaximumSize(dimension);
      this.canvas.setMinimumSize(dimension);
      this.setSize(dimension);
      this.setPreferredSize(dimension);
      this.setMaximumSize(dimension);
      this.setMinimumSize(dimension);
      // Link the component and the OpenGL
      this.setLayout(new BorderLayout());
      this.add(this.canvas, BorderLayout.CENTER);
      this.sceneRenderer.start(this.canvas);
   }

   /**
    * Constructs ComponentView3D with desired configuration.<br>
    * The configuration choose will be the nearest possible one that ask
    * 
    * @param width
    *           Width
    * @param height
    *           Height
    * @param doubleBuffered
    *           Indicates to activate or not the double buffering
    * @param hardwareAccelerated
    *           Indicates to activate or not the hardware acceleration
    * @param nuberOfSample
    *           Number of sample to use
    * @param depthBits
    *           Number of bits for depth
    */
   public ComponentView3D(final int width, final int height, final boolean doubleBuffered, final boolean hardwareAccelerated, final int nuberOfSample, final int depthBits)
   {
      // Create the renderer
      this.sceneRenderer = new JHelpSceneRenderer();
      // OpenGL capabilities
      //final GLCapabilities capabilities = new GLCapabilities(GLProfile.getMaximum(true));

      GLCapabilities capabilities = new GLCapabilities(GLProfile.getDefault());
      capabilities.setHardwareAccelerated(true);
      capabilities.setAlphaBits(8);


      capabilities.setNumSamples(1);
      //capabilities.setBackgroundOpaque(false);

      //capabilities.setDoubleBuffered(doubleBuffered);
      //capabilities.setHardwareAccelerated(hardwareAccelerated);
      if(nuberOfSample > 0)
      {
         capabilities.setNumSamples(nuberOfSample);
      }
      capabilities.setDepthBits(depthBits);
      // Create OpenGL context
      this.canvas = CanvasOpenGLMaker.CANVAS_OPENGL_MAKER.newGLCanvas(capabilities);
      this.canvas.setAutoSwapBufferMode(false);
      this.canvas.addGLEventListener(this.sceneRenderer);
      // Set the dimension
      final Dimension dimension = new Dimension(width, height);
      this.canvas.setSize(dimension);
      this.canvas.setPreferredSize(dimension);
      this.canvas.setMaximumSize(dimension);
      this.canvas.setMinimumSize(dimension);
      this.setSize(dimension);
      this.setPreferredSize(dimension);
      this.setMaximumSize(dimension);
      this.setMinimumSize(dimension);
      // Link the component and the OpenGL
      this.setLayout(new BorderLayout());
      this.add(this.canvas, BorderLayout.CENTER);
      this.sceneRenderer.start(this.canvas);
   }

   /**
    * Add a key listener
    * 
    * @param keyListener
    *           Key listener add
    * @see java.awt.Component#addKeyListener(KeyListener)
    */
   @Override
   public synchronized void addKeyListener(final KeyListener keyListener)
   {
      this.sceneRenderer.addKeyListener(keyListener);
   }

   /**
    * Add mouse listener
    * 
    * @param mouseListener
    *           Mouse listener add
    * @see java.awt.Component#addMouseListener(MouseListener)
    */
   @Override
   public synchronized void addMouseListener(final MouseListener mouseListener)
   {
      this.sceneRenderer.addMouseListener(mouseListener);
   }

   /**
    * Add mouse motion listener
    * 
    * @param mouseMotionListener
    *           Mouse motion listener add
    * @see java.awt.Component#addMouseMotionListener(MouseMotionListener)
    */
   @Override
   public synchronized void addMouseMotionListener(final MouseMotionListener mouseMotionListener)
   {
      this.sceneRenderer.addMouseMotionListener(mouseMotionListener);
   }

   /**
    * Add mouse wheel listener
    * 
    * @param mouseWheelListener
    *           Mouse wheel listener add
    * @see java.awt.Component#addMouseWheelListener(MouseWheelListener)
    */
   @Override
   public synchronized void addMouseWheelListener(final MouseWheelListener mouseWheelListener)
   {
      this.sceneRenderer.addMouseWheelListener(mouseWheelListener);
   }

   /**
    * Return sceneRenderer
    * 
    * @return sceneRenderer
    */
   public JHelpSceneRenderer getSceneRenderer()
   {
      return this.sceneRenderer;
   }
}