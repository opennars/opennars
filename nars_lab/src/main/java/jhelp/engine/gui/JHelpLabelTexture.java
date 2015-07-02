//package jhelp.engine.gui;
//
//import java.awt.Dimension;
//
//import jhelp.engine.Texture;
//import jhelp.gui.twoD.JHelpComponent2D;
//import jhelp.util.gui.JHelpImage;
//
///**
// * Lael 2d to show a texture
// *
// * @author JHelp
// */
//public class JHelpLabelTexture
//      extends JHelpComponent2D
//{
//   /** Image that draw the texture */
//   private final JHelpImage imageTexture;
//   /** Label texture height */
//   private final int        previewHeight;
//   /** Label texture width */
//   private final int        previewWidth;
//   /** Texture to draw */
//   private Texture          texture;
//
//   /**
//    * Create a new instance of JHelpLabelTexture
//    *
//    * @param width
//    *           Label width
//    * @param height
//    *           Label height
//    */
//   public JHelpLabelTexture(final int width, final int height)
//   {
//      this(null, width, height);
//   }
//
//   /**
//    * Create a new instance of JHelpLabelTexture
//    *
//    * @param texture
//    *           Texture to draw
//    * @param width
//    *           Label width
//    * @param height
//    *           Label height
//    */
//   public JHelpLabelTexture(final Texture texture, final int width, final int height)
//   {
//      this.previewWidth = width;
//      this.previewHeight = height;
//      this.imageTexture = new JHelpImage(this.previewWidth, this.previewHeight);
//      this.setTexture(texture);
//   }
//
//   /**
//    * Compute Label preferred size <br>
//    * <br>
//    * <b>Parent documentation:</b><br>
//    * {@inheritDoc}
//    *
//    * @param parentWidth
//    *           Parent width (-1 if unknkow)
//    * @param parentHeight
//    *           Parent height (-1 if unknow)
//    * @return Preferred size
//    * @see jhelp.gui.twoD.JHelpComponent2D#computePreferredSize(int, int)
//    */
//   @Override
//   protected Dimension computePreferredSize(final int parentWidth, final int parentHeight)
//   {
//      return new Dimension(this.previewWidth, this.previewHeight);
//   }
//
//   /**
//    * Draw the label <br>
//    * <br>
//    * <b>Parent documentation:</b><br>
//    * {@inheritDoc}
//    *
//    * @param x
//    *           X position on image parent
//    * @param y
//    *           Y position on image parent
//    * @param parent
//    *           Image parent where draw the label
//    * @see jhelp.gui.twoD.JHelpComponent2D#paint(int, int, jhelp.util.gui.JHelpImage)
//    */
//   @Override
//   protected void paint(final int x, final int y, final JHelpImage parent)
//   {
//      synchronized(this.imageTexture)
//      {
//         parent.drawImage(x, y, this.imageTexture);
//      }
//   }
//
//   /**
//    * Texture shown
//    *
//    * @return Texture shown
//    */
//   public Texture getTexture()
//   {
//      return this.texture;
//   }
//
//   /**
//    * Change the texture show
//    *
//    * @param texture
//    *           New texture to show (can be {@code null} to remove the texture)
//    */
//   public void setTexture(final Texture texture)
//   {
//      this.texture = texture;
//
//      synchronized(this.imageTexture)
//      {
//         this.imageTexture.startDrawMode();
//         if(texture == null)
//         {
//            final int w = this.previewWidth >> 3;
//            final int h = this.previewHeight >> 3;
//            boolean dark = true;
//            boolean dark1 = true;
//            for(int y = 0; y < this.previewHeight; y += h)
//            {
//               dark = dark1;
//               for(int x = 0; x < this.previewWidth; x += w)
//               {
//                  this.imageTexture.fillRectangle(x, y, w, h, dark == true
//                        ? 0xFF404040
//                        : 0xFFC0C0C0, false);
//
//                  dark = !dark;
//               }
//               dark1 = !dark1;
//            }
//         }
//         else
//         {
//            this.imageTexture.fillRectangleScale(0, 0, this.previewWidth, this.previewHeight, texture.toJHelpImage(), false);
//         }
//         this.imageTexture.endDrawMode();
//      }
//   }
//}