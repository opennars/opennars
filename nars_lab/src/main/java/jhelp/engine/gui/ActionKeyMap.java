package jhelp.engine.gui;

import jhelp.util.preference.Preferences;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Map that associate key to game action
 * 
 * @author JHelp
 */
public final class ActionKeyMap
{
   /** Preferences where read/write configuration */
   private final Preferences preferences;

   /**
    * Create a new instance of ActionKeyMap
    * 
    * @param preferences
    *           Preferences where read/write configuration
    */
   ActionKeyMap(final Preferences preferences)
   {
      this.preferences = preferences;
   }

   /**
    * Associate a key code to an action game.<br>
    * If the key code was previously associated to an other action game, then the action game that loose its association is
    * return
    * 
    * @param keyCode
    *           Key code
    * @param actionKey
    *           Action game to associate
    * @return Previous Action game previously associate to that key code. {@code null} if none
    */
   public ActionKey associate(final int keyCode, final ActionKey actionKey)
   {
      final int actualCode = this.getKeyCode(actionKey);

      if(actualCode == keyCode)
      {
         return null;
      }

      final ActionKey currentAction = this.obtainActionKey(keyCode);
      this.preferences.setValue(actionKey.name(), keyCode);

      if(keyCode == KeyEvent.VK_UNDEFINED)
      {
         return null;
      }

      if(currentAction != null)
      {
         this.preferences.setValue(currentAction.name(), KeyEvent.VK_UNDEFINED);
      }

      return currentAction;
   }

   /**
    * Dissociate a list of action game
    * 
    * @param actionKeys
    *           Action game to dissociate
    */
   public void disassociate(final ActionKey... actionKeys)
   {
      for(final ActionKey actionKey : actionKeys)
      {
         this.preferences.setValue(actionKey.name(), KeyEvent.VK_UNDEFINED);
      }
   }

   /**
    * Get the key code associate to an action game
    * 
    * @param actionKey
    *           Action game
    * @return Key code or {@link KeyEvent#VK_UNDEFINED} if not associate
    */
   public int getKeyCode(final ActionKey actionKey)
   {
      return this.preferences.getValue(actionKey.name(), actionKey.getDefaultKeyCode());
   }

   /**
    * Obtain action game for a key code
    * 
    * @param keyCode
    *           Key code
    * @return Action key associated or {@code null} if none
    */
   public ActionKey obtainActionKey(final int keyCode)
   {
      for(final ActionKey actionKey : ActionKey.values())
      {
         if(keyCode == this.getKeyCode(actionKey))
         {
            return actionKey;
         }
      }

      return null;
   }

   /**
    * Restore some action game to default association
    * 
    * @param actionKeys
    *           Action games to restore
    */
   public void restoreToDefault(final ActionKey... actionKeys)
   {
      for(final ActionKey actionKey : actionKeys)
      {
         this.preferences.setValue(actionKey.name(), actionKey.getDefaultKeyCode());
      }
   }

   /**
    * Restore all action game to default association
    */
   public void restoreToDefaultAll()
   {
      for(final ActionKey actionKey : ActionKey.values())
      {
         this.preferences.setValue(actionKey.name(), actionKey.getDefaultKeyCode());
      }
   }

   /**
    * Restore to default all action game not associated
    */
   public void restoreToDefaultUnassociated()
   {
      for(final ActionKey actionKey : ActionKey.values())
      {
         if(this.getKeyCode(actionKey) == KeyEvent.VK_UNDEFINED)
         {
            this.preferences.setValue(actionKey.name(), actionKey.getDefaultKeyCode());
         }
      }
   }

   /**
    * List of action game not associated
    * 
    * @return List of action game not associated
    */
   public List<ActionKey> unassociatedKeys()
   {
      final List<ActionKey> unassociatedList = new ArrayList<ActionKey>();

      for(final ActionKey actionKey : ActionKey.values())
      {
         if(this.getKeyCode(actionKey) == KeyEvent.VK_UNDEFINED)
         {
            unassociatedList.add(actionKey);
         }
      }

      return Collections.unmodifiableList(unassociatedList);
   }
}