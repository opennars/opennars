package jhelp.engine;

import jhelp.engine.util.PositionNode;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents a mirror.<br>
 * Mirror are special material that reflect the scene, like a real mirror should do.<br>
 * They are link to an object and a position where put the scene for compute the reflected image to allow various effect.<br>
 * Mirrors are heavy to compute, so don't put too much
 * 
 * @author JHelp
 */
public class Miror
{
   /** Next mirror ID */
   private static final AtomicInteger NEXT_ID = new AtomicInteger(0);
   /** Material that show the reflected image */
   private final Material             material;
   /** Object where draw the mirror image */
   private final NodeWithMaterial     nodeWithMaterial;
   /** Original scene position to put it at original place when mirror image is computed */
   private PositionNode               originalPosition;
   /** Mirror position and orientation (Remember that what it looks is what is draw) */
   private final PositionNode         positionNode;
   /** Texture where mirror image is draw */
   private final Texture              texture;
   /** Mirror background alpha */
   public float                       backgroundAlpha;
   /** Mirror background blue */
   public float                       backgroundBlue;
   /** Mirror background green */
   public float                       backgroundGreen;
   /** Mirror background red */
   public float                       backgroundRed;

   /**
    * Create a new instance of Miror
    * 
    * @param nodeWithMaterial
    *           Object where draw the mirror image
    * @param positionNode
    *           Mirror view position
    */
   public Miror(final NodeWithMaterial nodeWithMaterial, final PositionNode positionNode)
   {
      this.nodeWithMaterial = nodeWithMaterial;
      this.positionNode = positionNode;
      final String name = "JHelpMirror" + Miror.NEXT_ID.getAndIncrement();
      this.material = new Material(name);
      this.texture = new Texture(name, 16, 16, 0xCAFEFACE);
      this.material.setTextureDiffuse(this.texture);
      nodeWithMaterial.setMaterial(this.material);
   }

   /**
    * Called to finish mirror computing
    * 
    * @param scene
    *           Scene parent
    */
   void endRender(final Scene scene)
   {
      final Node root = scene.getRoot();
      root.setPosition(this.originalPosition.x, this.originalPosition.y, this.originalPosition.z);
      root.setAngleX(this.originalPosition.angleX);
      root.setAngleY(this.originalPosition.angleY);
      root.setAngleZ(this.originalPosition.angleZ);
      root.setScale(this.originalPosition.scaleX, this.originalPosition.scaleY, this.originalPosition.scaleZ);

      this.material.setTextureDiffuse(this.texture);
      this.nodeWithMaterial.setVisible(true);
   }

   /**
    * Called to start mirror computing
    * 
    * @param scene
    *           Scene parent
    * @return Texture where draw image. {@code null} means that the mirror no need to be render (Because the object is not
    *         visible)
    */
   Texture startRender(final Scene scene)
   {
      if(this.nodeWithMaterial.isVisible() == false)
      {
         return null;
      }

      this.nodeWithMaterial.setVisible(false);
      final Node root = scene.getRoot();
      this.originalPosition = new PositionNode(root);
      root.setPosition(this.positionNode.x, this.positionNode.y, this.positionNode.z);
      root.setAngleX(this.positionNode.angleX);
      root.setAngleY(this.positionNode.angleY);
      root.setAngleZ(this.positionNode.angleZ);
      root.setScale(this.positionNode.scaleX, this.positionNode.scaleY, this.positionNode.scaleZ);
      return this.texture;
   }

   /**
    * Mirror material
    * 
    * @return Mirror material
    */
   public Material getMaterial()
   {
      return this.material;
   }

   /**
    * Object linked
    * 
    * @return Object linked
    */
   public NodeWithMaterial getNodeWithMaterial()
   {
      return this.nodeWithMaterial;
   }

   /**
    * Mirror position
    * 
    * @return Mirror position
    */
   public PositionNode getPositionNode()
   {
      return this.positionNode;
   }
}