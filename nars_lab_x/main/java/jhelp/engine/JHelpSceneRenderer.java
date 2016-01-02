/**
 */
package jhelp.engine;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.gl2.GLUgl2;
import jhelp.engine.event.ClickInSpaceListener;
import jhelp.engine.event.JHelpSceneRendererListener;
import jhelp.engine.geom.Plane;
import jhelp.engine.graphics.CompressiveImage;
import jhelp.engine.gui.components.WindowMaterial;
import jhelp.engine.twoD.GUI2D;
import jhelp.engine.twoD.Object2D;
import jhelp.engine.util.BufferUtils;
import jhelp.engine.util.Tool3D;
import jhelp.util.gui.JHelpImage;
import jhelp.util.io.UtilIO;
import jhelp.util.list.QueueSynchronized;
import jhelp.util.thread.ThreadManager;
import jhelp.util.thread.ThreadedSimpleTask;
import jhelp.xml.MarkupXML;

import javax.swing.event.EventListenerList;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Render a scene.<br>
 * It is call by OpenGL on refresh, resize, ...<br>
 * It also manage the scene, refresh, play animations, ... <br>
 * <br>
 * Last modification : 24 janv. 2009<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public class JHelpSceneRenderer
      implements GLEventListener, MouseListener, MouseMotionListener, MouseWheelListener, KeyListener, Runnable
{
   /**
    * Information about last object 2D or 3D detected
    * 
    * @author JHelp
    */
   static class DetectionInfo
   {
      /** X detection position */
      int      detectX;
      /** Y detection position */
      int      detectY;
      /** 2D manipulator */
      GUI2D    gui2d;
      /** Indicates if mouse left is down */
      boolean  mouseButtonLeft;
      /** Indicates if mouse right is down */
      boolean  mouseButtonRight;
      /** Indicates if its is a drag (mouse move while at least one button is down) */
      boolean  mouseDrag;
      /** Node detected */
      Node     nodeDetect;
      /** Object 2D detected */
      Object2D object2DDetect;
      /** Reference scene */
      Scene    scene;

      /**
       * Create a new instance of DetectionInfo
       * 
       * @param object2dDetect
       *           Object 2D detected
       * @param gui2d
       *           2D manipulator
       * @param detectX
       *           X detection position
       * @param detectY
       *           Y detection position
       * @param mouseButtonLeft
       *           Indicates if mouse left is down
       * @param mouseButtonRight
       *           Indicates if mouse right is down
       * @param mouseDrag
       *           Indicates if its is a drag (mouse move while at least one button is down)
       * @param scene
       *           Reference scene
       * @param nodeDetect
       *           Node detected
       */
      DetectionInfo(final Object2D object2dDetect, final GUI2D gui2d, final int detectX, final int detectY, final boolean mouseButtonLeft,
            final boolean mouseButtonRight, final boolean mouseDrag, final Scene scene, final Node nodeDetect)
      {
         this.object2DDetect = object2dDetect;
         this.gui2d = gui2d;
         this.detectX = detectX;
         this.detectY = detectY;
         this.mouseButtonLeft = mouseButtonLeft;
         this.mouseButtonRight = mouseButtonRight;
         this.mouseDrag = mouseDrag;
         this.scene = scene;
         this.nodeDetect = nodeDetect;
      }
   }

   /**
    * Delayed action to fire events to listeners <br>
    * <br>
    * Last modification : 8 déc. 2010<br>
    * Version 0.0.0<br>
    * 
    * @author JHelp
    */
   class FireEventScheduleAction
         extends ThreadedSimpleTask<Integer>
   {
      /**
       * Constructs FireEventScheduleAction
       */
      public FireEventScheduleAction()
      {
      }

      /**
       * Fire the events
       * 
       * @param actionID
       *           Action ID
       */
      @Override
      protected void doSimpleAction(final Integer actionID)
      {
         for(final JHelpSceneRendererListener sceneRendererListener : JHelpSceneRenderer.this.sceneListeners.toArray(new JHelpSceneRendererListener[JHelpSceneRenderer.this.sceneListeners.size()]))
         {
            switch(actionID)
            {
               case JHelpSceneRenderer.ACTION_FIRE_SCENE_RENDERER_IS_INITIALIZED:
                  sceneRendererListener.sceneRendererIsInitialized(JHelpSceneRenderer.this);
               break;
            }
         }
      }
   }

   /**
    * Task for signal to listeners the last detection
    * 
    * @author JHelp
    */
   class UpdateMouseDetection
         extends ThreadedSimpleTask<DetectionInfo>
   {
      /**
       * Create a new instance of UpdateMouseDetection
       */
      UpdateMouseDetection()
      {
      }

      /**
       * Signal to listeners the last detection <br>
       * <br>
       * <b>Parent documentation:</b><br>
       * {@inheritDoc}
       * 
       * @param detectionInfo
       *           Last detection information
       * @see jhelp.util.thread.ThreadedSimpleTask#doSimpleAction(Object)
       */
      @Override
      protected void doSimpleAction(final DetectionInfo detectionInfo)
      {
         // If a 2D object is detect
         if(detectionInfo.object2DDetect != null)
         {
            // Update mouse state for 2D objects
            detectionInfo.gui2d.mouseState(detectionInfo.detectX, detectionInfo.detectY, detectionInfo.mouseButtonLeft, detectionInfo.mouseButtonRight,
                  detectionInfo.mouseDrag, detectionInfo.object2DDetect);
         }
         else if(detectionInfo.mouseDrag == false)
         {
            // If it is not a mouse drag, update mouse state for scene
            detectionInfo.scene.mouseState(detectionInfo.mouseButtonLeft, detectionInfo.mouseButtonRight, detectionInfo.nodeDetect);

            if((detectionInfo.nodeDetect == null) && ((detectionInfo.mouseButtonLeft == true) || (detectionInfo.mouseButtonRight == true)))
            {
               JHelpSceneRenderer.this.fireClickInSpace(detectionInfo.detectX, detectionInfo.detectY, detectionInfo.mouseButtonLeft,
                     detectionInfo.mouseButtonRight);
            }
         }
      }
   }

   /** Time between 2 evaluation of FPS value */
   private static final long                evaluteTime                               = 1000L;
   /** Transparent color (Use as background on FPS print) */
   private static final Color               TR                                        = new Color(1, 1, 1, 0.0f);
   /** Action ID for alert listener that the scene renderer is initialized */
   static final int                         ACTION_FIRE_SCENE_RENDERER_IS_INITIALIZED = 0;
   /** Actual absolute frame */
   private float                            absoluteFrame;
   /** Animations played list */
   private final Vector<Animation>          animations;
   /** FPS for play animations */
   private int                              animationsFps;
   /** Time to synchronize animations */
   private long                             animationTime;
   /** Canvas where OpenGL is draw */
   private GLCanvas canvas;
   /** Indicates if detection is activate */
   private boolean                          detectionActivate;
   /** X of detection point */
   private int                              detectX;
   /** Y of detection point */
   private int                              detectY;
   /** Delayed action to fire events to listeners */
   private final FireEventScheduleAction    fireEventScheduleAction                   = new FireEventScheduleAction();
   /** Font use to print FPS information */
   private Font                             font;
   /** Actual Field Of View */
   private final float                      fov;
   /** Actual FPS */
   private float                            fps;
   /** Count number of refresh between t<o evaluation of FPS */
   private int                              fpsCount;
   /** Reference time for FPS */
   private long                             fpsStart;
   /** 2D manager */
   private final GUI2D                      gui2d;
   /** OpenGL view height */
   private int                              height;
   /** Last U pick */
   private int                              lastPickU;
   /** Last V pick */
   private int                              lastPickV;
   /** Lights list */
   private Lights                           lights;
   /** Listeners of events appends on OpenGL view */
   private final EventListenerList          listeners;
   /** LOCK for synchronization */
   private final Object                     LOCK                                      = new Object();
   /**
    * Indicates if a screen shot has to make as soon as possible (That is to say when the current display is finish to draw)
    */
   private boolean                          makeAScreenShot;
   /** Material use for 2D objects */
   private Material                         material2D;
   /** List of defined mirors */
   private final List<Miror>                mirors;
   /** Temporary matrix for convert object space to view space */
   private double[]                         modelView;
   /** Indicates if mouse left button is down */
   private boolean                          mouseButtonLeft;
   /** Indicates if mouse right button is down */
   private boolean                          mouseButtonRight;
   /** Indicated if the mouse drag (Move with at least button down) */
   private boolean                          mouseDrag;
   /**
    * New scene, it replace the actual scene as soon as possible.<br>
    * It is not do immediatly to avoid array index of bound or null pointer exception if the change append when the OpenGL view
    * is drawing
    */
   private Scene                            newScene;
   /**
    * Actual detected node : (detectX, detectY) say the location of the detection
    */
   private Node                             nodeDetect;
   /**
    * Actual detected 2D object : (detectX, detectY) say the location of the detection
    */
   private Object2D                         object2DDetect;
   /**
    * Indicates if the render is in pause.<br>
    * Remember, you have to make a pause before hide the component (or make zero size), when is view again you can release the
    * pause
    */
   private boolean                          pause;
   /** Actual pick color */
   private Color4f                          pickColor;
   /** Last UV node pick */
   private Node                             pickUVnode;
   /** Temporary pixels used in snapshot */
   private int[]                            pixels;
   /** Plane use for 2D objects */
   private final Plane                      planeFor2D;
   /** Projection matrix for pass view to screen */
   private double[]                         projection;
   /**
    * Indicates if the render is ready.<br>
    * It is not ready on drawing, but when draw is finish, it becomes ready again<br>
    * Use for snapshot wait and to synchronize screen refresh
    */
   private boolean                          ready;
   /** Actual render scene */
   private Scene                            scene;
   /** The last screen shot */
   private JHelpImage                       screenShot;
   /** Indicates if FPS is print */
   private boolean                          showFPS;
   /** Texture use for FPS print */
   private Texture                          textureFPS;
   /** List of textures to remove from video memory */
   private final QueueSynchronized<Texture> texturesToRemove;
   /** Thread witch manage the screen refresh */
   private Thread                           thread;
   /** Task for signal to listeners the last detection */
   private final UpdateMouseDetection       UPDATE_MOUSE_DETECTION                    = new UpdateMouseDetection();
   /** View port to consider the FOV */
   private int[]                            viewPort;
   /** OpenGL view width */
   private int                              width;
   /** Window material list to refresh */
   private final Vector<WindowMaterial>     windowMaterials;

   /** Listener of scene renderer */
   ArrayList<JHelpSceneRendererListener>    sceneListeners;

   /**
    * Create a render
    */
   public JHelpSceneRenderer()
   {
      this.gui2d = new GUI2D();
      this.listeners = new EventListenerList();
      this.sceneListeners = new ArrayList<JHelpSceneRendererListener>();
      this.animations = new Vector<Animation>();
      this.scene = new Scene();
      this.width = this.height = 0;
      this.fov = 45f;
      this.fpsCount = 0;
      this.makeAScreenShot = false;
      this.ready = false;
      this.showFPS = false;
      this.detectX = -1;
      this.detectY = -1;
      this.planeFor2D = new Plane(false, true);
      this.animationsFps = 25;
      this.pause = false;
      this.windowMaterials = new Vector<WindowMaterial>();
      this.detectionActivate = true;
      this.texturesToRemove = new QueueSynchronized<Texture>();
      this.mirors = new ArrayList<Miror>();
   }

   /**
    * Compute actual model view
    * 
    * @param gl
    *           OpenGL context
    */
   private void computeModelView(final GL2 gl)
   {
      // Create, if need, temporary model view
      if(this.modelView == null)
      {
         this.modelView = new double[16];
      }

      // Get model view
      BufferUtils.TEMPORARY_DOUBLE_BUFFER.rewind();
      ((GL2)gl).glGetDoublev(GL2.GL_MODELVIEW_MATRIX, BufferUtils.TEMPORARY_DOUBLE_BUFFER);
      BufferUtils.fill(this.modelView);
   }

   /**
    * Compute actual projection
    * 
    * @param gl
    *           OpenGL context
    */
   private void computeProjection(final GL2 gl)
   {
      // Create, if need, temporary projection
      if(this.projection == null)
      {
         this.projection = new double[16];
      }

      // Get projection
      BufferUtils.TEMPORARY_DOUBLE_BUFFER.rewind();
      ((GL2)gl).glGetDoublev(GL2.GL_PROJECTION_MATRIX, BufferUtils.TEMPORARY_DOUBLE_BUFFER);
      BufferUtils.fill(this.projection);
   }

   /**
    * Compute actual view port
    * 
    * @param gl
    *           OpenGL context
    */
   private void computeViewPort(final GL2 gl)
   {
      // Create, if need, temporary view port
      if(this.viewPort == null)
      {
         this.viewPort = new int[16];
      }

      // Get view port
      BufferUtils.TEMPORARY_INT_BUFFER.rewind();
      gl.glGetIntegerv(GL2.GL_VIEWPORT, BufferUtils.TEMPORARY_INT_BUFFER);
      BufferUtils.fill(this.viewPort);
   }

   /**
    * Draw object 2D witch are over 3D
    * 
    * @param gl
    *           OpenGL context
    * @param glu
    *           GLU context
    */
   private void drawOver3D(final GL2 gl, final GLU glu)
   {
      // Get all 2D objects over 3D
      final Iterator<Object2D> iterator = this.gui2d.getIteratorOver3D();
      Object2D object2D;
      Texture texture;

      // For each object
      while(iterator.hasNext())
      {
         // Draw the object
         object2D = iterator.next();
         texture = object2D.getTexture();
         if(texture != null)
         {
            this.show2D(gl, glu, texture, object2D.getX(), object2D.getY(), object2D.getWidth(), object2D.getHeight());
         }
      }
   }

   /**
    * Draw object 2D witch are under 3D
    * 
    * @param gl
    *           OpenGL context
    * @param glu
    *           GLU context
    */
   private void drawUnder3D(final GL2 gl, final GLU glu)
   {
      // Get all 2D objects uder 3D
      final Iterator<Object2D> iterator = this.gui2d.getIteratorUnder3D();
      Object2D object2D;
      Texture texture;

      // For each object
      while(iterator.hasNext())
      {
         // Draw the object
         object2D = iterator.next();
         texture = object2D.getTexture();
         if(texture != null)
         {
            this.show2D(gl, glu, texture, object2D.getX(), object2D.getY(), object2D.getWidth(), object2D.getHeight());
         }
      }
   }

   /**
    * Project a 3D point to a screen point
    * 
    * @param x
    *           X
    * @param y
    *           Y
    * @param z
    *           Z
    * @param gl
    *           OpenGL context
    * @param glu
    *           GLU context
    * @return Projected point
    */
   private Point2D gluProject(final float x, final float y, final float z, final GL2 gl, final GLU glu)
   {
      this.computeModelView(gl);
      this.computeProjection(gl);
      this.computeViewPort(gl);
      final double[] point = new double[3];
      glu.gluProject(x, y, z, this.modelView, 0, this.projection, 0, this.viewPort, 0, point, 0);
      return new Point2D(Math.round(point[0] / point[2]), Math.round((this.height - point[1]) / point[2]));
   }

   /**
    * Convert a screen point to 3D point.<br>
    * You have to specify the Z of the 3D point you want
    * 
    * @param x
    *           X
    * @param y
    *           Y
    * @param z
    *           Wanted Z
    * @param gl
    *           OpenGL context
    * @param glu
    *           GLU context
    * @return Converted point
    */
   private Point3D gluUnProject(final float x, final float y, final float z, final GL2 gl, final GLU glu)
   {
      this.computeModelView(gl);
      this.computeProjection(gl);
      this.computeViewPort(gl);
      final double[] point = new double[4];
      glu.gluUnProject(x, this.viewPort[3] - y, z, this.modelView, 0, this.projection, 0, this.viewPort, 0, point, 0);
      final double raport = z / point[2];
      return new Point3D((float) (point[0] * raport), (float) (point[1] * raport), z);
   }

   /**
    * Make a snap shot
    * 
    * @param gl
    *           OpenGL context
    * @param invert
    *           Indicates if vertical flip is apply to the shot
    */
   private void makeSnapShot(final GL2 gl, final boolean invert)
   {
      // A screen shot is initiated ?
      if(this.makeAScreenShot == true)
      {
         // Get actual colors on screen
         BufferUtils.TEMPORARY_FLOAT_BUFFER.rewind();
         gl.glReadPixels(0, 0, this.width, this.height, GL2.GL_RGBA, GL2.GL_FLOAT, BufferUtils.TEMPORARY_FLOAT_BUFFER);
         BufferUtils.TEMPORARY_FLOAT_BUFFER.rewind();

         // Create or recreate the temporary pixels array
         final int nb = this.width * this.height;
         if((this.pixels == null) || (this.pixels.length != nb))
         {
            this.pixels = null;
            this.pixels = new int[nb];
         }

         // ********************************
         // *** Convert colors to pixels ***
         // ********************************
         int r;
         int g;
         int b;
         int a;

         // For each color
         for(int i = 0; i < nb; i++)
         {
            // Convert in ARGB value
            r = (int) (BufferUtils.TEMPORARY_FLOAT_BUFFER.get() * 255f) & 0xFF;
            g = (int) (BufferUtils.TEMPORARY_FLOAT_BUFFER.get() * 255f) & 0xFF;
            b = (int) (BufferUtils.TEMPORARY_FLOAT_BUFFER.get() * 255f) & 0xFF;
            a = (int) (BufferUtils.TEMPORARY_FLOAT_BUFFER.get() * 255f) & 0xFF;
            this.pixels[i] = (a << 24) | (r << 16) | (g << 8) | b;
         }

         // Update the screen shot
         this.screenShot.startDrawMode();
         this.screenShot.setPixels(0, 0, this.width, this.height, this.pixels);

         if(invert == true)
         {
            this.screenShot.flipVertical();
         }

         this.screenShot.endDrawMode();
         this.makeAScreenShot = false;
      }
   }

   /**
    * Compute actual pick color
    * 
    * @param gl
    *           OpenGL context
    * @param x
    *           X
    * @param y
    *           Y
    */
   private void pickColor(final GL2 gl, final int x, final int y)
   {
      if(this.pickColor == null)
      {
         this.pickColor = new Color4f();
      }

      // Get picking color
      BufferUtils.TEMPORARY_FLOAT_BUFFER.rewind();
      gl.glReadPixels(x, this.height - y, 1, 1, GL2.GL_RGBA, GL2.GL_FLOAT, BufferUtils.TEMPORARY_FLOAT_BUFFER);
      BufferUtils.TEMPORARY_FLOAT_BUFFER.rewind();

      // Convert in RGB value
      final float red = BufferUtils.TEMPORARY_FLOAT_BUFFER.get();
      final float green = BufferUtils.TEMPORARY_FLOAT_BUFFER.get();
      final float blue = BufferUtils.TEMPORARY_FLOAT_BUFFER.get();
      BufferUtils.TEMPORARY_FLOAT_BUFFER.rewind();

      // Update picking color
      this.pickColor.set(red, green, blue);
   }

   /**
    * Update alive animations
    * 
    * @param gl
    *           OpenGL context
    */
   private void playAnimations(final GL2 gl)
   {
      // Compute absolute frame
      this.absoluteFrame = (float) (((System.currentTimeMillis() - this.animationTime) * this.animationsFps) / 1000d);

      // For each animation
      final int nb = this.animations.size();
      Animation animation;
      for(int i = nb - 1; i >= 0; i--)
      {
         // Update the animation
         animation = this.animations.get(i);
         if(animation.animate(gl, this.absoluteFrame) == false)
         {
            // If the animation is done, remove it from alive animations
            this.animations.remove(i);
         }
      }
   }

   /**
    * Initialize material for 2D
    * 
    * @param gl
    *           OpenGL context
    */
   private void prepareMaterial2D(final GL2 gl)
   {
      if(this.material2D == null)
      {
         this.material2D = new Material(Material.MATERIAL_FOR_2D_NAME);
         this.material2D.getColorEmissive().set(1f);
         this.material2D.setSpecularLevel(1f);
         this.material2D.setShininess(128);
         this.material2D.getColorDiffuse().set(1f);
         this.material2D.getColorSpecular().set();
         this.material2D.getColorAmbiant().set(1f);
         this.material2D.setTwoSided(true);
      }
      this.material2D.prepareMaterial(gl);
   }

   /**
    * Render the mirrors
    * 
    * @param gl
    *           Open GL context
    * @param glu
    *           GLU context
    * @param camera
    *           Camera reference for compute mirrors
    */
   private void renderMirors(final GL2 gl, final GLU glu, final Camera camera)
   {
      synchronized(this.mirors)
      {
         Texture texture;

         for(final Miror miror : this.mirors)
         {
            texture = miror.startRender(this.scene);

            if(texture != null)
            {
               // Draw the background and clear Z-Buffer
               gl.glClearColor(miror.backgroundRed, miror.backgroundGreen, miror.backgroundBlue, miror.backgroundAlpha);
               gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

               gl.glEnable(GL2.GL_DEPTH_TEST);
               gl.glPushMatrix();

               // Put in camera view
               camera.render(glu);

               // Render the scene
               this.scene.renderTheScene(gl, glu, this);

               gl.glPopMatrix();
               gl.glDisable(GL2.GL_DEPTH_TEST);

               BufferUtils.TEMPORARY_BYTE_BUFFER.rewind();
               gl.glReadPixels(128, 128, this.width - 256, this.height - 256, GL2.GL_RGBA, GL2.GL_BYTE, BufferUtils.TEMPORARY_BYTE_BUFFER);
               texture.setPixelsFromByteBuffer(this.width - 256, this.height - 256);

               miror.endRender(this.scene);
            }
         }
      }
   }

   /**
    * Show a 2D object or hotspot
    * 
    * @param gl
    *           OpenGL context
    * @param glu
    *           GLU context
    * @param texture
    *           Texture to draw
    * @param x
    *           X
    * @param y
    *           Y
    * @param width
    *           Width
    * @param height
    *           Height
    */
   private void show2D(final GL2 gl, final GLU glu, final Texture texture, final int x, final int y, final int width, final int height)
   {
      // Make the material for 2D
      this.prepareMaterial2D(gl);

      // Compute up-left and down-right corner in 3D
      final Point3D point1 = this.gluUnProject(x, y, -1f, gl, glu);
      final Point3D point2 = this.gluUnProject(x + width, y + height, -1f, gl, glu);

      // get new positions and size
      final float x1 = point1.getX();
      final float y1 = point1.getY();
      final float x2 = point2.getX();
      final float y2 = point2.getY();
      final float w = Math.abs(x1 - x2);
      final float h = Math.abs(y1 - y2);
      // Compute middle point
      final float xx = Math.min(x1, x2) + (0.5f * w);
      final float yy = Math.min(y1, y2) + (0.5f * h);

      // Draw the object on the 2D plane
      gl.glDisable(GL2.GL_LIGHTING);
      gl.glEnable(GL2.GL_TEXTURE_2D);
      texture.bind(gl);
      gl.glPushMatrix();
      gl.glTranslatef(xx, yy, -1f);
      gl.glScalef(w, h, 1);
      this.planeFor2D.drawObject(gl, glu);
      gl.glPopMatrix();
      gl.glDisable(GL2.GL_TEXTURE_2D);
      gl.glEnable(GL2.GL_LIGHTING);
   }

   /**
    * Print FPS
    * 
    * @param gl
    *           OpenGL context
    * @param glu
    *           GlU context
    */
   private void showFPS(final GL2 gl, final GLU glu)
   {
      // If the print is enable
      if(this.showFPS == true)
      {
         // If the texture for print don't exists, create it now
         if(this.textureFPS == null)
         {
            this.textureFPS = new Texture("FPS", 128, 64);
         }
         // Print the FPS
         this.show2D(gl, glu, this.textureFPS, 0, 0, 128, 64);
      }
   }

   /**
    * Update FPS value
    */
   private void updateFPS()
   {
      this.fpsCount++;
      final long time = System.currentTimeMillis() - this.fpsStart;
      // If the evaluate time is reach
      if(time >= JHelpSceneRenderer.evaluteTime)
      {
         // Compute the FPS
         this.fps = (float) ((this.fpsCount * 1000d) / time);
         // If the print is enable
         if(this.showFPS == true)
         {
            // If the texture for print don't exists, create it now
            if(this.textureFPS == null)
            {
               this.textureFPS = new Texture("FPS", 128, 64);
            }

            // Print new FPS in the texture
            this.textureFPS.fillRect(0, 0, 128, 64, JHelpSceneRenderer.TR, false);
            if(this.font == null)
            {
               this.font = new Font("Arial", Font.BOLD, 20);
            }
            this.textureFPS.fillString(10, 30, "FPS : " + this.fps, Color.BLACK, this.font, false);
            this.textureFPS.flush();
         }

         // Re-initialize for a new evaluation
         this.fpsCount = 0;
         this.fpsStart = System.currentTimeMillis();
      }
   }

   /**
    * Draw a hotspot in picking mode (That is to say fill with it's node parent picking color)
    * 
    * @param gl
    *           OpenGL context
    * @param glu
    *           GLU context
    * @param node
    *           Node witch carry hotspot
    * @param red
    *           Red value of picking color
    * @param green
    *           Green value of picking color
    * @param blue
    *           Blue value of picking color
    */
   void drawPickHotspot(final GL2 gl, final GLU glu, final Node node, final float red, final float green, final float blue)
   {
      final Texture textureHotspot = node.getTextureHotspot();
      // If no hotspot texture, do nothing
      if(textureHotspot == null)
      {
         return;
      }

      // Project center node in the model view
      final Point3D center = node.getCenter();
      this.computeModelView(gl);
      final double cx = center.getX();
      final double cy = center.getY();
      final double cz = center.getZ();
      final double px = (cx * this.modelView[0]) + (cy * this.modelView[4]) + (cz * this.modelView[8]) + this.modelView[12];
      final double py = (cx * this.modelView[1]) + (cy * this.modelView[5]) + (cz * this.modelView[9]) + this.modelView[13];
      final double pz = (cx * this.modelView[2]) + (cy * this.modelView[6]) + (cz * this.modelView[10]) + this.modelView[14];

      // Project the new center in the screen
      final float z = (float) pz;
      final Point2D centerOnScreen = this.gluProject((float) px, (float) py, z, gl, glu);
      float x1 = centerOnScreen.getX() - (textureHotspot.getWidth() / 2f);
      float y1 = centerOnScreen.getY() - (textureHotspot.getHeight() / 2f);
      float x2 = centerOnScreen.getX() + (textureHotspot.getWidth() / 2f);
      float y2 = centerOnScreen.getY() + (textureHotspot.getHeight() / 2f);
      // Now we know where the hotspot must be on the screen

      // Project this position on 3D
      final Point3D point1 = this.gluUnProject(x1, y1, z, gl, glu);
      final Point3D point2 = this.gluUnProject(x2, y2, z, gl, glu);
      x1 = point1.getX();
      y1 = point1.getY();
      x2 = point2.getX();
      y2 = point2.getY();
      final float w = Math.abs(x1 - x2);
      final float h = Math.abs(y1 - y2);
      final float xx = Math.min(x1, x2) + (0.5f * w);
      final float yy = Math.min(y1, y2) + (0.5f * h);

      // We have all informations, so we can draw the hotspot
      gl.glPushMatrix();

      gl.glLoadIdentity();
      gl.glColor4f(red, green, blue, 1f);
      gl.glTranslatef(xx, yy, z);
      gl.glScalef(w, h, 1);
      this.planeFor2D.drawObject(gl, glu);

      gl.glPopMatrix();
   }

   /**
    * Render the scene
    * 
    * @param gl
    *           OpenGL context
    * @param glu
    *           GLU context
    * @param camera
    *           Actual camera
    */
   void render(final GL2 gl, final GLU glu, final Camera camera)
   {
      try
      {
         this.renderMirors(gl, glu, camera);

         // Draw the background and clear Z-Buffer
         this.scene.drawBackground(gl);
         gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
         // Draw 2D objects under 3D
         gl.glDisable(GL2.GL_DEPTH_TEST);
         this.drawUnder3D(gl, glu);
         gl.glEnable(GL2.GL_DEPTH_TEST);
         gl.glPushMatrix();
         // Put in camera view
         camera.render(glu);

         // Render the scene
         this.scene.renderTheScene(gl, glu, this);
         gl.glPopMatrix();
         gl.glDisable(GL2.GL_DEPTH_TEST);
         // Draw 2D objects over 3D
         this.drawOver3D(gl, glu);
      }
      catch(final Exception exception)
      {
      }
      catch(final Error error)
      {
      }
   }

   /**
    * Render the scene on picking mode
    * 
    * @param gl
    *           OpenGL context
    * @param glu
    *           GLU context
    * @param camera
    *           Actual camera
    */
   void renderPicking(final GL2 gl, final GLU glu, final Camera camera)
   {
      // Prepare for "picking rendering"
      gl.glDisable(GL2.GL_LIGHTING);
      gl.glDisable(GL2.GL_CULL_FACE);
      gl.glClearColor(1f, 1f, 1f, 1f);
      gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
      gl.glPushMatrix();
      // Put in camera view
      camera.render(glu);
      // Render the scene in picking mode
      this.scene.renderTheScenePicking(gl, glu, this);
      gl.glPopMatrix();
      gl.glEnable(GL2.GL_LIGHTING);

      if(this.object2DDetect != null)
      {
         if(this.object2DDetect.isDetected(this.detectX, this.detectY) == false)
         {
            this.gui2d.mouseState(this.detectX, this.detectY, this.mouseButtonLeft, this.mouseButtonRight, this.mouseDrag, null);
         }
      }

      // If detection point is on the screen
      if((this.detectX >= 0) && (this.detectX < this.width) && (this.detectY >= 0) && (this.detectY < this.height))
      {
         // Compute pick color and node pick
         this.pickColor(gl, this.detectX, this.detectY);

         this.nodeDetect = this.scene.getPickingNode(this.pickColor);
         if(this.nodeDetect != null)
         {
            // If node is detect, verify if a 2D object over the 3D can be
            // detect too
            this.object2DDetect = this.gui2d.detectOver3D(this.detectX, this.detectY);
         }
         else
         {
            // If no node detect, verify if a 2D object is detect
            this.object2DDetect = this.gui2d.detectOver3DorUnder3D(this.detectX, this.detectY);
         }
      }
      else
      {
         this.nodeDetect = null;
         this.object2DDetect = null;
      }

      ThreadManager.THREAD_MANAGER.doThread(this.UPDATE_MOUSE_DETECTION, new DetectionInfo(this.object2DDetect, this.gui2d, this.detectX, this.detectY,
            this.mouseButtonLeft, this.mouseButtonRight, this.mouseDrag, this.scene, this.nodeDetect));

      this.mouseButtonLeft = false;
      this.mouseButtonRight = false;
   }

   /**
    * Render for pick UV
    * 
    * @param gl
    *           Open GL context
    * @param glu
    *           Open GL utilities
    * @param camera
    *           Actual camera
    */
   void renderPickUV(final GL2 gl, final GLU glu, final Camera camera)
   {
      // Prepare for "picking rendering"
      gl.glDisable(GL2.GL_LIGHTING);
      gl.glDisable(GL2.GL_CULL_FACE);
      gl.glClearColor(1f, 1f, 1f, 1f);
      gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
      gl.glPushMatrix();
      // Put in camera view
      camera.render(glu);
      // Render the scene in picking mode
      this.scene.renderPickingUV(this.pickUVnode, gl, glu);
      gl.glPopMatrix();
      gl.glEnable(GL2.GL_LIGHTING);

      // If detection point is on the screen
      if((this.detectX >= 0) && (this.detectX < this.width) && (this.detectY >= 0) && (this.detectY < this.height))
      {
         // Compute pick color and node pick
         this.pickColor(gl, this.detectX, this.detectY);

         final int red = (int) (this.pickColor.getRed() * 255);
         final int green = (int) (this.pickColor.getGreen() * 255);
         final int blue = (int) (this.pickColor.getBlue() * 255);

         this.lastPickU = blue;
         this.lastPickV = green;

         if((this.pickUVnode.pickUVlistener != null) && (red < 128))
         {
            this.pickUVnode.pickUVlistener.pickUV(this.lastPickU, this.lastPickV, this.pickUVnode);
         }
      }
   }

   /**
    * Draw a hotspot
    * 
    * @param gl
    *           OpenGL context
    * @param glu
    *           GLU context
    * @param node
    *           Node witch carry hotspot
    */
   void showHotspot(final GL2 gl, final GLU glu, final Node node)
   {
      final Texture textureHotspot = node.getTextureHotspot();
      // If no hotspot texture, do nothing
      if(textureHotspot == null)
      {
         return;
      }

      // Use material for 2D
      this.prepareMaterial2D(gl);

      // Project center node in the model view
      final Point3D center = node.getCenter();
      this.computeModelView(gl);
      final double cx = center.getX();
      final double cy = center.getY();
      final double cz = center.getZ();
      final double px = (cx * this.modelView[0]) + (cy * this.modelView[4]) + (cz * this.modelView[8]) + this.modelView[12];
      final double py = (cx * this.modelView[1]) + (cy * this.modelView[5]) + (cz * this.modelView[9]) + this.modelView[13];
      final double pz = (cx * this.modelView[2]) + (cy * this.modelView[6]) + (cz * this.modelView[10]) + this.modelView[14];

      // Project the new center in the screen
      final float z = (float) pz;
      final Point2D centerOnScreen = this.gluProject((float) px, (float) py, z, gl, glu);
      float x1 = centerOnScreen.getX() - (textureHotspot.getWidth() / 2f);
      float y1 = centerOnScreen.getY() - (textureHotspot.getHeight() / 2f);
      float x2 = centerOnScreen.getX() + (textureHotspot.getWidth() / 2f);
      float y2 = centerOnScreen.getY() + (textureHotspot.getHeight() / 2f);
      final Point3D point1 = this.gluUnProject(x1, y1, z, gl, glu);
      final Point3D point2 = this.gluUnProject(x2, y2, z, gl, glu);
      // Now we know where the hotspot must be on the screen

      // Project this position on 3D
      x1 = point1.getX();
      y1 = point1.getY();
      x2 = point2.getX();
      y2 = point2.getY();
      final float w = Math.abs(x1 - x2) * 1000;
      final float h = Math.abs(y1 - y2) * 1000;
      final float xx = Math.min(x1, x2) + (0.5f * w);
      final float yy = Math.min(y1, y2) + (0.5f * h);

      // We have all informations, so we can draw the hotspot
      gl.glDisable(GL2.GL_LIGHTING);
      gl.glEnable(GL2.GL_TEXTURE_2D);
      gl.glPushMatrix();
      gl.glLoadIdentity();

      textureHotspot.bind(gl);
      gl.glTranslatef(xx, yy, z);
      gl.glScalef(w, h, 1);
      this.planeFor2D.drawObject(gl, glu);

      gl.glPopMatrix();
      gl.glDisable(GL2.GL_TEXTURE_2D);
      gl.glEnable(GL2.GL_LIGHTING);
   }

   /**
    * Signal to listeners that user click on nothing (space)
    * 
    * @param mouseX
    *           Mouse X
    * @param mouseY
    *           Mouse Y
    * @param leftButton
    *           Indicates if left mouse button is down
    * @param rightButton
    *           Indicates if right mouse button is down
    */
   protected void fireClickInSpace(final int mouseX, final int mouseY, final boolean leftButton, final boolean rightButton)
   {
      final ClickInSpaceListener[] clickInSpaceListeners = this.listeners.getListeners(ClickInSpaceListener.class);
      for(final ClickInSpaceListener clickInSpaceListener : clickInSpaceListeners)
      {
         clickInSpaceListener.clickInSpace(mouseX, mouseY, leftButton, rightButton);
      }
   }

   /**
    * Add click in space listener
    * 
    * @param listener
    *           Listener to register
    */
   public void addClickInSpaceListener(final ClickInSpaceListener listener)
   {
      this.listeners.add(ClickInSpaceListener.class, listener);
   }

   /**
    * Add key listener in 3D view
    * 
    * @param listener
    *           Listener to add
    */
   public void addKeyListener(final KeyListener listener)
   {
      this.listeners.add(KeyListener.class, listener);
   }

   /**
    * Ad a mirror.<br>
    * Mirror are heavy to compute, so don't put too much
    * 
    * @param miror
    *           Mirror to add
    */
   public void addMiror(final Miror miror)
   {
      if(miror == null)
      {
         throw new NullPointerException("miror musn't be null");
      }

      synchronized(this.mirors)
      {
         this.mirors.add(miror);
      }
   }

   /**
    * Add mouse listener in 3D view
    * 
    * @param listener
    *           Listener to add
    */
   public void addMouseListener(final MouseListener listener)
   {
      this.listeners.add(MouseListener.class, listener);
   }

   /**
    * Add mouse motion listener in 3D view
    * 
    * @param listener
    *           Listener to add
    */
   public void addMouseMotionListener(final MouseMotionListener listener)
   {
      this.listeners.add(MouseMotionListener.class, listener);
   }

   /**
    * Add mouse wheel listener in 3D view
    * 
    * @param listener
    *           Listener to add
    */
   public void addMouseWheelListener(final MouseWheelListener listener)
   {
      this.listeners.add(MouseWheelListener.class, listener);
   }

   /**
    * Disable the UV picking
    */
   public void disablePickUV()
   {
      this.pickUVnode = null;
   }

   @Override
   public void dispose(GLAutoDrawable glAutoDrawable) {

   }


   /**
    * Draw the OpenGL.<br>
    * It is call by JOGL when OpenGL need to be refresh
    * 
    * @param drawable
    *           Drawable context
    * @see javax.media.opengl.GLEventListener#display(javax.media.opengl.GLAutoDrawable)
    */
   @Override
   public void display(final GLAutoDrawable drawable)
   {
      System.out.println("display: " + drawable);

      this.ready = false;
      // Get OpenGL and GLU context
      final GL2 gl = drawable.getGL().getGL2();
      final GLU glu = new GLUgl2();

      if(this.texturesToRemove.isEmpty() == false)
      {
         this.texturesToRemove.outQueue().removeFromMemory(gl);
      }

      // If a new scene wait, change the scene
      if(this.newScene != null)
      {
         this.scene = this.newScene;
         this.newScene = null;
      }

      // Update played animations
      this.playAnimations(gl);

      // Initialize for screen shot
      if((this.screenShot == null) || (this.screenShot.getWidth() != this.width) || (this.screenShot.getHeight() != this.height))
      {
         this.screenShot = null;
         this.screenShot = new JHelpImage(this.width, this.height);
      }

      // Refresh window materials
      for(final WindowMaterial windowMaterial : this.windowMaterials)
      {
         windowMaterial.refreshIfNeed();
      }

      // Get actual camera
      final Camera camera = this.scene.getCamera();
      // Render picking mode
      if(this.detectionActivate == true)
      {
         if(this.pickUVnode != null)
         {
            this.renderPickUV(gl, glu, camera);
         }
         else
         {
            this.renderPicking(gl, glu, camera);
         }
      }

      // Render the lights
      this.lights.render(gl);
      // Render the scene
      this.render(gl, glu, camera);

      // Take the snapshot
      this.makeSnapShot(gl, true);
      // Print FPS
      this.showFPS(gl, glu);
      // Update FPS
      this.updateFPS();

      // Make ready for the next loop
      gl.glEnable(GL2.GL_DEPTH_TEST);
      drawable.swapBuffers();

      this.ready = true;
      synchronized(this.LOCK)
      {
         this.LOCK.notify();
      }
   }

//   /**
//    * Call by JOGL we screen resolution change.<br>
//    * Do nothing now, so it can append strange result if screen resolution change when engine is running
//    *
//    * @param drawable
//    *           Drawable context
//    * @param modeChanged
//    *           Indicates if the mode changed
//    * @param deviceChanged
//    *           Indicates if the device changed
//    * @see javax.media.opengl.GLEventListener#displayChanged(javax.media.opengl.GLAutoDrawable, boolean, boolean)
//    */
//   @Override
//   public void displayChanged(final GLAutoDrawable drawable, final boolean modeChanged, final boolean deviceChanged)
//   {
//   }
//

   /**
    * Actual absolute frame
    * 
    * @return Actual absolute frame
    */
   public float getAbsoluteFrame()
   {
      return this.absoluteFrame;
   }

   /**
    * Animation play list
    * 
    * @return Animation play list
    */
   public Iterator<Animation> getAnimations()
   {
      return this.animations.iterator();
   }

   /**
    * Actual animation FPS
    * 
    * @return Actual animation FPS
    */
   public int getAnimationsFps()
   {
      return this.animationsFps;
   }

   /**
    * Actual FPS
    * 
    * @return Actual FPS
    */
   public float getFps()
   {
      return this.fps;
   }

   /**
    * 2D manager
    * 
    * @return 2D manager
    */
   public GUI2D getGui2d()
   {
      return this.gui2d;
   }

   /**
    * Lights list for manipulate lights.<br>
    * This list is valid if the scene was draw at least one time
    * 
    * @return Lights list for manipulate lights
    */
   public Lights getLights()
   {
      return this.lights;
   }

   /**
    * Last detect node.<br>
    * Beware it becomes often {@code null}.<br>
    * Prefer use listener to detected mouse on node
    * 
    * @return Last detect node
    */
   public Node getNodeDetect()
   {
      return this.nodeDetect;
   }

   /**
    * Last detect 2D object<br>
    * Beware it becomes often {@code null}.<br>
    * Prefer use listener to detected mouse on 2D objects
    * 
    * @return Last detect 2D object
    */
   public Object2D getObject2DDetect()
   {
      return this.object2DDetect;
   }

   /**
    * Return pickUVnode
    * 
    * @return pickUVnode
    */
   public Node getPickUVnode()
   {
      return this.pickUVnode;
   }

   /**
    * Actual scene
    * 
    * @return Actual scene
    */
   public Scene getScene()
   {
      Scene scene = this.newScene;
      if(scene == null)
      {
         scene = this.scene;
      }
      return scene;
   }

   /**
    * Call by JOGL on OpenGL initialization
    * 
    * @param drawable
    *           Drawable context
    * @see javax.media.opengl.GLEventListener#init(javax.media.opengl.GLAutoDrawable)
    */
   @Override
   public void init(final GLAutoDrawable drawable)
   {
      // Delegate key and mouse events to this manager
      drawable.addGLEventListener(this);

//      drawable.addKeyListener(this);
//      drawable.addMouseListener(this);
//      drawable.addMouseMotionListener(this);
//      drawable.addMouseWheelListener(this);

      // Get OpenGL and GLU context
      final GLU glu = new GLU();
      final GL2 gl = drawable.getGL().getGL2();

      // Get dimensions and ration
      this.width = drawable.getSurfaceWidth();
      this.height = drawable.getSurfaceHeight();
      final float ratio = (float) this.width / (float) this.height;
      // Get number maximum of lights
      BufferUtils.TEMPORARY_INT_BUFFER.rewind();
      gl.glGetIntegerv(GL2.GL_MAX_LIGHTS, BufferUtils.TEMPORARY_INT_BUFFER);
      BufferUtils.TEMPORARY_INT_BUFFER.rewind();
      this.lights = new Lights(BufferUtils.TEMPORARY_INT_BUFFER.get());

      // *************************
      // *** Initialize OpenGL ***
      // *************************
      // Alpha enable
      gl.glEnable(GL2.GL_ALPHA_TEST);
      // Set alpha precision
      gl.glAlphaFunc(GL2.GL_GREATER, 0.01f);
      // Material can be colored
      gl.glEnable(GL2.GL_COLOR_MATERIAL);
      // For performance disable texture, we enable them only on need
      gl.glDisable(GL2.GL_TEXTURE_2D);
      // Way to compute alpha
      gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
      // We accept blinding
      gl.glEnable(GL2.GL_BLEND);
      // Fix the view port
      gl.glViewport(0, 0, this.width, this.height);
      // Normalization is enable
      gl.glEnable(GL2.GL_NORMALIZE);
      // Fix the view port. Yes again, I don't know why, but it work better on
      // doing that
      gl.glViewport(0, 0, this.width, this.height);

      // Set the "3D feeling".
      // That is to say how the 3D looks like
      // Here we want just see the depth, but not have fish eye effect
      gl.glMatrixMode(GL2.GL_PROJECTION);
      gl.glLoadIdentity();
      glu.gluPerspective(45.0f, ratio, 0.1f, 5000f);
      gl.glMatrixMode(GL2.GL_MODELVIEW);
      gl.glLoadIdentity();

      // Initialize background
      gl.glClearColor(1f, 1f, 1f, 1f);
      gl.glEnable(GL2.GL_DEPTH_TEST);

      // Enable see and hide face
      gl.glEnable(GL2.GL_CULL_FACE);
      gl.glCullFace(GL2.GL_FRONT);

      // Light base adjustment for smooth effect
      gl.glLightModeli(GL2.GL_LIGHT_MODEL_LOCAL_VIEWER, GL2.GL_TRUE);
      gl.glShadeModel(GL2.GL_SMOOTH);
      gl.glLightModeli(GL2.GL_LIGHT_MODEL_COLOR_CONTROL, GL2.GL_SEPARATE_SPECULAR_COLOR);
      gl.glLightModeli(GL2.GL_LIGHT_MODEL_TWO_SIDE, 1);

      // Enable lights and default light
      gl.glEnable(GL2.GL_LIGHTING);

      ThreadManager.THREAD_MANAGER.doThread(this.fireEventScheduleAction, JHelpSceneRenderer.ACTION_FIRE_SCENE_RENDERER_IS_INITIALIZED);
   }

   /**
    * Return detectionActivate
    * 
    * @return detectionActivate
    */
   public boolean isDetectionActivate()
   {
      return this.detectionActivate;
   }

   /**
    * Indicates if the renderer is in pause
    * 
    * @return {@code true} if the renderer is in pause
    */
   public boolean isPause()
   {
      return this.pause;
   }

   /**
    * Indicates if the FPS is show
    * 
    * @return {@code true} if the FPS is show
    */
   public boolean isShowFPS()
   {
      return this.showFPS;
   }

   /**
    * Action on key press.<br>
    * Here just signal to listeners
    * 
    * @param e
    *           Event description
    * @see KeyListener#keyPressed(KeyEvent)
    */
   @Override
   public void keyPressed(final KeyEvent e)
   {
      final KeyListener[] listeners = this.listeners.getListeners(KeyListener.class);
      for(final KeyListener listener : listeners)
      {
         listener.keyPressed(e);
      }
   }

   /**
    * Action on key release.<br>
    * Here just signal to listeners
    * 
    * @param e
    *           Event description
    * @see KeyListener#keyReleased(KeyEvent)
    */
   @Override
   public void keyReleased(final KeyEvent e)
   {
      final KeyListener[] listeners = this.listeners.getListeners(KeyListener.class);
      for(final KeyListener listener : listeners)
      {
         listener.keyReleased(e);
      }
   }

   /**
    * Action on key type.<br>
    * Here just signal to listeners
    * 
    * @param e
    *           Event description
    * @see KeyListener#keyTyped(KeyEvent)
    */
   @Override
   public void keyTyped(final KeyEvent e)
   {
      final KeyListener[] listeners = this.listeners.getListeners(KeyListener.class);
      for(final KeyListener listener : listeners)
      {
         listener.keyTyped(e);
      }
   }

   /**
    * Load scene from file
    * 
    * @param file
    *           File to read
    * @return Extracted scene
    * @throws Exception
    *            On reading issue or if file is not a valid scene
    */
   @SuppressWarnings("unchecked")
   public Scene load(final File file) throws Exception
   {
      final ZipFile zipFile = new ZipFile(file);
      ZipEntry zipEntry;
      Texture texture;
      String name;
      Enumeration<ZipEntry> enumeration = (Enumeration<ZipEntry>) zipFile.entries();
      while(enumeration.hasMoreElements() == true)
      {
         zipEntry = enumeration.nextElement();
         name = zipEntry.getName();

         if(name.startsWith("textures/") == true)
         {
            name = name.substring(9);

            texture = Texture.obtainTexture(name);
            if(texture == null)
            {
               texture = new Texture(name, 1, 1);
            }

            CompressiveImage.read(name, zipFile.getInputStream(zipEntry), texture);
         }
      }

      enumeration = (Enumeration<ZipEntry>) zipFile.entries();
      while(enumeration.hasMoreElements() == true)
      {
         zipEntry = enumeration.nextElement();
         name = zipEntry.getName();

         if(name.startsWith("materials/") == true)
         {
            Material.parseXML(MarkupXML.load(zipFile.getInputStream(zipEntry)));
         }
      }

      final Scene scene = new Scene();

      scene.loadFromXML(MarkupXML.load(zipFile.getInputStream(zipFile.getEntry("scene3D"))));

      this.newScene = scene;
      zipFile.close();

      return scene;
   }

   /**
    * Convert time in millisecond to number of frame in animation.<br>
    * The result depends on current animation FPS<br>
    * See {@link #getAnimationsFps()} and {@link #setAnimationsFps(int)}
    * 
    * @param millisecond
    *           Number of laps time in milliseconds
    * @return Number frame need to play the given time (depends on current animation FPS)
    */
   public int millisecondToFrameAnimation(final int millisecond)
   {
      return (int)(((millisecond * this.animationsFps) + 500.0f) / 1000f);
   }

   /**
    * Action on mouse click.<br>
    * Here just signal to listeners
    * 
    * @param e
    *           Event description
    * @see MouseListener#mouseClicked(MouseEvent)
    */
   @Override
   public void mouseClicked(final MouseEvent e)
   {
      final MouseListener[] listeners = this.listeners.getListeners(MouseListener.class);
      for(final MouseListener listener : listeners)
      {
         listener.mouseClicked(e);
      }
   }

   /**
    * Action on mouse drag.<br>
    * Here just signal to listeners
    * 
    * @param e
    *           Event description
    * @see MouseMotionListener#mouseDragged(MouseEvent)
    */
   @Override
   public void mouseDragged(final MouseEvent e)
   {
      final MouseMotionListener[] listeners = this.listeners.getListeners(MouseMotionListener.class);
      for(final MouseMotionListener listener : listeners)
      {
         listener.mouseDragged(e);
      }
   }

   /**
    * Action on mouse enter.<br>
    * Here just signal to listeners
    * 
    * @param e
    *           Event description
    * @see MouseListener#mouseEntered(MouseEvent)
    */
   @Override
   public void mouseEntered(final MouseEvent e)
   {
      final MouseListener[] listeners = this.listeners.getListeners(MouseListener.class);
      for(final MouseListener listener : listeners)
      {
         listener.mouseEntered(e);
      }
   }

   /**
    * Action on mouse exit.<br>
    * Here just signal to listeners
    * 
    * @param e
    *           Event description
    * @see MouseListener#mouseExited(MouseEvent)
    */
   @Override
   public void mouseExited(final MouseEvent e)
   {
      final MouseListener[] listeners = this.listeners.getListeners(MouseListener.class);
      for(final MouseListener listener : listeners)
      {
         listener.mouseExited(e);
      }
   }

   /**
    * Action on mouse move.<br>
    * Here just signal to listeners
    * 
    * @param e
    *           Event description
    * @see MouseMotionListener#mouseMoved(MouseEvent)
    */
   @Override
   public void mouseMoved(final MouseEvent e)
   {
      final MouseMotionListener[] listeners = this.listeners.getListeners(MouseMotionListener.class);
      for(final MouseMotionListener listener : listeners)
      {
         listener.mouseMoved(e);
      }
   }

   /**
    * Action on mouse press.<br>
    * Here just signal to listeners
    * 
    * @param e
    *           Event description
    * @see MouseListener#mousePressed(MouseEvent)
    */
   @Override
   public void mousePressed(final MouseEvent e)
   {
      final MouseListener[] listeners = this.listeners.getListeners(MouseListener.class);
      for(final MouseListener listener : listeners)
      {
         listener.mousePressed(e);
      }
   }

   /**
    * Action on mouse release.<br>
    * Here just signal to listeners
    * 
    * @param e
    *           Event description
    * @see MouseListener#mouseReleased(MouseEvent)
    */
   @Override
   public void mouseReleased(final MouseEvent e)
   {
      final MouseListener[] listeners = this.listeners.getListeners(MouseListener.class);
      for(final MouseListener listener : listeners)
      {
         listener.mouseReleased(e);
      }
   }

   /**
    * Action on mouse wheel.<br>
    * Here just signal to listeners
    * 
    * @param e
    *           Event description
    * @see MouseWheelListener#mouseWheelMoved(MouseWheelEvent)
    */
   @Override
   public void mouseWheelMoved(final MouseWheelEvent e)
   {
      final MouseWheelListener[] listeners = this.listeners.getListeners(MouseWheelListener.class);
      for(final MouseWheelListener listener : listeners)
      {
         listener.mouseWheelMoved(e);
      }
   }

   /**
    * Play animation .<br>
    * The animation is played as soon as possible
    * 
    * @param animation
    *           Animation to play
    */
   public void playAnimation(final Animation animation)
   {
      if(this.animations.contains(animation) == false)
      {
         this.animations.add(animation);
         animation.setStartAbsoluteFrame(this.absoluteFrame);
      }
   }

   /**
    * Force the 3D view to refresh.<br>
    * On most of situations you never need call this method.
    */
   public void refresh()
   {
      //this.canvas.repaint();
   }

   /**
    * Register a scene renderer listener
    * 
    * @param sceneRendererListener
    *           Listener to register
    */
   public void registerJHelpSceneRendererListener(final JHelpSceneRendererListener sceneRendererListener)
   {
      this.sceneListeners.add(sceneRendererListener);
   }

   /**
    * Register a window material to refresh
    * 
    * @param windowMaterial
    *           Window material to add
    */
   public void registerWindowMaterial(final WindowMaterial windowMaterial)
   {
      if(windowMaterial == null)
      {
         throw new NullPointerException("windowMaterial musn't be null");
      }

      this.windowMaterials.addElement(windowMaterial);
      windowMaterial.setSceneRenderer(this);
   }

   /**
    * Remove click in space listener
    * 
    * @param listener
    *           Listener to unregister
    */
   public void removeClickInSpaceListener(final ClickInSpaceListener listener)
   {
      this.listeners.remove(ClickInSpaceListener.class, listener);
   }

   /**
    * Add texture to remove from memory list, the real remove will append if OpenGL thread
    * 
    * @param textureName
    *           Texture to remove
    */
   public void removeFromMemory(final String textureName)
   {
      final Texture texture = Texture.obtainTexture(textureName);

      if(texture != null)
      {
         this.texturesToRemove.inQueue(texture);
      }
   }

   /**
    * Add texture to remove from memory list, the real remove will append if OpenGL thread
    * 
    * @param texture
    *           Texture to remove
    */
   public void removeFromMemory(final Texture texture)
   {
      this.texturesToRemove.inQueue(texture);
   }

   /**
    * Remove key listener
    * 
    * @param listener
    *           listener to remove
    */
   public void removeKeyListener(final KeyListener listener)
   {
      this.listeners.remove(KeyListener.class, listener);
   }

   /**
    * Remove a mirror
    * 
    * @param miror
    *           Mirror to remove
    */
   public void removeMiror(final Miror miror)
   {
      synchronized(this.mirors)
      {
         this.mirors.remove(miror);
      }
   }

   /**
    * Remove mouse listener
    * 
    * @param listener
    *           listener to remove
    */
   public void removeMouseListener(final MouseListener listener)
   {
      this.listeners.remove(MouseListener.class, listener);
   }

   /**
    * Remove mouse motion listener
    * 
    * @param listener
    *           listener to remove
    */
   public void removeMouseMotionListener(final MouseMotionListener listener)
   {
      this.listeners.remove(MouseMotionListener.class, listener);
   }

   /**
    * Remove mouse wheel listener
    * 
    * @param listener
    *           listener to remove
    */
   public void removeMouseWheelListener(final MouseWheelListener listener)
   {
      this.listeners.remove(MouseWheelListener.class, listener);
   }

   /**
    * Call by JOGL when 3D view is resize
    * 
    * @param drawable
    *           Drawable context
    * @param x
    *           New X
    * @param y
    *           New Y
    * @param width
    *           New width
    * @param height
    *           New height
    * @see javax.media.opengl.GLEventListener#reshape(javax.media.opengl.GLAutoDrawable, int, int, int, int)
    */
   @Override
   public void reshape(final GLAutoDrawable drawable, final int x, final int y, final int width, final int height)
   {
      // Get OpenGL and GLU context
      final GLU glu = new GLU();
      final GL2 gl = (GL2) drawable.getGL();

      // Compute new view with new size
      this.width = drawable.getSurfaceWidth();
      this.height = drawable.getSurfaceHeight();
      final float ratio = (float) this.width / ((float) this.height);
      gl.glViewport(0, 0, this.width, this.height);
      gl.glMatrixMode(GL2.GL_PROJECTION);
      gl.glLoadIdentity();
      glu.gluPerspective(this.fov, ratio, 0.1f, 5000f);
      gl.glMatrixMode(GL2.GL_MODELVIEW);
      gl.glLoadIdentity();
   }

   /**
    * Update automatic the 3D view. It is intern, you have never to call it
    * 
    * @see Runnable#run()
    */
   @Override
   public void run()
   {
      // Initial time to wait between 2 refresh
      int waitMax = 40;
      long laps;
      long waitLeft;

      // While the renderer have to update
      while(this.thread != null)
      {


         // Test if the drawing is allowed
         if((this.canvas.isVisible() == true) && (this.pause == false))
         {
            // We can draw, so call the refresh
            final long start = System.currentTimeMillis();
            this.ready = false;
            //this.canvas.repaint();

            // Wait for the renderer is ready, that is to say the scene is draw
            while(this.ready == false)
            {
               synchronized(this.LOCK)
               {
                  try
                  {
                     this.LOCK.wait(waitMax);
                  }
                  catch(final InterruptedException e)
                  {
                  }
               }
            }

            // Compute if the render takes more or less time than we expect and
            // try adjust the waiting near the computer capacity
            laps = System.currentTimeMillis() - start;
            waitLeft = waitMax - laps;
            if(waitLeft < 1)
            {
               waitLeft = 1;
            }
            try
            {
               System.out.println("waitleft: " + waitLeft);
               Thread.sleep(waitLeft);
            }
            catch(final InterruptedException e)
            {
            }
            if(laps < waitMax)
            {
               waitMax--;
            }
            else if(laps > waitMax)
            {
               waitMax++;
            }
            if(waitMax < 10)
            {
               waitMax = 10;
            }
            if(waitMax > 100)
            {
               waitMax = 100;
            }
         }
         else
         {
            // If the draw is forbidden, just wait 1 second before retry
            try
            {
               Thread.sleep(512);
            }
            catch(final InterruptedException e)
            {
            }
            // If the canvas is a can't draw state and we are not in pause, try
            // to repair the draw
            if((this.canvas.isVisible() == false) && (this.pause == false))
            {
               this.canvas.setVisible(true);
            }
         }
      }
      System.out.println("done");
   }

   /**
    * Save the current scene in a file
    * 
    * @param file
    *           File where save
    * @throws IOException
    *            On writing issue
    */
   public void save(final File file) throws IOException
   {
      if(UtilIO.createFile(file) == false)
      {
         throw new IOException("Can't create the file : " + file.getAbsolutePath());
      }

      ZipOutputStream zipOutputStream = null;
      CompressiveImage compressiveImage;
      ZipEntry zipEntry;

      try
      {
         zipOutputStream = new ZipOutputStream(new FileOutputStream(file));
         zipOutputStream.setLevel(9);

         for(final Texture texture : Tool3D.collectAllUsedTexture(this))
         {
            compressiveImage = texture.toCompressiveImage();

            zipEntry = new ZipEntry("textures/" + compressiveImage.getName().replace(File.separatorChar, '/'));
            zipOutputStream.putNextEntry(zipEntry);

            compressiveImage.write(zipOutputStream);

            zipOutputStream.closeEntry();
         }

         compressiveImage = null;

         for(final Material material : Tool3D.collectAllUsedMaterial(this.scene))
         {
            zipEntry = new ZipEntry("materials/" + material.getName());

            zipOutputStream.putNextEntry(zipEntry);

            material.saveToXML().write(zipOutputStream);

            zipOutputStream.closeEntry();
         }

         zipEntry = new ZipEntry("scene3D");

         zipOutputStream.putNextEntry(zipEntry);

         this.scene.saveToXML().write(zipOutputStream);

         zipOutputStream.closeEntry();
      }
      catch(final Exception exception)
      {
         throw new IOException("Save in " + file.getAbsolutePath() + " failed !", exception);
      }
      finally
      {
         if(zipOutputStream != null)
         {
            try
            {
               zipOutputStream.finish();
            }
            catch(final Exception exception)
            {
            }

            try
            {
               zipOutputStream.flush();
            }
            catch(final Exception exception)
            {
            }

            try
            {
               zipOutputStream.close();
            }
            catch(final Exception exception)
            {
            }
         }
      }
   }

   /**
    * Make a screen shot.<br>
    * This method can make time to exit, so it is good idea to call it in a thread.
    * 
    * @return The screen shot
    */
   public JHelpImage screenShot()
   {
      // Wait for the render is ready
      while(this.ready == false)
      {
         try
         {
            Thread.sleep(1);
         }
         catch(final InterruptedException e)
         {
         }
      }
      // Initialize the screen shot
      this.screenShot = new JHelpImage(this.width, this.height);
      this.makeAScreenShot = true;
      // Wait for the screen shot is done
      do
      {
         try
         {
            Thread.sleep(10);
         }
         catch(final InterruptedException e)
         {
         }
      }
      while(this.makeAScreenShot == true);

      // Return the screen shot
      return this.screenShot;
   }

   /**
    * Change animation FPS
    * 
    * @param animationsFps
    *           New animation FPS
    */
   public void setAnimationsFps(final int animationsFps)
   {
      this.animationsFps = Math.max(1, animationsFps);
   }

   /**
    * Modify detectionActivate
    * 
    * @param detectionActivate
    *           New detectionActivate value
    */
   public void setDetectionActivate(final boolean detectionActivate)
   {
      this.detectionActivate = detectionActivate;
   }

   /**
    * Position the detect position
    * 
    * @param x
    *           X
    * @param y
    *           Y
    * @param mouseButtonLeft
    *           Indicates mouse left button state
    * @param mouseButtonRight
    *           Indicates mouse right button state
    * @param mouseDrag
    *           Enable drag mode
    */
   public void setDetectPosition(final int x, final int y, final boolean mouseButtonLeft, final boolean mouseButtonRight, final boolean mouseDrag)
   {
      this.detectX = x;
      this.detectY = y;

      this.mouseButtonLeft = mouseButtonLeft;
      this.mouseButtonRight = mouseButtonRight;
      this.mouseDrag = mouseDrag;
   }

   /**
    * Change pause state
    * 
    * @param pause
    *           New pause state
    */
   public void setPause(final boolean pause)
   {
      if(this.pause == pause)
      {
         return;
      }

      this.pause = pause;
      if(this.pause == false)
      {
         final Stack<Node> stack = new Stack<Node>();
         stack.push(this.scene.getRoot());
         Node node;

         while(stack.isEmpty() == false)
         {
            node = stack.pop();

            if(node instanceof Object3D)
            {
               ((Object3D) node).reconstructTheList();
            }

            for(final Node child : node.children)
            {
               stack.push(child);
            }
         }

         Texture.refreshAllTextures();
         Material.refreshAllMaterials();
      }
   }

   /**
    * Modify pickUVnode
    * 
    * @param pickUVnode
    *           New pickUVnode value
    */
   public void setPickUVnode(final Node pickUVnode)
   {
      this.pickUVnode = pickUVnode;
   }

   /**
    * Change the scene.<br>
    * Does nothing is the argument is {@code null}
    * 
    * @param scene
    *           New scene
    */
   public void setScene(final Scene scene)
   {
      // The scene will change as soon as possible
      this.newScene = scene;
   }

   /**
    * Show or hide FPS print
    * 
    * @param showFPS
    *           {@code true} to print FPS
    */
   public void setShowFPS(final boolean showFPS)
   {
      this.showFPS = showFPS;
   }

   /**
    * Launch the renderer.<br>
    * Canvas for draw for real must create outside
    * 
    * @param canvas
    *           The canvas to refresh
    */
   public void start(final GLCanvas canvas)
   {
      if(canvas == null)
      {
         throw new NullPointerException("The canvas couldn't be null");
      }
      this.canvas = canvas;
      if(this.thread == null)
      {
         this.thread = new Thread(this);
         this.thread.start();
      }
      this.animationTime = System.currentTimeMillis();
   }

   /**
    * Stop the renderer
    */
   public void stop()
   {
      this.thread = null;
      this.canvas.setVisible(false);
   }

   /**
    * Stop animation
    * 
    * @param animation
    *           Animation to stop
    */
   public void stopAnimation(final Animation animation)
   {
      this.animations.remove(animation);
   }

   /**
    * Try to restart the automatic refresh
    * 
    * @return {@code true} if restart success
    */
   public boolean tryRestart()
   {
      if(this.canvas == null)
      {
         return false;
      }

      if(this.thread == null)
      {
         this.thread = new Thread(this);
         this.thread.start();
      }

      return true;
   }

   /**
    * Unregister a scene renderer listener
    * 
    * @param sceneRendererListener
    *           Listener to unregister
    */
   public void unregisterJHelpSceneRendererListener(final JHelpSceneRendererListener sceneRendererListener)
   {
      this.sceneListeners.remove(sceneRendererListener);
   }

   /**
    * Unregister a window material to refresh
    * 
    * @param windowMaterial
    *           Window material to remove
    */
   public void unregisterWindowMaterial(final WindowMaterial windowMaterial)
   {
      if(windowMaterial == null)
      {
         throw new NullPointerException("windowMaterial musn't be null");
      }

      this.windowMaterials.removeElement(windowMaterial);
   }
}