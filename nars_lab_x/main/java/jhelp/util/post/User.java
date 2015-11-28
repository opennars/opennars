package jhelp.util.post;

/**
 * User of {@link PostOffice} have to extends this class
 * 
 * @author JHelp
 */
public abstract class User
{
   /** User name */
   private final String name;

   /**
    * Create a new instance of User
    * 
    * @param name
    *           User name
    */
   public User(final String name)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      this.name = name;
   }

   /**
    * User name
    * 
    * @return User name
    */
   public final String getName()
   {
      return this.name;
   }

   /**
    * Call when user receive a message.<br>
    * Since it is call in its own thread, you can do operation as long/heavy as you need
    * 
    * @param message
    *           Received message
    */
   public abstract void receiveMessage(Message message);
}