package jhelp.engine.gui;

import jhelp.engine.JHelpSceneRenderer;
import jhelp.util.MemorySweeper;
import jhelp.util.gui.UtilGUI;
import jhelp.util.io.UtilIO;
import jhelp.util.preference.Preferences;
import jhelp.util.resources.ResourceText;
import jhelp.util.resources.Resources;

import java.io.File;

/**
 * Frame for help to create 3D game
 * 
 * @author JHelp
 */
public abstract class Game3DFrame
      extends JFrame
{
   /**
    * Event manager that react to mouse and keyboard
    * 
    * @author JHelp
    */
   class EventManager
         implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener
   {
      /** Indicates if mouse left button is down */
      private boolean left;

      /** Indicates if mouse right button is down */
      private boolean right;

      /***
       * Create a new instance of EventManager
       */
      EventManager()
      {
      }

      /**
       * Report to scene renderer mouse event
       * 
       * @param mouseEvent
       *           Mouse event
       * @param drag
       *           Indicates if its a drag or not
       */
      private void mouseReport(final MouseEvent mouseEvent, final boolean drag)
      {
         Game3DFrame.this.getSceneRenderer().setDetectPosition(mouseEvent.getX(), mouseEvent.getY(), this.left, this.right, drag);
      }

      /**
       * Called when key pressed <br>
       * <br>
       * <b>Parent documentation:</b><br>
       * {@inheritDoc}
       * 
       * @param e
       *           Key event
       * @see KeyListener#keyPressed(KeyEvent)
       */
      @Override
      public void keyPressed(final KeyEvent e)
      {
         Game3DFrame.this.doKeyPressed(e);
      }

      /**
       * Called when key released <br>
       * <br>
       * <b>Parent documentation:</b><br>
       * {@inheritDoc}
       * 
       * @param e
       *           Key event
       * @see KeyListener#keyReleased(KeyEvent)
       */
      @Override
      public void keyReleased(final KeyEvent e)
      {
      }

      /**
       * Called when key typed <br>
       * <br>
       * <b>Parent documentation:</b><br>
       * {@inheritDoc}
       * 
       * @param e
       *           Key event
       * @see KeyListener#keyTyped(KeyEvent)
       */
      @Override
      public void keyTyped(final KeyEvent e)
      {
         Game3DFrame.this.doKeyTyped(e);
      }

      /**
       * Called when mouse clicked <br>
       * <br>
       * <b>Parent documentation:</b><br>
       * {@inheritDoc}
       * 
       * @param e
       *           Mouse event
       * @see MouseListener#mouseClicked(MouseEvent)
       */
      @Override
      public void mouseClicked(final MouseEvent e)
      {
         this.mouseReport(e, false);
      }

      /**
       * Called when mouse dragged <br>
       * <br>
       * <b>Parent documentation:</b><br>
       * {@inheritDoc}
       * 
       * @param e
       *           Mouse event
       * @see MouseMotionListener#mouseDragged(MouseEvent)
       */
      @Override
      public void mouseDragged(final MouseEvent e)
      {
         this.mouseReport(e, true);
      }

      /**
       * Called when mouse entered <br>
       * <br>
       * <b>Parent documentation:</b><br>
       * {@inheritDoc}
       * 
       * @param e
       *           Mouse event
       * @see MouseListener#mouseEntered(MouseEvent)
       */
      @Override
      public void mouseEntered(final MouseEvent e)
      {
         this.mouseReport(e, false);
      }

      /**
       * Called when mouse exited <br>
       * <br>
       * <b>Parent documentation:</b><br>
       * {@inheritDoc}
       * 
       * @param e
       *           Mouse event
       * @see MouseListener#mouseExited(MouseEvent)
       */
      @Override
      public void mouseExited(final MouseEvent e)
      {
         this.mouseReport(e, false);
      }

      /**
       * Called when mouse moved <br>
       * <br>
       * <b>Parent documentation:</b><br>
       * {@inheritDoc}
       * 
       * @param e
       *           Mouse event
       * @see MouseMotionListener#mouseMoved(MouseEvent)
       */
      @Override
      public void mouseMoved(final MouseEvent e)
      {
         this.mouseReport(e, false);
      }

      /**
       * Called when mouse pressed <br>
       * <br>
       * <b>Parent documentation:</b><br>
       * {@inheritDoc}
       * 
       * @param e
       *           Mouse event
       * @see MouseListener#mousePressed(MouseEvent)
       */
      @Override
      public void mousePressed(final MouseEvent e)
      {
         this.left |= SwingUtilities.isLeftMouseButton(e);
         this.right |= SwingUtilities.isRightMouseButton(e);
         this.mouseReport(e, false);
      }

      /**
       * Called when mouse released <br>
       * <br>
       * <b>Parent documentation:</b><br>
       * {@inheritDoc}
       * 
       * @param e
       *           Mouse event
       * @see MouseListener#mouseReleased(MouseEvent)
       */
      @Override
      public void mouseReleased(final MouseEvent e)
      {
         this.left &= !SwingUtilities.isLeftMouseButton(e);
         this.right &= !SwingUtilities.isRightMouseButton(e);
         this.mouseReport(e, false);
      }

      /**
       * Called when mouse wheel moved <br>
       * <br>
       * <b>Parent documentation:</b><br>
       * {@inheritDoc}
       * 
       * @param e
       *           Mouse event
       * @see MouseWheelListener#mouseWheelMoved(MouseWheelEvent)
       */
      @Override
      public void mouseWheelMoved(final MouseWheelEvent e)
      {
         this.mouseReport(e, false);
      }
   }

   /** Undefined char */
   public static final char      CHAR_UNDEFINED = KeyEvent.CHAR_UNDEFINED;
   /** Key code, action game association */
   private final ActionKeyMap    actionKeyMap;
   /** Component with 3D */
   private final ComponentView3D componentView3D;
   /** Current edit text cursor position */
   private int                   cursor;
   /** Current text in current edit text */
   private final StringBuilder   editText;
   /** Manager of key and mouse event */
   private final EventManager    eventManager;
   /** Game directory */
   private final File            gameDirectory;
   /** Game name */
   private final String          gameName;
   /** Game resource directory */
   private final File            gameResourcesDirectory;
   /** Actual keyboard mode */
   private KeyboardMode          keyboardMode;
   /** Game preferences */
   private final Preferences     preferences;
   /** Game resources */
   private final Resources       resources;
   /** Game texts */
   private final ResourceText    resourceText;

   /**
    * Create a new instance of Game3DFrame
    * 
    * @param gameName
    *           Game name
    * @throws HeadlessException
    *            If computer don't support display, manage key or mouse
    */
   public Game3DFrame(String gameName)
         throws HeadlessException
   {
      if(gameName == null)
      {
         gameName = "JHelpGame3D";
      }

      //gameName = UtilText.replaceWhiteCharactersBy(gameName, '_');
      gameName = "GAMENAME";

      if(gameName.length() == 0)
      {
         gameName = "JHelpGame3D";
      }

      this.gameName = gameName;
      this.gameDirectory = UtilIO.obtainExternalFile("JHelp/Game3D/" + gameName);
      this.gameResourcesDirectory = new File(this.gameDirectory, "resources");

      if(UtilIO.createDirectory(this.gameDirectory) == false)
      {
         throw new IllegalStateException("Can create the directory game : " + this.gameDirectory.getAbsolutePath());
      }

      this.preferences = new Preferences(new File(this.gameDirectory, "preferences"));
      this.resources = new Resources(this.gameResourcesDirectory);
      this.resourceText = this.resources.obtainResourceText("texts/texts");
      this.actionKeyMap = new ActionKeyMap(this.preferences);
      this.eventManager = new EventManager();
      this.keyboardMode = KeyboardMode.GAME;
      this.editText = new StringBuilder();

      MemorySweeper.launch();

      this.setUndecorated(true);
      this.setResizable(false);
      this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
      UtilGUI.takeAllScreen(this);

      this.componentView3D = new ComponentView3D(this.getWidth(), this.getHeight());
      this.componentView3D.addMouseListener(this.eventManager);
      this.componentView3D.addMouseMotionListener(this.eventManager);
      this.componentView3D.addMouseWheelListener(this.eventManager);
      this.componentView3D.addKeyListener(this.eventManager);

      this.setLayout(new BorderLayout());
      this.add(this.componentView3D, BorderLayout.CENTER);
   }

   /**
    * Obtain current edit text character (under the cursor)
    * 
    * @return Current edit text, current character
    */
   private char otainCurrentChar()
   {
      if((this.editText == null) || (this.cursor < 0) || (this.cursor >= this.editText.length()))
      {
         return Game3DFrame.CHAR_UNDEFINED;
      }

      return this.editText.charAt(this.cursor);
   }

   /**
    * Do action on key pressed
    * 
    * @param keyEvent
    *           Key event
    */
   final void doKeyPressed(final KeyEvent keyEvent)
   {
      switch(this.keyboardMode)
      {
         case GAME:
            final ActionKey actionKey = this.actionKeyMap.obtainActionKey(keyEvent.getKeyCode());

            if(actionKey != null)
            {
               this.actionKey(actionKey);
            }

         break;
         case CAPTURE_KEY_CODE:
            this.keyCodeCaptured(keyEvent.getKeyCode());
         break;
         case EDIT_TEXT:
            switch(keyEvent.getKeyCode())
            {
               case KeyEvent.VK_LEFT:
                  if(this.cursor > 0)
                  {
                     this.cursor--;
                     this.editTextCursorMoved(this.otainCurrentChar(), this.cursor, this.editText.toString());
                  }
               break;
               case KeyEvent.VK_RIGHT:
                  if(this.cursor < this.editText.length())
                  {
                     this.cursor++;
                     this.editTextCursorMoved(this.otainCurrentChar(), this.cursor, this.editText.toString());
                  }
               break;
               case KeyEvent.VK_BACK_SPACE:
                  if(this.cursor > 0)
                  {
                     this.cursor--;
                     final char character = this.editText.charAt(this.cursor);
                     this.editText.delete(this.cursor, this.cursor + 1);
                     this.editTextDelete(character, this.cursor, this.editText.toString());
                  }
               break;
               case KeyEvent.VK_DELETE:
                  if(this.cursor < this.editText.length())
                  {
                     final char character = this.editText.charAt(this.cursor);
                     this.editText.delete(this.cursor, this.cursor + 1);
                     this.editTextDelete(character, this.cursor, this.editText.toString());
                  }
               break;
               case KeyEvent.VK_ENTER:
                  this.setKeyboardMode(this.editTextKeyboardModeAfterEnter(this.editText.toString()));
               break;
            }
         break;
      }
   }

   /**
    * Do action on key typed
    * 
    * @param keyEvent
    *           Key event
    */
   final void doKeyTyped(final KeyEvent keyEvent)
   {
      final char character = keyEvent.getKeyChar();

      if((this.keyboardMode != KeyboardMode.EDIT_TEXT) || (character == Game3DFrame.CHAR_UNDEFINED) || (character < ' '))
      {
         return;
      }

      this.editText.insert(this.cursor, character);
      this.cursor++;
      this.editTextAdd(character, this.cursor, this.editText.toString());
   }

   /**
    * Called when game action happen
    * 
    * @param actionKey
    *           Action game
    */
   protected abstract void actionKey(ActionKey actionKey);

   /**
    * Called just before exit.<br>
    * It allow to developer to ask "do you really want to exit ?"
    * 
    * @return {@code true} to let game exit. {@code false} to cancel the exit
    */
   protected abstract boolean canExitNow();

   /**
    * Called on edit text mode when a character is add by the player
    * 
    * @param character
    *           Added character
    * @param cursor
    *           Cursor position
    * @param text
    *           Actual edit text
    */
   protected abstract void editTextAdd(char character, int cursor, String text);

   /**
    * Called on edit text mode when cursor moved by the player
    * 
    * @param character
    *           Character under the cursor
    * @param cursor
    *           Cursor position
    * @param text
    *           Actual edit text
    */
   protected abstract void editTextCursorMoved(char character, int cursor, String text);

   /**
    * Called on edit text mode when a character is deleted
    * 
    * @param character
    *           Deleted character
    * @param cursor
    *           Cursor position
    * @param text
    *           Actual edit text
    */
   protected abstract void editTextDelete(char character, int cursor, String text);

   /**
    * Called on edit text mode when edit terminated
    * 
    * @param text
    *           Final edit text
    */
   protected abstract void editTextEnd(String text);

   /**
    * Called on edit text mode when player press enter to know witch mode to pass after the edition
    * 
    * @param text
    *           Final edit text
    * @return Mode after player press enter
    */
   protected abstract KeyboardMode editTextKeyboardModeAfterEnter(String text);

   /**
    * Called on edit text mode when edition started
    */
   protected abstract void editTextStart();

   /**
    * Called on capture mode when a key code is captured
    * 
    * @param keyCode
    *           Captured key code
    */
   protected abstract void keyCodeCaptured(int keyCode);

   /**
    * Call on widow event append
    * 
    * @param e
    *           Event description
    * @see JFrame#processWindowEvent(WindowEvent)
    */
   @Override
   protected final void processWindowEvent(final WindowEvent e)
   {
      switch(e.getID())
      {
         case WindowEvent.WINDOW_CLOSED:
         case WindowEvent.WINDOW_CLOSING:
            this.closeGame();
         break;
      }
   }

   /**
    * Append text to current edit text
    * 
    * @param text
    *           Text to append
    */
   public final void appendText(final String text)
   {
      if((this.keyboardMode != KeyboardMode.EDIT_TEXT) || (text.length() == 0))
      {
         return;
      }

      this.editText.insert(this.cursor, text);
      this.cursor += text.length();

      this.editTextAdd(this.otainCurrentChar(), this.cursor, this.editText.toString());
   }

   /**
    * Clear actual edit text
    */
   public final void clearText()
   {
      if(this.keyboardMode != KeyboardMode.EDIT_TEXT)
      {
         return;
      }

      this.editText.delete(0, this.editText.length());
      this.cursor = 0;
      this.editTextStart();
   }

   /**
    * Close properly the game
    */
   public final void closeGame()
   {
      if(this.canExitNow() == false)
      {
         return;
      }

      this.componentView3D.getSceneRenderer().stop();
      this.setVisible(false);
      this.dispose();

      MemorySweeper.exit(0);
   }

   /**
    * Key code association on the game
    * 
    * @return Key code association on the game
    */
   public final ActionKeyMap getActionKeyMap()
   {
      return this.actionKeyMap;
   }

   /**
    * Game directory
    * 
    * @return Game directory
    */
   public final File getGameDirectory()
   {
      return this.gameDirectory;
   }

   /**
    * Game name
    * 
    * @return Game name
    */
   public final String getGameName()
   {
      return this.gameName;
   }

   /**
    * Game resource directory
    * 
    * @return Game resource directory
    */
   public File getGameResourcesDirectory()
   {
      return this.gameResourcesDirectory;
   }

   /**
    * Actual keyboard mode
    * 
    * @return Keyboard mode
    */
   public final KeyboardMode getKeyboardMode()
   {
      return this.keyboardMode;
   }

   /**
    * Game preferences
    * 
    * @return Game preferences
    */
   public final Preferences getPreferences()
   {
      return this.preferences;
   }

   /**
    * Game resources
    * 
    * @return Game resources
    */
   public final Resources getResources()
   {
      return this.resources;
   }

   /**
    * Game texts
    * 
    * @return Game texts
    */
   public final ResourceText getResourceText()
   {
      return this.resourceText;
   }

   /**
    * Scene renderer for manipulate the 3D
    * 
    * @return Scene renderer
    */
   public final JHelpSceneRenderer getSceneRenderer()
   {
      return this.componentView3D.getSceneRenderer();
   }

   /**
    * Change edit text cursor position
    * 
    * @param position
    *           Cursor position
    */
   public final void setCursorPosition(int position)
   {
      if(this.keyboardMode != KeyboardMode.EDIT_TEXT)
      {
         return;
      }

      position = Math.max(0, Math.min(this.editText.length(), position));

      if(position == this.cursor)
      {
         return;
      }

      this.cursor = position;
      this.editTextCursorMoved(this.otainCurrentChar(), this.cursor, this.editText.toString());
   }

   /**
    * Change the key board mode
    * 
    * @param keyboardMode
    *           New keyboard mode
    */
   public final void setKeyboardMode(final KeyboardMode keyboardMode)
   {
      if(keyboardMode == null)
      {
         throw new NullPointerException("keyboardMode musn't be null");
      }

      if(this.keyboardMode == keyboardMode)
      {
         return;
      }

      if(this.keyboardMode == KeyboardMode.EDIT_TEXT)
      {
         this.editTextEnd(this.editText.toString());
      }

      this.keyboardMode = keyboardMode;

      if(this.keyboardMode == KeyboardMode.EDIT_TEXT)
      {
         this.editText.delete(0, this.editText.length());
         this.cursor = 0;
         this.editTextStart();
      }
   }
}