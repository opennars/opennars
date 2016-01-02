/**
 */
package jhelp.engine.gui;

import com.jogamp.opengl.awt.GLCanvas;
import jhelp.engine.JHelpSceneRenderer;
import jhelp.engine.Node;
import jhelp.engine.Scene;
import jhelp.engine.event.NodeListener;
import jhelp.engine.twoD.Object2D;
import jhelp.engine.util.CanvasOpenGLMaker;
import jhelp.util.MemorySweeper;
import jhelp.util.debug.Debug;
import jhelp.util.debug.DebugLevel;
import jhelp.util.gui.JHelpImage;

import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.Stack;

/**
 * Frame with 3D inside<br>
 * <br>
 * 
 * @author JHelp
 */
public class JHelpFrame3D
      extends JFrame
      implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener, NodeListener
{
   /** Default title */
   private static final String      JHELP_FRAME_DEAULT_TITLE = "JHelp Frame".intern();
   /** Canvas for 3D */
   private final GLCanvas canvas;
   /** Indicates if use the default move */
   private boolean                  defaultMouvment;
   /** Indicates if events are enable */
   private boolean                  enableEvents;
   /** Mouse x */
   private int                      mouseX;
   /** Mouse y */
   private int                      mouseY;
   /** Robot */
   private Robot                    robot;
   /** Scene renderer */
   private final JHelpSceneRenderer sceneRenderer;

   /**
    * Constructs the frame
    * 
    * @throws HeadlessException
    *            On construction problem
    */
   public JHelpFrame3D()
         throws HeadlessException
   {
      this(JHelpFrame3D.JHELP_FRAME_DEAULT_TITLE);
   }

   /**
    * Constructs the frame
    * 
    * @param resizable
    *           Indicates if the frame is resizable
    * @param undecrored
    *           Indicates if the frame is undecrored
    * @throws HeadlessException
    *            On construction problem
    */
   public JHelpFrame3D(final boolean resizable, final boolean undecrored)
         throws HeadlessException
   {
      this(resizable, JHelpFrame3D.JHELP_FRAME_DEAULT_TITLE, undecrored);
   }

   /**
    * Constructs the frame
    * 
    * @param resizable
    *           Indicates if the frame is resizable
    * @param title
    *           Title
    * @throws HeadlessException
    *            On construction problem
    */
   public JHelpFrame3D(final boolean resizable, final String title)
         throws HeadlessException
   {
      this(resizable, title, false);
   }

   /**
    * Constructs the frame
    * 
    * @param resizable
    *           Indicates if the frame is resizable
    * @param title
    *           Title
    * @param undecrored
    *           Indicates if the frame is undecrored
    * @throws HeadlessException
    *            On construction problem
    */
   public JHelpFrame3D(final boolean resizable, final String title, final boolean undecrored)
         throws HeadlessException
   {
      this(1000, 750, resizable, title, undecrored);
   }

   /**
    * Constructs the frame
    * 
    * @param width
    *           Width
    * @param height
    *           Height
    * @throws HeadlessException
    *            On construction problem
    */
   public JHelpFrame3D(final int width, final int height)
         throws HeadlessException
   {
      this(width, height, false);
   }

   /**
    * Constructs the frame
    * 
    * @param width
    *           Width
    * @param height
    *           Height
    * @param resizable
    *           Indicates if the frame is resizable
    * @throws HeadlessException
    *            On construction problem
    */
   public JHelpFrame3D(final int width, final int height, final boolean resizable)
         throws HeadlessException
   {
      this(width, height, resizable, false);
   }

   /**
    * Constructs the frame
    * 
    * @param width
    *           Width
    * @param height
    *           Height
    * @param resizable
    *           Indicates if the frame is resizable
    * @param undecrored
    *           Indicates if the frame is undecrored
    * @throws HeadlessException
    *            On construction problem
    */
   public JHelpFrame3D(final int width, final int height, final boolean resizable, final boolean undecrored)
         throws HeadlessException
   {
      this(width, height, resizable, JHelpFrame3D.JHELP_FRAME_DEAULT_TITLE, undecrored);
   }

   /**
    * Constructs the frame
    * 
    * @param width
    *           Width
    * @param height
    *           Height
    * @param resizable
    *           Indicates if the frame is resizable
    * @param title
    *           Title
    * @throws HeadlessException
    *            On construction problem
    */
   public JHelpFrame3D(final int width, final int height, final boolean resizable, final String title)
         throws HeadlessException
   {
      this(width, height, resizable, title, false);
   }

   /**
    * Constructs the frame
    * 
    * @param width
    *           Width
    * @param height
    *           Height
    * @param resizable
    *           Indicates if the frame is resizable
    * @param title
    *           Title
    * @param undecrored
    *           Indicates if the frame is undecrored
    * @throws HeadlessException
    *            On construction problem
    */
   public JHelpFrame3D(final int width, final int height, final boolean resizable, final String title, final boolean undecrored)
         throws HeadlessException
   {
      if(title != null)
      {
         this.setTitle(title);
      }

      //MemorySweeper.launch();
      this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

      this.defaultMouvment = true;
      this.enableEvents = true;
      this.setUndecorated(undecrored);
      this.setResizable(resizable);
      this.sceneRenderer = new JHelpSceneRenderer();
      this.sceneRenderer.addMouseListener(this);
      this.sceneRenderer.addMouseMotionListener(this);
      this.sceneRenderer.addMouseWheelListener(this);
      this.sceneRenderer.addKeyListener(this);

      this.canvas = CanvasOpenGLMaker.CANVAS_OPENGL_MAKER.newGLCanvas();
      this.canvas.setAutoSwapBufferMode(false);
      this.canvas.addGLEventListener(this.sceneRenderer);

      final Dimension dimension = new Dimension(width, height);
      setSize(dimension);
      this.canvas.setSize(dimension);
      this.canvas.setPreferredSize(dimension);
      this.canvas.setMaximumSize(dimension);
      this.canvas.setMinimumSize(dimension);
      this.setLayout(new BorderLayout());
      this.add(this.canvas, BorderLayout.CENTER);
      this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      this.pack();
      SwingUtilities.invokeLater(() -> this.sceneRenderer.start(this.canvas));

   }

   /**
    * Constructs the frame
    * 
    * @param width
    *           Width
    * @param height
    *           Height
    * @param title
    *           Title
    * @throws HeadlessException
    *            On construction problem
    */
   public JHelpFrame3D(final int width, final int height, final String title)
         throws HeadlessException
   {
      this(width, height, false, title);
   }

   /**
    * Constructs the frame
    * 
    * @param width
    *           Width
    * @param height
    *           Height
    * @param title
    *           Title
    * @param undecrored
    *           Indicates if the frame is undecrored
    * @throws HeadlessException
    *            On construction problem
    */
   public JHelpFrame3D(final int width, final int height, final String title, final boolean undecrored)
         throws HeadlessException
   {
      this(width, height, false, title, undecrored);
   }

   /**
    * Constructs the frame
    * 
    * @param title
    *           Title
    * @throws HeadlessException
    *            On construction problem
    */
   public JHelpFrame3D(final String title)
         throws HeadlessException
   {
      this(false, title);
   }

   /**
    * Constructs the frame
    * 
    * @param title
    *           Title
    * @param undecrored
    *           Indicates if the frame is undecrored
    * @throws HeadlessException
    *            On construction problem
    */
   public JHelpFrame3D(final String title, final boolean undecrored)
         throws HeadlessException
   {
      this(false, title, undecrored);
   }

   /**
    * Make a screen shot with a robot
    */
   private void robot()
   {
      final Thread thread = new Thread()
      {
         @Override
         public void run()
         {
            JHelpFrame3D.this.runRobot();
         }
      };
      thread.start();
   }

   /**
    * Make a screen shot
    */
   private void screenShot()
   {
      final Thread thread = new Thread()
      {
         @Override
         public void run()
         {
            JHelpFrame3D.this.runScreenShot();
         }
      };
      thread.start();
   }

   /**
    * Mouse x
    * 
    * @return Mouse x
    */
   int getMouseX()
   {
      return this.mouseX;
   }

   /**
    * Mouse y
    * 
    * @return Mouse y
    */
   int getMouseY()
   {
      return this.mouseY;
   }

   /**
    * The robot capture
    */
   void runRobot()
   {
      if(this.robot == null)
      {
         try
         {
            this.robot = new Robot();
         }
         catch(final AWTException e)
         {
            this.robot = null;
         }
      }
      if(this.robot != null)
      {
         final BufferedImage screenShot = this.robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
         final JFrame frame = new JFrame();
         frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
         frame.setLayout(new BorderLayout());
         //frame.add(new LabelBufferedImage(screenShot), BorderLayout.CENTER);
         frame.pack();
         frame.setVisible(true);
      }
   }

   /**
    * The screen shot
    */
   void runScreenShot()
   {
      final JHelpImage screenShot = this.sceneRenderer.screenShot();
      final JFrame frame = new JFrame();
      frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
      frame.setLayout(new BorderLayout());
      //frame.add(new LabelJHelpImage(screenShot), BorderLayout.CENTER);
      frame.pack();
      frame.setVisible(true);
   }

   /**
    * Call at each window event <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param e
    *           Event description
    * @see JFrame#processWindowEvent(WindowEvent)
    */
   @Override
   protected final void processWindowEvent(final WindowEvent e)
   {
      switch(e.getID())
      {
         case WindowEvent.WINDOW_CLOSED:
         case WindowEvent.WINDOW_CLOSING:
            this.closeFrame();
         break;
         default:
            super.processWindowEvent(e);
         break;
      }
   }

   /**
    * Add mouse trace to the actual scene
    */
   public void addMouseTrace()
   {
      final Scene scene = this.sceneRenderer.getScene();
      final Stack<Node> stack = new Stack<Node>();
      stack.push(scene.getRoot());
      while(stack.isEmpty() == false)
      {
         final Node node = stack.pop();
         node.addNodeListener(this);
         for(final Iterator<Node> children = node.getChildren(); children.hasNext(); stack.push(children.next()))
         {
            ;
         }
      }
   }

   /**
    * Close the current frame
    */
   public final void closeFrame()
   {
      this.setVisible(false);
      this.dispose();
      MemorySweeper.exit(0);
   }

   /**
    * Scene renderer
    * 
    * @return Scene renderer
    */
   public JHelpSceneRenderer getSceneRenderer()
   {
      return this.sceneRenderer;
   }

   /**
    * Indicates if default move is used
    * 
    * @return {@code true} if default move is used
    */
   public boolean isDefaultMouvment()
   {
      return this.defaultMouvment;
   }

   /**
    * Indicates if the events are enable
    * 
    * @return {@code true} if the events are enable
    */
   public boolean isEnableEvents()
   {
      return this.enableEvents;
   }

   /**
    * Action on key pressed
    * 
    * @param e
    *           Event description
    * @see KeyListener#keyPressed(KeyEvent)
    */
   @Override
   public void keyPressed(final KeyEvent e)
   {
      if(this.defaultMouvment == true)
      {
         if((e.isAltDown() == false) || (e.isShiftDown() == false) || (e.isControlDown() == false))
         {
            return;
         }
         final Scene scene = this.sceneRenderer.getScene();
         final Node root = scene.getRoot();
         switch(e.getKeyCode())
         {
            case KeyEvent.VK_SPACE:
               this.screenShot();
            break;
            case KeyEvent.VK_R:
               this.robot();
            break;
            case KeyEvent.VK_S:
               this.sceneRenderer.setShowFPS(true);
            break;
            case KeyEvent.VK_H:
               this.sceneRenderer.setShowFPS(false);
            break;
            case KeyEvent.VK_P:
               Debug.println(DebugLevel.INFORMATION, "(" + root.getX() + "f, " + root.getY() + "f, " + root.getZ() + "f) | AngleX=" + root.getAngleX() + "f | AngleY=" + root.getAngleY() + "f | AngleZ=" + root.getAngleZ() + "f");
            break;
         }
      }
   }

   /**
    * Action on key released
    * 
    * @param e
    *           Event description
    * @see KeyListener#keyReleased(KeyEvent)
    */
   @Override
   public void keyReleased(final KeyEvent e)
   {
   }

   /**
    * Action on key typed
    * 
    * @param e
    *           Event description
    * @see KeyListener#keyTyped(KeyEvent)
    */
   @Override
   public void keyTyped(final KeyEvent e)
   {
   }

   /**
    * Action on click on a node
    * 
    * @param node
    *           Node click
    * @param leftButton
    *           Indicates if left button is down
    * @param rightButton
    *           Indicates if right button is down
    * @see jhelp.engine.event.NodeListener#mouseClick(jhelp.engine.Node, boolean, boolean)
    */
   @Override
   public void mouseClick(final Node node, final boolean leftButton, final boolean rightButton)
   {
      Debug.println(DebugLevel.INFORMATION, "JHelpFrame.mouseClick() : node=" + node + " leftButton=" + leftButton + " rightButton=" + rightButton);
   }

   /**
    * Action on mouse clicked
    * 
    * @param e
    *           Event description
    * @see MouseListener#mouseClicked(MouseEvent)
    */
   @Override
   public void mouseClicked(final MouseEvent e)
   {
      this.mouseX = e.getX();
      this.mouseY = e.getY();

      if(this.defaultMouvment || this.enableEvents)
      {
         this.sceneRenderer.setDetectPosition(this.mouseX, this.mouseY, false, false, false);
      }
   }

   /**
    * Action on mouse dragged
    * 
    * @param e
    *           Event description
    * @see MouseMotionListener#mouseDragged(MouseEvent)
    */
   @Override
   public void mouseDragged(final MouseEvent e)
   {
      final int dx = e.getX() - this.mouseX;
      final int dy = e.getY() - this.mouseY;
      final boolean left = SwingUtilities.isLeftMouseButton(e);
      final boolean right = SwingUtilities.isRightMouseButton(e);
      if(this.defaultMouvment)
      {
         float factor = 0.01f;
         if(e.isShiftDown() == true)
         {
            factor = 0.1f;

            if(e.isControlDown() == true)
            {
               factor = 1f;
            }
         }
         else if(e.isAltDown() == true)
         {
            factor = 0.001f;

            if(e.isControlDown() == true)
            {
               factor = 0.0001f;
            }
         }

         final Scene scene = this.sceneRenderer.getScene();
         if((left == true) && (right == true))
         {
            scene.translate(dx * factor, -dy * factor, 0);
         }
         else if(left == true)
         {
            scene.rotateAngleX(dy);
            scene.rotateAngleY(dx);
         }
         else if(right == true)
         {
            scene.translate(0, 0, -dy * factor);
         }
      }
      this.mouseX = e.getX();
      this.mouseY = e.getY();
      if(this.defaultMouvment || this.enableEvents)
      {
         this.sceneRenderer.setDetectPosition(this.mouseX, this.mouseY, left, right, true);
      }
   }

   /**
    * Action on mouse enter on a node
    * 
    * @param node
    *           Node enter
    * @see jhelp.engine.event.NodeListener#mouseEnter(jhelp.engine.Node)
    */
   @Override
   public void mouseEnter(final Node node)
   {
      Debug.println(DebugLevel.INFORMATION, "JHelpFrame.mouseEnter() : node=" + node);
   }

   /**
    * Action on mouse entered
    * 
    * @param e
    *           Event description
    * @see MouseListener#mouseEntered(MouseEvent)
    */
   @Override
   public void mouseEntered(final MouseEvent e)
   {
      this.mouseX = e.getX();
      this.mouseY = e.getY();
      if(this.defaultMouvment || this.enableEvents)
      {
         this.sceneRenderer.setDetectPosition(this.mouseX, this.mouseY, false, false, false);
      }
      // if(this.enableEvents)
      // {
      // Object2D over2D = this.sceneRenderer.getObject2DDetect();
      // this.sceneRenderer.getGui2d().mouseState(this.mouseX, this.mouseY,
      // false, false, false,
      // over2D);
      // Node over3D = null;
      // if(over2D == null)
      // {
      // over3D = this.sceneRenderer.getNodeDetect();
      // }
      // this.sceneRenderer.getScene().mouseState(false, false, over3D);
      // }
   }

   /**
    * Action on mouse exit on a node
    * 
    * @param node
    *           Node exit
    * @see jhelp.engine.event.NodeListener#mouseExit(jhelp.engine.Node)
    */
   @Override
   public void mouseExit(final Node node)
   {
      Debug.println(DebugLevel.INFORMATION, "JHelpFrame.mouseExit() : node=" + node);
   }

   /**
    * Action on mouse exited
    * 
    * @param e
    *           Event description
    * @see MouseListener#mouseExited(MouseEvent)
    */
   @Override
   public void mouseExited(final MouseEvent e)
   {
      this.mouseX = e.getX();
      this.mouseY = e.getY();
      if(this.defaultMouvment || this.enableEvents)
      {
         this.sceneRenderer.setDetectPosition(this.mouseX, this.mouseY, false, false, false);
      }
      // if(this.enableEvents)
      // {
      // Object2D over2D = this.sceneRenderer.getObject2DDetect();
      // this.sceneRenderer.getGui2d().mouseState(this.mouseX, this.mouseY,
      // false, false, false,
      // over2D);
      // Node over3D = null;
      // if(over2D == null)
      // {
      // over3D = this.sceneRenderer.getNodeDetect();
      // }
      // this.sceneRenderer.getScene().mouseState(false, false, over3D);
      // }
   }

   /**
    * Action on mouse moved
    * 
    * @param e
    *           Event description
    * @see MouseMotionListener#mouseMoved(MouseEvent)
    */
   @Override
   public void mouseMoved(final MouseEvent e)
   {
      this.mouseX = e.getX();
      this.mouseY = e.getY();
      if(this.defaultMouvment || this.enableEvents)
      {
         this.sceneRenderer.setDetectPosition(this.mouseX, this.mouseY, false, false, false);
      }
      if(this.defaultMouvment)
      {
         final Node node = this.sceneRenderer.getNodeDetect();
         final Object2D object2D = this.sceneRenderer.getObject2DDetect();
         if((node != null) || (object2D != null))
         {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
         }
         else
         {
            this.setCursor(Cursor.getDefaultCursor());
         }
      }
   }

   /**
    * Action on mouse pressed
    * 
    * @param e
    *           Event description
    * @see MouseListener#mousePressed(MouseEvent)
    */
   @Override
   public void mousePressed(final MouseEvent e)
   {
      this.mouseX = e.getX();
      this.mouseY = e.getY();
      final boolean left = SwingUtilities.isLeftMouseButton(e);
      final boolean right = SwingUtilities.isRightMouseButton(e);

      if(this.defaultMouvment || this.enableEvents)
      {
         this.sceneRenderer.setDetectPosition(this.mouseX, this.mouseY, left, right, false);
      }
   }

   /**
    * Action on mouse released
    * 
    * @param e
    *           Event description
    * @see MouseListener#mouseReleased(MouseEvent)
    */
   @Override
   public void mouseReleased(final MouseEvent e)
   {
      this.mouseX = e.getX();
      this.mouseY = e.getY();
      if(this.defaultMouvment || this.enableEvents)
      {
         this.sceneRenderer.setDetectPosition(this.mouseX, this.mouseY, false, false, false);
      }
   }

   /**
    * Action on mouse wheel moved
    * 
    * @param e
    *           Event description
    * @see MouseWheelListener#mouseWheelMoved(MouseWheelEvent)
    */
   @Override
   public void mouseWheelMoved(final MouseWheelEvent e)
   {
      if(this.defaultMouvment)
      {
         final Scene scene = this.sceneRenderer.getScene();
         scene.translate(0, 0, -e.getWheelRotation() * 0.1f);
      }
   }

   /**
    * Change the cursor
    * 
    * @param cursor
    *           New cursor
    * @see java.awt.Window#setCursor(Cursor)
    */
   @Override
   public void setCursor(final Cursor cursor)
   {
      this.canvas.setCursor(cursor);
      super.setCursor(cursor);
   }

   /**
    * Change the default movement state
    * 
    * @param defaultMouvment
    *           New default movement state
    */
   public void setDefaultMouvment(final boolean defaultMouvment)
   {
      this.defaultMouvment = defaultMouvment;
   }

   /**
    * Change the enable event state
    * 
    * @param enableEvents
    *           New enable event state
    */
   public void setEnableEvents(final boolean enableEvents)
   {
      this.enableEvents = enableEvents;
   }
}