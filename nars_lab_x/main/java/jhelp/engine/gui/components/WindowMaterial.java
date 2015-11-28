/**
 * Project : JHelpEngine<br>
 * Package : jhelp.gui.components<br>
 * Class : Component3D<br>
 * Date : 26 juil. 2009<br>
 * By JHelp
 */
package jhelp.engine.gui.components;

import jhelp.engine.*;
import jhelp.engine.event.NodeListener;
import jhelp.engine.util.ColorsUtil;
import jhelp.util.text.UtilText;
import jhelp.util.thread.ThreadManager;
import jhelp.util.thread.ThreadedVerySimpleTask;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Window as material<br>
 * <br>
 * Last modification : 26 juil. 2009<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public class WindowMaterial
      implements NodeListener, PickUVlistener
{
   /** Call it for refresh the window */
   private final ThreadedVerySimpleTask delayedRefresh = new ThreadedVerySimpleTask()
                                                       {
                                                          @Override
                                                          protected void doVerySimpleAction()
                                                          {
                                                             WindowMaterial.this.refresh();
                                                          }
                                                       };
   /** Synchronization lock */
   private final ReentrantLock          lock;
   /** Main component draw on window */
   private Component                    mainComponent;
   /** Material base */
   private final Material               material;
   /** Indicates if mouse button left is down */
   private boolean                      mouseLeft;
   /** Indicates if mouse button right is down */
   private boolean                      mouseRight;
   /** Window name */
   private final String                 name;
   /** Indicates if window need refresh as soon as possible */
   private boolean                      needRefresh;
   /** Object that carry the window */
   private NodeWithMaterial             object;
   /** Base renderer */
   private JHelpSceneRenderer           sceneRenderer;
   /** Window texture */
   private final Texture                texture;
   /** Widow texture background */
   private Texture                      textureBackground;

   /**
    * Constructs WindowMaterial
    * 
    * @param name
    *           Window name
    * @param object
    *           Object to apply the window
    */
   public WindowMaterial(final String name, final NodeWithMaterial object)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      if(object == null)
      {
         throw new NullPointerException("object musn't be null");
      }

      this.name = name;
      this.object = object;

      this.texture = new Texture(UtilText.concatenate("JHELP_WINDOW_", name, "_TEXTURE"), 1024, 1024);
      this.texture.setAutoFlush(false);

      this.material = new Material(UtilText.concatenate("JHELP_WINDOW_", name, "_MATERIAL"));
      this.material.getColorEmissive().set(1f);
      this.material.setSpecularLevel(1f);
      this.material.setShininess(128);
      this.material.getColorDiffuse().set(1f);
      this.material.getColorSpecular().set();
      this.material.getColorAmbiant().set(1f);
      this.material.setTwoSided(true);
      this.material.setTextureDiffuse(this.texture);

      this.object.setMaterial(this.material);
      this.object.addNodeListener(this);
      this.object.pickUVlistener = this;

      this.needRefresh = true;
      this.lock = new ReentrantLock();
   }

   /**
    * Refresh the window
    */
   void refresh()
   {
      this.needRefresh = false;
      this.lock.lock();

      if(this.texture.willBeRefresh() == true)
      {
         return;
      }

      this.texture.fillRect(0, 0, 1024, 1024, ColorsUtil.TRANSPARENT, false);

      if(this.textureBackground != null)
      {
         final int w = this.textureBackground.getWidth();
         final int h = this.textureBackground.getHeight();

         int x = 0;
         int y = 0;
         while(y < 1024)
         {
            x = 0;
            while(x < 1024)
            {
               this.texture.drawTexture(x, y, this.textureBackground, 0, 0, w, h);

               x += w;
            }

            y += h;
         }
      }

      this.mainComponent.setBounds(0, 0, 1024, 1024);
      this.mainComponent.paint(this.texture, 0, 0);

      this.texture.flush();

      this.lock.unlock();
   }

   /**
    * Return mainComponent
    * 
    * @return mainComponent
    */
   public Component getMainComponent()
   {
      return this.mainComponent;
   }

   /**
    * Return material
    * 
    * @return material
    */
   public Material getMaterial()
   {
      return this.material;
   }

   /**
    * Return name
    * 
    * @return name
    */
   public String getName()
   {
      return this.name;
   }

   /**
    * Return object
    * 
    * @return object
    */
   public NodeWithMaterial getObject()
   {
      return this.object;
   }

   /**
    * Return texture
    * 
    * @return texture
    */
   public Texture getTexture()
   {
      return this.texture;
   }

   /**
    * Return textureBackground
    * 
    * @return textureBackground
    */
   public Texture getTextureBackground()
   {
      return this.textureBackground;
   }

   /**
    * Call when mouse click
    * 
    * @param node
    *           Node where click
    * @param leftButton
    *           Indicates if mouse left button is down
    * @param rightButton
    *           Indicates if mouse right button is down
    * @see jhelp.engine.event.NodeListener#mouseClick(jhelp.engine.Node, boolean, boolean)
    */
   @Override
   public void mouseClick(final Node node, final boolean leftButton, final boolean rightButton)
   {
      this.mouseLeft = leftButton;
      this.mouseRight = rightButton;

      if(this.sceneRenderer != null)
      {
         this.sceneRenderer.setPickUVnode(this.object);
      }
   }

   /**
    * Call when mouse enter
    * 
    * @param node
    *           Node enter
    * @see jhelp.engine.event.NodeListener#mouseEnter(jhelp.engine.Node)
    */
   @Override
   public void mouseEnter(final Node node)
   {
   }

   /**
    * Call when mouse exit
    * 
    * @param node
    *           Node exit
    * @see jhelp.engine.event.NodeListener#mouseExit(jhelp.engine.Node)
    */
   @Override
   public void mouseExit(final Node node)
   {
   }

   /**
    * Call on UV picking
    * 
    * @param u
    *           U
    * @param v
    *           V
    * @param node
    *           Node pick
    * @see jhelp.engine.PickUVlistener#pickUV(int, int, jhelp.engine.Node)
    */
   @Override
   public void pickUV(final int u, final int v, final Node node)
   {
      this.sceneRenderer.disablePickUV();

      final int x = u << 2;
      final int y = v << 2;

      if(this.mainComponent != null)
      {
         WindowMaterial.this.mainComponent.mouseClick(x, y, this.mouseLeft, this.mouseRight);
      }
   }

   /**
    * Refresh the window if need
    */
   public void refreshIfNeed()
   {
      if((this.needRefresh == true) || (this.mainComponent.isNeedRefresh() == true))
      {
         ThreadManager.THREAD_MANAGER.doThread(this.delayedRefresh, null);
      }

      this.needRefresh = false;
   }

   /**
    * Modify mainComponent
    * 
    * @param mainComponent
    *           New mainComponent value
    */
   public void setMainComponent(final Component mainComponent)
   {
      this.mainComponent = mainComponent;

      if(this.mainComponent != null)
      {
         this.mainComponent.x = this.mainComponent.y = 0;
         this.mainComponent.width = this.mainComponent.height = 1024;
      }

      this.needRefresh = true;
   }

   /**
    * Modify object
    * 
    * @param object
    *           New object value
    */
   public void setObject(final NodeWithMaterial object)
   {
      if(object == null)
      {
         throw new NullPointerException("object musn't be null");
      }

      this.object.removeNodeListener(this);
      this.object.pickUVlistener = null;

      this.object = object;
      this.object.setMaterial(this.material);
      this.object.addNodeListener(this);
      this.object.pickUVlistener = this;
   }

   /**
    * Modify sceneRenderer
    * 
    * @param sceneRenderer
    *           New sceneRenderer value
    */
   public void setSceneRenderer(final JHelpSceneRenderer sceneRenderer)
   {
      if(sceneRenderer == null)
      {
         throw new NullPointerException("sceneRenderer musn't be null");
      }

      this.sceneRenderer = sceneRenderer;
   }

   /**
    * Modify textureBackground
    * 
    * @param textureBackground
    *           New textureBackground value
    */
   public void setTextureBackground(final Texture textureBackground)
   {
      this.textureBackground = textureBackground;

      this.needRefresh = true;
   }
}