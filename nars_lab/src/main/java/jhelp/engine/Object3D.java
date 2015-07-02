/**
 */
package jhelp.engine;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;
import jhelp.engine.io.ConstantsXML;
import jhelp.util.list.EnumerationIterator;
import jhelp.util.text.UtilText;
import jhelp.xml.MarkupXML;

import java.util.ArrayList;

/**
 * 3D object <br>
 * <br>
 * Last modification : 25 janv. 2009<br>
 * Version 0.0.1<br>
 * 
 * @author JHelp
 */
public class Object3D
      extends NodeWithMaterial
{
   /** Lock for synchronization */
   private static final Object        LOCK = new Object();
   /** Bounding box */
   private VirtualBox                 box;
   /** Isobarycenter of object */
   private Point3D                    center;
   /** List ID for OpenGL */
   private int                        idList;
   /** Material apply to the object */
   private Material                   material;
   /** Material used on selection */
   private Material                   materialForSelection;
   /** Indicates if the list must be reconstruct */
   private boolean                    needReconstructTheList;
   /** List of polygons to add */
   private final ArrayList<Polygon3D> polygon3D;
   /** Object's mesh */
   public Mesh                        mesh;

   /**
    * Empty object
    */
   public Object3D()
   {
      this.polygon3D = new ArrayList<Polygon3D>();
      this.nodeType = NodeType.OBJECT3D;
      this.setCanBePick(true);
      this.material = Material.DEFAULT_MATERIAL;
      this.idList = -1;
      this.reconstructTheList();
      this.mesh = new Mesh();
   }

   /**
    * Draw object
    * 
    * @param gl
    *           OpenGL context
    * @param glu
    *           Open GL utilities
    */
   protected void drawObject(final GL2 gl, final GLU glu)
   {
      // If no list is create or actual list needs to be update
      if((this.idList < 0) || (this.needReconstructTheList == true))
      {
         this.needReconstructTheList = false;
         synchronized(Object3D.LOCK)
         {
            Polygon3D polygon3D;
            for(int i = this.polygon3D.size() - 1; i >= 0; i--)
            {
               polygon3D = this.polygon3D.get(i);

               polygon3D.addToObject(this, glu);

               this.polygon3D.remove(i);
            }
         }

         // Delete old list
         if(this.idList >= 0)
         {
            gl.glDeleteLists(this.idList, 1);
         }
         // Create list
         this.idList = gl.glGenLists(1);
         gl.glNewList(this.idList, GL2.GL_COMPILE);
         try
         {
            this.mesh.render(gl);
         }
         catch(final Exception e)
         {
            this.needReconstructTheList = true;
         }
         catch(final Error e)
         {
            this.needReconstructTheList = true;
         }
         gl.glEndList();
      }
      // Draw the list
      gl.glCallList(this.idList);
   }

   /**
    * @see Node#endParseXML()
    */
   @Override
   protected void endParseXML()
   {
      this.reconstructTheList();
   }

   /**
    * Extract Object parameters from XML
    * 
    * @param markupXML
    *           Markup to parse
    * @throws Exception
    *            On parsing problem
    * @see Node#readFromMarkup
    */
   @Override
   protected void readFromMarkup(final MarkupXML markupXML) throws Exception
   {
      this.readMaterialFromMarkup(markupXML);

      final EnumerationIterator<MarkupXML> enumerationIterator = markupXML.obtainChildren(ConstantsXML.MARKUP_MESH);
      if(enumerationIterator.hasMoreElements() == false)
      {
         throw new IllegalArgumentException(UtilText.concatenate("Missing mendatory child ", ConstantsXML.MARKUP_MESH, " in ", markupXML.getName()));
      }

      final MarkupXML mesh = enumerationIterator.getNextElement();
      this.mesh.loadFromXML(mesh);
   }

   /**
    * Extract Materials in XML
    * 
    * @param markupXML
    *           Markup to parse
    */
   protected void readMaterialFromMarkup(final MarkupXML markupXML)
   {
      String material = markupXML.obtainParameter(ConstantsXML.MARKUP_NODE_material, "");
      if(material.length() < 1)
      {
         throw new IllegalArgumentException(
               UtilText.concatenate("Missing mendatory parameter ", ConstantsXML.MARKUP_NODE_material, " in ", markupXML.getName()));
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
   }

   /**
    * Render the object
    * 
    * @param gl
    *           OpenGL context
    * @param glu
    *           Open GL utilities
    * @see Node#renderSpecific(GL, GLU)
    */
   @Override
   protected void renderSpecific(final GL2 gl, final GLU glu)
   {
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

         this.materialForSelection.renderMaterial(gl, glu, this);

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

         this.material.renderMaterial(gl, glu, this);

         this.material.setTwoSided(twoSided);
      }
   }

   /**
    * Render object in picking mode
    * 
    * @param gl
    *           OpenGL context
    * @param glu
    *           Open GL utilities
    * @see Node#renderSpecificPicking(GL, GLU)
    */
   @Override
   protected void renderSpecificPicking(final GL2 gl, final GLU glu)
   {
      this.drawObject(gl, glu);
   }

   /**
    * Render specific for picking UV
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
      final boolean showWire = this.isShowWire();
      this.setShowWire(false);
      Material.obtainMaterialForPickUV().renderMaterial(gl, glu, this);
      this.setShowWire(showWire);
   }

   /**
    * @see Node#startParseXML()
    */
   @Override
   protected final void startParseXML()
   {
      synchronized(Object3D.LOCK)
      {
         this.box = null;
         this.center = null;
      }
   }

   /**
    * Write object in XML
    * 
    * @param markupXML
    *           XML to fill
    * @see Node#writeInMarkup
    */
   @Override
   protected void writeInMarkup(final MarkupXML markupXML)
   {
      this.writeMaterialInMarkup(markupXML);
      markupXML.addChild(this.mesh.saveToXML());
   }

   /**
    * Write materials in XML
    * 
    * @param markupXML
    *           Markup to fill
    */
   protected void writeMaterialInMarkup(final MarkupXML markupXML)
   {
      markupXML.addParameter(ConstantsXML.MARKUP_NODE_material, this.material.getName());
      if(this.materialForSelection != null)
      {
         markupXML.addParameter(ConstantsXML.MARKUP_NODE_materialSelection, this.materialForSelection.getName());
      }

      markupXML.addParameter(ConstantsXML.MARKUP_NODE_twoSided, this.getTwoSidedState().name());
   }

   /**
    * Add vertex to actual object face.<br>
    * the result is see as soon as possible
    * 
    * @param vertex
    *           Vertex to add
    */
   public void add(final Vertex vertex)
   {
      if(vertex == null)
      {
         throw new NullPointerException("The vertex couldn't be null");
      }
      this.mesh.addVertexToTheActualFace(vertex);
      this.reconstructTheList();
   }

   /**
    * Add vertex to the actual face of the object.<br>
    * It is call fast because the vertex is only add, but list is not reconstructs, you have to call
    * <code>reconstructTheList</code> method to see the result<br>
    * It is use when you want add several vertex and see result at the end
    * 
    * @param vertex
    *           Vertex to add
    */
   public void addFast(final Vertex vertex)
   {
      if(vertex == null)
      {
         throw new NullPointerException("The vertex couldn't be null");
      }
      this.mesh.addVertexToTheActualFace(vertex);
   }

   /**
    * Add a polygon face
    * 
    * @param polygon3D
    *           Face to add
    */
   public void addPolygon(final Polygon3D polygon3D)
   {
      if(polygon3D != null)
      {
         synchronized(Object3D.LOCK)
         {
            this.polygon3D.add(polygon3D);
         }
      }
   }

   /**
    * Generate UV on using the better plane for each face.
    * 
    * @param multU
    *           U multiplier
    * @param multV
    *           V multiplier
    */
   public void computeUVfromMax(final float multU, final float multV)
   {
      this.mesh.computeUVfromMax(multU, multV);
      this.reconstructTheList();
   }

   /**
    * Generate UV on using (X, Y) plane.<br>
    * X values are considered like U, Y like V, and we normalize to have good values
    * 
    * @param multU
    *           U multiplier
    * @param multV
    *           V multiplier
    */
   public void computeUVfromPlaneXY(final float multU, final float multV)
   {
      this.mesh.computeUVfromPlaneXY(multU, multV);
      this.reconstructTheList();
   }

   /**
    * Generate UV on using (X, Z) plane.<br>
    * X values are considered like U, Z like V, and we normalize to have good values
    * 
    * @param multU
    *           U multiplier
    * @param multV
    *           V multiplier
    */
   public void computeUVfromPlaneXZ(final float multU, final float multV)
   {
      this.mesh.computeUVfromPlaneXZ(multU, multV);
      this.reconstructTheList();
   }

   /**
    * Generate UV on using (Y, Z) plane.<br>
    * Y values are considered like U, Z like V, and we normalize to have good values
    * 
    * @param multU
    *           U multiplier
    * @param multV
    *           V multiplier
    */
   public void computeUVfromPlaneYZ(final float multU, final float multV)
   {
      this.mesh.computeUVfromPlaneYZ(multU, multV);
      this.reconstructTheList();
   }

   /**
    * Generate UV in spherical way.<br>
    * Imagine you have a mapped sphere around your object, then project it to him
    * 
    * @param multU
    *           U multiplier
    * @param multV
    *           V multiplier
    */
   public void computeUVspherical(final float multU, final float multV)
   {
      this.mesh.computeUVspherical(multU, multV);
      this.reconstructTheList();
   }

   /**
    * Update last changes
    */
   public void flush()
   {
      this.reconstructTheList();
      this.mesh.recomputeTheBox();
   }

   /**
    * Compute bounding box
    * 
    * @return Bounding box
    */
   @Override
   public VirtualBox getBox()
   {
      synchronized(Object3D.LOCK)
      {
         if(this.box == null)
         {
            this.box = this.mesh.computeBox();
         }
         return this.box;
      }
   }

   /**
    * Center of object
    * 
    * @return Object's center
    * @see Node#getCenter()
    */
   @Override
   public Point3D getCenter()
   {
      synchronized(Object3D.LOCK)
      {
         if(this.center == null)
         {
            if(this.box == null)
            {
               this.box = this.mesh.computeBox();
            }
            this.center = this.box.getCenter();
         }
         return this.center;
      }
   }

   /**
    * Object's material
    * 
    * @return Object's material
    */
   @Override
   public Material getMaterial()
   {
      return this.material;
   }

   /**
    * Object's selection material (Can be {@code null}
    * 
    * @return Object's selection material
    */
   @Override
   public Material getMaterialForSelection()
   {
      return this.materialForSelection;
   }

   /**
    * Translate a vertex in the mesh.<br>
    * This translation can translate neighbor vertex, the translation apply to them depends of the solidity.<br>
    * If you specify a 0 solidity, then neighbor don't move, 1, all vertex translate in the same translate, some where between,
    * the object morph
    * 
    * @param indexPoint
    *           Vertex index to translate
    * @param vx
    *           X
    * @param vy
    *           Y
    * @param vz
    *           Z
    * @param solidity
    *           Solidity
    */
   public void movePoint(final int indexPoint, final float vx, final float vy, final float vz, final float solidity)
   {
      this.mesh.movePoint(indexPoint, vx, vy, vz, solidity);
      this.reconstructTheList();
   }

   /**
    * Translate some vertex in the mesh.<br>
    * This translation can translate neighbor vertex, the translation apply to them depends of the solidity.<br>
    * If you specify a 0 solidity, then neighbor don't move, 1, all vertex translate in the same translate, some where between,
    * the object morph<br>
    * You specify a near deep to determine the level of points are translate the same way as the specified index
    * 
    * @param indexPoint
    *           Vertex index to translate
    * @param vx
    *           X
    * @param vy
    *           Y
    * @param vz
    *           Z
    * @param solidity
    *           Solidity
    * @param near
    *           Level of neighbor move with specified point. 0 the point, 1 : one level neighbor, ...
    */
   public void movePoint(final int indexPoint, final float vx, final float vy, final float vz, final float solidity, final int near)
   {
      this.mesh.movePoint(indexPoint, vx, vy, vz, solidity, near);
      this.reconstructTheList();
   }

   /**
    * Create a new face for the object, and this new face become the actual one
    */
   public void nextFace()
   {
      this.mesh.endFace();
   }

   /**
    * Make the center of object vertexes be also the center of the object
    */
   public void recenterObject()
   {
      this.mesh.centerMesh();
      this.flush();
   }

   /**
    * Force update the last changes on the mesh
    */
   public void reconstructTheList()
   {
      synchronized(Object3D.LOCK)
      {
         this.box = null;
         this.center = null;
         this.needReconstructTheList = true;
      }
   }

   /**
    * Remove all children and make the object empty
    */
   public void reset()
   {
      this.removeAllChildren();
      this.mesh.reset();
      this.flush();
   }

   /**
    * Change object's material
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
    * Change object's selection material.<br>
    * Use {@code null} if you don't have a selection state
    * 
    * @param materialForSelection
    *           New object's selection material
    */
   @Override
   public void setMaterialForSelection(final Material materialForSelection)
   {
      this.materialForSelection = materialForSelection;
   }
}