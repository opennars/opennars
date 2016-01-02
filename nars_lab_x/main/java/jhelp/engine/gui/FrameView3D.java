/**
 * Project : JHelpEngine<br>
 * Package : jhelp.gui<br>
 * Class : FrameView3D<br>
 * Date : 26 juil. 2009<br>
 * By JHelp
 */
package jhelp.engine.gui;

import jhelp.engine.JHelpSceneRenderer;
import jhelp.engine.Node;
import jhelp.engine.Scene;
import jhelp.engine.gui.events.Frame3DViewListener;
import jhelp.engine.util.PositionNode;
import jhelp.util.MemorySweeper;
import jhelp.util.debug.Debug;
import jhelp.util.debug.DebugLevel;
import jhelp.util.gui.UtilGUI;

import java.util.ArrayList;

/**
 * Frame with a single 3D view in it<br>
 * <br>
 * Last modification : 26 juil. 2009<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public class FrameView3D
      extends JFrame
{
   /**
    * Treat all user events <br>
    * <br>
    * Last modification : 30 nov. 2010<br>
    * Version 0.0.0<br>
    * 
    * @author JHelp
    */
   private class EventManager
         implements MouseListener, MouseMotionListener, MouseWheelListener
   {
      /** Indicates if mouse left button is down */
      private boolean left;
      /** Indicates if mouse right button is down */
      private boolean right;
      /** Mouse X */
      private int     x;
      /** Mouse Y */
      private int     y;

      /**
       * Constructs EventManager
       */
      EventManager()
      {
         this.left = this.right = false;
      }

      /**
       * Call when mouse clicked
       * 
       * @param e
       *           Event description
       * @see MouseListener#mouseClicked(MouseEvent)
       */
      @Override
      public void mouseClicked(final MouseEvent e)
      {
         this.x = e.getX();
         this.y = e.getY();

         if(e.isAltDown() == true)
         {
            Debug.println(DebugLevel.INFORMATION, new PositionNode(FrameView3D.this.manipulatedNode));
         }

         FrameView3D.this.componentView3D.getSceneRenderer().setDetectPosition(e.getX(), e.getY(), this.left, this.right, false);
      }

      /**
       * Call when mouse dragged
       * 
       * @param e
       *           Event description
       * @see MouseMotionListener#mouseDragged(MouseEvent)
       */
      @Override
      public void mouseDragged(final MouseEvent e)
      {
         if(FrameView3D.this.manipulatedNode != null)
         {
            final boolean left = SwingUtilities.isLeftMouseButton(e);
            final boolean right = SwingUtilities.isRightMouseButton(e);

            if((left == true) && (right == true))
            {
               FrameView3D.this.manipulatedNode.translate((e.getX() - this.x) * 0.1f, (this.y - e.getY()) * 0.1f, 0);
            }
            else if(left == true)
            {
               FrameView3D.this.manipulatedNode.rotateAngleY(e.getX() - this.x);
               FrameView3D.this.manipulatedNode.rotateAngleX(e.getY() - this.y);
            }
            else if(right == true)
            {
               FrameView3D.this.manipulatedNode.translate(0, 0, (e.getY() - this.y) * FrameView3D.FACTOR);
            }
         }

         this.x = e.getX();
         this.y = e.getY();

         FrameView3D.this.componentView3D.getSceneRenderer().setDetectPosition(e.getX(), e.getY(), this.left, this.right, true);
      }

      /**
       * Call when mouse entered
       * 
       * @param e
       *           Event description
       * @see MouseListener#mouseEntered(MouseEvent)
       */
      @Override
      public void mouseEntered(final MouseEvent e)
      {
         this.x = e.getX();
         this.y = e.getY();

         FrameView3D.this.componentView3D.getSceneRenderer().setDetectPosition(e.getX(), e.getY(), false, false, false);
      }

      /**
       * Call when mouse exited
       * 
       * @param e
       *           Event description
       * @see MouseListener#mouseExited(MouseEvent)
       */
      @Override
      public void mouseExited(final MouseEvent e)
      {
         this.x = e.getX();
         this.y = e.getY();

         FrameView3D.this.componentView3D.getSceneRenderer().setDetectPosition(e.getX(), e.getY(), false, false, false);
      }

      /**
       * Call when mouse moved
       * 
       * @param e
       *           Event description
       * @see MouseMotionListener#mouseMoved(MouseEvent)
       */
      @Override
      public void mouseMoved(final MouseEvent e)
      {
         this.x = e.getX();
         this.y = e.getY();

         FrameView3D.this.componentView3D.getSceneRenderer().setDetectPosition(e.getX(), e.getY(), false, false, false);
      }

      /**
       * Call when mouse pressed
       * 
       * @param e
       *           Event description
       * @see MouseListener#mousePressed(MouseEvent)
       */
      @Override
      public void mousePressed(final MouseEvent e)
      {
         this.x = e.getX();
         this.y = e.getY();

         this.left |= SwingUtilities.isLeftMouseButton(e);
         this.right |= SwingUtilities.isRightMouseButton(e);

         FrameView3D.this.componentView3D.getSceneRenderer().setDetectPosition(e.getX(), e.getY(), this.left, this.right, false);
      }

      /**
       * Call when mouse released
       * 
       * @param e
       *           Event description
       * @see MouseListener#mouseReleased(MouseEvent)
       */
      @Override
      public void mouseReleased(final MouseEvent e)
      {
         this.x = e.getX();
         this.y = e.getY();

         this.left &= !SwingUtilities.isLeftMouseButton(e);
         this.right &= !SwingUtilities.isRightMouseButton(e);

         FrameView3D.this.componentView3D.getSceneRenderer().setDetectPosition(e.getX(), e.getY(), this.left, this.right, false);
      }

      /**
       * Call when mouse wheel moved
       * 
       * @param e
       *           Event description
       * @see MouseWheelListener#mouseWheelMoved(MouseWheelEvent)
       */
      @Override
      public void mouseWheelMoved(final MouseWheelEvent e)
      {
         if(FrameView3D.this.manipulatedNode != null)
         {
            FrameView3D.this.manipulatedNode.translate(0, 0, (e.getWheelRotation()) * 0.1f);
         }

         this.x = e.getX();
         this.y = e.getY();
      }
   }

   /** Default manipulation factor */
   private static final float                   FACTOR = 0.01f;

   /** Manage user events */
   private final EventManager                   eventManager;
   /** Listeners of events */
   private final ArrayList<Frame3DViewListener> listeners;
   /** Component that carray the 3D */
   final ComponentView3D                              componentView3D;
   /** Actual node manipulate by the user */
   Node                                         manipulatedNode;

   /**
    * Constructs FrameView3D
    */
   public FrameView3D()
   {
      this("3D");
   }

   /**
    * Constructs FrameView3D
    * 
    * @param width
    *           3D area width
    * @param height
    *           3D area height
    */
   public FrameView3D(final int width, final int height)
   {
      this("3D", width, height);
   }

   /**
    * Constructs FrameView3D
    * 
    * @param title
    *           Title to use
    */
   public FrameView3D(final String title)
   {
      super(title);

      MemorySweeper.launch();

      this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
      this.setUndecorated(true);

      UtilGUI.takeAllScreen(this);

      this.eventManager = new EventManager();
      this.listeners = new ArrayList<Frame3DViewListener>();

      this.componentView3D = new ComponentView3D(this.getWidth(), this.getHeight());
      this.componentView3D.addMouseListener(this.eventManager);
      this.componentView3D.addMouseMotionListener(this.eventManager);
      this.componentView3D.addMouseWheelListener(this.eventManager);

      this.setLayout(new BorderLayout());
      this.add(this.componentView3D, BorderLayout.CENTER);
   }

   /**
    * Constructs FrameView3D
    * 
    * @param title
    *           Frame title
    * @param width
    *           3D area width
    * @param height
    *           3D area height
    */
   public FrameView3D(final String title, final int width, final int height)
   {
      super(title);

      //MemorySweeper.launch();

      this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

      this.eventManager = new EventManager();
      this.listeners = new ArrayList<Frame3DViewListener>();

      this.componentView3D = new ComponentView3D(this.getWidth(), this.getHeight());
      this.componentView3D.addMouseListener(this.eventManager);
      this.componentView3D.addMouseMotionListener(this.eventManager);
      this.componentView3D.addMouseWheelListener(this.eventManager);

      setLayout(new BorderLayout());
      add(this.componentView3D, BorderLayout.CENTER);

      pack();

      UtilGUI.centerOnScreen(this);
   }

   /**
    * Called when frame is about to close.<br>
    * Override it to not allow to close without a user confirmation, by example to say him that something is not saved.<br>
    * By default closing frame is always allowed
    * 
    * @return {@code true} to allow close frame.
    */
   protected boolean canCloseNow()
   {
      return true;
   }

   /**
    * Alert listeners that frame will go on exit
    */
   protected void fireFrame3DExit()
   {
      for(final Frame3DViewListener listener : this.listeners)
      {
         listener.frame3DExit(this);
      }
   }

   /**
    * Call on widow event append
    * 
    * @param e
    *           Event description
    * @see JFrame#processWindowEvent(WindowEvent)
    */
   @Override
   protected void processWindowEvent(final WindowEvent e)
   {
      switch(e.getID())
      {
         case WindowEvent.WINDOW_CLOSED:
         case WindowEvent.WINDOW_CLOSING:
            this.closeFrame();
         break;
      }
   }

   /**
    * Add frame listener
    * 
    * @param listener
    *           Listener to add
    */
   public void addFrame3DViewListener(final Frame3DViewListener listener)
   {
      this.listeners.add(listener);
   }

   /**
    * Close the frame
    */
   public void closeFrame()
   {
      if(this.canCloseNow() == false)
      {
         return;
      }

      this.fireFrame3DExit();

      this.componentView3D.getSceneRenderer().stop();
      this.setVisible(false);
      this.dispose();

      MemorySweeper.exit(0);
   }

   /**
    * Disable all default manipulation
    */
   public void disableManipulation()
   {
      this.manipulatedNode = null;
   }

   /**
    * Component that carry the 3D
    * 
    * @return Component that carry the 3D
    */
   public ComponentView3D getComponentView3D()
   {
      return this.componentView3D;
   }

   /**
    * Return manipulatedNode
    * 
    * @return manipulatedNode
    */
   public Node getManipulatedNode()
   {
      return this.manipulatedNode;
   }

   /**
    * Scene draw
    * 
    * @return Scene draw
    */
   public Scene getScene()
   {
      return this.componentView3D.getSceneRenderer().getScene();
   }

   /**
    * Scene height
    * 
    * @return Scene height
    */
   public int getSceneHeight()
   {
      return this.componentView3D.getHeight();
   }

   /**
    * Scene renderer link to the 3D view
    * 
    * @return Scene renderer
    */
   public JHelpSceneRenderer getSceneRenderer()
   {
      return this.componentView3D.getSceneRenderer();
   }

   /**
    * Scene width
    * 
    * @return Scene width
    */
   public int getSceneWidth()
   {
      return this.componentView3D.getWidth();
   }

   /**
    * Manipulation go to the all scene, that is to say the scene root
    */
   public void manipulateAllScene()
   {
      this.manipulatedNode = this.componentView3D.getSceneRenderer().getScene().getRoot();
   }

   /**
    * Remove frame listener
    * 
    * @param listener
    *           Listener to remove
    */
   public void remove(final Frame3DViewListener listener)
   {
      this.listeners.remove(listener);
   }

   /**
    * Modify manipulatedNode
    * 
    * @param manipulatedNode
    *           New manipulatedNode value
    */
   public void setManipulatedNode(final Node manipulatedNode)
   {
      this.manipulatedNode = manipulatedNode;
   }

   /**
    * Show the frame
    */
   public void showFrame()
   {
      this.setVisible(true);

      this.doLayout();
      this.repaint();
   }
}