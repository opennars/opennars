/**
 * Project : JHelpEngine<br>
 * Package : jhelp.engine.gui.components<br>
 * Class : InternalFrame<br>
 * Date : 26 juin 2010<br>
 * By JHelp
 */
package jhelp.engine.gui.components;

import jhelp.engine.Texture;
import jhelp.engine.gui.events.ButtonClickListener;
import jhelp.engine.gui.events.InternalFrameListener;
import jhelp.engine.gui.layout.BorderLayout;
import jhelp.engine.gui.layout.BorderLayoutConstraints;
import jhelp.engine.gui.layout.HorizontalLayout;
import jhelp.engine.gui.layout.HorizontalLayoutConstraints;
import jhelp.util.debug.Debug;

import java.util.ArrayList;

/**
 * Internal frame inside a {@link Desktop3D}<br>
 * <br>
 * Last modification : 26 juin 2010<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public class InternalFrame
      extends Panel
{
   /**
    * Internal frame title <br>
    * <br>
    * Last modification : 2 déc. 2010<br>
    * Version 0.0.0<br>
    * 
    * @author JHelp
    */
   class Title
         extends LabelText
   {
      /**
       * Constructs Title
       * 
       * @param title
       *           Title
       */
      Title(final String title)
      {
         super(title);
      }

      /**
       * Call when mouse click
       * 
       * @param x
       *           Mouse X
       * @param y
       *           Mouse Y
       * @param buttonLeft
       *           Indicates if left button is down
       * @param buttonRight
       *           Indicates if right button is down
       * @see LabelText#mouseClick(int, int, boolean, boolean)
       */
      @Override
      protected void mouseClick(final int x, final int y, final boolean buttonLeft, final boolean buttonRight)
      {
         InternalFrame.this.fireInternalFrameSelect();
      }

      /**
       * Paint the component
       * 
       * @param texture
       *           Texture where paint
       * @param x
       *           X
       * @param y
       *           Y
       * @see LabelText#paintComponent(jhelp.engine.Texture, int, int)
       */
      @Override
      protected void paintComponent(final Texture texture, final int x, final int y)
      {
         texture.fillRect(x, y, this.width, this.height, Color.GREEN, Color.BLUE, Color.WHITE, Color.RED, false);

         super.paintComponent(texture, x, y);
      }
   }

   /** Close action */
   private static final int          ACTION_CLOSE          = 0;
   /** Reduce/restore action */
   private static final int          ACTION_REDUCE_RESTORE = 1;

   /** Listener on button click */
   private final ButtonClickListener buttonClickListener   = new ButtonClickListener()
                                                           {
                                                              /**
                                                               * Call when button click
                                                               * 
                                                               * @param button
                                                               *           Button clicked
                                                               * @see ButtonClickListener#buttonClick(Button)
                                                               */
                                                              @Override
                                                              public void buttonClick(final Button button)
                                                              {
                                                                 switch(button.internalID)
                                                                 {
                                                                    case InternalFrame.ACTION_CLOSE:
                                                                       InternalFrame.this.actionClose();
                                                                    break;
                                                                    case InternalFrame.ACTION_REDUCE_RESTORE:
                                                                       InternalFrame.this.actionReduceRestore(button);
                                                                    break;
                                                                 }
                                                              }
                                                           };

   /** Main Component */
   private final Component           content;
   /** Internal frame reduce state */
   private boolean                   reduce;
   /** Internal frame listeners */
   ArrayList<InternalFrameListener>  liteners;
   /** Object name */
   String                            objectName;

   /**
    * Constructs InternalFrame
    * 
    * @param title
    *           Title
    * @param content
    *           Main component
    */
   public InternalFrame(final String title, final Component content)
   {
      super(new BorderLayout());

      if(content == null)
      {
         throw new NullPointerException("content musn't be null");
      }

      this.content = content;
      this.reduce = false;
      this.liteners = new ArrayList<InternalFrameListener>();

      final Panel panel = new Panel(new HorizontalLayout());

      Button button = new Button("-");
      button.internalID = InternalFrame.ACTION_REDUCE_RESTORE;
      button.addButtonClickListener(this.buttonClickListener);
      panel.addComponent(button, HorizontalLayoutConstraints.obtainHorizontalLayoutConstraints());

      button = new Button("X");
      button.internalID = InternalFrame.ACTION_CLOSE;
      panel.addComponent(button, HorizontalLayoutConstraints.obtainHorizontalLayoutConstraints());

      final Title label = new Title(title);

      panel.setBackGround(Color.LIGHT_GRAY);

      this.addComponent(label, BorderLayoutConstraints.TOP);
      this.addComponent(panel, BorderLayoutConstraints.TOP_RIGHT);
      this.addComponent(content, BorderLayoutConstraints.CENTER);
   }

   /**
    * Action close
    */
   void actionClose()
   {
      Debug.printTodo();
   }

   /**
    * Action for reduce/restore
    * 
    * @param button
    *           Button click
    */
   void actionReduceRestore(final Button button)
   {
      this.reduce = !this.reduce;

      if(this.reduce == true)
      {
         this.components.remove(BorderLayoutConstraints.CENTER);

         button.setText("O");
      }
      else
      {
         this.addComponent(this.content, BorderLayoutConstraints.CENTER);

         button.setText("-");
      }
   }

   /**
    * Signal to listeners that the internal frame is select
    */
   protected void fireInternalFrameSelect()
   {
      for(final InternalFrameListener listener : this.liteners)
      {
         listener.internalFrameSelect(this);
      }
   }

   /**
    * Add internal frame listener
    * 
    * @param internalFrameListener
    *           Listener to add
    */
   public void addInternalFrameListener(final InternalFrameListener internalFrameListener)
   {
      this.liteners.add(internalFrameListener);
   }

   /**
    * Remove internal frame listener
    * 
    * @param internalFrameListener
    *           Listener to remove
    */
   public void removeInternalFrameListener(final InternalFrameListener internalFrameListener)
   {
      this.liteners.remove(internalFrameListener);
   }
}