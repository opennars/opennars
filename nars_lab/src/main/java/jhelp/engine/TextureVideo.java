///**
// * Project : JHelpSceneGraph<br>
// * Package : jhelp.engine<br>
// * Class : TextureVideo<br>
// * Date : 27 sept. 2008<br>
// * By JHelp
// */
//package jhelp.engine;
//
//import java.awt.Color;
//import java.awt.Image;
//import java.io.IOException;
//
//import jhelp.util.Utilities;
//
///**
// * Texture witch carry a video<br>
// * Video is read loop, if it is possible <br>
// * <br>
// * Last modification : 22 janv. 2009<br>
// * Version 0.0.1<br>
// *
// * @author JHelp
// */
//public class TextureVideo
//      extends Texture
//      implements Runnable
//{
//   /** Video FPS play */
//   private int               fps;
//   /** Indicates if the video is on pause */
//   private boolean           pause;
//   /** Thread for refresh the video */
//   private Thread            thread;
//   /** Video reader */
//   private final VideoReader videoReader;
//
//   /**
//    * Constructs TextureVideo with standard 25 FPS
//    *
//    * @param videoReader
//    *           Video reader
//    */
//   public TextureVideo(final VideoReader videoReader)
//   {
//      this(videoReader, 25);
//   }
//
//   /**
//    * Constructs TextureVideo with a FPS.<br>
//    * The texture is refresh on trying to respect the chosen FPS, but if it's too big, it refresh as soon as it cans
//    *
//    * @param videoReader
//    *           Video reader
//    * @param fps
//    *           FPS for read video
//    */
//   public TextureVideo(final VideoReader videoReader, final int fps)
//   {
//      super(videoReader.getName(), Texture.REFERENCE_VIDEO);
//      int width;
//      int height;
//
//      this.videoReader = videoReader;
//      this.setFPS(fps);
//      this.pause = false;
//
//      width = this.videoReader.getWidth();
//      height = this.videoReader.getHeight();
//
//      if((width < 1) || (height < 1))
//      {
//         throw new IllegalArgumentException("The video must have width>0 and height>0");
//      }
//
//      this.setPixels(width, height, new byte[width * height * 4]);
//      this.fillRect(0, 0, width, height, Color.WHITE, false);
//   }
//
//   /**
//    * Video FPS
//    *
//    * @return Video FPS
//    */
//   public int getFPS()
//   {
//      return this.fps;
//   }
//
//   /**
//    * Indicates if the video is on pause
//    *
//    * @return {@code true} if the video is on pause
//    */
//   public boolean isPause()
//   {
//      return this.pause;
//   }
//
//   /**
//    * Don't call it directly !<br>
//    * It is called by the thread<br>
//    * This method refresh the video
//    *
//    * @see Runnable#run()
//    */
//   @Override
//   public void run()
//   {
//      Image image;
//      long sleep;
//      long start;
//
//      // While the video is alive
//      while(this.thread != null)
//      {
//         // Wait until we are not in pause
//         while((this.pause == true) && (this.thread != null))
//         {
//            try
//            {
//               Thread.sleep(1000);
//            }
//            catch(final Exception exception)
//            {
//            }
//         }
//         // Refresh the video
//         while((this.pause == false) && (this.thread != null))
//         {
//            // Is an other image to read ?
//            if(this.videoReader.hasNextImage() == true)
//            {
//               // Prepare the FPS synchronization
//               sleep = 1000L / this.fps;
//               start = System.currentTimeMillis();
//               // Read an print the next image
//               try
//               {
//                  image = this.videoReader.nextImage();
//
//                  while((image.getWidth(null) < 1) || (image.getHeight(null) < 1))
//                  {
//                     Utilities.sleep(4);
//                  }
//
//                  while((this.pause == true) && (this.thread != null))
//                  {
//                     try
//                     {
//                        Thread.sleep(1000);
//                     }
//                     catch(final Exception exception)
//                     {
//                     }
//                  }
//
//                  this.drawImage(0, 0, image);
//               }
//               catch(final IOException e)
//               {
//               }
//
//               // If left time before the FPS, wait a moment
//               sleep = sleep - (System.currentTimeMillis() - start);
//               if(sleep < 1)
//               {
//                  sleep = 1;
//               }
//               try
//               {
//                  Thread.sleep(sleep);
//               }
//               catch(final Exception exception)
//               {
//               }
//            }
//            else
//            {
//               // No more image
//               this.thread = null;
//            }
//         }
//      }
//
//      image = null;
//   }
//
//   /**
//    * Change the FPS
//    *
//    * @param fps
//    *           New FPS
//    */
//   public void setFPS(int fps)
//   {
//      if(fps < 1)
//      {
//         fps = 1;
//      }
//
//      if(fps > 100)
//      {
//         fps = 100;
//      }
//
//      this.fps = fps;
//   }
//
//   /**
//    * Change pause state
//    *
//    * @param pause
//    *           New pause state
//    */
//   public void setPause(final boolean pause)
//   {
//      this.pause = pause;
//   }
//
//   /**
//    * Start read the video
//    */
//   public void startVideo()
//   {
//      this.pause = false;
//      if(this.thread == null)
//      {
//         this.thread = new Thread(this);
//         this.thread.start();
//      }
//   }
//
//   /**
//    * Stop read the video
//    */
//   public void stopVideo()
//   {
//      this.thread = null;
//   }
//}