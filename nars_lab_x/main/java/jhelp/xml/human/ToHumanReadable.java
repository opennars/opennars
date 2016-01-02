/**
 * Project : JHelpXML<br>
 * Package : jhelp.xml.human<br>
 * Class : ToHumanReadable<br>
 * Date : 13 mai 2009<br>
 * By JHelp
 */
package jhelp.xml.human;

import jhelp.util.debug.Debug;
import jhelp.util.io.UtilIO;
import jhelp.util.text.UtilText;
import jhelp.util.xml.DynamicWriteXML;
import jhelp.xml.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Convert an XML compact to human readable XML<br>
 * <br>
 * Last modification : 13 may 2009<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public class ToHumanReadable
      implements ParseXMLlistener
{

   /**
    * Launch the converter
    * 
    * @param args
    *           Unused
    */
   public static void main(final String[] args)
   {
      try
      {
         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      }
      catch(final Exception exception)
      {
      }

      final FileDialog fileDialog = new FileDialog(new JFrame());

      boolean oneMore = true;
      String file;
      String directory;
      File source;
      File destitnation;
      DynamicWriteXML dynamicWriteXML;
      ToHumanReadable toHumanReadable;
      ParserXML parserXML = new ParserXML();
      parserXML.setCompressWhiteCharactersInTextOnOneSpace(true);

      while(oneMore == true)
      {
         JOptionPane.showMessageDialog(null, "Choose the file to convert");

         fileDialog.setMode(FileDialog.LOAD);
         fileDialog.setVisible(true);

         file = fileDialog.getFile();
         directory = fileDialog.getDirectory();

         if((file == null) || (directory == null))
         {
            JOptionPane.showMessageDialog(null, "If you don't choose something, the program can't continue");
            break;
         }

         source = new File(directory, file);
         if(source.exists() == false)
         {
            JOptionPane.showMessageDialog(null, UtilText.concatenate("File ", source.getAbsolutePath(), " doesn't exists !"), "Stupid !", JOptionPane.WARNING_MESSAGE);
            break;
         }

         JOptionPane.showMessageDialog(null, "Choose where save the converted file (must be different than the previous one)");

         fileDialog.setMode(FileDialog.SAVE);
         fileDialog.setVisible(true);

         file = fileDialog.getFile();
         directory = fileDialog.getDirectory();

         if((file == null) || (directory == null))
         {
            JOptionPane.showMessageDialog(null, "If you don't choose something, the program can't continue", "Stupid !", JOptionPane.WARNING_MESSAGE);
            break;
         }

         destitnation = new File(directory, file);

         if(source.equals(destitnation) == true)
         {
            JOptionPane.showMessageDialog(null, "You looking for program crash ? I said choose a different one", "Idiot !", JOptionPane.ERROR_MESSAGE);
            break;
         }

         if(UtilIO.createFile(destitnation) == false)
         {
            JOptionPane.showMessageDialog(null, UtilText.concatenate("Can't create ", destitnation.getAbsolutePath()), "Error !", JOptionPane.ERROR_MESSAGE);
            break;
         }

         FileInputStream fileInputStream = null;

         try
         {
            dynamicWriteXML = new DynamicWriteXML(new FileOutputStream(destitnation), false);
            toHumanReadable = new ToHumanReadable(dynamicWriteXML);
            fileInputStream = new FileInputStream(source);
            parserXML.parse(toHumanReadable, fileInputStream);

            JOptionPane.showMessageDialog(null, "Conversion done with success !", "Done !", JOptionPane.INFORMATION_MESSAGE);
         }
         catch(final Exception exception)
         {
            JOptionPane.showMessageDialog(null, UtilText.concatenate("<html>Problem append during conversion : <b>", exception.getLocalizedMessage(), "</b><br>See console for more details</html>"), "Error !",
                  JOptionPane.ERROR_MESSAGE);
            exception.printStackTrace();
         }
         finally
         {
            if(fileInputStream != null)
            {
               try
               {
                  fileInputStream.close();
               }
               catch(final Exception exception)
               {
               }
            }
         }

         oneMore = JOptionPane.showConfirmDialog(null, "Do you want do an other conversion ?", "One more ?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
      }

      dynamicWriteXML = null;
      toHumanReadable = null;
      parserXML = null;
      file = directory = null;
      source = destitnation = null;

      JOptionPane.showMessageDialog(null, "Good bye !");
      System.exit(0);
   }

   /** Write the XML */
   private final DynamicWriteXML dynamicWriteXML;

   /**
    * Constructs ToHumanReadable
    * 
    * @param dynamicWriteXML
    *           Where write XML
    */
   public ToHumanReadable(final DynamicWriteXML dynamicWriteXML)
   {
      this.dynamicWriteXML = dynamicWriteXML;
   }

   /**
    * Action on meet comment
    * 
    * @param comment
    *           Comment read
    * @see jhelp.xml.ParseXMLlistener#commentFind(String)
    */
   @Override
   public void commentFind(final String comment)
   {
      try
      {
         this.dynamicWriteXML.appendComment(comment);
      }
      catch(final Exception exception)
      {
         Debug.printException(exception, "Append comment failed !");
      }
   }

   /**
    * Call when a markup end
    * 
    * @param markupName
    *           Markup name
    * @throws UnexpectedEndOfMarkup
    *            If end of markup can't be happen here
    * @see jhelp.xml.ParseXMLlistener#endMarkup(String)
    */
   @Override
   public void endMarkup(final String markupName) throws UnexpectedEndOfMarkup
   {
      try
      {
         this.dynamicWriteXML.closeMarkup();
      }
      catch(final IOException exception)
      {
         throw new UnexpectedEndOfMarkup("Close markup failed", exception);
      }
   }

   /**
    * End the parsing
    * 
    * @throws UnexpectedEndOfParse
    *            If the parsing can't end now
    * @see jhelp.xml.ParseXMLlistener#endParse()
    */
   @Override
   public void endParse() throws UnexpectedEndOfParse
   {
   }

   /**
    * Call when exception happen and force to stop the parsing
    * 
    * @param exceptionParseXML
    *           Exception source
    * @see jhelp.xml.ParseXMLlistener#exceptionForceEndParse(jhelp.xml.ExceptionParseXML)
    */
   @Override
   public void exceptionForceEndParse(final ExceptionParseXML exceptionParseXML)
   {
      exceptionParseXML.printStackTrace();
   }

   /**
    * Call when markup starts
    * 
    * @param markupName
    *           Markup name
    * @param parameters
    *           Markup parameters
    * @throws MissingRequiredParameterException
    *            If a parameter missing
    * @throws InvalidParameterValueException
    *            If a parameter have an unexpected value
    * @see jhelp.xml.ParseXMLlistener#startMakup(String, Hashtable)
    */
   @Override
   public void startMakup(final String markupName, final Hashtable<String, String> parameters) throws MissingRequiredParameterException, InvalidParameterValueException
   {
      try
      {
         this.dynamicWriteXML.openMarkup(markupName);
      }
      catch(final IOException exception)
      {
         Debug.printException(exception);

         throw new RuntimeException("Issue while opening markup : " + markupName, exception);
      }

      final Enumeration<String> names = parameters.keys();
      String name;
      while(names.hasMoreElements() == true)
      {
         name = names.nextElement();

         try
         {
            this.dynamicWriteXML.appendParameter(name, parameters.get(name));
         }
         catch(final IOException exception)
         {
            Debug.printException(exception);

            throw new RuntimeException("Issue while adding parameter " + name + " in " + markupName, exception);
         }
      }
   }

   /**
    * Start the parsing
    * 
    * @see jhelp.xml.ParseXMLlistener#startParse()
    */
   @Override
   public void startParse()
   {
   }

   /**
    * Call when text found
    * 
    * @param text
    *           Text find
    * @throws InvalidTextException
    *            If text is not valid
    * @see jhelp.xml.ParseXMLlistener#textFind(String)
    */
   @Override
   public void textFind(final String text) throws InvalidTextException
   {
      try
      {
         this.dynamicWriteXML.setText(text);
      }
      catch(final IOException exception)
      {
         throw new InvalidTextException("Issue while writing text", exception);
      }
   }
}