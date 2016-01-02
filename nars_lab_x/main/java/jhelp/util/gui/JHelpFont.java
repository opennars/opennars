package jhelp.util.gui;

import java.awt.*;
import java.awt.font.LineMetrics;
import java.awt.image.BufferedImage;

/**
 * Represents a font with underline information
 * 
 * @author JHelp
 */
public final class JHelpFont
      implements ConstantsGUI
{
   /** Default font */
   public static final JHelpFont DEFAULT      = new JHelpFont("Monospaced", 18);

   /** Character unicode for the smiley :) */
   public static final char      SMILEY_HAPPY = (char) 0x263A;
   /** Character unicode for the smiley :( */
   public static final char      SMILEY_SAD   = (char) 0x2639;

   /** Embeded font */
   private final Font            font;
   /** Metrics for measure strings */
   private final FontMetrics     fontMetrics;
   /** Font maximum character width */
   private int                   maximumWidth = -1;
   /** Underline information */
   private final boolean         underline;

   /**
    * Create a new instance of JHelpFont not bold, not italic, not underline
    * 
    * @param familly
    *           Font family name
    * @param size
    *           Font size
    */
   public JHelpFont(final String familly, final int size)
   {
      this(familly, size, false);
   }

   /**
    * Create a new instance of JHelpFont not italic, not underline
    * 
    * @param familly
    *           Font family name
    * @param size
    *           Font size
    * @param bold
    *           Indicates if bold or not
    */
   public JHelpFont(final String familly, final int size, final boolean bold)
   {
      this(familly, size, bold, false);
   }

   /**
    * Create a new instance of JHelpFont not underline
    * 
    * @param familly
    *           Font family name
    * @param size
    *           Font size
    * @param bold
    *           Bold enable/disable
    * @param italic
    *           Italic enable/disable
    */
   public JHelpFont(final String familly, final int size, final boolean bold, final boolean italic)
   {
      this(familly, size, bold, italic, false);
   }

   /**
    * Create a new instance of JHelpFont
    * 
    * @param familly
    *           Font family name
    * @param size
    *           Font size
    * @param bold
    *           Bold enable/disable
    * @param italic
    *           Italic enable/disable
    * @param underline
    *           Underline enable/disable
    */
   public JHelpFont(final String familly, final int size, final boolean bold, final boolean italic, final boolean underline)
   {
      this.underline = underline;

      int style = Font.PLAIN;

      if(bold == true)
      {
         style |= Font.BOLD;
      }

      if(italic == true)
      {
         style |= Font.ITALIC;
      }

      this.font = new Font(familly, style, size);

      final BufferedImage bufferedImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
      final Graphics2D graphics2d = bufferedImage.createGraphics();
      this.fontMetrics = graphics2d.getFontMetrics(this.font);
      graphics2d.dispose();
      bufferedImage.flush();
   }

   /**
    * Compute shape of a string
    * 
    * @param string
    *           String
    * @param x
    *           X position of top-left
    * @param y
    *           Y position of top-left
    * @return Computed shape
    */
   public Shape computeShape(final String string, final int x, final int y)
   {
      return this.font.createGlyphVector(ConstantsGUI.FONT_RENDER_CONTEXT, string).getOutline(x, y + this.font.getLineMetrics(string, ConstantsGUI.FONT_RENDER_CONTEXT).getAscent());
   }

//   /**
//    * Compute text lines representation with this font
//    *
//    * @param text
//    *           Text to use
//    * @param textAlign
//    *           Align to use
//    * @return The couple of the list of each computed lines and the total size of all lines together
//    */
//   public Pair<List<JHelpTextLine>, Dimension> computeTextLines(final String text, final JHelpTextAlign textAlign)
//   {
//      return this.computeTextLines(text, textAlign, Integer.MAX_VALUE);
//   }
//
//   /**
//    * Compute text lines representation with this font
//    *
//    * @param text
//    *           Text to use
//    * @param textAlign
//    *           Align to use
//    * @param limitWidth
//    *           Number maximum of pixels in width
//    * @return The couple of the list of each computed lines and the total size of all lines together
//    */
//   public Pair<List<JHelpTextLine>, Dimension> computeTextLines(final String text, final JHelpTextAlign textAlign, final int limitWidth)
//   {
//      return this.computeTextLines(text, textAlign, limitWidth, Integer.MAX_VALUE);
//   }
//
//   /**
//    * Compute text lines representation with this font
//    *
//    * @param text
//    *           Text to use
//    * @param textAlign
//    *           Align to use
//    * @param limitWidth
//    *           Number maximum of pixels in width
//    * @param limitHeight
//    *           Limit height in pixels
//    * @return The couple of the list of each computed lines and the total size of all lines together
//    */
//   public Pair<List<JHelpTextLine>, Dimension> computeTextLines(final String text, final JHelpTextAlign textAlign, final int limitWidth, final int limitHeight)
//   {
//      return this.computeTextLines(text, textAlign, limitWidth, limitHeight, true);
//   }
//
//   /**
//    * Compute text lines representation with this font
//    *
//    * @param text
//    *           Text to use
//    * @param textAlign
//    *           Align to use
//    * @param limitWidth
//    *           Number maximum of pixels in width
//    * @param limitHeight
//    *           Limit height in pixels
//    * @param trim
//    *           Indicates if have to trim lines
//    * @return The couple of the list of each computed lines and the total size of all lines together
//    */
//   public Pair<List<JHelpTextLine>, Dimension> computeTextLines(final String text, final JHelpTextAlign textAlign, final int limitWidth, final int limitHeight, final boolean trim)
//   {
//      final int limit = Math.max(this.fontMetrics.getMaxAdvance() + 2, limitWidth);
//
//      final ArrayList<JHelpTextLine> textLines = new ArrayList<JHelpTextLine>();
//      final StringExtractor lines = new StringExtractor(text, "\n\f\r", "", "");
//      final Dimension size = new Dimension();
//
//      int width, index, start;
//      final int height = this.getHeight();
//
//      String line = lines.next();
//      String head, tail;
//
//      while(line != null)
//      {
//         if(trim == true)
//         {
//            line = line.trim();
//         }
//
//         width = this.stringWidth(line);
//         index = line.length() - 1;
//         head = line;
//         tail = "";
//
//         while((width > limit) && (index > 0))
//         {
//            start = index;
//            index = UtilText.lastIndexOf(line, index, ' ', '\t', '\'', '&', '~', '"', '#', '{', '(', '[', '-', '|', '`', '_', '\\', '^', '@', '°', ')', ']', '+', '=', '}', '"', 'µ', '*', ',', '?', '.', ';', ':', '/', '!', '§', '<',
//                  '>', '²');
//
//            if(index >= 0)
//            {
//               if(trim == true)
//               {
//                  head = line.substring(0, index).trim();
//                  tail = line.substring(index).trim();
//               }
//               else
//               {
//                  head = line.substring(0, index);
//                  tail = line.substring(index);
//               }
//            }
//            else
//            {
//               start--;
//               index = start;
//               head = line.substring(0, index) + "-";
//               tail = line.substring(index);
//            }
//
//            width = this.stringWidth(head);
//            if(width <= limit)
//            {
//               size.width = Math.max(size.width, width);
//
//               textLines.add(new JHelpTextLine(head, 0, size.height, width, height, this.createMask(head)));
//
//               size.height += height;
//
//               line = tail;
//               head = tail;
//               tail = "";
//               width = this.stringWidth(line);
//               index = line.length() - 1;
//
//               if(size.height >= limitHeight)
//               {
//                  break;
//               }
//            }
//            else
//            {
//               index--;
//            }
//         }
//
//         if(size.height >= limitHeight)
//         {
//            break;
//         }
//
//         size.width = Math.max(size.width, width);
//
//         textLines.add(new JHelpTextLine(line, 0, size.height, width, height, this.createMask(line)));
//
//         size.height += height;
//
//         if(size.height >= limitHeight)
//         {
//            break;
//         }
//
//         line = lines.next();
//      }
//
//      for(final JHelpTextLine textLine : textLines)
//      {
//         switch(textAlign)
//         {
//            case CENTER:
//               textLine.x = (size.width - textLine.getWidth()) >> 1;
//            break;
//            case LEFT:
//               textLine.x = 0;
//            break;
//            case RIGHT:
//               textLine.x = size.width - textLine.getWidth();
//            break;
//         }
//      }
//
//      size.width = Math.max(1, size.width);
//      size.height = Math.max(1, size.height);
//      return new Pair<List<JHelpTextLine>, Dimension>(Collections.unmodifiableList(textLines), size);
//   }

   /**
    * Create a mask from a string
    * 
    * @param string
    *           String to convert in mask
    * @return Created mask
    */
   public JHelpMask createMask(final String string)
   {
      final int width = Math.max(1, this.fontMetrics.stringWidth(string));
      final int height = Math.max(1, this.fontMetrics.getHeight());
      final int ascent = this.fontMetrics.getAscent();

      int[] pixels = new int[width * height];
      final BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
      bufferedImage.setRGB(0, 0, width, height, pixels, 0, width);
      final Graphics2D graphics2d = bufferedImage.createGraphics();
      graphics2d.setColor(Color.WHITE);
      graphics2d.setFont(this.font);
      graphics2d.drawString(string, 0, ascent);
      pixels = bufferedImage.getRGB(0, 0, width, height, pixels, 0, width);

      final JHelpMask mask = new JHelpMask(width, height);
      int pix = 0;
      for(int y = 0; y < height; y++)
      {
         for(int x = 0; x < width; x++)
         {
            mask.setValue(x, y, (pixels[pix++] & 0xFF) > 0x80);
         }
      }

      graphics2d.dispose();
      bufferedImage.flush();

      return mask;
   }

   /**
    * Indicates if an object is equals to this font <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param object
    *           Compared object
    * @return {@code true} if equals
    * @see Object#equals(Object)
    */
   @Override
   public boolean equals(final Object object)
   {
      if(object == null)
      {
         return false;
      }

      if(object == this)
      {
         return true;
      }

      if((object instanceof JHelpFont) == false)
      {
         return false;
      }

      final JHelpFont font = (JHelpFont) object;

      if(this.underline != font.underline)
      {
         return false;
      }

      return this.font.equals(font.font);
   }

   /**
    * Font family
    * 
    * @return Font family
    */
   public String getFamily()
   {
      return this.font.getFamily();
   }

   /**
    * {@link Font} embed by this font
    * 
    * @return {@link Font} embed by this font
    */
   public Font getFont()
   {
      return this.font;
   }

   /**
    * Font height
    * 
    * @return Font height
    */
   public int getHeight()
   {
      return this.fontMetrics.getHeight();
   }

   /**
    * Maximum character witdh
    * 
    * @return Biggest width of one character
    */
   public int getMaximumCharacterWidth()
   {
      if(this.maximumWidth < 0)
      {
         for(char car = 32; car < 128; car++)
         {
            this.maximumWidth = Math.max(this.maximumWidth, this.fontMetrics.charWidth(car));
         }
      }

      return this.maximumWidth;
   }

   /**
    * Font size
    * 
    * @return Font size
    */
   public int getSize()
   {
      return this.font.getSize();
   }

   /**
    * Indicates if font is bold
    * 
    * @return {@code true} if font is bold
    */
   public boolean isBold()
   {
      return (this.font.getStyle() & Font.BOLD) != 0;
   }

   /**
    * Indicates if font is italic
    * 
    * @return {@code true} if font is italic
    */
   public boolean isItalic()
   {
      return (this.font.getStyle() & Font.ITALIC) != 0;
   }

   /**
    * Underline status
    * 
    * @return Underline status
    */
   public boolean isUnderline()
   {
      return this.underline;
   }

   /**
    * Compute size of a string
    * 
    * @param string
    *           String to measure
    * @return String size
    */
   public Dimension stringSize(final String string)
   {
      return new Dimension(this.stringWidth(string), this.getHeight());
   }

   /**
    * Compute string width
    * 
    * @param string
    *           String to measure
    * @return String width
    */
   public int stringWidth(final String string)
   {
      return this.fontMetrics.stringWidth(string);
   }

   /**
    * Compute underline position
    * 
    * @param string
    *           String
    * @param y
    *           Y of top
    * @return Y result
    */
   public int underlinePosition(final String string, final int y)
   {
      final LineMetrics lineMetrics = this.font.getLineMetrics(string, ConstantsGUI.FONT_RENDER_CONTEXT);

      return Math.round(y + lineMetrics.getUnderlineOffset() + lineMetrics.getAscent());
   }
}