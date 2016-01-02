/**
 * Project : JHelpEngine<br>
 * Package : jhelp.engine.gui.components<br>
 * Class : Dessktop3D<br>
 * Date : 26 juin 2010<br>
 * By JHelp
 */
package jhelp.engine.gui.components;

import jhelp.engine.JHelpSceneRenderer;
import jhelp.engine.Node;
import jhelp.engine.Scene;
import jhelp.engine.geom.Plane;
import jhelp.engine.gui.ComponentView3D;
import jhelp.engine.gui.events.InternalFrameListener;
import jhelp.util.text.UtilText;

import java.util.ArrayList;

/**
 * 3D view like a desktop, contains some {@link InternalFrame}<br>
 * <br>
 * Last modification : 26 juin 2010<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public class Desktop3D
      extends ComponentView3D
{
   /**
    * Manage mouse and internal frame events <br>
    * <br>
    * Last modification : 2 déc. 2010<br>
    * Version 0.0.0<br>
    * 
    * @author JHelp
    */
   private class EventManager
         implements MouseListener, MouseMotionListener, MouseWheelListener, InternalFrameListener
   {
      /** Indicates if mouse button left is down */
      private boolean left;
      /** Indicates if mouse button right is down */
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
       * Call when internal frame select
       * 
       * @param internalFrame
       *           Internal frame select
       * @see InternalFrameListener#internalFrameSelect(jhelp.engine.gui.components.InternalFrame)
       */
      @Override
      public void internalFrameSelect(final InternalFrame internalFrame)
      {
         Desktop3D.this.manipulatedNode = Desktop3D.this.scene.getFirstNode(internalFrame.objectName);
      }

      /**
       * Call when mouse click
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

         Desktop3D.this.sceneRenderer.setDetectPosition(e.getX(), e.getY(), this.left, this.right, false);
      }

      /**
       * Call when mouse drag
       * 
       * @param e
       *           Event description
       * @see MouseMotionListener#mouseDragged(MouseEvent)
       */
      @Override
      public void mouseDragged(final MouseEvent e)
      {
         if(Desktop3D.this.manipulatedNode != null)
         {
            final boolean left = SwingUtilities.isLeftMouseButton(e);
            final boolean right = SwingUtilities.isRightMouseButton(e);

            if((left == true) && (right == true))
            {
               Desktop3D.this.manipulatedNode.translate((e.getX() - this.x) * Desktop3D.FACTOR, (this.y - e.getY()) * Desktop3D.FACTOR, 0);
            }
            else if(left == true)
            {
               Desktop3D.this.manipulatedNode.rotateAngleY(e.getX() - this.x);
               Desktop3D.this.manipulatedNode.rotateAngleX(e.getY() - this.y);
            }
            else if(right == true)
            {
               Desktop3D.this.manipulatedNode.translate(0, 0, (e.getY() - this.y) * Desktop3D.FACTOR);
            }
         }

         this.x = e.getX();
         this.y = e.getY();

         Desktop3D.this.sceneRenderer.setDetectPosition(e.getX(), e.getY(), this.left, this.right, true);
      }

      /**
       * Call when mouse enter
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

         Desktop3D.this.sceneRenderer.setDetectPosition(e.getX(), e.getY(), false, false, false);
      }

      /**
       * Call when mouse exit
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

         Desktop3D.this.sceneRenderer.setDetectPosition(e.getX(), e.getY(), false, false, false);
      }

      /**
       * Call when mouse move
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

         Desktop3D.this.sceneRenderer.setDetectPosition(e.getX(), e.getY(), false, false, false);
      }

      /**
       * Call when mouse press
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

         Desktop3D.this.sceneRenderer.setDetectPosition(e.getX(), e.getY(), this.left, this.right, false);
      }

      /**
       * Call when mouse release
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

         Desktop3D.this.sceneRenderer.setDetectPosition(e.getX(), e.getY(), this.left, this.right, false);
      }

      /**
       * Call when mouse wheel move
       * 
       * @param e
       *           Event description
       * @see MouseWheelListener#mouseWheelMoved(MouseWheelEvent)
       */
      @Override
      public void mouseWheelMoved(final MouseWheelEvent e)
      {
         if(Desktop3D.this.manipulatedNode != null)
         {
            Desktop3D.this.manipulatedNode.translate(0, 0, (e.getWheelRotation()) * 0.1f);
         }

         this.x = e.getX();
         this.y = e.getY();
      }
   }

   /** Mouse factor */
   private static final float             FACTOR  = 0.01f;

   /** Next ID */
   private static int                     NEXT_ID = 0;

   /** Listener to events */
   private final EventManager             eventManager;
   /** Internal frame list */
   private final ArrayList<InternalFrame> internalFrameList;
   /** Last start placement */
   private float                          last;
   /** Current node manipulate */
   Node                                   manipulatedNode;
   /** Scene */
   Scene                                  scene;
   /** Scene renderer */
   JHelpSceneRenderer                     sceneRenderer;

   /**
    * Constructs Dessktop3D
    * 
    * @param width
    *           Width
    * @param height
    *           Heught
    */
   public Desktop3D(final int width, final int height)
   {
      super(width, height);

      this.internalFrameList = new ArrayList<InternalFrame>();

      this.sceneRenderer = this.getSceneRenderer();
      this.scene = this.sceneRenderer.getScene();

      this.scene.translate(0f, 0f, -2.3456789f);

      this.last = -0.1f;

      this.eventManager = new EventManager();

      this.sceneRenderer.addMouseListener(this.eventManager);
      this.sceneRenderer.addMouseMotionListener(this.eventManager);
      this.sceneRenderer.addMouseWheelListener(this.eventManager);
   }

   /**
    * Add internal frame
    * 
    * @param internalFrame
    *           Internal frame to add
    */
   public void addInternalFrame(final InternalFrame internalFrame)
   {
      this.internalFrameList.add(internalFrame);

      internalFrame.objectName = UtilText.concatenate("InternalFrame", Desktop3D.NEXT_ID++);
      final Plane plane = new Plane(false, true);
      plane.translate(this.last, this.last, 0);
      this.last += 0.1f;
      plane.nodeName = internalFrame.objectName;
      this.scene.add(plane);

      final WindowMaterial windowMaterial = new WindowMaterial(internalFrame.objectName, plane);
      windowMaterial.setMainComponent(internalFrame);
      this.sceneRenderer.registerWindowMaterial(windowMaterial);

      this.manipulatedNode = plane;

      internalFrame.addInternalFrameListener(this.eventManager);
   }
}