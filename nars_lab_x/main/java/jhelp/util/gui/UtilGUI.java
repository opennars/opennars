package jhelp.util.gui;

import jhelp.util.Utilities;
import jhelp.util.debug.Debug;
import jhelp.util.debug.DebugLevel;

import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;

/**
 * Utilities for GUI
 * 
 * @author JHelp
 */
public final class UtilGUI
{
   /** Empty image */
   private static BufferedImage            EMPTY_IMAGE;
   /** List of graphics devices */
   private static final GraphicsDevice[]   GRAPHICS_DEVICES;
   /** Invisible cursor */
   private static Cursor                   INVISIBLE_CURSOR;
   /** Robot for simulate keyboard and mouse events */
   private final static Robot              ROBOT;
   /** Current graphics environment */
   public static final GraphicsEnvironment GRAPHICS_ENVIRONMENT;
   /** Current toolkit */
   public static final Toolkit             TOOLKIT;

   static
   {
      Robot robot = null;

      try
      {
         robot = new Robot();
      }
      catch(final Exception exception)
      {
         robot = null;
      }

      ROBOT = robot;

      //

      final Toolkit toolkit = Toolkit.getDefaultToolkit();
      final GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
      final GraphicsDevice[] graphicsDevices = graphicsEnvironment.getScreenDevices();

      TOOLKIT = toolkit;
      GRAPHICS_ENVIRONMENT = graphicsEnvironment;
      GRAPHICS_DEVICES = graphicsDevices;
   }

   /**
    * Check if a screen index is valid
    * 
    * @param screenIndex
    *           Screen index to check
    */
   private static void checkScreenIndex(final int screenIndex)
   {
      if((screenIndex < 0) || (screenIndex >= UtilGUI.GRAPHICS_DEVICES.length))
      {
         throw new IllegalArgumentException("You have " + UtilGUI.GRAPHICS_DEVICES.length + " screens so the screen index must be in [0, " + UtilGUI.GRAPHICS_DEVICES.length + "[ not " + screenIndex);
      }
   }

   /**
    * Center a window on its screen
    * 
    * @param window
    *           Widow to center
    */
   public static void centerOnScreen(final Window window)
   {
      final Dimension dimension = window.getSize();
      final Rectangle screen = UtilGUI.computeScreenRectangle(window);
      Debug.println(DebugLevel.DEBUG, dimension, " | ", screen);
      window.setLocation(screen.x + ((screen.width - dimension.width) / 2),//
            screen.y + ((screen.height - dimension.height) / 2));
   }

   /**
    * Change a window of screen
    * 
    * @param window
    *           Widow to translate
    * @param screenIndex
    *           Destination screen
    */
   public static void changeScreen(final Window window, final int screenIndex)
   {
      UtilGUI.checkScreenIndex(screenIndex);

      final Rectangle sourceScreen = UtilGUI.computeScreenRectangle(window);
      final int x = window.getX() - sourceScreen.x;
      final int y = window.getY() - sourceScreen.y;

      final GraphicsConfiguration graphicsConfiguration = UtilGUI.GRAPHICS_DEVICES[screenIndex].getDefaultConfiguration();
      final Rectangle destinationScreen = graphicsConfiguration.getBounds();
      final Insets insets = UtilGUI.TOOLKIT.getScreenInsets(graphicsConfiguration);

      window.setLocation(x + destinationScreen.x + insets.left,//
            y + destinationScreen.y + insets.top);
   }

   /**
    * Compute intersection area between two rectangles
    * 
    * @param rectangle1
    *           First rectangle
    * @param rectangle2
    *           Second rectangle
    * @return Computed area
    */
   public static int computeIntresectedArea(final Rectangle rectangle1, final Rectangle rectangle2)
   {
      final int xmin1 = rectangle1.x;
      final int xmax1 = rectangle1.x + rectangle1.width;
      final int ymin1 = rectangle1.y;
      final int ymax1 = rectangle1.y + rectangle1.height;
      final int xmin2 = rectangle2.x;
      final int xmax2 = rectangle2.x + rectangle2.width;
      final int ymin2 = rectangle2.y;
      final int ymax2 = rectangle2.y + rectangle2.height;

      if((xmin1 > xmax2) || (ymin1 > ymax2) || (xmin2 > xmax1) || (ymin2 > ymax1))
      {
         return 0;
      }

      final int xmin = Math.max(xmin1, xmin2);
      final int xmax = Math.min(xmax1, xmax2);
      if(xmin >= xmax)
      {
         return 0;
      }

      final int ymin = Math.max(ymin1, ymin2);
      final int ymax = Math.min(ymax1, ymax2);
      if(ymin >= ymax)
      {
         return 0;
      }

      return (xmax - xmin) * (ymax - ymin);
   }

   /**
    * Compute the maximum dimension of a component
    * 
    * @param component
    *           Component to compute it's maximum size
    * @return Maximum size
    */
   public static Dimension computeMaximumDimension(final Component component)
   {
      if(component instanceof WithFixedSize)
      {
         return ((WithFixedSize) component).getFixedSize();
      }

      if(component instanceof Container)
      {
         final Container container = (Container) component;
         if(container.getComponentCount() < 1)
         {
            return new Dimension(100, 100);
         }

         return container.getLayout().preferredLayoutSize(container);
      }

      return component.getMaximumSize();
   }

   /**
    * Compute the minimum dimension of a component
    * 
    * @param component
    *           Component to compute it's minimum size
    * @return Minimum size
    */
   public static Dimension computeMinimumDimension(final Component component)
   {
      if(component instanceof WithFixedSize)
      {
         return ((WithFixedSize) component).getFixedSize();
      }

      if(component instanceof Container)
      {
         final Container container = (Container) component;
         if(container.getComponentCount() < 1)
         {
            return new Dimension(1, 1);
         }

         return container.getLayout().minimumLayoutSize(container);
      }

      return component.getMinimumSize();
   }

   /**
    * Compute the preferred dimension of a component
    * 
    * @param component
    *           Component to compute it's preferred size
    * @return Preferred size
    */
   public static Dimension computePreferredDimension(final Component component)
   {
      if(component instanceof WithFixedSize)
      {
         return ((WithFixedSize) component).getFixedSize();
      }

      if(component instanceof Container)
      {
         final Container container = (Container) component;
         if(container.getComponentCount() < 1)
         {
            return new Dimension(10, 10);
         }

         return container.getLayout().preferredLayoutSize(container);
      }

      return component.getPreferredSize();
   }

   /**
    * Compute the rectangle of the screen where is a window
    * 
    * @param window
    *           Window we looking for its screen
    * @return Screen's rectangle
    */
   public static Rectangle computeScreenRectangle(final Window window)
   {
      final Rectangle windowBounds = window.getBounds();

      GraphicsConfiguration graphicsConfiguration = UtilGUI.GRAPHICS_DEVICES[0].getDefaultConfiguration();
      Rectangle screenBounds = graphicsConfiguration.getBounds();
      int areaMax = UtilGUI.computeIntresectedArea(windowBounds, screenBounds);

      int totalWidth = screenBounds.x + screenBounds.width;
      int totalHeight = screenBounds.y + screenBounds.height;

      Rectangle bounds;
      int area;
      GraphicsConfiguration cg;
      for(int i = 1; i < UtilGUI.GRAPHICS_DEVICES.length; i++)
      {
         cg = UtilGUI.GRAPHICS_DEVICES[i].getDefaultConfiguration();
         bounds = cg.getBounds();
         area = UtilGUI.computeIntresectedArea(windowBounds, bounds);

         totalWidth = Math.max(totalWidth, bounds.x + bounds.width);
         totalHeight = Math.max(totalHeight, bounds.y + bounds.height);

         if(area > areaMax)
         {
            graphicsConfiguration = cg;
            screenBounds = bounds;
            areaMax = area;
         }
      }

      final Insets margin = UtilGUI.TOOLKIT.getScreenInsets(graphicsConfiguration);

      final Rectangle screenRectangle = new Rectangle(screenBounds);
      screenRectangle.x = margin.left;
      screenRectangle.y = margin.top;
      screenRectangle.width = totalWidth - margin.left - margin.right;
      screenRectangle.height = totalHeight - margin.top - margin.bottom;

      return screenRectangle;
   }

   /**
    * Obtain frame parent of a container
    * 
    * @param container
    *           Container to get its parent
    * @return Parent frame
    */
   public static JFrame getFrameParent(Container container)
   {
      while(container != null)
      {
         if((container instanceof JFrame) == true)
         {
            return (JFrame) container;
         }

         container = container.getParent();
      }

      return null;
   }

   /**
    * Give the relative of a component for an other one
    * 
    * @param component
    *           Component to search its position
    * @param parent
    *           A component ancestor
    * @return Relative position of {@code null} if parent is not an ancestor of component
    */
   public static Point getLocationOn(Component component, final Component parent)
   {
      final Point point = new Point();

      while((component != null) && (component.equals(parent) == false))
      {
         point.translate(component.getX(), component.getY());

         component = component.getParent();
      }

      if(component == null)
      {
         return null;
      }

      return point;
   }

   /**
    * Give bounds of a screen
    * 
    * @param screen
    *           Screen index
    * @return Screen bounds
    */
   public static Rectangle getScreenBounds(final int screen)
   {
      final Rectangle bounds = UtilGUI.GRAPHICS_DEVICES[screen].getDefaultConfiguration().getBounds();

      final Insets insets = UtilGUI.TOOLKIT.getScreenInsets(UtilGUI.GRAPHICS_DEVICES[screen].getDefaultConfiguration());

      if(bounds.x < insets.left)
      {
         insets.left -= bounds.x;
      }

      bounds.x += insets.left;
      bounds.y += insets.top;
      bounds.width -= insets.left + insets.right;
      bounds.height -= insets.top + insets.bottom;

      return bounds;
   }

   /**
    * Screen identifier
    * 
    * @param screenIndex
    *           Screen index
    * @return Screen identifier
    */
   public static String getScreenIdentifier(final int screenIndex)
   {
      UtilGUI.checkScreenIndex(screenIndex);

      final StringBuffer stringBuffer = new StringBuffer();
      stringBuffer.append(System.getProperty("java.vendor"));
      stringBuffer.append(" | ");
      stringBuffer.append(UtilGUI.GRAPHICS_DEVICES[screenIndex].getIDstring());
      stringBuffer.append(" | ");
      stringBuffer.append(screenIndex);

      return stringBuffer.toString();
   }

   /**
    * Initialize GUI with operating system skin, call it before create any frame
    */
   public static void initializeGUI()
   {
      try
      {
         //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      }
      catch(final Exception exception)
      {
      }
   }

   /**
    * Place the mouse on a location on the screen
    * 
    * @param x
    *           X screen position
    * @param y
    *           Y screen position
    */
   public static void locateMouseAt(final int x, final int y)
   {
      if(UtilGUI.ROBOT == null)
      {
         return;
      }

      UtilGUI.ROBOT.mouseMove(x, y);
   }

   /**
    * Place the mouse over the middle of a component
    * 
    * @param component
    *           Component mouse go over
    */
   public static void locateMouseOver(final Component component)
   {
      if(component == null)
      {
         throw new NullPointerException("component musn't be null");
      }

      if((component.isValid() == false) || (component.isVisible() == false))
      {
         return;
      }

      Dimension dimension = component.getSize();
      UtilGUI.locateMouseOver(component, dimension.width / 2, dimension.height / 2);
      dimension = null;
   }

   /**
    * Place the mouse over a component
    * 
    * @param component
    *           Component mouse go over
    * @param x
    *           X relative to component up-left corner
    * @param y
    *           Y relative to component up-left corner
    */
   public static void locateMouseOver(final Component component, final int x, final int y)
   {
      if(component == null)
      {
         throw new NullPointerException("component musn't be null");
      }

      if((component.isValid() == false) || (component.isVisible() == false))
      {
         return;
      }

      Point position = component.getLocationOnScreen();
      UtilGUI.locateMouseAt(position.x + x, position.y + y);
      position = null;
   }

   /**
    * Number of screen
    * 
    * @return Number of screen
    */
   public static int numberOfScreen()
   {
      return UtilGUI.GRAPHICS_DEVICES.length;
   }

   /**
    * Invisible cursor
    * 
    * @return Invisible cursor
    */
   public static Cursor obtainInvisbleCursor()
   {
      if(UtilGUI.INVISIBLE_CURSOR != null)
      {
         return UtilGUI.INVISIBLE_CURSOR;
      }

      Dimension dimension = Toolkit.getDefaultToolkit().getBestCursorSize(32, 32);

      if((dimension == null) || (dimension.width < 1) || (dimension.height < 1))
      {
         UtilGUI.INVISIBLE_CURSOR = Cursor.getDefaultCursor();
      }
      else
      {
         BufferedImage bufferedImage = new BufferedImage(dimension.width, dimension.height, BufferedImage.TYPE_INT_ARGB);
         bufferedImage.flush();
         UtilGUI.INVISIBLE_CURSOR = Toolkit.getDefaultToolkit().createCustomCursor(bufferedImage, new Point(dimension.width >> 1, dimension.height >> 1), "Invisible");
         bufferedImage = null;
      }

      dimension = null;

      return UtilGUI.INVISIBLE_CURSOR;
   }

   /**
    * Number of screen
    * 
    * @return Number of screen
    */
   public static int obtainNumberOffScreen()
   {
      return UtilGUI.GRAPHICS_DEVICES.length;
   }

   /**
    * Obtain index of the screen where is the window
    * 
    * @param window
    *           Considered window
    * @return Screen index
    */
   public static int obtainScreenIndex(final Window window)
   {
      final Rectangle windowBounds = window.getBounds();

      GraphicsConfiguration graphicsConfiguration = UtilGUI.GRAPHICS_DEVICES[0].getDefaultConfiguration();
      Rectangle bounds = graphicsConfiguration.getBounds();
      int areaMax = UtilGUI.computeIntresectedArea(windowBounds, bounds);
      int screenIndex = 0;
      int area;
      for(int i = 1; i < UtilGUI.GRAPHICS_DEVICES.length; i++)
      {
         graphicsConfiguration = UtilGUI.GRAPHICS_DEVICES[i].getDefaultConfiguration();
         bounds = graphicsConfiguration.getBounds();
         area = UtilGUI.computeIntresectedArea(windowBounds, bounds);

         if(area > areaMax)
         {
            screenIndex = i;
            areaMax = area;
         }
      }

      return screenIndex;
   }

   /**
    * Put a window in it's pack size<br>
    * Size is automatic limited to the window's screen
    * 
    * @param window
    *           Window to pack
    */
   public static void packedSize(final Window window)
   {
      window.pack();

      final Dimension dimension = window.getSize();
      final Rectangle screen = UtilGUI.computeScreenRectangle(window);

      if(dimension.width > screen.width)
      {
         dimension.width = screen.width;
      }

      if(dimension.height > screen.height)
      {
         dimension.height = screen.height;
      }

      window.setSize(dimension);
   }

   /**
    * Make a screen shot
    * 
    * @return Screen shot
    */
   public static BufferedImage screenShot()
   {
      int xMin = 0;
      int xMax = 0;
      int yMin = 0;
      int yMax = 0;
      Rectangle bounds;

      for(final GraphicsDevice graphicsDevice : UtilGUI.GRAPHICS_DEVICES)
      {
         bounds = graphicsDevice.getDefaultConfiguration().getBounds();

         xMin = Math.min(xMin, -bounds.x);
         xMax = Math.max(xMax, -bounds.x + bounds.width);
         yMin = Math.min(yMin, -bounds.y);
         yMax = Math.max(yMax, -bounds.y + bounds.height);
      }

      final Dimension size = Toolkit.getDefaultToolkit().getScreenSize();

      final int width = Math.max(xMax - xMin, size.width);
      final int height = Math.max(yMax - yMin, size.height);

      if(UtilGUI.ROBOT == null)
      {
         if(UtilGUI.EMPTY_IMAGE == null)
         {
            UtilGUI.EMPTY_IMAGE = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
         }

         int[] pixels = new int[width * height];
         UtilGUI.EMPTY_IMAGE.setRGB(0, 0, width, height, pixels, 0, width);
         pixels = null;

         return UtilGUI.EMPTY_IMAGE;
      }

      Debug.println(DebugLevel.VERBOSE, "Screen shot");
      return UtilGUI.ROBOT.createScreenCapture(new Rectangle(xMin, yMin, width, height));
   }

   /**
    * Search JFrame parent of a component
    * 
    * @param component
    *           Component search it's JFram parent
    * @return JFrame parent or {@code null} if component haven't a JFrame parent
    */
   public static JFrame searchFrameParent(Component component)
   {
      while(component != null)
      {
         if((component instanceof JFrame) == true)
         {
            return (JFrame) component;
         }

         if((component instanceof Window) == true)
         {
            component = ((Window) component).getOwner();
         }
         else
         {
            component = component.getParent();
         }
      }

      return null;
   }

   /**
    * Simulate a key press
    * 
    * @param keyCode
    *           Key code
    */
   public static void simulateKeyPress(final int keyCode)
   {
      if(UtilGUI.ROBOT == null)
      {
         return;
      }

      UtilGUI.ROBOT.keyPress(keyCode);
   }

   /**
    * Simulate a key release
    * 
    * @param keyCode
    *           Key code
    */
   public static void simulateKeyRelease(final int keyCode)
   {
      if(UtilGUI.ROBOT == null)
      {
         return;
      }

      UtilGUI.ROBOT.keyRelease(keyCode);
   }

   /**
    * Simulate a mouse press
    * 
    * @param button
    *           Mouse buttons
    */
   public static void simulateMousePress(final int button)
   {
      if(UtilGUI.ROBOT == null)
      {
         return;
      }

      UtilGUI.ROBOT.mousePress(button);
   }

   /**
    * Simulate a mouse release
    * 
    * @param button
    *           Mouse buttons
    */
   public static void simulateMouseRelease(final int button)
   {
      if(UtilGUI.ROBOT == null)
      {
         return;
      }

      UtilGUI.ROBOT.mouseRelease(button);
   }

   /**
    * Simulate a release then press mouse button
    * 
    * @param time
    *           Time between release and press
    */
   public static void simulateReleasedPressed(final int time)
   {
      if(UtilGUI.ROBOT == null)
      {
         return;
      }

      UtilGUI.ROBOT.mouseRelease(InputEvent.BUTTON1_MASK | InputEvent.BUTTON2_MASK | InputEvent.BUTTON3_MASK);
      Utilities.sleep(time);
      UtilGUI.ROBOT.mousePress(InputEvent.BUTTON1_MASK);
   }

   /**
    * Make widow take all it's screen
    * 
    * @param window
    *           Window to maximize
    */
   public static void takeAllScreen(final Window window)
   {
      final Rectangle screen = UtilGUI.computeScreenRectangle(window);
      window.setBounds(screen);
   }

   /**
    * For avoid instance
    */
   private UtilGUI()
   {
   }
}