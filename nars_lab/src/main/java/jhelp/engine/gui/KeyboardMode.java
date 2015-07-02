package jhelp.engine.gui;

/**
 * Represents the mode of key board.<br>
 * By mode mean where keyboard user action are redirect to :
 * <ul>
 * <li>For capture the key code : Generally for associate key to game action</li>
 * <li>Edit text : When need to key some text from player</li>
 * <li>Game : To convert key pressed to action game</li>
 * </ul>
 * 
 * @author JHelp
 */
public enum KeyboardMode
{
   /** Mode capture next key press and return its code */
   CAPTURE_KEY_CODE,
   /** Mode get input text from player */
   EDIT_TEXT,
   /** Mode playing game */
   GAME
}