package jhelp.util.list;

import java.util.HashMap;
import java.util.Map.Entry;

/**
 * Hash map with limited number of element in memory.<br>
 * Here its limited by "weight", that is to say when element is add :
 * <ol>
 * <li>If theire enough space, the element is add</li>
 * <li>If its allowed, remove less used elements to make enough room to put the new one</li>
 * </ol>
 * 
 * @author JHelp
 * @param <KEY>
 *           Key type
 * @param <VALUE>
 *           Value type
 */
public class LimitedWeightHashMapFrequency<KEY, VALUE extends HeavyObject>
      implements LimitedWeightHashMap<KEY, VALUE>
{
   /**
    * Map element
    * 
    * @author JHelp
    */
   class Element
   {
      /** Usage count of element */
      long  frequency;
      /** Element value */
      VALUE value;

      /**
       * Create a new instance of Element
       * 
       * @param value
       *           Carry value
       */
      Element(final VALUE value)
      {
         this.frequency = 1;
         this.value = value;
      }
   }

   /** Weight free left */
   private long                        freeWeight;
   /** Map of elements */
   private final HashMap<KEY, Element> hashMap;
   /** Maximum weight */
   private final long                  maximumWeight;

   /**
    * Create a hash map with limited number of element in memory
    * 
    * @param maximumWeight
    *           Maximum weight
    */
   public LimitedWeightHashMapFrequency(final long maximumWeight)
   {
      this.hashMap = new HashMap<KEY, Element>();
      this.maximumWeight = Math.max(1024L, maximumWeight);
      this.freeWeight = this.maximumWeight;
   }

   /**
    * Automatic remove operation for making room
    * 
    * @param keepIt
    *           Element to keep ({@code null} if no element to keep)
    * @param weightNeed
    *           Minimum free room need
    */
   private void automaticRemove(final Element keepIt, final long weightNeed)
   {
      Entry<KEY, Element> lessFrequency;

      while(this.freeWeight < weightNeed)
      {
         lessFrequency = this.lessFrequencyElement(keepIt);

         this.freeWeight += lessFrequency.getValue().value.getWeight();
         this.hashMap.remove(lessFrequency.getKey());
      }
   }

   /**
    * Obtain the less used element in the map
    * 
    * @param ignoreIt
    *           Elemnt to ignore ({@code null} if no element to ignore)
    * @return Less used element in the map
    */
   private Entry<KEY, Element> lessFrequencyElement(final Element ignoreIt)
   {
      long min = Long.MAX_VALUE;
      Entry<KEY, Element> lessFrequency = null;
      Element element;

      for(final Entry<KEY, Element> entry : this.hashMap.entrySet())
      {
         element = entry.getValue();
         if((element.value != ignoreIt) && (element.frequency < min))
         {
            lessFrequency = entry;
            min = entry.getValue().frequency;
         }
      }

      return lessFrequency;
   }

   /**
    * Obtain an element of the map <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param key
    *           Element key
    * @return Element value
    * @see LimitedWeightHashMap#get(Object)
    */
   @Override
   public VALUE get(final KEY key)
   {
      final Element element = this.hashMap.get(key);

      if(element == null)
      {
         return null;
      }

      element.frequency++;
      return element.value;
   }

   /**
    * Free weight left <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @return Free weight left
    * @see LimitedWeightHashMap#getFreeWeight()
    */
   @Override
   public long getFreeWeight()
   {
      return this.freeWeight;
   }

   /**
    * Maximum weight <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @return Maximum weight
    * @see LimitedWeightHashMap#getMaximuWeight()
    */
   @Override
   public long getMaximuWeight()
   {
      return this.maximumWeight;
   }

   /**
    * Number of elements inside the map <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @return Number of elements inside the map
    * @see LimitedWeightHashMap#getSize()
    */
   @Override
   public int getSize()
   {
      return this.hashMap.size();
   }

   /**
    * Actual map weight <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @return Actual map weight
    * @see HeavyObject#getWeight()
    */
   @Override
   public long getWeight()
   {
      return this.maximumWeight - this.freeWeight;
   }

   /**
    * Add/modify element in the map <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param key
    *           Element key
    * @param creator
    *           Describe how to create the element (To be able avoid create object if not add/modify)
    * @return {@code true} if value is add or modify without removing any object. {@code false} if at least one element is
    *         removes from the map
    * @see LimitedWeightHashMap#put(Object, HeavyObjectCreator)
    * @throws IllegalArgumentException
    *            If the element weight is bigger than map maximum weight
    */
   @Override
   public boolean put(final KEY key, final HeavyObjectCreator<VALUE> creator)
   {
      final Result result = this.put(key, creator, true);

      if(result == Result.TOO_MUCH_HEAVY)
      {
         throw new IllegalArgumentException("The given object is too heavy for the list");
      }

      return (result == Result.UPDATED) || (result == Result.ADDED);
   }

   /**
    * Add/modify element in the map.<br>
    * The result can be :
    * <table border=0>
    * <tr>
    * <th>{@link Result#ADD_WITH_AT_LEAST_ONE_REMOVED}</th>
    * <td>:</td>
    * <td>If the element is add, but at least one element is removed frrom the list (Only happen if remove is allow)</td>
    * </tr>
    * <tr>
    * <th>{@link Result#ADDED}</th>
    * <td>:</td>
    * <td>If the element is added, and no element is removed frrom the list</td>
    * </tr>
    * <tr>
    * <th>{@link Result#NOT_ADDED}</th>
    * <td>:</td>
    * <td>If the element is not added, it need to make room but remove is not allow</td>
    * </tr>
    * <tr>
    * <th>{@link Result#NOT_UPDATED}</th>
    * <td>:</td>
    * <td>If the element is not updated, it need make room but remove not allowed</td>
    * </tr>
    * <tr>
    * <th>{@link Result#TOO_MUCH_HEAVY}</th>
    * <td>:</td>
    * <td>If the element weight is bigger than the maximum weight</td>
    * </tr>
    * <tr>
    * <th>{@link Result#UPDATED}</th>
    * <td>:</td>
    * <td>If the element is updated</td>
    * </tr>
    * <tr>
    * <th>{@link Result#UPDATED_WITH_AT_LEAST_ONE_REMOVED}</th>
    * <td>:</td>
    * <td>If the element isupdated, but at least one element is removed frrom the list (Only happen if remove is allow)</td>
    * </tr>
    * </table>
    * <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param key
    *           Element key
    * @param creator
    *           Describe how to create the element (To be able avoid create object if not add/modify)
    * @param allowAutomaticRemove
    *           Indicates if remove elemnts to make room is allowed
    * @return The result operation
    * @see LimitedWeightHashMap#put(Object, HeavyObjectCreator, boolean)
    */
   @Override
   public Result put(final KEY key, final HeavyObjectCreator<VALUE> creator, final boolean allowAutomaticRemove)
   {
      if(key == null)
      {
         throw new NullPointerException("key musn't be null");
      }

      long weight = creator.getFutureWeight();
      if(weight > this.maximumWeight)
      {
         return Result.TOO_MUCH_HEAVY;
      }

      final Element element = this.hashMap.get(key);

      if(element != null)
      {
         weight -= element.value.getWeight();

         if(weight <= this.freeWeight)
         {
            element.frequency++;
            element.value = creator.createHeavyObject();
            this.freeWeight -= weight;

            return Result.UPDATED;
         }

         if(allowAutomaticRemove == false)
         {
            return Result.NOT_UPDATED;
         }

         this.automaticRemove(element, weight);

         element.frequency++;
         element.value = creator.createHeavyObject();
         this.freeWeight -= weight;

         return Result.UPDATED_WITH_AT_LEAST_ONE_REMOVED;
      }

      if(weight <= this.freeWeight)
      {
         this.hashMap.put(key, new Element(creator.createHeavyObject()));
         this.freeWeight -= weight;

         return Result.ADDED;
      }

      if(allowAutomaticRemove == false)
      {
         return Result.NOT_ADDED;
      }

      this.automaticRemove(null, weight);

      this.hashMap.put(key, new Element(creator.createHeavyObject()));
      this.freeWeight -= weight;

      return Result.ADD_WITH_AT_LEAST_ONE_REMOVED;
   }

   /**
    * Add/modify element in the map <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param key
    *           Element key
    * @param value
    *           Element value
    * @return {@code true} if value is add or modify without removing any object. {@code false} if at least one element is
    *         removes from the map
    * @see LimitedWeightHashMap#put(Object, HeavyObjectCreator)
    * @throws IllegalArgumentException
    *            If the element weight is bigger than map maximum weight
    */
   @Override
   public boolean put(final KEY key, final VALUE value)
   {
      final Result result = this.put(key, value, true);

      if(result == Result.TOO_MUCH_HEAVY)
      {
         throw new IllegalArgumentException("The given object is too heavy for the list");
      }

      return (result == Result.UPDATED) || (result == Result.ADDED);
   }

   /**
    * Add/modify element in the map.<br>
    * The result can be :
    * <table border=0>
    * <tr>
    * <th>{@link Result#ADD_WITH_AT_LEAST_ONE_REMOVED}</th>
    * <td>:</td>
    * <td>If the element is add, but at least one element is removed frrom the list (Only happen if remove is allow)</td>
    * </tr>
    * <tr>
    * <th>{@link Result#ADDED}</th>
    * <td>:</td>
    * <td>If the element is added, and no element is removed frrom the list</td>
    * </tr>
    * <tr>
    * <th>{@link Result#NOT_ADDED}</th>
    * <td>:</td>
    * <td>If the element is not added, it need to make room but remove is not allow</td>
    * </tr>
    * <tr>
    * <th>{@link Result#NOT_UPDATED}</th>
    * <td>:</td>
    * <td>If the element is not updated, it need make room but remove not allowed</td>
    * </tr>
    * <tr>
    * <th>{@link Result#TOO_MUCH_HEAVY}</th>
    * <td>:</td>
    * <td>If the element weight is bigger than the maximum weight</td>
    * </tr>
    * <tr>
    * <th>{@link Result#UPDATED}</th>
    * <td>:</td>
    * <td>If the element is updated</td>
    * </tr>
    * <tr>
    * <th>{@link Result#UPDATED_WITH_AT_LEAST_ONE_REMOVED}</th>
    * <td>:</td>
    * <td>If the element isupdated, but at least one element is removed frrom the list (Only happen if remove is allow)</td>
    * </tr>
    * </table>
    * <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param key
    *           Element key
    * @param value
    *           Element value
    * @param allowAutomaticRemove
    *           Indicates if remove elemnts to make room is allowed
    * @return The result operation
    * @see LimitedWeightHashMap#put(Object, HeavyObjectCreator, boolean)
    */
   @Override
   public Result put(final KEY key, final VALUE value, final boolean allowAutomaticRemove)
   {
      if(key == null)
      {
         throw new NullPointerException("key musn't be null");
      }

      long weight = value.getWeight();
      if(weight > this.maximumWeight)
      {
         return Result.TOO_MUCH_HEAVY;
      }

      final Element element = this.hashMap.get(key);

      if(element != null)
      {
         weight -= element.value.getWeight();

         if(weight <= this.freeWeight)
         {
            element.frequency++;
            element.value = value;
            this.freeWeight -= weight;

            return Result.UPDATED;
         }

         if(allowAutomaticRemove == false)
         {
            return Result.NOT_UPDATED;
         }

         this.automaticRemove(element, weight);

         element.frequency++;
         element.value = value;
         this.freeWeight -= weight;

         return Result.UPDATED_WITH_AT_LEAST_ONE_REMOVED;
      }

      if(weight <= this.freeWeight)
      {
         this.hashMap.put(key, new Element(value));
         this.freeWeight -= weight;

         return Result.ADDED;
      }

      if(allowAutomaticRemove == false)
      {
         return Result.NOT_ADDED;
      }

      this.automaticRemove(null, weight);

      this.hashMap.put(key, new Element(value));
      this.freeWeight -= weight;

      return Result.ADD_WITH_AT_LEAST_ONE_REMOVED;
   }

   /**
    * Remove an element from the map <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param key
    *           Key of element to remove
    * @see LimitedWeightHashMap#remove(Object)
    */
   @Override
   public void remove(final KEY key)
   {
      final Element element = this.hashMap.remove(key);

      if(element == null)
      {
         return;
      }

      this.freeWeight += element.value.getWeight();
   }
}