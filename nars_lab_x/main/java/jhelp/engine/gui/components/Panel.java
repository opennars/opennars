/**
 * Project : JHelpEngine<br>
 * Package : jhelp.gui.components<br>
 * Class : Panel<br>
 * Date : 29 juil. 2009<br>
 * By JHelp
 */
package jhelp.engine.gui.components;

import jhelp.engine.Texture;
import jhelp.engine.gui.layout.Constraints;
import jhelp.engine.gui.layout.Layout;
import jhelp.engine.gui.layout.LayoutElement;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * A panel (Contains other components with a layout) <br>
 * <br>
 * Last modification : 29 juil. 2009<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public class Panel
      extends Component
{
   /** Describe how layout components */
   private final Layout              layout;
   /** Components inside the panel */
   Hashtable<Constraints, Component> components;

   /**
    * Constructs Panel
    * 
    * @param layout
    *           Layout to use
    */
   public Panel(final Layout layout)
   {
      if(layout == null)
      {
         throw new NullPointerException("layout musn't be null");
      }

      this.layout = layout;
      this.components = new Hashtable<Constraints, Component>();
   }

   /**
    * Call when mouse click
    * 
    * @param x
    *           Mouse X
    * @param y
    *           Mouse Y
    * @param buttonLeft
    *           Indicates if mouse button left is down
    * @param buttonRight
    *           Indicates if mouse button right is down
    * @see jhelp.engine.gui.components.Component#mouseClick(int, int, boolean, boolean)
    */
   @Override
   protected void mouseClick(final int x, final int y, final boolean buttonLeft, final boolean buttonRight)
   {
      final Enumeration<Component> enumeration = this.components.elements();
      Component component;

      while(enumeration.hasMoreElements() == true)
      {
         component = enumeration.nextElement();

         if((x >= component.x) && (y >= component.y) && (x < (component.x + component.width)) && (y < (component.y + component.height)))
         {
            component.mouseClick(x - component.x, y - component.y, buttonLeft, buttonRight);

            break;
         }
      }
   }

   /**
    * Paint the panel
    * 
    * @param texture
    *           Texture where paint
    * @param x
    *           X
    * @param y
    *           Y
    * @see jhelp.engine.gui.components.Component#paintComponent(jhelp.engine.Texture, int, int)
    */
   @Override
   protected void paintComponent(final Texture texture, final int x, final int y)
   {
      final int count = this.components.size();

      if(count == 0)
      {
         return;
      }

      int index = 0;
      LayoutElement[] layoutElement = new LayoutElement[count];
      Constraints constraints;
      Component component;

      Enumeration<Constraints> enumeration = this.components.keys();
      while(enumeration.hasMoreElements() == true)
      {
         constraints = enumeration.nextElement();
         component = this.components.get(constraints);

         component.refreshPreferredSize();
         layoutElement[index++] = new LayoutElement(component, constraints);
      }

      this.layout.layout(this.width, this.height, layoutElement);

      for(index = 0; index < count; index++)
      {
         component = layoutElement[index].getComponent();

         component.paint(texture, x + component.x, y + component.y);

         layoutElement[index].destroy();
         layoutElement[index] = null;
      }

      layoutElement = null;
      component = null;
      constraints = null;
      enumeration = null;
   }

   /**
    * Add component
    * 
    * @param component
    *           Component to add
    * @param constraints
    *           Constraints to apply to the component
    */
   public void addComponent(final Component component, final Constraints constraints)
   {
      if(component == null)
      {
         throw new NullPointerException("component musn't be null");
      }

      if(constraints == null)
      {
         throw new NullPointerException("constraints musn't be null");
      }

      this.components.put(constraints, component);

      this.needRefresh = true;
   }

   /**
    * Indicates if panel need to refresh
    * 
    * @return {@code true} if panel need to refresh
    * @see jhelp.engine.gui.components.Component#isNeedRefresh()
    */
   @Override
   public boolean isNeedRefresh()
   {
      if(super.isNeedRefresh() == true)
      {
         return true;
      }

      final Enumeration<Component> enumeration = this.components.elements();
      while(enumeration.hasMoreElements() == true)
      {
         if(enumeration.nextElement().isNeedRefresh() == true)
         {
            return true;
         }
      }

      return false;
   }

   /**
    * Refresh the preferred size
    * 
    * @see jhelp.engine.gui.components.Component#refreshPreferredSize()
    */
   @Override
   public void refreshPreferredSize()
   {
      final int count = this.components.size();

      if(count == 0)
      {
         return;
      }

      int index = 0;
      LayoutElement[] layoutElement = new LayoutElement[count];
      Constraints constraints;
      Component component;

      final Enumeration<Constraints> enumeration = this.components.keys();
      while(enumeration.hasMoreElements() == true)
      {
         constraints = enumeration.nextElement();
         component = this.components.get(constraints);

         component.refreshPreferredSize();
         layoutElement[index++] = new LayoutElement(component, constraints);
      }

      Dimension dimension = this.layout.computePrefferedSize(this.width, this.height, layoutElement);

      this.preferredWidth = dimension.width;
      this.preferredHeight = dimension.height;

      layoutElement = null;
      dimension = null;
      component = null;
      constraints = null;
   }
}