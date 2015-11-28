package jhelp.engine;

import com.jogamp.opengl.GL;

/**
 * Chain of node.<br>
 * All nodes are attached to previous one. Move the head, will move other nodes when animation refresh.<br>
 * All node must be placed to their start position, constraints are computed at creation of the chain and can't be changed after
 * (For now)
 * 
 * @author JHelp
 */
public class NodeChain
      implements Animation
{
   /** Main node, the head, that all other nodes will follow */
   private final Node      head;
   /** Queue size */
   private final int       length;
   /** Links/constraints to respect */
   private final Bone[]    links;
   /** Points reference */
   private final Point3D[] points;
   /** Chain queue */
   private final Node[]    queue;

   /**
    * Create a new instance of NodeChain
    * 
    * @param head
    *           Head node (All other nodes will follow it)
    * @param queue
    *           Queue list, must have at least 1 node. Order is important, first of the queue follow the head, second follow the
    *           first, third follow the second, ...
    */
   public NodeChain(final Node head, final Node... queue)
   {
      this.length = queue.length;
      if(this.length < 1)
      {
         throw new IllegalArgumentException("Must have at least one element in the queue");
      }

      this.head = head;
      this.queue = queue;
      this.links = new Bone[this.length];
      this.points = new Point3D[this.length + 1];

      this.points[0] = new Point3D(head.getX(), head.getY(), head.getZ());

      for(int i = 0; i < this.length; i++)
      {
         this.points[i + 1] = new Point3D(queue[i].getX(), queue[i].getY(), queue[i].getZ());
         this.links[i] = new Bone(this.points[i], this.points[i + 1]);
      }
   }

   /**
    * Animate node to respects constraints depends on head last moves <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param gl
    *           Open GL context
    * @param absoluteFrame
    *           Absolute frame
    * @return {@code true} because its a never ending animation. Need to use {@link JHelpSceneRenderer#stopAnimation(Animation)}
    *         to stop it
    * @see Animation#animate(javax.media.opengl.GL, float)
    */
   @Override
   public boolean animate(final GL gl, final float absoluteFrame)
   {
      this.points[0].set(this.head.getX(), this.head.getY(), this.head.getZ());

      Point3D point;
      for(int i = 0; i < this.length; i++)
      {
         this.links[i].updateBone();
         point = this.points[i + 1];
         this.queue[i].setPosition(point.x, point.y, point.z);
      }

      return true;
   }

   /**
    * Called when animation initialized <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param startAbsoluteFrame
    *           Starting frame
    * @see Animation#setStartAbsoluteFrame(float)
    */
   @Override
   public void setStartAbsoluteFrame(final float startAbsoluteFrame)
   {
   }
}