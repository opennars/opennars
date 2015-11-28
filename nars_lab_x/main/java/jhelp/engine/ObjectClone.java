/**
 */
package jhelp.engine;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;
import jhelp.engine.io.ConstantsXML;
import jhelp.util.text.UtilText;
import jhelp.xml.MarkupXML;

/**
 * A clone is an object who used the same mesh than other.<br>
 * The aim is to economize video memory if we use same mesh several times.<br>
 * But if the original object change its mesh, then this change also <br>
 * <br>
 * Last modification : 25 janv. 2009<br>
 * Version 0.0.1<br>
 * 
 * @author JHelp
 */
public class ObjectClone
      extends NodeWithMaterial
{
   /** Material used */
   private Material material;
   /** Selection material */
   private Material materialForSelection;
   /** Reference object */
   private Object3D reference;
   /** Object reference name */
   private String   referenceName;

   /**
    * Constructs ObjectClone
    * 
    * @param reference
    *           Reference object
    */
   public ObjectClone(final Object3D reference)
   {
      this.nodeType = NodeType.CLONE;

      this.setCanBePick(true);
      this.reference = reference;
      this.material = Material.DEFAULT_MATERIAL;
      if(reference != null)
      {
         this.referenceName = reference.nodeName;
      }
      else
      {
         this.referenceName = null;
      }
   }

   /**
    * Constructs ObjectClone
    * 
    * @param reference
    *           Reference object name
    */
   public ObjectClone(final String reference)
   {
      if(reference == null)
      {
         throw new NullPointerException("reference musn't be null");
      }

      this.nodeType = NodeType.CLONE;

      this.setCanBePick(true);
      this.material = Material.DEFAULT_MATERIAL;
      this.referenceName = reference;
   }

   /**
    * @see Node#endParseXML()
    */
   @Override
   protected void endParseXML()
   {
      this.reference = null;
   }

   /**
    * Read clone parameters form XML
    * 
    * @param markupXML
    *           XML to parse
    * @see Node#readFromMarkup
    */
   @Override
   protected void readFromMarkup(final MarkupXML markupXML)
   {
      String material = markupXML.obtainParameter(ConstantsXML.MARKUP_NODE_material, "");
      if(material.length() < 1)
      {
         throw new IllegalArgumentException(UtilText.concatenate("Missing mandatory parameter ", ConstantsXML.MARKUP_NODE_material, " in ", markupXML.getName()));
      }
      this.material = Material.obtainMaterial(material);
      material = markupXML.obtainParameter(ConstantsXML.MARKUP_NODE_materialSelection, "");
      if(material.length() > 0)
      {
         this.materialForSelection = Material.obtainMaterial(material);
      }

      material = markupXML.obtainParameter(ConstantsXML.MARKUP_NODE_twoSided, TwoSidedState.AS_MATERIAL.name());
      this.setTwoSidedState(TwoSidedState.valueOf(material));

      material = null;
      this.referenceName = markupXML.obtainParameter(ConstantsXML.MARKUP_NODE_reference, "");
      if(this.referenceName.length() < 1)
      {
         this.referenceName = null;
      }
   }

   /**
    * Render this object
    * 
    * @param gl
    *           OpenGL context
    * @param glu
    *           Open GL utilities
    * @see Node#renderSpecific(GL, GLU)
    */
   @Override
   protected synchronized void renderSpecific(final GL2 gl, final GLU glu)
   {
      if((this.reference == null) && (this.referenceName != null))
      {
         this.reference = (Object3D) this.getRoot().getFirstNode(this.referenceName);
      }
      if(this.reference != null)
      {
         final boolean showWire = this.reference.isShowWire();
         final Color4f wireColor = this.reference.getWireColor();

         this.reference.setShowWire(this.isShowWire());
         this.reference.setWireColor(this.getWireColor());

         if((this.isSelected() == true) && (this.materialForSelection != null))
         {
            final boolean twoSided = this.materialForSelection.isTwoSided();

            switch(this.getTwoSidedState())
            {
               case AS_MATERIAL:
               break;
               case FORCE_ONE_SIDE:
                  this.materialForSelection.setTwoSided(false);
               break;
               case FORCE_TWO_SIDE:
                  this.materialForSelection.setTwoSided(true);
               break;
            }

            this.materialForSelection.renderMaterial(gl, glu, this.reference);

            this.materialForSelection.setTwoSided(twoSided);
         }
         else
         {
            final boolean twoSided = this.material.isTwoSided();

            switch(this.getTwoSidedState())
            {
               case AS_MATERIAL:
               break;
               case FORCE_ONE_SIDE:
                  this.material.setTwoSided(false);
               break;
               case FORCE_TWO_SIDE:
                  this.material.setTwoSided(true);
               break;
            }

            this.material.renderMaterial(gl, glu, this.reference);

            this.material.setTwoSided(twoSided);
         }
         this.reference.setShowWire(showWire);
         this.reference.setWireColor(wireColor);
      }
   }

   /**
    * Render in picking mode
    * 
    * @param gl
    *           OpenGL context
    * @see Node#renderSpecificPicking(GL, GLU)
    */
   @Override
   protected synchronized void renderSpecificPicking(final GL2 gl, final GLU glu)
   {
      if((this.reference == null) && (this.referenceName != null))
      {
         this.reference = (Object3D) this.getRoot().getFirstNode(this.referenceName);
      }

      if(this.reference != null)
      {
         this.reference.drawObject(gl, glu);
      }
   }

   /**
    * Render for pick UV specific for clone
    * 
    * @param gl
    *           Open GL context
    * @param glu
    *           Open GL utilities
    * @see Node#renderSpecificPickingUV(javax.media.opengl.GL, javax.media.opengl.glu.GLU)
    */
   @Override
   protected void renderSpecificPickingUV(final GL2 gl, final GLU glu)
   {
      if((this.reference == null) && (this.referenceName != null))
      {
         this.reference = (Object3D) this.getRoot().getFirstNode(this.referenceName);
      }

      if(this.reference != null)
      {
         final boolean showWire = this.reference.isShowWire();
         this.reference.setShowWire(false);
         Material.obtainMaterialForPickUV().renderMaterial(gl, glu, this.reference);
         this.reference.setShowWire(showWire);
      }
   }

   /**
    * Start parsing XML
    * 
    * @see Node#startParseXML()
    */
   @Override
   protected void startParseXML()
   {
   }

   /**
    * Write clone in XML
    * 
    * @param markupXML
    *           XML to fill
    * @see Node#writeInMarkup
    */
   @Override
   protected void writeInMarkup(final MarkupXML markupXML)
   {
      markupXML.addParameter(ConstantsXML.MARKUP_NODE_material, this.material.getName());
      if(this.materialForSelection != null)
      {
         markupXML.addParameter(ConstantsXML.MARKUP_NODE_materialSelection, this.materialForSelection.getName());
      }

      markupXML.addParameter(ConstantsXML.MARKUP_NODE_reference, this.referenceName);
      markupXML.addParameter(ConstantsXML.MARKUP_NODE_twoSided, this.getTwoSidedState().name());
   }

   /**
    * Bonding box
    * 
    * @return Bonding box
    * @see jhelp.engine.NodeWithMaterial#getBox()
    */
   @Override
   public VirtualBox getBox()
   {
      if(this.reference == null)
      {
         return new VirtualBox();
      }

      return this.reference.getBox();
   }

   /**
    * Object center
    * 
    * @return Object center
    * @see Node#getCenter()
    */
   @Override
   public Point3D getCenter()
   {
      return this.reference.getCenter();
   }

   /**
    * Material
    * 
    * @return Material
    */
   @Override
   public Material getMaterial()
   {
      return this.material;
   }

   /**
    * Selection material
    * 
    * @return Selection material
    */
   @Override
   public Material getMaterialForSelection()
   {
      return this.materialForSelection;
   }

   /**
    * Reference object
    * 
    * @return Reference object
    */
   public Object3D getReference()
   {
      return this.reference;
   }

   /**
    * Change material
    * 
    * @param material
    *           New material
    */
   @Override
   public void setMaterial(final Material material)
   {
      if(material == null)
      {
         throw new NullPointerException("The material couldn't be null");
      }
      this.material = material;
   }

   /**
    * Change selection material.<br>
    * Used {@code null} for disable selection distinction
    * 
    * @param materialForSelection
    *           Selection material
    */
   @Override
   public void setMaterialForSelection(final Material materialForSelection)
   {
      this.materialForSelection = materialForSelection;
   }

   /**
    * Change reference object
    * 
    * @param reference
    *           New reference
    */
   public synchronized void setReference(final Object3D reference)
   {
      this.reference = reference;
      if(reference != null)
      {
         this.referenceName = reference.nodeName;
      }
      else
      {
         this.referenceName = null;
      }
   }
}