package jhelp.util.math.random;

import jhelp.util.debug.Debug;
import jhelp.util.debug.DebugLevel;
import jhelp.util.reflection.Reflector;
import jhelp.util.text.UtilText;

import java.util.ArrayList;

/**
 * Able to choice an object randomly.<br>
 * The chance to have an object can be different than to have an& nbsp;other one.<br>
 * For example if <font color="#008800">"A"</font> have 50% chance, <font color="#008800">"B"</font> 25%, <font
 * color="#008800">"C"</font> 12%, <font color="#008800">"D"</font> 6%,<font color="#008800">"E"</font> 3%, <font
 * color="#008800">"F"</font> 2%, <font color="#008800">"G"</font> 1% and <font color="#008800">"H"</font> 1% :<br>
 * <code lang="java"><!--
 * JHelpRandom<String> random = new JHelpRandom<String>();
 * random.addChoice(50, "A");
 * random.addChoice(25, "B");
 * random.addChoice(12, "C");
 * random.addChoice(6,  "D");
 * random.addChoice(3,  "E");
 * random.addChoice(2,  "F");
 * random.addChoice(1,  "G");
 * random.addChoice(1,  "H");
 * --></code> After each time you do<br>
 * <code lang="java"><!--
 * String choice = random.choose();
 * --></code> Choice have 50% chance to be <font color="#008800">"A"</font>,<br>
 * 25% chance to be <font color="#008800">"B"</font>, <br>
 * 12% chance to be <font color="#008800">"C"</font>, <br>
 * 6% chance to be <font color="#008800">"D"</font>, <br>
 * 3% chance to be <font color="#008800">"E"</font>, <br>
 * 2% chance to be <font color="#008800">"F"</font>, <br>
 * 1% chance to be <font color="#008800">"G"</font> and <br>
 * 1% chance to be <font color="#008800">"H"</font><br>
 * <br>
 * You can also see things like this, you have a set of element compose of 55 <font color="#008800">"P"</font> and 87 <font
 * color="#008800">"R"</font> :<br>
 * <code lang="java"><!--
 * JHelpRandom<String> random = new JHelpRandom<String>();
 * random.addChoice(55, "P");
 * random.addChoice(87, "R");
 * --></code> After each time you do<br>
 * <code lang="java"><!--
 * String choice = random.choose();
 * --></code> Choice have 55 chance of 142 to be <font color="#008800">"P"</font> and 87 chance of 142 to be <font
 * color="#008800">"R"</font>
 * 
 * @author JHelp
 * @param <CHOICE>
 *           Choice type
 */
public final class JHelpRandom<CHOICE>
{
   /**
    * A registered limit.<br>
    * This is a couple of value maximum and element
    * 
    * @author JHelp
    * @param <ELEMENT>
    *           Element type
    */
   private class Limit<ELEMENT>
   {
      /** The element */
      final ELEMENT element;
      /** Maximum value */
      final int     maximum;

      /**
       * Create a new instance of Limit
       * 
       * @param maximum
       *           Maximum value
       * @param element
       *           Element
       */
      public Limit(final int maximum, final ELEMENT element)
      {
         this.maximum = maximum;
         this.element = element;
      }

      /**
       * String representation <br>
       * <br>
       * <b>Parent documentation:</b><br>
       * {@inheritDoc}
       * 
       * @return String representation
       * @see Object#toString()
       */
      @Override
      public String toString()
      {
         return UtilText.concatenate("Limit (", this.maximum, " : ", this.element, ")");
      }
   }

   /**
    * Choose a value of an enum
    * 
    * @param <E>
    *           Enum to get a value
    * @param clas
    *           Enum class
    * @return An enum value or {@code null} if failed
    */
   @SuppressWarnings(
   {
         "rawtypes", "unchecked"
   })
   public static final <E extends Enum> E random(final Class<E> clas)
   {
      E[] array = null;

      try
      {
         array = (E[]) Reflector.invokePublicMethod(clas, "values");
      }
      catch(final Exception exception)
      {
         Debug.printException(exception, "Failed to get values of ", clas.getName());

         return null;
      }

      return JHelpRandom.random(array);
   }

   /**
    * Give a random value between 0 (include) and given limit (exclude)
    * 
    * @param limit
    *           Limit to respect
    * @return Random value
    */
   public static final int random(final int limit)
   {
      if(limit == 0)
      {
         throw new IllegalArgumentException("limit can't be 0");
      }

      return (int) (Math.random() * limit);
   }

   /**
    * Give random number inside an interval, each limit are includes
    * 
    * @param minimum
    *           Minimum value
    * @param maximum
    *           Maximum value
    * @return Random value
    */
   public static final int random(final int minimum, final int maximum)
   {
      final int min = Math.min(minimum, maximum);
      final int max = Math.max(minimum, maximum);

      return min + (int) (Math.random() * ((max - min) + 1));
   }

   /**
    * Return an element of an array.<br>
    * {@code null} is return is the array is {@code null} or empty
    * 
    * @param <T>
    *           Type of array's element
    * @param array
    *           Array to get one element
    * @return Element get or {@code null} if array {@code null} or empty
    */
   public static final <T> T random(final T[] array)
   {
      if((array == null) || (array.length == 0))
      {
         return null;
      }

      return array[JHelpRandom.random(array.length)];
   }

   /** Registered limits */
   private final ArrayList<Limit<CHOICE>> limits;
   /** Actual maximum */
   private int                            maximum;

   /**
    * Create a new instance of JHelpRandom
    */
   public JHelpRandom()
   {
      this.limits = new ArrayList<Limit<CHOICE>>();
      this.maximum = 0;
   }

   /**
    * Add a choice
    * 
    * @param number
    *           Frequency of the choice (Can't be < 1)
    * @param choice
    *           The choice
    */
   public void addChoice(final int number, final CHOICE choice)
   {
      if(number <= 0)
      {
         throw new IllegalArgumentException("number MUST be > 0, not " + number);
      }

      this.limits.add(new Limit<CHOICE>((this.maximum + number) - 1, choice));

      this.maximum += number;
   }

   /**
    * Choose a value randomly
    * 
    * @return Chosen value
    */
   public CHOICE choose()
   {
      if(this.maximum == 0)
      {
         Debug.println(DebugLevel.WARNING, "You have to add at least something to be able have a result");

         return null;
      }

      final int random = JHelpRandom.random(this.maximum);

      for(final Limit<CHOICE> limit : this.limits)
      {
         if(random <= limit.maximum)
         {
            return limit.element;
         }
      }

      Debug.println(DebugLevel.ERROR, "Shouldn't arrive here !!! random=", random, " maximum=", this.maximum, " limits=", this.limits);
      return null;
   }

   /**
    * String representation <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @return String representation
    * @see Object#toString()
    */
   @Override
   public String toString()
   {
      return UtilText.concatenate("JHelpRandom maximum=", this.maximum, " limits=", this.limits);
   }
}