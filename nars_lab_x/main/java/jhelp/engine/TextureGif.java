//package jhelp.engine;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.InputStream;
//
//import jhelp.util.debug.Debug;
//import jhelp.util.gui.GIF;
//import jhelp.util.thread.ThreadManager;
//import jhelp.util.thread.ThreadedSimpleTask;
//import jhelp.util.thread.ThreadedVerySimpleTask;
//
///**
// * Texture with GIF image.<br>
// * If GIF is animated, the animtaion is played
// *
// * @author JHelp
// */
//public class TextureGif
//      extends Texture
//{
//   /**
//    * Task to load the GIF in background thread
//    *
//    * @author JHelp
//    */
//   class TaskObtainGIF
//         extends ThreadedSimpleTask<File>
//   {
//      /**
//       * Create a new instance of TaskObtainGIF
//       */
//      TaskObtainGIF()
//      {
//      }
//
//      /**
//       * Do the task to get the GIF image <br>
//       * <br>
//       * <b>Parent documentation:</b><br>
//       * {@inheritDoc}
//       *
//       * @param parameter
//       *           Image GIF file
//       * @see jhelp.util.thread.ThreadedSimpleTask#doSimpleAction(Object)
//       */
//      @Override
//      protected void doSimpleAction(final File parameter)
//      {
//         TextureGif.this.obtainGIF(parameter);
//      }
//   }
//
//   /**
//    * Task for refresh GIF animation
//    *
//    * @author JHelp
//    */
//   class TaskRefreshGIF
//         extends ThreadedVerySimpleTask
//   {
//      /**
//       * Create a new instance of TaskRefreshGIF
//       */
//      TaskRefreshGIF()
//      {
//      }
//
//      /**
//       * Refresh the texture in GIF animation <br>
//       * <br>
//       * <b>Parent documentation:</b><br>
//       * {@inheritDoc}
//       *
//       * @see jhelp.util.thread.ThreadedVerySimpleTask#doVerySimpleAction()
//       */
//      @Override
//      protected void doVerySimpleAction()
//      {
//         TextureGif.this.refreshGIF();
//      }
//   }
//
//   /** Indicates if GIF is animated on the texture */
//   private boolean              animate;
//   /** Associated GIF */
//   private GIF                  gif;
//   /** Image index in animation */
//   private int                  index;
//   /** Indicates if next refresh is launched */
//   private boolean              launch;
//   /** Synchronization lock */
//   private final Object         lock;
//   /** Task for load the GIF */
//   private final TaskObtainGIF  taskObtainGIF;
//   /** Task for refresh GIF animation */
//   private final TaskRefreshGIF taskRefreshGIF;
//
//   /**
//    * Create a new instance of TextureGif
//    *
//    * @param file
//    *           Image GIF file
//    */
//   public TextureGif(final File file)
//   {
//      super(file.getAbsolutePath(), Texture.REFERENCE_IMAGE_GIF);
//
//      if(GIF.isGIF(file) == false)
//      {
//         throw new IllegalArgumentException("The file " + file.getAbsolutePath() + " is not a valid GIF");
//      }
//
//      this.index = -1;
//      this.animate = true;
//      this.launch = false;
//      this.taskObtainGIF = new TaskObtainGIF();
//      this.taskRefreshGIF = new TaskRefreshGIF();
//      this.lock = new Object();
//      this.setPixels(1, 1, new byte[4]);
//      ThreadManager.THREAD_MANAGER.doThread(this.taskObtainGIF, file);
//   }
//
//   /**
//    * Load the GIF image<br>
//    * Called by a background thread
//    *
//    * @param file
//    *           Image GIF file
//    */
//   void obtainGIF(final File file)
//   {
//      InputStream inputStream = null;
//
//      try
//      {
//         inputStream = new FileInputStream(file);
//         this.gif = new GIF(inputStream);
//
//         synchronized(this.lock)
//         {
//            this.animate = true;
//            this.launch = true;
//         }
//
//         ThreadManager.THREAD_MANAGER.doThread(this.taskRefreshGIF, null);
//      }
//      catch(final Exception exception)
//      {
//         Debug.printException(exception, "Failed to load GIF : ", file.getAbsolutePath());
//         this.gif = null;
//         this.setPixels(1, 1, new byte[4]);
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
//    * Refresh GIF animation.<br>
//    * Called by a background thread
//    */
//   void refreshGIF()
//   {
//      if(this.gif == null)
//      {
//         return;
//      }
//
//      synchronized(this.lock)
//      {
//         if(this.animate == false)
//         {
//            this.launch = false;
//            return;
//         }
//      }
//
//      this.index = (this.index + 1) % this.gif.numberOfImage();
//      this.setImage(this.gif.getImage(this.index));
//      final long time = Math.max(this.gif.getDelay(this.index), 16);
//
//      synchronized(this.lock)
//      {
//         if(this.animate == false)
//         {
//            this.launch = false;
//            return;
//         }
//
//         this.launch = true;
//         ThreadManager.THREAD_MANAGER.delayedThread(this.taskRefreshGIF, null, time);
//      }
//   }
//
//   /**
//    * Called by garbage collector when object is destroy to free some memory <br>
//    * <br>
//    * <b>Parent documentation:</b><br>
//    * {@inheritDoc}
//    *
//    * @throws Throwable
//    *            On issue
//    * @see Object#finalize()
//    */
//   @Override
//   protected void finalize() throws Throwable
//   {
//      if(this.gif != null)
//      {
//         this.setAnimate(false);
//         this.gif.destroy();
//      }
//
//      super.finalize();
//   }
//
//   /**
//    * Change the GIF image file.<br>
//    * Nothing happen if file not a valid GIF image file
//    *
//    * @param gifFile
//    *           New GIF image file
//    */
//   public void changeGifFile(final File gifFile)
//   {
//      if(GIF.isGIF(gifFile) == false)
//      {
//         return;
//      }
//
//      this.setAnimate(false);
//      ThreadManager.THREAD_MANAGER.doThread(this.taskObtainGIF, gifFile);
//   }
//
//   /**
//    * Indicates if GIF is animated
//    *
//    * @return {@code true} if GIF is animated
//    */
//   public boolean isAnimate()
//   {
//      synchronized(this.lock)
//      {
//         return this.animate;
//      }
//   }
//
//   /**
//    * Start/stop the animation
//    *
//    * @param animate
//    *           New animation state
//    */
//   public void setAnimate(final boolean animate)
//   {
//      synchronized(this.lock)
//      {
//         if(this.animate == animate)
//         {
//            return;
//         }
//
//         this.animate = animate;
//
//         if((this.animate == true) && (this.launch == false) && (this.gif != null))
//         {
//            this.launch = true;
//            ThreadManager.THREAD_MANAGER.doThread(this.taskRefreshGIF, null);
//         }
//      }
//   }
//}