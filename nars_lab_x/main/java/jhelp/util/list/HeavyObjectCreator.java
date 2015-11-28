package jhelp.util.list;

/**
 * Creator off {@link HeavyObject}, to be able to create the heavy object only if necessary
 * 
 * @author JHelp
 * @param <HEAVY>
 *           Heavy object type
 */
public interface HeavyObjectCreator<HEAVY extends HeavyObject>
{
   /**
    * Create the object
    * 
    * @return Created object
    */
   public HEAVY createHeavyObject();

   /**
    * Weight of the future object, it is recommands to know it without created the object
    * 
    * @return Weight of the future object
    */
   public long getFutureWeight();
}