package jhelp.util.list;

/**
 * Generic hash map with limited number of element in memory.<br>
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
public interface LimitedWeightHashMap<KEY, VALUE extends HeavyObject>
      extends HeavyObject
{
   /**
    * Methods {@link LimitedWeightHashMap#put(Object, HeavyObjectCreator, boolean)} or
    * {@link LimitedWeightHashMap#put(Object, HeavyObject, boolean)} results
    * 
    * @author JHelp
    */
   public enum Result
   {
      /** Indicates that add is done, but some elements was been removed to make room */
      ADD_WITH_AT_LEAST_ONE_REMOVED,
      /** Indicates that add is done without removing */
      ADDED,
      /** Indicates that add is not done, because remove is not allow and some room is need */
      NOT_ADDED,
      /** Indicates that update is not done, because remove is not allow and some room is need */
      NOT_UPDATED,
      /** Indicates that the given object weight is bigger than the map total weight */
      TOO_MUCH_HEAVY,
      /** Indicates that the element is updated without removing */
      UPDATED,
      /** Indicates that update is done, but some elements was been removed to make room */
      UPDATED_WITH_AT_LEAST_ONE_REMOVED
   }

   /**
    * Obtain an element of the map
    * 
    * @param key
    *           Element key
    * @return Element value
    */
   public VALUE get(final KEY key);

   /**
    * Free weight left *
    * 
    * @return Free weight left
    * @see LimitedWeightHashMap#getFreeWeight()
    */
   public long getFreeWeight();

   /**
    * Maximum weight
    * 
    * @return Maximum weight
    */
   public long getMaximuWeight();

   /**
    * Number of elements inside the map
    * 
    * @return Number of elements inside the map
    */
   public int getSize();

   /**
    * Add/modify element in the map
    * 
    * @param key
    *           Element key
    * @param creator
    *           Describe how to create the element (To be able avoid create object if not add/modify)
    * @return {@code true} if value is add or modify without removing any object. {@code false} if at least one element is
    *         removes from the map
    * @throws IllegalArgumentException
    *            If the element weight is bigger than map maximum weight
    */
   public boolean put(final KEY key, final HeavyObjectCreator<VALUE> creator);

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
    * 
    * @param key
    *           Element key
    * @param creator
    *           Describe how to create the element (To be able avoid create object if not add/modify)
    * @param allowAutomaticRemove
    *           Indicates if remove elemnts to make room is allowed
    * @return The result operation
    */
   public Result put(final KEY key, final HeavyObjectCreator<VALUE> creator, final boolean allowAutomaticRemove);

   /**
    * Add/modify element in the map
    * 
    * @param key
    *           Element key
    * @param value
    *           Element value
    * @return {@code true} if value is add or modify without removing any object. {@code false} if at least one element is
    *         removes from the map
    * @throws IllegalArgumentException
    *            If the element weight is bigger than map maximum weight
    */
   public boolean put(final KEY key, final VALUE value);

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
    * 
    * @param key
    *           Element key
    * @param value
    *           Element value
    * @param allowAutomaticRemove
    *           Indicates if remove elemnts to make room is allowed
    * @return The result operation
    */
   public Result put(final KEY key, final VALUE value, final boolean allowAutomaticRemove);

   /**
    * Remove an element from the map
    * 
    * @param key
    *           Key of element to remove
    */
   public void remove(final KEY key);
}