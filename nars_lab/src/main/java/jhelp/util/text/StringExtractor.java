package jhelp.util.text;

import jhelp.util.Utilities;
import jhelp.util.list.Pair;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Cut string with separator, like {@link StringTokenizer}, but in addition it can detect Strings and not cut on them, it can
 * also ignore escaped character.<br>
 * By example, you type :<br>
 * <code lang="java"><!--
 * StringExtractor extractor=new StringExtractor("Hello world ! 'This is a phrase'");
 * System.out.println(extractor.next());
 * System.out.println(extractor.next());
 * System.out.println(extractor.next());
 * System.out.println(extractor.next());
 * --></code> <br>
 * You will obtain <br>
 * <code>Hello<br>world<br>!<br>This is a phrase</code>
 * 
 * @author JHelp
 */
public class StringExtractor
{
   /** Escape characters */
   private final char[]                                escapeCharacters;
   /** Current read index */
   private int                                         index;
   /** String to parse length */
   private final int                                   length;
   /** Open/close pairs, to consider like "normal" character something between an open and a close character */
   private final ArrayList<Pair<Character, Character>> openCloseIgnore;
   /** Indicates if separators are return */
   private final boolean                               returnSeparators;
   /** Separators characters */
   private final char[]                                separators;
   /** String to parse */
   private final char[]                                string;
   /** String delimiters */
   private final char[]                                stringLimiters;

   /**
    * Create a new instance of StringExtractor with default separators (see {@link UtilText#DEFAULT_SEPARATORS}), string
    * delimiters (see {@link UtilText#DEFAULT_STRING_LIMITERS}) and escape characters (see
    * {@link UtilText#DEFAULT_ESCAPE_CHARACTERS}). And no return of separators
    * 
    * @param string
    *           String to parse
    */
   public StringExtractor(final String string)
   {
      this(string, false);
   }

   /**
    * Create a new instance of StringExtractor with default separators (see {@link UtilText#DEFAULT_SEPARATORS}), string
    * delimiters (see {@link UtilText#DEFAULT_STRING_LIMITERS}) and escape characters (see
    * {@link UtilText#DEFAULT_ESCAPE_CHARACTERS})
    * 
    * @param string
    *           String to parse
    * @param returnSeparators
    *           Indicates if return separators
    */
   public StringExtractor(final String string, final boolean returnSeparators)
   {
      this(string, UtilText.DEFAULT_SEPARATORS, UtilText.DEFAULT_STRING_LIMITERS, UtilText.DEFAULT_ESCAPE_CHARACTERS, returnSeparators);
   }

   /**
    * Create a new instance of StringExtractor with no return separators
    * 
    * @param string
    *           String to parse
    * @param separators
    *           Separators list
    * @param stringLimiters
    *           String delimiters
    * @param escapeCharacters
    *           Escape characters
    */
   public StringExtractor(final String string, final String separators, final String stringLimiters, final String escapeCharacters)
   {
      this(string, separators, stringLimiters, escapeCharacters, false);
   }

   /**
    * Create a new instance of StringExtractor
    * 
    * @param string
    *           String to parse
    * @param separators
    *           Separators list
    * @param stringLimiters
    *           String delimiters
    * @param escapeCharacters
    *           Escape characters
    * @param returnSeparators
    *           Indicates if return separators
    */
   public StringExtractor(final String string, final String separators, final String stringLimiters, final String escapeCharacters, final boolean returnSeparators)
   {
      this.string = string.toCharArray();
      this.separators = separators.toCharArray();
      this.stringLimiters = stringLimiters.toCharArray();
      this.escapeCharacters = escapeCharacters.toCharArray();
      this.returnSeparators = returnSeparators;

      this.index = 0;
      this.length = string.length();

      this.openCloseIgnore = new ArrayList<Pair<Character, Character>>();
   }

   /**
    * Add a open close pairs, to consider like "normal" character something between an open and a close character
    * 
    * @param open
    *           Open character
    * @param close
    *           Close character
    */
   public void addOpenCloseIgnore(final char open, final char close)
   {
      if(open == close)
      {
         throw new IllegalArgumentException("Open and close can't have same value");
      }

      for(final Pair<Character, Character> openClose : this.openCloseIgnore)
      {
         if((openClose.element1 == open) || (openClose.element2 == open) || (openClose.element1 == close) || (openClose.element2 == close))
         {
            throw new IllegalArgumentException("Open or close is already used !");
         }
      }

      this.openCloseIgnore.add(new Pair<Character, Character>(open, close));
   }

   /**
    * Next extracted string.<br>
    * It can be a separator if you ask for return them.<br>
    * It returns {@code null} if no more string to extract
    * 
    * @return Next part or {@code null} if no more to extract
    */
   public String next()
   {
      if(this.index >= this.length)
      {
         return null;
      }

      boolean insideString = false;
      int start = this.index;
      int end = this.length;
      char currentStringLimiter = ' ';
      Pair<Character, Character> openClose = null;
      char character = this.string[this.index];

      do
      {
         if(openClose == null)
         {
            for(final Pair<Character, Character> openClos : this.openCloseIgnore)
            {
               if(openClos.element1 == character)
               {
                  openClose = openClos;
                  break;
               }
            }
         }

         if(openClose == null)
         {
            if(insideString == true)
            {
               if(currentStringLimiter == character)
               {
                  insideString = false;

                  end = this.index;
                  this.index++;

                  break;
               }
            }
            else if(Utilities.contains(character, this.escapeCharacters) == true)
            {
               this.index++;
            }
            else if(Utilities.contains(character, this.stringLimiters) == true)
            {
               if(start < this.index)
               {
                  end = this.index;

                  break;
               }

               start++;
               insideString = true;
               currentStringLimiter = character;
            }
            else if(Utilities.contains(character, this.separators) == true)
            {
               if(start < this.index)
               {
                  end = this.index;

                  break;
               }

               if(this.returnSeparators == true)
               {
                  end = start + 1;
                  this.index++;

                  break;
               }

               start++;
            }
         }
         else if(character == openClose.element2)
         {
            openClose = null;
         }

         this.index++;

         if(this.index < this.length)
         {
            character = this.string[this.index];
         }
      }
      while(this.index < this.length);

      return new String(this.string, start, end - start);
   }
}