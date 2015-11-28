//package jhelp.engine.gui;
//
//import java.awt.Dimension;
//import java.awt.event.MouseEvent;
//import java.awt.event.MouseWheelEvent;
//import java.util.ArrayList;
//
//import jhelp.engine.Point2D;
//import jhelp.engine.Point3D;
//import jhelp.engine.twoD.Line2D;
//import jhelp.engine.twoD.Path;
//import jhelp.engine.twoD.Path.Couple;
//import jhelp.engine.twoD.PathElement;
//import jhelp.gui.JHelpMouseListener;
//import jhelp.gui.twoD.JHelpComponent2D;
//import jhelp.util.gui.JHelpImage;
//
///**
// * Label that show and edit a path with mouse
// *
// * @author JHelp
// */
//public class JHelpLabelPath
//      extends JHelpComponent2D
//{
//   /**
//    * Manager of user events
//    *
//    * @author JHelp
//    */
//   class EventManager
//         implements JHelpMouseListener
//   {
//      /**
//       * Create a new instance of EventManager
//       */
//      EventManager()
//      {
//      }
//
//      /**
//       * Update the mouse position
//       *
//       * @param e
//       *           Mouse event
//       */
//      private void updateMousePosition(final MouseEvent e)
//      {
//         JHelpLabelPath.this.mouseX = (((float) e.getX() - (float) JHelpLabelPath.SIZE_2 - JHelpLabelPath.ANCHOR_SIZE_2) / JHelpLabelPath.SIZE_2);
//         JHelpLabelPath.this.mouseY = (((-(float) e.getY() + JHelpLabelPath.SIZE_2) + JHelpLabelPath.ANCHOR_SIZE_2) / JHelpLabelPath.SIZE_2);
//      }
//
//      /**
//       * Called when mouse clicked
//       *
//       * @param e
//       *           Mouse event
//       */
//      @Override
//      public void mouseClicked(final MouseEvent e)
//      {
//         this.updateMousePosition(e);
//         if(JHelpLabelPath.this.selectedPath == null)
//         {
//            final ArrayList<Couple> list = JHelpLabelPath.this.path.obtainCoupleNear(JHelpLabelPath.this.mouseX, JHelpLabelPath.this.mouseY, JHelpLabelPath.MOUSE_PRECISION);
//            if(list.size() == 0)
//            {
//               return;
//            }
//
//            JHelpLabelPath.this.fromClick = true;
//            final Couple couple = list.get(0);
//            JHelpLabelPath.this.indexSelected = couple.index;
//            JHelpLabelPath.this.selectedPath = couple.pathElement;
//
//            JHelpLabelPath.this.updateImagePath();
//
//            return;
//         }
//
//         if(JHelpLabelPath.this.fromClick == true)
//         {
//            JHelpLabelPath.this.selectedPath = null;
//            JHelpLabelPath.this.indexSelected = -1;
//
//            JHelpLabelPath.this.updateImagePath();
//
//            return;
//         }
//
//         if(JHelpLabelPath.this.indexSelected == 0)
//         {
//            JHelpLabelPath.this.indexSelected = JHelpLabelPath.this.selectedPath.points.length - 1;
//
//            JHelpLabelPath.this.updateImagePath();
//
//            return;
//         }
//
//         if(JHelpLabelPath.this.indexSelected == (JHelpLabelPath.this.selectedPath.points.length - 1))
//         {
//            if(JHelpLabelPath.this.selectedPath.points.length == 2)
//            {
//               JHelpLabelPath.this.selectedPath = null;
//               JHelpLabelPath.this.indexSelected = -1;
//
//               JHelpLabelPath.this.updateImagePath();
//               return;
//            }
//
//            JHelpLabelPath.this.indexSelected = 1;
//
//            JHelpLabelPath.this.updateImagePath();
//            return;
//         }
//
//         if((JHelpLabelPath.this.indexSelected == 2) || (JHelpLabelPath.this.selectedPath.points.length == 3))
//         {
//            JHelpLabelPath.this.selectedPath = null;
//            JHelpLabelPath.this.indexSelected = -1;
//
//            JHelpLabelPath.this.updateImagePath();
//            return;
//         }
//
//         JHelpLabelPath.this.indexSelected = 2;
//
//         JHelpLabelPath.this.updateImagePath();
//      }
//
//      /**
//       * Called when mouse dragged
//       *
//       * @param e
//       *           Mouse event
//       */
//      @Override
//      public void mouseDragged(final MouseEvent e)
//      {
//         this.updateMousePosition(e);
//      }
//
//      /**
//       * Called when mouse entered
//       *
//       * @param e
//       *           Mouse event
//       */
//      @Override
//      public void mouseEntered(final MouseEvent e)
//      {
//         this.updateMousePosition(e);
//      }
//
//      /**
//       * Called when mouse exited
//       *
//       * @param e
//       *           Mouse event
//       */
//      @Override
//      public void mouseExited(final MouseEvent e)
//      {
//         this.updateMousePosition(e);
//      }
//
//      /**
//       * Called when mouse moved
//       *
//       * @param e
//       *           Mouse event
//       */
//      @Override
//      public void mouseMoved(final MouseEvent e)
//      {
//         this.updateMousePosition(e);
//
//         if(JHelpLabelPath.this.selectedPath == null)
//         {
//            return;
//         }
//
//         final ArrayList<Couple> list = JHelpLabelPath.this.path.obtainCoupleNear(JHelpLabelPath.this.mouseX, JHelpLabelPath.this.mouseY, JHelpLabelPath.MOUSE_PRECISION_2);
//         if(list.size() > 0)
//         {
//            final Couple couple = list.get(0);
//            final Point3D point3d = couple.pathElement.points[couple.index];
//            JHelpLabelPath.this.mouseX = point3d.x;
//            JHelpLabelPath.this.mouseY = point3d.y;
//         }
//
//         if(Math.abs(JHelpLabelPath.this.mouseX) <= JHelpLabelPath.MOUSE_PRECISION_2)
//         {
//            JHelpLabelPath.this.mouseX = 0;
//         }
//
//         if(Math.abs(JHelpLabelPath.this.mouseY) <= JHelpLabelPath.MOUSE_PRECISION_2)
//         {
//            JHelpLabelPath.this.mouseY = 0;
//         }
//
//         if(JHelpLabelPath.this.fromClick == true)
//         {
//            JHelpLabelPath.this.selectedPath.points[JHelpLabelPath.this.indexSelected].x = JHelpLabelPath.this.mouseX;
//            JHelpLabelPath.this.selectedPath.points[JHelpLabelPath.this.indexSelected].y = JHelpLabelPath.this.mouseY;
//            JHelpLabelPath.this.updateImagePath();
//            return;
//         }
//
//         if(JHelpLabelPath.this.indexSelected == 0)
//         {
//            for(final Point3D point3d : JHelpLabelPath.this.selectedPath.points)
//            {
//               point3d.x = JHelpLabelPath.this.mouseX;
//               point3d.y = JHelpLabelPath.this.mouseY;
//            }
//
//            JHelpLabelPath.this.updateImagePath();
//            return;
//         }
//
//         if(JHelpLabelPath.this.indexSelected == (JHelpLabelPath.this.selectedPath.points.length - 1))
//         {
//            final float x0 = JHelpLabelPath.this.selectedPath.points[0].x;
//            final float y0 = JHelpLabelPath.this.selectedPath.points[0].y;
//
//            JHelpLabelPath.this.selectedPath.points[JHelpLabelPath.this.indexSelected].x = JHelpLabelPath.this.mouseX;
//            JHelpLabelPath.this.selectedPath.points[JHelpLabelPath.this.indexSelected].y = JHelpLabelPath.this.mouseY;
//
//            if(JHelpLabelPath.this.selectedPath.points.length == 3)
//            {
//               JHelpLabelPath.this.selectedPath.points[1].x = (JHelpLabelPath.this.mouseX + x0) / 2f;
//               JHelpLabelPath.this.selectedPath.points[1].y = (JHelpLabelPath.this.mouseY + y0) / 2f;
//            }
//            else if(JHelpLabelPath.this.selectedPath.points.length == 4)
//            {
//               JHelpLabelPath.this.selectedPath.points[1].x = ((JHelpLabelPath.this.mouseX * 2f) + x0) / 3f;
//               JHelpLabelPath.this.selectedPath.points[1].y = ((JHelpLabelPath.this.mouseY * 2f) + y0) / 3f;
//
//               JHelpLabelPath.this.selectedPath.points[2].x = (JHelpLabelPath.this.mouseX + (x0 * 2f)) / 3f;
//               JHelpLabelPath.this.selectedPath.points[2].y = (JHelpLabelPath.this.mouseY + (y0 * 2f)) / 3f;
//            }
//
//            JHelpLabelPath.this.updateImagePath();
//            return;
//         }
//
//         JHelpLabelPath.this.selectedPath.points[JHelpLabelPath.this.indexSelected].x = JHelpLabelPath.this.mouseX;
//         JHelpLabelPath.this.selectedPath.points[JHelpLabelPath.this.indexSelected].y = JHelpLabelPath.this.mouseY;
//         JHelpLabelPath.this.updateImagePath();
//      }
//
//      /**
//       * Called when mouse pressed
//       *
//       * @param e
//       *           Mouse event
//       */
//      @Override
//      public void mousePressed(final MouseEvent e)
//      {
//         this.updateMousePosition(e);
//      }
//
//      /**
//       * Called when mouse released
//       *
//       * @param e
//       *           Mouse event
//       */
//      @Override
//      public void mouseReleased(final MouseEvent e)
//      {
//         this.updateMousePosition(e);
//      }
//
//      /**
//       * Called when mouse wheel moved
//       *
//       * @param e
//       *           Mouse event
//       */
//      @Override
//      public void mouseWheelMoved(final MouseWheelEvent e)
//      {
//         this.updateMousePosition(e);
//      }
//   }
//
//   /** Anchor size (width and height) */
//   private static final int   ANCHOR_SIZE       = 16;
//   /** Anchor size divide by 2 */
//   private static final int   ANCHOR_SIZE_2     = JHelpLabelPath.ANCHOR_SIZE >> 1;
//   /** Label size (width and height) */
//   private static final int   SIZE              = 512;
//   /** Label size divide by 2 */
//   private static final int   SIZE_2            = JHelpLabelPath.SIZE >> 1;
//   /** Mouse precision */
//   static final float         MOUSE_PRECISION   = (float) JHelpLabelPath.ANCHOR_SIZE / (float) JHelpLabelPath.SIZE;
//   /** Mouse precsion divide by 2 */
//   static final float         MOUSE_PRECISION_2 = JHelpLabelPath.MOUSE_PRECISION / 2f;
//   /** Default path precision */
//   public static final int    DEFAULT_PRECISION = 16;
//   /** Manage user events */
//   private final EventManager eventManager;
//   /** Image the draw path, anchors, slection */
//   private final JHelpImage   imagePath;
//   /** Current path precision */
//   private int                precision;
//   /** Indicates that last selection comes from click */
//   boolean                    fromClick;
//   /** Selected index point inside selected path element */
//   int                        indexSelected;
//   /** Current mouse X */
//   float                      mouseX;
//   /** Current mouse Y */
//   float                      mouseY;
//   /** Edited path */
//   Path                       path;
//   /** Selected path element */
//   PathElement                selectedPath;
//
//   /**
//    * Create a new instance of JHelpLabelPath
//    */
//   public JHelpLabelPath()
//   {
//      this.imagePath = new JHelpImage(JHelpLabelPath.SIZE, JHelpLabelPath.SIZE);
//      this.eventManager = new EventManager();
//
//      this.precision = JHelpLabelPath.DEFAULT_PRECISION;
//      this.selectedPath = null;
//      this.indexSelected = -1;
//      this.updateImagePath();
//
//      this.setMouseListener(this.eventManager);
//   }
//
//   /**
//    * Update the iage to draw
//    */
//   void updateImagePath()
//   {
//      synchronized(this.imagePath)
//      {
//         this.imagePath.startDrawMode();
//         this.imagePath.clear(0xFFFFFFFF);
//         this.imagePath.drawHorizontalLine(0, JHelpLabelPath.SIZE, JHelpLabelPath.SIZE_2, 0xFF000000);
//         this.imagePath.drawVerticalLine(JHelpLabelPath.SIZE_2, 0, JHelpLabelPath.SIZE, 0xFF000000);
//         if(this.path != null)
//         {
//            for(final Line2D line2d : this.path.computePath(this.precision))
//            {
//               this.imagePath.drawLine((int) (JHelpLabelPath.SIZE_2 + (JHelpLabelPath.SIZE_2 * line2d.pointStart.getX())), JHelpLabelPath.SIZE - (int) (JHelpLabelPath.SIZE_2 + (JHelpLabelPath.SIZE_2 * line2d.pointStart.getY())),
//                     (int) (JHelpLabelPath.SIZE_2 + (JHelpLabelPath.SIZE_2 * line2d.pointEnd.getX())), JHelpLabelPath.SIZE - (int) (JHelpLabelPath.SIZE_2 + (JHelpLabelPath.SIZE_2 * line2d.pointEnd.getY())), 0xFF0000FF);
//            }
//
//            int x, y;
//            for(final Point2D point2d : this.path.obtainControlPoints())
//            {
//               x = (int) (JHelpLabelPath.SIZE_2 + (JHelpLabelPath.SIZE_2 * point2d.getX()));
//               y = JHelpLabelPath.SIZE - (int) (JHelpLabelPath.SIZE_2 + (JHelpLabelPath.SIZE_2 * point2d.getY()));
//
//               this.imagePath.drawRectangle(x - JHelpLabelPath.ANCHOR_SIZE_2, y - JHelpLabelPath.ANCHOR_SIZE_2, JHelpLabelPath.ANCHOR_SIZE, JHelpLabelPath.ANCHOR_SIZE, 0xFFFF0000);
//            }
//
//            if(this.selectedPath != null)
//            {
//               final Point3D point3d = this.selectedPath.points[this.indexSelected];
//               x = (int) (JHelpLabelPath.SIZE_2 + (JHelpLabelPath.SIZE_2 * point3d.getX()));
//               y = JHelpLabelPath.SIZE - (int) (JHelpLabelPath.SIZE_2 + (JHelpLabelPath.SIZE_2 * point3d.getY()));
//               this.imagePath.drawRectangle(x - JHelpLabelPath.ANCHOR_SIZE_2, y - JHelpLabelPath.ANCHOR_SIZE_2, JHelpLabelPath.ANCHOR_SIZE, JHelpLabelPath.ANCHOR_SIZE, 0xFF00FF00);
//            }
//         }
//         this.imagePath.endDrawMode();
//      }
//   }
//
//   /**
//    * Compute label preferred size <br>
//    * <br>
//    * <b>Parent documentation:</b><br>
//    * {@inheritDoc}
//    *
//    * @param parentWidth
//    *           Parent width (-1 if unknow)
//    * @param parentHeight
//    *           Parent height (-1 if unknown)
//    * @return Preferred size
//    * @see jhelp.gui.twoD.JHelpComponent2D#computePreferredSize(int, int)
//    */
//   @Override
//   protected Dimension computePreferredSize(final int parentWidth, final int parentHeight)
//   {
//      return new Dimension(JHelpLabelPath.SIZE, JHelpLabelPath.SIZE);
//   }
//
//   /**
//    * Draw the label <br>
//    * <br>
//    * <b>Parent documentation:</b><br>
//    * {@inheritDoc}
//    *
//    * @param x
//    *           X location on image parent
//    * @param y
//    *           Y location on image parent
//    * @param parent
//    *           Image parent where draw
//    * @see jhelp.gui.twoD.JHelpComponent2D#paint(int, int, jhelp.util.gui.JHelpImage)
//    */
//   @Override
//   protected void paint(final int x, final int y, final JHelpImage parent)
//   {
//      parent.drawImage(x, y, this.imagePath);
//   }
//
//   /**
//    * Add a cubic element
//    */
//   public void addCubic()
//   {
//      this.path.appendCubic(new Point2D(this.mouseX, this.mouseY), new Point2D(this.mouseX, this.mouseY), new Point2D(this.mouseX, this.mouseY), new Point2D(this.mouseX, this.mouseY));
//      this.fromClick = false;
//      this.indexSelected = 0;
//      this.selectedPath = this.path.obtainPathElement(this.path.countPathElement() - 1);
//      this.updateImagePath();
//   }
//
//   /**
//    * Add a line element
//    */
//   public void addLine()
//   {
//      this.path.appendLine(new Point2D(this.mouseX, this.mouseY), new Point2D(this.mouseX, this.mouseY));
//      this.fromClick = false;
//      this.indexSelected = 0;
//      this.selectedPath = this.path.obtainPathElement(this.path.countPathElement() - 1);
//      this.updateImagePath();
//   }
//
//   /**
//    * Add a quadric element
//    */
//   public void addQuadric()
//   {
//      this.path.appendQuad(new Point2D(this.mouseX, this.mouseY), new Point2D(this.mouseX, this.mouseY), new Point2D(this.mouseX, this.mouseY));
//      this.fromClick = false;
//      this.indexSelected = 0;
//      this.selectedPath = this.path.obtainPathElement(this.path.countPathElement() - 1);
//      this.updateImagePath();
//   }
//
//   /**
//    * Current edited path
//    *
//    * @return Current edited path
//    */
//   public Path getPath()
//   {
//      return this.path;
//   }
//
//   /**
//    * Current precision
//    *
//    * @return Current precision
//    */
//   public int getPrecision()
//   {
//      return this.precision;
//   }
//
//   /**
//    * Remove current selected element
//    */
//   public void removeSelectedElement()
//   {
//      if(this.selectedPath == null)
//      {
//         return;
//      }
//
//      this.path.removeElement(this.selectedPath);
//      this.selectedPath = null;
//      this.indexSelected = -1;
//      this.updateImagePath();
//   }
//
//   /**
//    * Change/define the path
//    *
//    * @param path
//    *           New path
//    */
//   public void setPath(final Path path)
//   {
//      if(path == null)
//      {
//         throw new NullPointerException("path musn't be null");
//      }
//
//      this.selectedPath = null;
//      this.indexSelected = -1;
//      this.path = path;
//
//      this.updateImagePath();
//   }
//
//   /**
//    * Change/define the precision
//    *
//    * @param precision
//    *           New precision
//    */
//   public void setPrecision(final int precision)
//   {
//      if(this.precision == precision)
//      {
//         return;
//      }
//
//      this.precision = Math.max(precision, 2);
//
//      this.updateImagePath();
//   }
//}