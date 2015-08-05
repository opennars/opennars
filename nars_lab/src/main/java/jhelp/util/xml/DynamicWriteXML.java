package jhelp.util.xml;

import jhelp.util.text.UtilText;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Stack;

/**
 * Write dynamically an XML to a stream.<br>
 * No need to create a XML structure for writing it.
 * 
 * @author JHelp
 */
public class DynamicWriteXML
{
   /** End of comment */
   private static final String  COMMENT_END                = " -->";
   /** Start of comment */
   private static final String  COMMENT_START              = "<!-- ";
   /** Status indicate that a markup have to be open */
   private static final int     STATUS_HAVE_TO_OPEN_MARKUP = 1;
   /** Status indicates that a markup just be closed */
   private static final int     STATUS_MARKUP_CLOSED       = 3;
   /** Status indicates that a markup is open */
   private static final int     STATUS_MARKUP_OPENED       = 2;
   /** Status indicates that all is finsish */
   private static final int     STATUS_TERMINATE           = -1;
   /** Status indicates that a text just writen */
   private static final int     STATUS_TEXT_WRITEN         = 4;
   /** Tabulation characters */
   private static final String  TAB                        = "   ";
   /** UTF 8 header */
   private static final String  UTF8_FORMAT                = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";
   /** Buffer writer use to write on the stream */
   private final BufferedWriter bufferedWriter;
   /** Indicates if have to close writer when finish */
   private final boolean        closeWriterAtEnd;
   /** Current stack of markups */
   private final Stack<String>  markups;
   /** Current status */
   private int                  status;
   /** Current number of tabulation */
   private int                  tab;
   /** Indicates if the text is on same line as the markup that contains it */
   private final boolean        textSameLine;

   /**
    * Create a new instance of DynamicWriteXML not compact and text in separate line
    * 
    * @param outputStream
    *           Stream where write
    * @throws IOException
    *            On writing header issue
    */
   public DynamicWriteXML(final OutputStream outputStream)
         throws IOException
   {
      this(outputStream, false);
   }

   /**
    * Create a new instance of DynamicWriteXML not compact
    * 
    * @param outputStream
    *           Stream where write
    * @param textSameLine
    *           Indicates if text have to be in same line
    * @throws IOException
    *            On writing header issue
    */
   public DynamicWriteXML(final OutputStream outputStream, final boolean textSameLine)
         throws IOException
   {
      this(outputStream, false, textSameLine);
   }

   /**
    * Create a new instance of DynamicWriteXML
    * 
    * @param outputStream
    *           Stream where write
    * @param compact
    *           Indicates if use compact mode
    * @param textSameLine
    *           Indicates if text at same line as its markup. Text will be forced in same line if compact mode is {@code true}
    * @throws IOException
    *            On writing header issue
    */
   public DynamicWriteXML(final OutputStream outputStream, final boolean compact, final boolean textSameLine)
         throws IOException
   {
      this(outputStream, compact, textSameLine, true);
   }

   /**
    * Create a new instance of DynamicWriteXML
    * 
    * @param outputStream
    *           Stream where write
    * @param compact
    *           Indicates if use compact mode
    * @param textSameLine
    *           Indicates if text at same line as its markup. Text will be forced in same line if compact mode is {@code true}
    * @param closeWriterAtEnd
    *           Indicates if have to close the stream when finish
    * @throws IOException
    *            On writing header issue
    */
   public DynamicWriteXML(final OutputStream outputStream, final boolean compact, final boolean textSameLine, final boolean closeWriterAtEnd)
         throws IOException
   {
      this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, UtilText.UTF8));

      this.bufferedWriter.write(DynamicWriteXML.UTF8_FORMAT);
      this.bufferedWriter.newLine();

      if(compact == true)
      {
         this.tab = -1;
      }
      else
      {
         this.tab = 0;
      }

      this.markups = new Stack<String>();
      this.status = DynamicWriteXML.STATUS_HAVE_TO_OPEN_MARKUP;
      this.textSameLine = textSameLine;
      this.closeWriterAtEnd = closeWriterAtEnd;
   }

   /**
    * Add a comment
    * 
    * @param comment
    *           Comment to add
    * @throws IOException
    *            On writing issue
    */
   public void appendComment(final String comment) throws IOException
   {
      if(this.status == DynamicWriteXML.STATUS_MARKUP_OPENED)
      {
         throw new IllegalStateException("A markup is opened, please \"wait\" its closed !");
      }

      if((this.tab >= 0) && (this.textSameLine == false))
      {
         this.bufferedWriter.newLine();

         for(int i = 0; i < this.tab; i++)
         {
            this.bufferedWriter.write(DynamicWriteXML.TAB);
         }
      }

      this.bufferedWriter.write(DynamicWriteXML.COMMENT_START);
      this.bufferedWriter.write(comment);
      this.bufferedWriter.write(DynamicWriteXML.COMMENT_END);
   }

   /**
    * Append a boolean parameter at current markup
    * 
    * @param name
    *           Parameter name
    * @param value
    *           Parameter value
    * @throws IOException
    *            On writing issue
    */
   public void appendParameter(final String name, final boolean value) throws IOException
   {
      this.appendParameter(name, value == true
            ? "TRUE"
            : "FALSE");
   }

   /**
    * Append a double parameter at current markup
    * 
    * @param name
    *           Parameter name
    * @param value
    *           Parameter value
    * @throws IOException
    *            On writing issue
    */
   public void appendParameter(final String name, final double value) throws IOException
   {
      this.appendParameter(name, String.valueOf(value));
   }

   /**
    * Append a float parameter at current markup
    * 
    * @param name
    *           Parameter name
    * @param value
    *           Parameter value
    * @throws IOException
    *            On writing issue
    */
   public void appendParameter(final String name, final float value) throws IOException
   {
      this.appendParameter(name, String.valueOf(value));
   }

   /**
    * Append a int parameter at current markup
    * 
    * @param name
    *           Parameter name
    * @param value
    *           Parameter value
    * @throws IOException
    *            On writing issue
    */
   public void appendParameter(final String name, final int value) throws IOException
   {
      this.appendParameter(name, String.valueOf(value));
   }

   /**
    * Append a string parameter to current markup
    * 
    * @param name
    *           Parameter name
    * @param value
    *           Parameter value
    * @throws IOException
    *            On writing issue
    */
   public void appendParameter(final String name, final String value) throws IOException
   {
      if(this.status != DynamicWriteXML.STATUS_MARKUP_OPENED)
      {
         throw new IllegalStateException("No markup open !");
      }

      this.bufferedWriter.write(" ");
      this.bufferedWriter.write(name);
      this.bufferedWriter.write("=\"");
      this.bufferedWriter.write(value);
      this.bufferedWriter.write("\"");
   }

   /**
    * Close current markup
    * 
    * @return {@code true} if there are more markup to close. {@code false} if this is the last markup close and the the XML
    *         writing is finish
    * @throws IOException
    *            On writing issue
    */
   public boolean closeMarkup() throws IOException
   {
      switch(this.status)
      {
         case STATUS_TERMINATE:
            throw new IllegalStateException("The writing can't continue");
         case STATUS_HAVE_TO_OPEN_MARKUP:
            throw new IllegalStateException("Have to start by open a markup");
         case STATUS_MARKUP_CLOSED:
         case STATUS_TEXT_WRITEN:
            if(this.markups.isEmpty() == true)
            {
               throw new IllegalStateException("No markup to close");
            }

            if(this.tab >= 0)
            {
               this.tab--;
            }

            if((this.textSameLine == false) || (this.status != DynamicWriteXML.STATUS_TEXT_WRITEN))
            {
               if(this.tab >= 0)
               {
                  this.bufferedWriter.newLine();
               }

               for(int i = 0; i < this.tab; i++)
               {
                  this.bufferedWriter.write(DynamicWriteXML.TAB);
               }
            }

            this.bufferedWriter.write("</");
            this.bufferedWriter.write(this.markups.pop());
            this.bufferedWriter.write(">");
         break;
         case STATUS_MARKUP_OPENED:
            if(this.markups.isEmpty() == true)
            {
               throw new IllegalStateException("No markup to close");
            }

            this.markups.pop();
            this.bufferedWriter.write("/>");

            if(this.tab >= 0)
            {
               this.tab--;
            }
         break;
      }

      this.status = DynamicWriteXML.STATUS_MARKUP_CLOSED;

      if(this.markups.isEmpty() == true)
      {
         this.status = DynamicWriteXML.STATUS_TERMINATE;

         this.bufferedWriter.flush();

         if(this.closeWriterAtEnd == true)
         {
            this.bufferedWriter.close();
         }

         return false;
      }

      return true;
   }

   /**
    * Open a markup
    * 
    * @param markup
    *           Markup name
    * @throws IOException
    *            On writing issue
    */
   public void openMarkup(final String markup) throws IOException
   {
      switch(this.status)
      {
         case STATUS_TERMINATE:
            throw new IllegalStateException("The writing can't continue");
         case STATUS_MARKUP_OPENED:
            this.bufferedWriter.write(">");

            if(this.tab >= 0)
            {
               this.bufferedWriter.newLine();
            }
         break;
         case STATUS_MARKUP_CLOSED:
            if(this.tab >= 0)
            {
               this.bufferedWriter.newLine();
            }

            if(this.markups.isEmpty() == true)
            {
               throw new IllegalStateException("Can't open markup after close the main one");
            }
         break;
         case STATUS_TEXT_WRITEN:
            throw new IllegalStateException("Can't open markup without have closing a markup with some text");
      }

      this.markups.push(markup);

      for(int i = 0; i < this.tab; i++)
      {
         this.bufferedWriter.write(DynamicWriteXML.TAB);
      }

      if(this.tab >= 0)
      {
         this.tab++;
      }

      this.bufferedWriter.write("<");
      this.bufferedWriter.write(markup);

      this.status = DynamicWriteXML.STATUS_MARKUP_OPENED;
   }

   /**
    * Append a text to current markup
    * 
    * @param text
    *           Text to write
    * @throws IOException
    *            On writing issue
    */
   public void setText(final String text) throws IOException
   {
      if(this.status != DynamicWriteXML.STATUS_MARKUP_OPENED)
      {
         throw new IllegalStateException("No markup open !");
      }
      this.bufferedWriter.write(">");

      if(this.textSameLine == false)
      {
         if(this.tab >= 0)
         {
            this.bufferedWriter.newLine();
         }

         for(int i = 0; i < this.tab; i++)
         {
            this.bufferedWriter.write(DynamicWriteXML.TAB);
         }
      }

      this.bufferedWriter.write(text);

      this.status = DynamicWriteXML.STATUS_TEXT_WRITEN;
   }
}