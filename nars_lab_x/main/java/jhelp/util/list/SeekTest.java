package jhelp.util.list;

/**
 * Test for seek element
 * 
 * @author JHelp
 * @param <TYPE>
 *           Element seek type
 */
public interface SeekTest<TYPE>
{
   /**
    * Indicates if an element is the seek one
    * 
    * @param element
    *           Element test
    * @return {@code true} if the element match the seek test
    */
   public boolean isElementSeek(TYPE element);
}