//package jhelp.engine.tools.ui;
//
//import javafx.stage.FileChooser;
//import jhelp.engine.event.SensitiveArea;
//import jhelp.engine.event.SensitiveArea.Area;
//import jhelp.engine.event.SensitiveArea.Position;
//import jhelp.util.debug.Debug;
//import jhelp.util.filter.FileFilter;
//import jhelp.util.gui.JHelpImage;
//import jhelp.util.list.Pair;
//import jhelp.util.math.random.JHelpRandom;
//
//import javax.swing.*;
//import java.awt.*;
//import java.awt.event.ActionEvent;
//import java.awt.event.MouseEvent;
//import java.awt.event.MouseListener;
//import java.awt.event.MouseMotionListener;
//import java.io.*;
//
///**
// * Frame for help to create/edit sensitive area
// *
// * @author JHelp
// */
//public class SensitiveAreaEditorFrame
//      extends JHelpFrame
//{
//   /**
//    * Action for load an image
//    *
//    * @author JHelp
//    */
//   class ActionLoadImage
//         extends GenericAction
//   {
//      /**
//       * Create a new instance of ActionLoadImage
//       */
//      ActionLoadImage()
//      {
//         super("Load image");
//      }
//
//      /**
//       * Load an image <br>
//       * <br>
//       * <b>Parent documentation:</b><br>
//       * {@inheritDoc}
//       *
//       * @param actionEvent
//       *           Event description
//       * @see jhelp.gui.action.GenericAction#doActionPerformed(ActionEvent)
//       */
//      @Override
//      protected void doActionPerformed(final ActionEvent actionEvent)
//      {
//         SensitiveAreaEditorFrame.this.doLoadImage();
//      }
//   }
//
//   /**
//    * Action for open a sensitive area
//    *
//    * @author JHelp
//    */
//   class ActionOpen
//         extends GenericAction
//   {
//      /**
//       * Create a new instance of ActionOpen
//       */
//      ActionOpen()
//      {
//         super("Open");
//      }
//
//      /**
//       * Open a sensitive area <br>
//       * <br>
//       * <b>Parent documentation:</b><br>
//       * {@inheritDoc}
//       *
//       * @param actionEvent
//       *           Event description
//       * @see jhelp.gui.action.GenericAction#doActionPerformed(ActionEvent)
//       */
//      @Override
//      protected void doActionPerformed(final ActionEvent actionEvent)
//      {
//         SensitiveAreaEditorFrame.this.doOpen();
//      }
//   }
//
//   /**
//    * Action for save current sensitive area
//    *
//    * @author JHelp
//    */
//   class ActionSave
//         extends GenericAction
//   {
//      /**
//       * Create a new instance of ActionSave
//       */
//      ActionSave()
//      {
//         super("Save");
//      }
//
//      /**
//       * Save current sensitive area <br>
//       * <br>
//       * <b>Parent documentation:</b><br>
//       * {@inheritDoc}
//       *
//       * @param actionEvent
//       *           Event description
//       * @see jhelp.gui.action.GenericAction#doActionPerformed(ActionEvent)
//       */
//      @Override
//      protected void doActionPerformed(final ActionEvent actionEvent)
//      {
//         SensitiveAreaEditorFrame.this.doSave();
//      }
//   }
//
//   /**
//    * Manage users events
//    *
//    * @author JHelp
//    */
//   class EventManager
//         implements MouseMotionListener, MouseListener, OverLabelJHelpImage
//   {
//      /**
//       * Create a new instance of EventManager
//       */
//      EventManager()
//      {
//      }
//
//      /**
//       * Register mouse event and react to it
//       *
//       * @param mouseEvent
//       *           Mouse event description
//       * @param click
//       *           Indicates if it is a click
//       */
//      private void callDoMouse(final MouseEvent mouseEvent, final boolean click)
//      {
//         final int x = mouseEvent.getX();
//         final int y = mouseEvent.getY();
//         final boolean shift = mouseEvent.isShiftDown();
//         final boolean control = mouseEvent.isControlDown();
//         final boolean alt = mouseEvent.isAltDown();
//         SensitiveAreaEditorFrame.this.doMouse(x, y, click, shift, control, alt);
//      }
//
//      /**
//       * Draw areas over the image <br>
//       * <br>
//       * <b>Parent documentation:</b><br>
//       * {@inheritDoc}
//       *
//       * @param g
//       *           Graphics context
//       * @param width
//       *           Area width
//       * @param height
//       *           Area height
//       * @see jhelp.gui.OverLabelJHelpImage#draw(Graphics, int, int)
//       */
//      @Override
//      public void draw(final Graphics g, final int width, final int height)
//      {
//         SensitiveAreaEditorFrame.this.doDraw(g, width, height);
//      }
//
//      /**
//       * Called when mouse clicked <br>
//       * <br>
//       * <b>Parent documentation:</b><br>
//       * {@inheritDoc}
//       *
//       * @param mouseEvent
//       *           Mouse event description
//       * @see MouseListener#mouseClicked(MouseEvent)
//       */
//      @Override
//      public void mouseClicked(final MouseEvent mouseEvent)
//      {
//         this.callDoMouse(mouseEvent, true);
//      }
//
//      /**
//       * Called when mouse dragged <br>
//       * <br>
//       * <b>Parent documentation:</b><br>
//       * {@inheritDoc}
//       *
//       * @param mouseEvent
//       *           Mouse event description
//       * @see MouseMotionListener#mouseDragged(MouseEvent)
//       */
//      @Override
//      public void mouseDragged(final MouseEvent mouseEvent)
//      {
//         this.callDoMouse(mouseEvent, true);
//      }
//
//      /**
//       * Called when mouse enter <br>
//       * <br>
//       * <b>Parent documentation:</b><br>
//       * {@inheritDoc}
//       *
//       * @param mouseEvent
//       *           Mouse event description
//       * @see MouseListener#mouseEntered(MouseEvent)
//       */
//      @Override
//      public void mouseEntered(final MouseEvent mouseEvent)
//      {
//      }
//
//      /**
//       * Called when mouse exit <br>
//       * <br>
//       * <b>Parent documentation:</b><br>
//       * {@inheritDoc}
//       *
//       * @param mouseEvent
//       *           Mouse event description
//       * @see MouseListener#mouseExited(MouseEvent)
//       */
//      @Override
//      public void mouseExited(final MouseEvent mouseEvent)
//      {
//      }
//
//      /**
//       * Called when mouse moved <br>
//       * <br>
//       * <b>Parent documentation:</b><br>
//       * {@inheritDoc}
//       *
//       * @param mouseEvent
//       *           Mouse event description
//       * @see MouseMotionListener#mouseMoved(MouseEvent)
//       */
//      @Override
//      public void mouseMoved(final MouseEvent mouseEvent)
//      {
//         this.callDoMouse(mouseEvent, false);
//      }
//
//      /**
//       * Called when mouse button pressed <br>
//       * <br>
//       * <b>Parent documentation:</b><br>
//       * {@inheritDoc}
//       *
//       * @param mouseEvent
//       *           Mouse event description
//       * @see MouseListener#mousePressed(MouseEvent)
//       */
//      @Override
//      public void mousePressed(final MouseEvent mouseEvent)
//      {
//      }
//
//      /**
//       * Called when mouse button released <br>
//       * <br>
//       * <b>Parent documentation:</b><br>
//       * {@inheritDoc}
//       *
//       * @param mouseEvent
//       *           Mouse event description
//       * @see MouseListener#mouseReleased(MouseEvent)
//       */
//      @Override
//      public void mouseReleased(final MouseEvent mouseEvent)
//      {
//      }
//   }
//
//   /** Area colors */
//   private static final int[] AREA_COLORS =
//                                          {//
//         JHelpConstantsSmooth.COLOR_ALPHA_HINT | (JHelpConstantsSmooth.MASK_COLOR & JHelpConstantsSmooth.COLOR_AMBER_0900),//
//         JHelpConstantsSmooth.COLOR_ALPHA_HINT | (JHelpConstantsSmooth.MASK_COLOR & JHelpConstantsSmooth.COLOR_BLUE_0900),//
//         JHelpConstantsSmooth.COLOR_ALPHA_HINT | (JHelpConstantsSmooth.MASK_COLOR & JHelpConstantsSmooth.COLOR_BLUE_GREY_0900),//
//         JHelpConstantsSmooth.COLOR_ALPHA_HINT | (JHelpConstantsSmooth.MASK_COLOR & JHelpConstantsSmooth.COLOR_BROWN_0900),//
//         JHelpConstantsSmooth.COLOR_ALPHA_HINT | (JHelpConstantsSmooth.MASK_COLOR & JHelpConstantsSmooth.COLOR_CYAN_0900),//
//         JHelpConstantsSmooth.COLOR_ALPHA_HINT | (JHelpConstantsSmooth.MASK_COLOR & JHelpConstantsSmooth.COLOR_DEEP_ORANGE_0900),//
//         JHelpConstantsSmooth.COLOR_ALPHA_HINT | (JHelpConstantsSmooth.MASK_COLOR & JHelpConstantsSmooth.COLOR_DEEP_PURPLE_0900),//
//         JHelpConstantsSmooth.COLOR_ALPHA_HINT | (JHelpConstantsSmooth.MASK_COLOR & JHelpConstantsSmooth.COLOR_GREEN_0900),//
//         JHelpConstantsSmooth.COLOR_ALPHA_HINT | (JHelpConstantsSmooth.MASK_COLOR & JHelpConstantsSmooth.COLOR_GREY_0900),//
//         JHelpConstantsSmooth.COLOR_ALPHA_HINT | (JHelpConstantsSmooth.MASK_COLOR & JHelpConstantsSmooth.COLOR_INDIGO_0900),//
//         JHelpConstantsSmooth.COLOR_ALPHA_HINT | (JHelpConstantsSmooth.MASK_COLOR & JHelpConstantsSmooth.COLOR_LIGHT_BLUE_0900),//
//         JHelpConstantsSmooth.COLOR_ALPHA_HINT | (JHelpConstantsSmooth.MASK_COLOR & JHelpConstantsSmooth.COLOR_LIGHT_GREEN_0900),//
//         JHelpConstantsSmooth.COLOR_ALPHA_HINT | (JHelpConstantsSmooth.MASK_COLOR & JHelpConstantsSmooth.COLOR_LIME_0900),//
//         JHelpConstantsSmooth.COLOR_ALPHA_HINT | (JHelpConstantsSmooth.MASK_COLOR & JHelpConstantsSmooth.COLOR_ORANGE_0900),//
//         JHelpConstantsSmooth.COLOR_ALPHA_HINT | (JHelpConstantsSmooth.MASK_COLOR & JHelpConstantsSmooth.COLOR_PINK_0900),//
//         JHelpConstantsSmooth.COLOR_ALPHA_HINT | (JHelpConstantsSmooth.MASK_COLOR & JHelpConstantsSmooth.COLOR_PURPLE_0900),//
//         JHelpConstantsSmooth.COLOR_ALPHA_HINT | (JHelpConstantsSmooth.MASK_COLOR & JHelpConstantsSmooth.COLOR_RED_0900),//
//         JHelpConstantsSmooth.COLOR_ALPHA_HINT | (JHelpConstantsSmooth.MASK_COLOR & JHelpConstantsSmooth.COLOR_TEAL_0900),//
//         JHelpConstantsSmooth.COLOR_ALPHA_HINT | (JHelpConstantsSmooth.MASK_COLOR & JHelpConstantsSmooth.COLOR_YELLOW_0900)//
//                                          };
//   /** Font to use */
//   private static final Font  FONT        = new Font("Courier", Font.PLAIN, 12);
//   /** Distance for decide if mouse near a area border */
//   private static final int   NEAR        = 8;
//
//   /**
//    * Obtain one area color
//    *
//    * @return One area color
//    */
//   private static Color obtainOneColor()
//   {
//      return new Color(JHelpRandom.random(SensitiveAreaEditorFrame.AREA_COLORS), true);
//   }
//
//   /** Action that load image */
//   private ActionLoadImage actionLoadImage;
//   /** Action open a file of sensitive area */
//   private ActionOpen      actionOpen;
//   /** Action save current sensitive area */
//   private ActionSave      actionSave;
//   /** Current area */
//   private Area            area;
//   /** Button that loads image */
//   private JButton         buttonLoadImage;
//   /** Button that opens a sensitive area */
//   private JButton         buttonOpen;
//   /** Button that saves current sensitive area */
//   private JButton         buttonSave;
//   /** User events manager */
//   private EventManager    eventManager;
//   /** File chooser */
//   private FileChooser fileChooser;
//   /** Filter for sensitive area files */
//   private FileFilter      fileFilterArea;
//   /** Filter for base images files */
//   private FileFilter      fileFilterImage;
//   /** Font accent */
//   private int             fontAscent;
//   /** Font height */
//   private int             fontHeight;
//   /** Metrics for measure strings */
//   private FontMetrics     fontMetrics;
//   /** Component with image and sensitive area draws */
//   private LabelJHelpImage labelJHelpImage;
//   /** current over area */
//   private Area            overArea;
//   /** Current area position */
//   private Position        position;
//   /** Sensitive area edited */
//   private SensitiveArea   sensitiveArea;
//
//   /**
//    * Create a new instance of SensitiveAreaEditorFrame
//    */
//   public SensitiveAreaEditorFrame()
//   {
//      super("Sensitive area editor");
//   }
//
//   /**
//    * Draw area ID
//    *
//    * @param id
//    *           Area ID
//    * @param x
//    *           X center position
//    * @param y
//    *           Y center position
//    * @param graphics
//    *           Graphics context
//    */
//   private void drawID(final int id, final int x, final int y, final Graphics graphics)
//   {
//      final String string = String.valueOf(id);
//      final int width = this.fontMetrics.stringWidth(string);
//      final int xx = x - (width >> 1);
//      final int yy = (y - (this.fontHeight >> 1)) + this.fontAscent;
//      graphics.drawString(string, xx, yy);
//   }
//
//   /**
//    * Draw sentive area over the image
//    *
//    * @param graphics
//    *           Graphics context
//    * @param width
//    *           Area with
//    * @param height
//    *           Aera height
//    */
//   void doDraw(final Graphics graphics, final int width, final int height)
//   {
//      if(this.sensitiveArea == null)
//      {
//         return;
//      }
//
//      graphics.setFont(SensitiveAreaEditorFrame.FONT);
//
//      if(this.fontMetrics == null)
//      {
//         this.fontMetrics = graphics.getFontMetrics(SensitiveAreaEditorFrame.FONT);
//         this.fontHeight = this.fontMetrics.getHeight();
//         this.fontAscent = this.fontMetrics.getAscent();
//      }
//
//      final int number = this.sensitiveArea.numberOfArea();
//      Area area;
//      Color color;
//      Rectangle bounds;
//      int mx, my;
//
//      for(int index = 0; index < number; index++)
//      {
//         area = this.sensitiveArea.getArea(index);
//         bounds = area.getBounds();
//         color = (Color) area.getDevelopperInformation();
//         mx = bounds.x + (bounds.width >> 1);
//         my = bounds.y + (bounds.height >> 1);
//
//         if(color == null)
//         {
//            color = SensitiveAreaEditorFrame.obtainOneColor();
//            area.setDevelopperInformation(color);
//         }
//
//         graphics.setColor(color);
//         graphics.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
//
//         graphics.setColor(Color.WHITE);
//         graphics.drawRect(bounds.x - 1, bounds.y - 1, bounds.width + 2, bounds.height + 2);
//         graphics.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
//         graphics.drawRect(bounds.x + 1, bounds.y + 1, bounds.width - 2, bounds.height - 2);
//
//         graphics.setColor(Color.BLACK);
//         graphics.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
//
//         this.drawID(area.getID(), mx, my, graphics);
//
//         if(area == this.overArea)
//         {
//            final int diameter = SensitiveAreaEditorFrame.NEAR << 1;
//            graphics.setColor(Color.RED);
//
//            switch(this.position)
//            {
//               case DOWN:
//                  graphics.fillRect(bounds.x, (bounds.y + bounds.height) - SensitiveAreaEditorFrame.NEAR, bounds.width, diameter);
//               break;
//               case INSIDE:
//                  graphics.fillOval(mx - SensitiveAreaEditorFrame.NEAR, my - SensitiveAreaEditorFrame.NEAR, diameter, diameter);
//               break;
//               case LEFT:
//                  graphics.fillRect(bounds.x - SensitiveAreaEditorFrame.NEAR, bounds.y, diameter, bounds.height);
//               break;
//               case LEFT_DOWN:
//                  graphics.fillRect(bounds.x - SensitiveAreaEditorFrame.NEAR, (bounds.y + bounds.height) - SensitiveAreaEditorFrame.NEAR, diameter, diameter);
//               break;
//               case LEFT_UP:
//                  graphics.fillRect(bounds.x - SensitiveAreaEditorFrame.NEAR, bounds.y - SensitiveAreaEditorFrame.NEAR, diameter, diameter);
//               break;
//               case OUTSIDE:
//               break;
//               case RIGHT:
//                  graphics.fillRect((bounds.x + bounds.width) - SensitiveAreaEditorFrame.NEAR, bounds.y, diameter, bounds.height);
//               break;
//               case RIGHT_DOWN:
//                  graphics.fillRect((bounds.x + bounds.width) - SensitiveAreaEditorFrame.NEAR, (bounds.y + bounds.height) - SensitiveAreaEditorFrame.NEAR,
//                        diameter, diameter);
//               break;
//               case RIGHT_UP:
//                  graphics.fillRect((bounds.x + bounds.width) - SensitiveAreaEditorFrame.NEAR, bounds.y - SensitiveAreaEditorFrame.NEAR, diameter, diameter);
//               break;
//               case UP:
//                  graphics.fillRect(bounds.x, bounds.y - SensitiveAreaEditorFrame.NEAR, bounds.width, diameter);
//               break;
//            }
//         }
//      }
//   }
//
//   /**
//    * Load base image
//    */
//   void doLoadImage()
//   {
//      this.fileChooser.setFileFilter(this.fileFilterImage);
//      final File file = this.fileChooser.showOpenFile();
//
//      if(file == null)
//      {
//         return;
//      }
//
//      try
//      {
//         final JHelpImage image = JHelpImage.loadImage(file);
//         this.sensitiveArea = new SensitiveArea(image.getWidth(), image.getHeight());
//         this.labelJHelpImage.setJHelpImage(image);
//      }
//      catch(final Exception exception)
//      {
//         Debug.printException(exception, "Failed to load image ", file.getAbsolutePath());
//      }
//   }
//
//   /**
//    * React to mouse event
//    *
//    * @param x
//    *           Mouse X
//    * @param y
//    *           Mouse Y
//    * @param click
//    *           Indicates if click
//    * @param shift
//    *           Indicates if shift down
//    * @param control
//    *           Indicates if control down
//    * @param alt
//    *           Indicates if alt down
//    */
//   void doMouse(final int x, final int y, final boolean click, final boolean shift, final boolean control, final boolean alt)
//   {
//      if(this.sensitiveArea == null)
//      {
//         return;
//      }
//
//      if((shift == true) && (click == true))
//      {
//         this.position = Position.OUTSIDE;
//         this.area = null;
//         this.labelJHelpImage.setCursor(Cursor.getDefaultCursor());
//         this.sensitiveArea.putArea(this.sensitiveArea.nextFreeID(), x - 32, y - 32, 64, 64);
//         this.labelJHelpImage.refresh();
//         return;
//      }
//
//      if(this.position == null)
//      {
//         this.position = Position.OUTSIDE;
//      }
//
//      if(this.area == null)
//      {
//         final Pair<Position, Area> pair = this.sensitiveArea.obtainArea(x, y, SensitiveAreaEditorFrame.NEAR);
//         this.overArea = pair.element2;
//         this.position = pair.element1;
//
//         if(click == true)
//         {
//            this.area = pair.element2;
//         }
//      }
//      else if(click == false)
//      {
//         this.area = null;
//      }
//
//      if(this.area != null)
//      {
//         this.area.move(x, y, this.position);
//      }
//
//      this.labelJHelpImage.refresh();
//   }
//
//   /**
//    * Open a sensitive area
//    */
//   void doOpen()
//   {
//      this.fileChooser.setFileFilter(this.fileFilterArea);
//      final File file = this.fileChooser.showOpenFile();
//
//      if(file == null)
//      {
//         return;
//      }
//
//      InputStream inputStream = null;
//
//      try
//      {
//         inputStream = new FileInputStream(file);
//         this.sensitiveArea = SensitiveArea.load(inputStream);
//         this.labelJHelpImage.refresh();
//      }
//      catch(final Exception exception)
//      {
//         Debug.printException(exception, "Failed to open ", file.getAbsolutePath());
//      }
//      finally
//      {
//         if(inputStream != null)
//         {
//            try
//            {
//               inputStream.close();
//            }
//            catch(final Exception exception)
//            {
//            }
//         }
//      }
//   }
//
//   /**
//    * Save current sensitive area
//    */
//   void doSave()
//   {
//      this.fileChooser.setFileFilter(this.fileFilterArea);
//      final File file = this.fileChooser.showSaveFile();
//
//      if((file == null) || (this.sensitiveArea == null))
//      {
//         return;
//      }
//
//      OutputStream outputStream = null;
//
//      try
//      {
//         outputStream = new FileOutputStream(file);
//         this.sensitiveArea.save(outputStream);
//      }
//      catch(final Exception exception)
//      {
//         Debug.printException(exception, "Failed to save ", file.getAbsolutePath());
//      }
//      finally
//      {
//         if(outputStream != null)
//         {
//            try
//            {
//               outputStream.flush();
//            }
//            catch(final Exception exception)
//            {
//            }
//
//            try
//            {
//               outputStream.close();
//            }
//            catch(final Exception exception)
//            {
//            }
//         }
//      }
//   }
//
//   /**
//    * Add listeners <br>
//    * <br>
//    * <b>Parent documentation:</b><br>
//    * {@inheritDoc}
//    *
//    * @see jhelp.gui.JHelpFrame#addListeners()
//    */
//   @Override
//   protected void addListeners()
//   {
//      this.eventManager = new EventManager();
//      this.labelJHelpImage.addMouseListener(this.eventManager);
//      this.labelJHelpImage.addMouseMotionListener(this.eventManager);
//      this.labelJHelpImage.setOverLabelJHelpImage(this.eventManager);
//   }
//
//   /**
//    * Create components <br>
//    * <br>
//    * <b>Parent documentation:</b><br>
//    * {@inheritDoc}
//    *
//    * @see jhelp.gui.JHelpFrame#createComponents()
//    */
//   @Override
//   protected void createComponents()
//   {
//      this.fileFilterArea = new FileFilter();
//      this.fileFilterArea.addExtension("area");
//      this.fileFilterImage = FileFilter.createFilterForImageByFileImageInformation();
//      this.fileChooser = new FileChooser(this);
//
//      this.actionOpen = new ActionOpen();
//      this.actionSave = new ActionSave();
//      this.actionLoadImage = new ActionLoadImage();
//
//      this.labelJHelpImage = new LabelJHelpImage();
//      this.buttonOpen = new JButton(this.actionOpen);
//      this.buttonSave = new JButton(this.actionSave);
//      this.buttonLoadImage = new JButton(this.actionLoadImage);
//   }
//
//   /**
//    * Layout components <br>
//    * <br>
//    * <b>Parent documentation:</b><br>
//    * {@inheritDoc}
//    *
//    * @see jhelp.gui.JHelpFrame#layoutComponents()
//    */
//   @Override
//   protected void layoutComponents()
//   {
//      this.setLayout(new BorderLayout());
//      this.add(this.labelJHelpImage, BorderLayout.CENTER);
//
//      final JPanel panel = new JPanel(new FlowLayout());
//      panel.add(this.buttonOpen);
//      panel.add(this.buttonSave);
//      panel.add(this.buttonLoadImage);
//      this.add(panel, BorderLayout.NORTH);
//   }
//}