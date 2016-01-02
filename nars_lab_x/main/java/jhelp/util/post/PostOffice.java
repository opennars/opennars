package jhelp.util.post;

import jhelp.util.list.Pair;
import jhelp.util.list.SortedArray;
import jhelp.util.thread.ThreadManager;
import jhelp.util.thread.ThreadedSimpleTask;

/**
 * Post office for deliver {@link Message} between {@link User users}.<br>
 * Each {@link User} have to register for be able receive {@link Message}.<br>
 * 2 different {@link User users} can't have the same name
 * 
 * @author JHelp
 */
public class PostOffice
{
   /** Post office singleton */
   public static final PostOffice                        POST_OFFICE = new PostOffice();
   /** Deliver message to user in separate thread */
   private final ThreadedSimpleTask<Pair<Message, User>> giveMessage = new ThreadedSimpleTask<Pair<Message, User>>()
                                                                     {
                                                                        /**
                                                                         * Deliver message to user in separate thread <br>
                                                                         * <br>
                                                                         * <b>Parent documentation:</b><br>
                                                                         * {@inheritDoc}
                                                                         * 
                                                                         * @param parameter
                                                                         *           Pair of message to send and user
                                                                         *           destination
                                                                         * @see jhelp.util.thread.ThreadedSimpleTask#doSimpleAction(Object)
                                                                         */
                                                                        @Override
                                                                        protected void doSimpleAction(final Pair<Message, User> parameter)
                                                                        {
                                                                           parameter.element2.receiveMessage(parameter.element1);
                                                                        }
                                                                     };
   /** Groups map */
   private final HashMap<String, SortedArray<String>>    groups;
   /** Users map */
   private final HashMap<String, User>                   users;

   /**
    * Create a new instance of PostOffice
    */
   private PostOffice()
   {
      this.users = new HashMap<String, User>();
      this.groups = new HashMap<String, SortedArray<String>>();
   }

   /**
    * Add a user to a group.<br>
    * If the user is not already registered, it is automatically register.<br>
    * If the group not already exits, it is created
    * 
    * @param user
    *           User to add
    * @param group
    *           Group where add.
    */
   public void addToGroup(final User user, final String group)
   {
      if(this.isRegisteredUser(user.getName()) == false)
      {
         this.registerUser(user);
      }

      SortedArray<String> members = this.groups.get(group);

      if(members == null)
      {
         members = new SortedArray<String>(String.class, true);

         this.groups.put(group, members);
      }

      members.add(user.getName());
   }

   /**
    * Set of actual groups
    * 
    * @return Set of actual groups
    */
   public Set<String> getGroupNames()
   {
      return Collections.unmodifiableSet(this.groups.keySet());
   }

   /**
    * Give the list where the user is in
    * 
    * @param user
    *           User name
    * @return List of groups where is the user
    */
   public List<String> getGroupsFor(final String user)
   {
      final ArrayList<String> groups = new ArrayList<String>();

      SortedArray<String> members;

      for(final String group : this.groups.keySet())
      {
         members = this.groups.get(group);

         if((members != null) && (members.contains(user) == true))
         {
            groups.add(group);
         }
      }

      return Collections.unmodifiableList(groups);
   }

   /**
    * Give the list of the members of a group
    * 
    * @param group
    *           Group name
    * @return List of members
    */
   public List<String> getMembers(final String group)
   {
      final SortedArray<String> members = this.groups.get(group);

      if(members == null)
      {
         return Collections.emptyList();
      }

      return Collections.unmodifiableList(Arrays.asList(members.toArray()));
   }

   /**
    * List of registered users
    * 
    * @return List of registered users
    */
   public Set<String> getUserNames()
   {
      return Collections.unmodifiableSet(this.users.keySet());
   }

   /**
    * Indicates if a user is in a group
    * 
    * @param user
    *           User tested
    * @param group
    *           Group name
    * @return {@code true} if the user is in the group
    */
   public boolean isInGroup(final User user, final String group)
   {
      final SortedArray<String> members = this.groups.get(group);

      if(members == null)
      {
         return false;
      }

      return members.contains(user.getName());
   }

   /**
    * Indicates if a user is registered
    * 
    * @param user
    *           User name tested
    * @return {@code true} if the user is registered
    */
   public boolean isRegisteredUser(final String user)
   {
      return this.users.containsKey(user);
   }

   /**
    * Post a message for a specific user
    * 
    * @param sender
    *           Message sender
    * @param messageId
    *           Message ID
    * @param destination
    *           User destination
    * @param message
    *           Message to send
    * @return {@code true} if message posted.{@code false} if message not posted, because the destination user dosen't exits
    */
   public boolean postMessage(final User sender, final int messageId, final String destination, final Object message)
   {
      final User user = this.users.get(destination);

      if(user == null)
      {
         return false;
      }

      ThreadManager.THREAD_MANAGER.doThread(this.giveMessage, new Pair<Message, User>(new Message(messageId, sender.getName(), message), user));

      return true;
   }

   /**
    * Post a message for all registered users
    * 
    * @param sender
    *           Sender of message
    * @param messageId
    *           Message ID
    * @param message
    *           Message it self
    * @param excludeMe
    *           Indicates if the sender is not in the distribution
    */
   public void postMessageForAll(final User sender, final int messageId, final Object message, final boolean excludeMe)
   {
      final String senderName = sender.getName();

      for(final String userName : this.users.keySet())
      {
         if((excludeMe == false) || (senderName.equals(userName) == false))
         {
            this.postMessage(sender, messageId, userName, message);
         }
      }
   }

   /**
    * Post a message for all members of a group
    * 
    * @param sender
    *           Sender
    * @param messageId
    *           Message ID
    * @param group
    *           Group to send
    * @param message
    *           Message itself
    */
   public void postMessageForGroup(final User sender, final int messageId, final String group, final Object message)
   {
      final SortedArray<String> members = this.groups.get(group);

      if(members == null)
      {
         return;
      }

      User user;

      for(final String userName : members)
      {
         user = this.users.get(userName);

         if(user != null)
         {
            this.postMessage(sender, messageId, userName, message);
         }
      }
   }

   /**
    * Register a user
    * 
    * @param user
    *           User to register
    */
   public void registerUser(final User user)
   {
      if(this.users.containsKey(user.getName()) == true)
      {
         throw new IllegalArgumentException("A user named " + user.getName() + " is already registered");
      }

      this.users.put(user.getName(), user);
   }

   /**
    * Remove a user from a group
    * 
    * @param user
    *           User to remove
    * @param group
    *           Group where remove
    */
   public void removeFromGroup(final User user, final String group)
   {
      final SortedArray<String> members = this.groups.get(group);

      if(members == null)
      {
         return;
      }

      members.remove(user.getName());

      if(members.isEmpty() == true)
      {
         this.groups.remove(group);
      }
   }

   /**
    * Unregister a user
    * 
    * @param user
    *           User to unregister
    */
   public void unregisterUser(final User user)
   {
      this.users.remove(user.getName());

      for(final String group : this.groups.keySet())
      {
         this.removeFromGroup(user, group);
      }
   }
}