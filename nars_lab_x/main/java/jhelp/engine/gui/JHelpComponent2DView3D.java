//package jhelp.engine.gui;
//
//import java.awt.Dimension;
//import java.awt.Rectangle;
//import java.awt.event.KeyListener;
//
//import jhelp.engine.JHelpSceneRenderer;
//import jhelp.engine.event.ClickInSpaceListener;
//import jhelp.gui.JHelpMouseListener;
//import jhelp.gui.twoD.JHelpComponent2D;
//import jhelp.util.debug.Debug;
//import jhelp.util.debug.DebugLevel;
//import jhelp.util.gui.JHelpImage;
//
///**
// * Component 2D with 3D scene inside
// *
// * @author JHelp
// */
//public class JHelpComponent2DView3D
//      extends JHelpComponent2D
//{
//   /** Minium time between each refresh */
//   private static final long        TIMEOUT_REFRESH = 1024;
//   /** Indicates if 3D area is already add to the frame */
//   private boolean                  alreadyAdded;
//   /** Listener of click in empty space */
//   private ClickInSpaceListener     clickInSpaceListener;
//   /** Component that carry the 3D */
//   private final ComponentView3D    componentView3D;
//   /** Preffered size */
//   private final Dimension          dimensionPreferred;
//   /** Frame where component is draw */
//   private final JHelp2DFrame       frame;
//   /** Listener of key */
//   private KeyListener              keyListener;
//   /** Last refresh time in millisecond from epoch */
//   private long                     lastTimeRefesh;
//   /** Previous aread 3D height */
//   private int                      previousHeight;
//   /** Previous aread 3D width */
//   private int                      previousWidth;
//   /** Previous aread 3D X */
//   private int                      previousX;
//   /** Previous aread 3D Y */
//   private int                      previousY;
//   /** Scene renderer */
//   private final JHelpSceneRenderer sceneRenderer;
//   /** Current/last screen shot used to show something even if the scene is in pause mode */
//   private JHelpImage               screenShot;
//
//   /**
//    * Create a new instance of JHelpComponent2DView3D
//    *
//    * @param width
//    *           Start width
//    * @param height
//    *           Start height
//    * @param componentView3D
//    *           Component that carry the 3D
//    * @param frame
//    *           Frame where lies the component
//    */
//   JHelpComponent2DView3D(final int width, final int height, final ComponentView3D componentView3D, final JHelp2DFrame frame)
//   {
//      this.dimensionPreferred = new Dimension(width, height);
//
//      // Create the renderer
//      this.frame = frame;
//      this.componentView3D = componentView3D;
//      this.sceneRenderer = componentView3D.getSceneRenderer();
//      this.alreadyAdded = false;
//   }
//
//   /**
//    * Pause the 3D view
//    */
//   void pause()
//   {
//      if(this.sceneRenderer.isPause() == true)
//      {
//         return;
//      }
//
//      this.screenShot = this.sceneRenderer.screenShot();
//      this.sceneRenderer.setPause(true);
//      this.frame.remove(this.componentView3D);
//      this.alreadyAdded = false;
//   }
//
//   /**
//    * Resume the 3D view
//    */
//   void resume()
//   {
//      if(this.sceneRenderer.isPause() == false)
//      {
//         return;
//      }
//
//      this.sceneRenderer.setPause(false);
//
//      this.alreadyAdded = true;
//      this.frame.add(this.componentView3D, 0);
//
//      synchronized(this.sceneRenderer)
//      {
//         this.screenShot = null;
//      }
//   }
//
//   /**
//    * Compute component preferred szie <br>
//    * <br>
//    * <b>Parent documentation:</b><br>
//    * {@inheritDoc}
//    *
//    * @param parentWidth
//    *           Parent width
//    * @param parentHeight
//    *           Parent height
//    * @return Component preferred szie
//    * @see jhelp.gui.twoD.JHelpComponent2D#computePreferredSize(int, int)
//    */
//   @Override
//   protected Dimension computePreferredSize(final int parentWidth, final int parentHeight)
//   {
//      return this.dimensionPreferred;
//   }
//
//   /**
//    * Called when component draw on the scene <br>
//    * <br>
//    * <b>Parent documentation:</b><br>
//    * {@inheritDoc}
//    *
//    * @param x
//    *           X
//    * @param y
//    *           Y
//    * @param parent
//    *           Image to refresh
//    * @see jhelp.gui.twoD.JHelpComponent2D#paint(int, int, jhelp.util.gui.JHelpImage)
//    */
//   @Override
//   protected void paint(final int x, final int y, final JHelpImage parent)
//   {
//      synchronized(this.sceneRenderer)
//      {
//         if(this.screenShot != null)
//         {
//            parent.drawImage(x, y, this.screenShot);
//
//            return;
//         }
//      }
//
//      final Rectangle rectangle = this.getBounds();
//
//      if(((this.previousX != x) || (this.previousY != y) || (this.previousWidth != rectangle.width) || (this.previousHeight != rectangle.height))
//            && ((System.currentTimeMillis() - this.lastTimeRefesh) > JHelpComponent2DView3D.TIMEOUT_REFRESH))
//      {
//         this.componentView3D.setBounds(x, y, rectangle.width, rectangle.height);
//         this.componentView3D.doLayout();
//
//         this.previousX = x;
//         this.previousY = y;
//         this.previousWidth = rectangle.width;
//         this.previousHeight = rectangle.height;
//
//         this.lastTimeRefesh = System.currentTimeMillis();
//      }
//
//      if(this.alreadyAdded == false)
//      {
//         if(this.sceneRenderer.tryRestart() == true)
//         {
//            this.alreadyAdded = true;
//            this.frame.add(this.componentView3D, 0);
//         }
//         else
//         {
//            Debug.println(DebugLevel.WARNING, "Impossible to start the renderer !");
//         }
//      }
//   }
//
//   /**
//    * Called when component will remove from its parent <br>
//    * <br>
//    * <b>Parent documentation:</b><br>
//    * {@inheritDoc}
//    *
//    * @see jhelp.gui.twoD.JHelpComponent2D#willRemove()
//    */
//   @Override
//   protected void willRemove()
//   {
//      this.sceneRenderer.stop();
//      this.frame.remove(this.componentView3D);
//      this.alreadyAdded = false;
//   }
//
//   /**
//    * Scene renderer for manipulate the 3D
//    *
//    * @return Scene renderer for manipulate the 3D
//    */
//   public JHelpSceneRenderer getJHelpSceneRenderer()
//   {
//      return this.sceneRenderer;
//   }
//
//   /**
//    * The key listener
//    *
//    * @return The key listener
//    */
//   @Override
//   public KeyListener getKeyListener()
//   {
//      return this.keyListener;
//   }
//
//   /**
//    * Change/define the click on epty space listener
//    *
//    * @param clickInSpaceListener
//    *           The click in empty space listener (Can us {@code null} for remove the current listener)
//    */
//   public void setClickInSpaceListener(final ClickInSpaceListener clickInSpaceListener)
//   {
//      if(this.clickInSpaceListener != null)
//      {
//         this.sceneRenderer.removeClickInSpaceListener(this.clickInSpaceListener);
//      }
//
//      this.clickInSpaceListener = clickInSpaceListener;
//
//      if(this.clickInSpaceListener != null)
//      {
//         this.sceneRenderer.addClickInSpaceListener(clickInSpaceListener);
//      }
//   }
//
//   /**
//    * Change current key listener. Use {@code null} to remove key listener
//    *
//    * @param keyListener
//    *           New key listener or {@code null} to remove key listener
//    */
//   public void setKeyListner(final KeyListener keyListener)
//   {
//      if(this.keyListener != null)
//      {
//         this.sceneRenderer.removeKeyListener(this.keyListener);
//      }
//
//      this.keyListener = keyListener;
//
//      if(this.keyListener != null)
//      {
//         this.sceneRenderer.addKeyListener(this.keyListener);
//      }
//   }
//
//   /**
//    * Change current mouse listener. Use {@code null} to remove mouse listener <br>
//    * <br>
//    * <b>Parent documentation:</b><br>
//    * {@inheritDoc}
//    *
//    * @param mouseListener
//    *           New mouse listener or {@code null} to remove mouse listener
//    * @see jhelp.gui.twoD.JHelpComponent2D#setMouseListener(jhelp.gui.JHelpMouseListener)
//    */
//   @Override
//   public void setMouseListener(final JHelpMouseListener mouseListener)
//   {
//      final JHelpMouseListener listener = this.getMouseListener();
//
//      if(listener != null)
//      {
//         this.sceneRenderer.removeMouseListener(listener);
//         this.sceneRenderer.removeMouseMotionListener(listener);
//         this.sceneRenderer.removeMouseWheelListener(listener);
//      }
//
//      if(mouseListener != null)
//      {
//         this.sceneRenderer.addMouseListener(mouseListener);
//         this.sceneRenderer.addMouseMotionListener(mouseListener);
//         this.sceneRenderer.addMouseWheelListener(mouseListener);
//      }
//
//      super.setMouseListener(mouseListener);
//   }
//}