package jhelp.util.filter;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Pattern;

/**
 * File filter based on regular expression.<br>
 * See {@link Pattern java.util.regex.Pattern} javadoc for explanation about regular expression
 * 
 * @author JHelp
 */
public class RegexFileFilter
      implements FileFilter
{
   /** Regular expression */
   private String  regex;
   /** Indicates if directory are also filter or all are accepted */
   private boolean useRegexOnDirectory;

   /**
    * Create a new instance of RegexFileFilter that accept all files and directory
    */
   public RegexFileFilter()
   {
      this(false, null);
   }

   /**
    * Create a new instance of RegexFileFilter that accept all files and directory
    * 
    * @param useRegexOnDirectory
    *           Indicates if a regular expression is given later, that is apply also on directory ({@code true}) or all
    *           directory are always accepted ({@code false})
    */
   public RegexFileFilter(final boolean useRegexOnDirectory)
   {
      this(useRegexOnDirectory, null);
   }

   /**
    * Create a new instance of RegexFileFilter
    * 
    * @param useRegexOnDirectory
    *           Indicates if the regular expression is apply also on directory ({@code true}) or all directory are always
    *           accepted ({@code false})
    * @param regex
    *           Regular expression. See {@link Pattern java.util.regex.Pattern} javadoc for explanation about regular expression
    */
   public RegexFileFilter(final boolean useRegexOnDirectory, final String regex)
   {
      this.useRegexOnDirectory = useRegexOnDirectory;
      this.regex = regex;
   }

   /**
    * Create a new instance of RegexFileFilter that accept all directories
    * 
    * @param regex
    *           Regular expression. See {@link Pattern java.util.regex.Pattern} javadoc for explanation about regular expression
    */
   public RegexFileFilter(final String regex)
   {
      this(false, regex);
   }

   /**
    * Indicates if a file/directory pass the filter <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param pathname
    *           Tested file/directory
    * @return {@code true} if the file/directory pass the filter
    * @see FileFilter#accept(File)
    */
   @Override
   public boolean accept(final File pathname)
   {
      if((this.useRegexOnDirectory == false) && (pathname.isDirectory() == true))
      {
         return true;
      }

      if(this.regex == null)
      {
         return true;
      }

      String name = pathname.getName().trim();
      if((name == null) || (name.length() == 0))
      {
         name = pathname.toString();
      }

      return name.matches(this.regex);
   }

   /**
    * The regular expression used by the filter<br>
    * See {@link Pattern java.util.regex.Pattern} javadoc for explanation about regular expression
    * 
    * @return The regular expression used by the filter
    */
   public String getRegex()
   {
      return this.regex;
   }

   /**
    * Indicats if driectory are also filtered by the regular expression
    * 
    * @return {@code true} if driectory are also filtered by the regular expression
    */
   public boolean isUseRegexOnDirectory()
   {
      return this.useRegexOnDirectory;
   }

   /**
    * Change the regular expression.<br>
    * {@code null} for accept all file/directory<br>
    * See {@link Pattern java.util.regex.Pattern} javadoc for explanation about regular expression
    * 
    * @param regex
    *           New regular expression
    */
   public void setRegex(final String regex)
   {
      this.regex = regex;
   }

   /**
    * Change the fact that directories are also filter or not
    * 
    * @param useRegexOnDirectory
    *           {@code true} for filter directory also
    */
   public void setUseRegexOnDirectory(final boolean useRegexOnDirectory)
   {
      this.useRegexOnDirectory = useRegexOnDirectory;
   }
}