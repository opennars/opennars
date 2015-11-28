/**
 * Project : JHelpXML<br>
 * Package : jhelp.xml<br>
 * Class : ParserXML<br>
 * Date : 21 fevr. 2009<br>
 * By JHelp
 */
package jhelp.xml;

import jhelp.util.text.UtilText;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;

/**
 * Parse dynamically an XML.<br>
 * You have to give a {@link ParseXMLlistener} to react at each part of XML meet <br>
 * Last modification : 21 fevr. 2009<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public class ParserXML
{
   /** Default progress listener */
   private static final DefaultParseXMLprogressListener DEFAULT_PROGRESS = new DefaultParseXMLprogressListener();

   /**
    * Obtain a boolean in parameters
    * 
    * @param markup
    *           Markup
    * @param parameters
    *           Parameters
    * @param parameter
    *           Parameter boolean name
    * @param mustHave
    *           Indicates if the parameter must exists
    * @param defaultValue
    *           Value return if the value not exist and <code>mustHave</code> is {@code false}
    * @return The boolean
    * @throws MissingRequiredParameterException
    *            If <code>mustHave</code> is {@code true} and the parameter not exists
    * @throws InvalidParameterValueException
    *            If the parameter is not a boolean
    */
   public static boolean obtainBoolean(final String markup, final Hashtable<String, String> parameters, final String parameter, final boolean mustHave, final boolean defaultValue) throws MissingRequiredParameterException,
         InvalidParameterValueException
   {
      final String value = parameters.get(parameter);

      if((value == null) && (mustHave == true))
      {
         throw new MissingRequiredParameterException(parameter, markup);
      }

      if(value == null)
      {
         return defaultValue;
      }

      if((value.equalsIgnoreCase("true") == false) && (value.equalsIgnoreCase("false") == false))
      {
         throw new InvalidParameterValueException(parameter, markup, UtilText.concatenate(value, " is not a boolean"));
      }

      return Boolean.parseBoolean(value);
   }

   /**
    * Obtain an integer in parameters
    * 
    * @param markup
    *           Markup
    * @param parameters
    *           Parameters
    * @param parameter
    *           Parameter integer name
    * @param mustHave
    *           Indicates if the parameter must exists
    * @param defaultValue
    *           Value return if the value not exist and <code>mustHave</code> is {@code false}
    * @return The intger
    * @throws MissingRequiredParameterException
    *            If <code>mustHave</code> is {@code true} and the parameter not exists
    * @throws InvalidParameterValueException
    *            If the parameter is not an integer
    */
   public static int obtainInteger(final String markup, final Hashtable<String, String> parameters, final String parameter, final boolean mustHave, final int defaultValue) throws MissingRequiredParameterException,
         InvalidParameterValueException
   {
      final String value = parameters.get(parameter);

      if((value == null) && (mustHave == true))
      {
         throw new MissingRequiredParameterException(parameter, markup);
      }

      if(value == null)
      {
         return defaultValue;
      }

      try
      {
         return UtilText.parseInteger(value);
      }
      catch(final Exception exception)
      {
         throw new InvalidParameterValueException(parameter, markup, "Must be an integer", exception);
      }
   }

   /**
    * Obtain a parameter
    * 
    * @param markup
    *           Markup
    * @param parameters
    *           Parameters to look
    * @param parameter
    *           Search parameter
    * @param mustHave
    *           Indicates if the parameter must exist
    * @return Parameter value
    * @throws MissingRequiredParameterException
    *            If <code>mustHave</code> is {@code true} and parameter not exists
    */
   public static String obtainParameter(final String markup, final Hashtable<String, String> parameters, final String parameter, final boolean mustHave) throws MissingRequiredParameterException
   {
      final String value = parameters.get(parameter);

      if((value == null) && (mustHave == true))
      {
         throw new MissingRequiredParameterException(parameter, markup);
      }

      return value;
   }

   /**
    * Read array of char until meet a character under space or = or ><br>
    * The read characters are cumulate in the given {@link StringBuffer}.<br>
    * It return the position of stop character execpt for > that give the previous position
    * 
    * @param stringBuffer
    *           Buffer to fill
    * @param chars
    *           Characters to read
    * @param start
    *           Offset to start read in char array
    * @return The position stop
    */
   public static int readUntilWhiteEqualOrEnd(final StringBuffer stringBuffer, final char[] chars, int start)
   {
      final int length = chars.length;
      char car;
      for(; start < length; start++)
      {
         car = chars[start];
         if((car <= 32) || (car == '=') || (car == '>'))
         {
            if(car == '>')
            {
               return start - 1;
            }
            return start;
         }
         stringBuffer.append(car);
      }
      return start;
   }

   /**
    * Indicates if the parser have to compress space in text or let them as they are
    */
   private boolean                  compressWhiteCharactersInTextOnOneSpace;
   /** Level of parse for the progress listener (If have one) */
   private ParseLevel               parseLevel;
   /** Listener to react on meeting markup, text, comment */
   private ParseXMLlistener         parseXMLlistener;
   /** Listener of progression */
   private ParseXMLprogressListener parseXMLprogressListener;

   /**
    * Constructs ParserXML
    */
   public ParserXML()
   {
      this.compressWhiteCharactersInTextOnOneSpace = false;
      this.parseLevel = ParseLevel.EXCEPTION;
      this.parseXMLprogressListener = ParserXML.DEFAULT_PROGRESS;
   }

   /**
    * Actual parser level
    * 
    * @return Actual parser level
    */
   public ParseLevel getParseLevel()
   {
      return this.parseLevel;
   }

   /**
    * Return compressWhiteCharactersInTextOnOneSpace
    * 
    * @return compressWhiteCharactersInTextOnOneSpace
    */
   public boolean isCompressWhiteCharactersInTextOnOneSpace()
   {
      return this.compressWhiteCharactersInTextOnOneSpace;
   }

   /**
    * Launch a parsing.<br>
    * It didn't close the stream, you have to do it by your self.<br>
    * Beware you have to call {@link #setParseXMLlistener(ParseXMLlistener)} or {@link #parse(ParseXMLlistener, InputStream)}
    * before this
    * 
    * @param inputStream
    *           Stream to parse
    * @throws ExceptionParseXML
    *            On parsing problem
    */
   public void parse(final InputStream inputStream) throws ExceptionParseXML
   {
      if(inputStream == null)
      {
         throw new NullPointerException("inputStream musn't be null");
      }
      if(this.parseXMLlistener == null)
      {
         throw new IllegalStateException("The ParseXMLlistener must be set before parsing");
      }
      int line = 0;
      int column = 0;
      StringBuffer stringBuffer = new StringBuffer();
      boolean onComment = false;
      boolean onQuote = false;
      boolean startMarkup = false;
      boolean onText = false;
      boolean atLeatOneWhiteChar = false;
      boolean searchNextParameter = false;
      boolean backSlash = false;
      boolean waitCloseBarcket = false;
      char[] characters;
      char car;
      int length, indexStart, indexEnd;
      String markup = "";
      String temp;
      final Hashtable<String, String> parameters = new Hashtable<String, String>();
      try
      {
         this.parseXMLlistener.startParse();
         final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
         String lineRead = bufferedReader.readLine();
         while(lineRead != null)
         {
            indexStart = lineRead.indexOf("<?xml");

            if(indexStart >= 0)
            {
               indexEnd = lineRead.indexOf("?>", indexStart + 5);

               if(indexEnd > indexStart)
               {
                  lineRead = lineRead.substring(0, indexStart) + lineRead.substring(indexEnd + 2, lineRead.length());
               }
            }

            line++;
            characters = lineRead.toCharArray();
            length = characters.length;
            for(column = 1; column <= length; column++)
            {
               car = characters[column - 1];
               switch(car)
               {
                  case '<':
                     if((onQuote == true) || (onComment == true))
                     {
                        stringBuffer.append(car);
                     }
                     else if((startMarkup == true) || (waitCloseBarcket == true))
                     {
                        throw new IllegalArgumentException("Syntax error");
                     }
                     else
                     {
                        if(onText == true)
                        {
                           temp = stringBuffer.toString().trim();
                           if(temp.length() > 0)
                           {
                              this.parseXMLlistener.textFind(temp);
                              if(this.parseLevel.isText() == true)
                              {
                                 this.parseXMLprogressListener.findTextMarkup(markup, temp, line, column);
                              }
                           }
                           onText = false;
                        }
                        stringBuffer.delete(0, stringBuffer.length());
                        column = ParserXML.readUntilWhiteEqualOrEnd(stringBuffer, characters, column) + 1;
                        if(stringBuffer.toString().equals("!--") == true)
                        {
                           onComment = true;
                        }
                        else if(stringBuffer.charAt(0) == '/')
                        {
                           waitCloseBarcket = true;
                           this.parseXMLlistener.endMarkup(stringBuffer.substring(1));
                           if(this.parseLevel.isMakup() == true)
                           {
                              this.parseXMLprogressListener.findCloseMarkup(markup, line, column);
                           }
                        }
                        else
                        {
                           if(stringBuffer.charAt(stringBuffer.length() - 1) == '/')
                           {
                              stringBuffer = stringBuffer.delete(stringBuffer.length() - 1, stringBuffer.length());
                           }

                           markup = stringBuffer.toString();
                           parameters.clear();
                           if(this.parseLevel.isMakup() == true)
                           {
                              this.parseXMLprogressListener.findOpenMarkup(markup, line, column);
                           }
                           startMarkup = true;
                           searchNextParameter = true;
                        }
                        stringBuffer.delete(0, stringBuffer.length());
                     }
                     backSlash = false;
                  break;
                  case '>':
                     if((onQuote == true) || (onComment == true))
                     {
                        stringBuffer.append(car);
                        if((onComment == true) && (column > 2) && (characters[column - 2] == '-') && (characters[column - 3] == '-'))
                        {
                           this.parseXMLlistener.commentFind(stringBuffer.substring(0, stringBuffer.length() - 3).trim());
                           stringBuffer.delete(0, stringBuffer.length());
                           onComment = false;
                        }
                     }
                     else
                     {
                        if((waitCloseBarcket == false) && (startMarkup == false))
                        {
                           throw new IllegalArgumentException("Syntax error");
                        }
                        if(startMarkup == true)
                        {
                           this.parseXMLlistener.startMakup(markup, parameters);
                           onText = true;
                        }
                        if((column > 1) && (characters[column - 2] == '/'))
                        {
                           this.parseXMLlistener.endMarkup(markup);
                           if(this.parseLevel.isMakup() == true)
                           {
                              this.parseXMLprogressListener.findCloseMarkup(markup, line, column);
                           }
                           onText = false;
                        }
                        searchNextParameter = false;
                        waitCloseBarcket = false;
                        startMarkup = false;
                     }
                     backSlash = false;
                  break;
                  case '\\':
                     if((onComment == false) && (onQuote == false))
                     {
                        throw new IllegalArgumentException("Syntax error");
                     }
                     if(backSlash == true)
                     {
                        stringBuffer.append(car);
                        backSlash = false;
                     }
                     else
                     {
                        backSlash = true;
                     }
                  break;
                  case '"':
                     if((onText == true) || (onComment == true) || (backSlash == true))
                     {
                        stringBuffer.append(car);
                        backSlash = false;
                     }
                     else
                     {
                        onQuote = !onQuote;
                     }
                  break;
                  default:
                     if(searchNextParameter == true)
                     {
                        if(car > 32)
                        {
                           if(car == '=')
                           {
                              temp = stringBuffer.toString();
                              stringBuffer.delete(0, stringBuffer.length());
                              column++;
                              car = characters[column - 1];
                              backSlash = false;
                              onQuote = true;
                              if(car != '"')
                              {
                                 throw new IllegalArgumentException("Syntax error");
                              }
                              while(onQuote == true)
                              {
                                 column++;
                                 car = characters[column - 1];
                                 switch(car)
                                 {
                                    case '\\':
                                       if(backSlash == true)
                                       {
                                          stringBuffer.append(car);
                                       }
                                       backSlash = !backSlash;
                                    break;
                                    case '"':
                                       if(backSlash == true)
                                       {
                                          stringBuffer.append(car);
                                          backSlash = false;
                                       }
                                       else
                                       {
                                          onQuote = false;
                                       }
                                    break;
                                    default:
                                       stringBuffer.append(car);
                                    break;
                                 }
                              }
                              parameters.put(temp, stringBuffer.toString());
                              if(this.parseLevel.isParameter() == true)
                              {
                                 this.parseXMLprogressListener.findParameter(markup, temp, line, column);
                              }

                              stringBuffer.delete(0, stringBuffer.length());
                           }
                           else
                           {
                              stringBuffer.append(car);
                           }
                        }
                     }
                     else
                     {
                        if(((onText == true) || (onComment == true)) && (car <= 32))
                        {
                           if((atLeatOneWhiteChar == false) || (this.compressWhiteCharactersInTextOnOneSpace == false))
                           {
                              stringBuffer.append(' ');
                              atLeatOneWhiteChar = true;
                           }
                        }
                        else if(car >= 32)
                        {
                           stringBuffer.append(car);
                           atLeatOneWhiteChar = false;
                        }
                     }
               }
            }
            lineRead = bufferedReader.readLine();
            if((lineRead != null) && ((onText == true) || (onComment == true)))
            {
               if((atLeatOneWhiteChar == false) || (this.compressWhiteCharactersInTextOnOneSpace == false))
               {
                  stringBuffer.append(' ');
                  atLeatOneWhiteChar = true;
               }
            }
         }
         this.parseXMLlistener.endParse();
      }
      catch(final Exception exception)
      {
         final ExceptionParseXML exceptionParseXML = new ExceptionParseXML("Error on parsing", line, column, exception);
         this.parseXMLprogressListener.exceptionAppend(exceptionParseXML);
         this.parseXMLlistener.exceptionForceEndParse(exceptionParseXML);
         throw exceptionParseXML;
      }
   }

   /**
    * Parse a stream
    * 
    * @param parseXMLlistener
    *           Listener for react on marhup meet
    * @param inputStream
    *           Stream to parse
    * @throws ExceptionParseXML
    *            On parsing problem
    */
   public void parse(final ParseXMLlistener parseXMLlistener, final InputStream inputStream) throws ExceptionParseXML
   {
      this.setParseXMLlistener(parseXMLlistener);
      this.parse(inputStream);
   }

   /**
    * Modify compressWhiteCharactersInTextOnOneSpace
    * 
    * @param compressWhiteCharactersInTextOnOneSpace
    *           New compressWhiteCharactersInTextOnOneSpace value
    */
   public void setCompressWhiteCharactersInTextOnOneSpace(final boolean compressWhiteCharactersInTextOnOneSpace)
   {
      this.compressWhiteCharactersInTextOnOneSpace = compressWhiteCharactersInTextOnOneSpace;
   }

   /**
    * Modify parseLevel
    * 
    * @param parseLevel
    *           New parseLevel value
    */
   public void setParseLevel(final ParseLevel parseLevel)
   {
      if(parseLevel == null)
      {
         throw new NullPointerException("parseLevel musn't be null");
      }

      this.parseLevel = parseLevel;
   }

   /**
    * Change the reaction on markup meet
    * 
    * @param parseXMLlistener
    *           Listener off parsing
    */
   public void setParseXMLlistener(final ParseXMLlistener parseXMLlistener)
   {
      if(parseXMLlistener == null)
      {
         throw new NullPointerException("parseXMLlistener musn't be null");
      }
      this.parseXMLlistener = parseXMLlistener;
   }

   /**
    * Change progress parsing listener
    * 
    * @param parseLevel
    *           Parsing level
    * @param parseXMLprogressListener
    *           New progress parsing listener
    */
   public void setParseXMLprogressListener(final ParseLevel parseLevel, final ParseXMLprogressListener parseXMLprogressListener)
   {
      if(parseLevel == null)
      {
         throw new NullPointerException("parseLevel musn't be null");
      }
      if(parseXMLprogressListener == null)
      {
         this.parseXMLprogressListener = ParserXML.DEFAULT_PROGRESS;
         return;
      }
      this.parseXMLprogressListener = parseXMLprogressListener;
   }
}