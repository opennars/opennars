package jhelp.util.post;

/**
 * Message description
 * 
 * @author JHelp
 */
public class Message
{
   /** Message sender, use for reply */
   public String from;
   /** Message ID */
   public int    mesageId;
   /** Message itself */
   public Object message;

   /**
    * Create a new instance of Message
    * 
    * @param mesageId
    *           Message ID
    * @param from
    *           Sender
    * @param message
    *           Message itself
    */
   Message(final int mesageId, final String from, final Object message)
   {
      this.mesageId = mesageId;
      this.from = from;
      this.message = message;
   }
}