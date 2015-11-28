/**
 */
package jhelp.engine;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;
import jhelp.engine.io.ConstantsXML;
import jhelp.engine.util.Math3D;
import jhelp.util.list.EnumerationIterator;
import jhelp.util.text.UtilText;
import jhelp.xml.MarkupXML;

import java.util.Hashtable;

//import com.jogamp.opengl.glu.GLU;

/**
 * Material for 3D object<br>
 * It's a mix with diffuse an environment<br>
 * <br>
 * 
 * @author JHelp
 */
public class Material
{
   /** Materials table */
   private static Hashtable<String, Material> hashtableMaterials;
   /** Material use for pick UV */
   private static Material                    materialForPickUV;
   /** New material default header name */
   private static final String                NEW_MATERIAL_HEADER  = "MATERIAL_";

   /** Next default ID name */
   private static int                         nextID               = 0;
   /** Default material */
   public static final Material               DEFAULT_MATERIAL     = new Material("Default");

   /** Name for material 2D */
   public static final String                 MATERIAL_FOR_2D_NAME = "MATERIAL_FOR_2D";

   /**
    * Register a material
    * 
    * @param material
    *           Material to register
    */
   private static void registerMaterial(final Material material)
   {
      if(Material.hashtableMaterials == null)
      {
         Material.hashtableMaterials = new Hashtable<String, Material>();
      }
      Material.hashtableMaterials.put(material.name, material);
   }

   /**
    * New default named material
    * 
    * @return New default named material
    */
   public static Material createNewMaterial()
   {
      if(Material.hashtableMaterials == null)
      {
         Material.hashtableMaterials = new Hashtable<String, Material>();
      }
      String name = Material.NEW_MATERIAL_HEADER + (Material.nextID++);
      while(Material.hashtableMaterials.containsKey(name) == true)
      {
         name = Material.NEW_MATERIAL_HEADER + (Material.nextID++);
      }
      return new Material(name);
   }

   /**
    * Create a new maetirial with a specific base name
    * 
    * @param name
    *           Base name
    * @return Created material
    */
   public static Material createNewMaterial(String name)
   {
      if(Material.hashtableMaterials == null)
      {
         Material.hashtableMaterials = new Hashtable<String, Material>();
      }

      if((name == null) || ((name = name.trim()).length() == 0))
      {
         name = Material.NEW_MATERIAL_HEADER + "0";
      }

      name = UtilText.computeNotInsideName(name, Material.hashtableMaterials.keySet());

      return new Material(name);
   }

   /**
    * Obtain material with its name
    * 
    * @param name
    *           Material name
    * @return The material or {@link #DEFAULT_MATERIAL} if the material not exists
    */
   public static Material obtainMaterial(final String name)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }
      if(Material.hashtableMaterials == null)
      {
         return Material.DEFAULT_MATERIAL;
      }
      final Material material = Material.hashtableMaterials.get(name);
      if(material == null)
      {
         return Material.DEFAULT_MATERIAL;
      }
      return material;
   }

   /**
    * Material use for pick UV
    * 
    * @return Material use for pick UV
    */
   public static Material obtainMaterialForPickUV()
   {
      if(Material.materialForPickUV != null)
      {
         return Material.materialForPickUV;
      }

      Material.materialForPickUV = new Material("JHELP_MATERIAL_FOR_PICK_UV");
      Material.materialForPickUV.getColorEmissive().set(1f);
      Material.materialForPickUV.setSpecularLevel(1f);
      Material.materialForPickUV.setShininess(128);
      Material.materialForPickUV.getColorDiffuse().set(1f);
      Material.materialForPickUV.getColorSpecular().set();
      Material.materialForPickUV.getColorAmbiant().set(1f);
      Material.materialForPickUV.setTwoSided(true);
      Material.materialForPickUV.setTextureDiffuse(Texture.obtainTextureForPickUV());

      return Material.materialForPickUV;
   }

   /**
    * Obtain a material or create a new one if not exists
    * 
    * @param name
    *           Material name
    * @return Searched material or newly created
    */
   public static Material obtainMaterialOrCreate(final String name)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }

      if(Material.hashtableMaterials == null)
      {
         return new Material(name);
      }

      final Material material = Material.hashtableMaterials.get(name);
      if(material == null)
      {
         return new Material(name);
      }

      return material;
   }

   /**
    * Parse a XML markup to create a material
    * 
    * @param markupXML
    *           Markup XML to parse
    * @return Created material
    */
   public static Material parseXML(final MarkupXML markupXML)
   {
      final Material material = Material.obtainMaterialOrCreate(markupXML.obtainParameter(ConstantsXML.MARKUP_MATERIAL_name));

      material.colorAmbiant.parseString(markupXML.obtainParameter(ConstantsXML.MARKUP_MATERIAL_colorAmbiant));
      material.colorDiffuse.parseString(markupXML.obtainParameter(ConstantsXML.MARKUP_MATERIAL_colorDiffuse));
      material.colorEmissive.parseString(markupXML.obtainParameter(ConstantsXML.MARKUP_MATERIAL_colorEmissive));
      material.colorSpecular.parseString(markupXML.obtainParameter(ConstantsXML.MARKUP_MATERIAL_colorSpecular));
      material.shininess = markupXML.obtainParameter(ConstantsXML.MARKUP_MATERIAL_shininess, 12);
      material.specularLevel = markupXML.obtainParameter(ConstantsXML.MARKUP_MATERIAL_specularLevel, 0.1f);
      material.sphericRate = markupXML.obtainParameter(ConstantsXML.MARKUP_MATERIAL_sphericRate, 1f);
      material.transparency = markupXML.obtainParameter(ConstantsXML.MARKUP_MATERIAL_sphericRate, 1f);
      material.twoSided = markupXML.obtainParameter(ConstantsXML.MARKUP_MATERIAL_twoSided, false);

      String name = markupXML.obtainParameter(ConstantsXML.MARKUP_MATERIAL_textureDiffuse);
      if(name != null)
      {
         material.textureDiffuse = Texture.obtainTexture(name);
      }

      name = markupXML.obtainParameter(ConstantsXML.MARKUP_MATERIAL_textureSpheric);
      if(name != null)
      {
         material.textureSpheric = Texture.obtainTexture(name);
      }

      return material;
   }

   /**
    * Force refresh all materials
    */
   public static final void refreshAllMaterials()
   {
      if(Material.hashtableMaterials == null)
      {
         return;
      }

      for(final Material material : new EnumerationIterator<Material>(Material.hashtableMaterials.elements()))
      {
         if(material.textureDiffuse != null)
         {
            material.textureDiffuse.flush();
         }

         if(material.textureSpheric != null)
         {
            material.textureSpheric.flush();
         }

         if(material.cubeMap != null)
         {
            material.cubeMap.flush();
         }
      }
   }

   /**
    * Rename a material
    * 
    * @param material
    *           Material to rename
    * @param newName
    *           New name
    */
   public static void rename(final Material material, String newName)
   {
      if(material == null)
      {
         throw new NullPointerException("material musn't be null");
      }
      if(newName == null)
      {
         throw new NullPointerException("newName musn't be null");
      }
      newName = newName.trim();
      if(newName.length() < 1)
      {
         throw new IllegalArgumentException("newName musn't be empty");
      }
      if(material.name.equals(newName) == true)
      {
         return;
      }
      Material.hashtableMaterials.remove(material.name);
      material.name = newName;
      Material.hashtableMaterials.put(newName, material);
   }

   /** Ambiant color */
   private Color4f colorAmbiant;
   /** Diffuse color */
   private Color4f colorDiffuse;
   /** Emissive color */
   private Color4f colorEmissive;
   /** Specular color */
   private Color4f colorSpecular;
   /** Cube map */
   private CubeMap cubeMap;
   /** Rate for cube map */
   private float   cubeMapRate;
   /** Material name */
   private String  name;
   /** Shininess (0 <-> 128) */
   private int     shininess;
   /** Specular level (0 <-> 1) */
   private float   specularLevel;
   /** Rate for environment */
   private float   sphericRate;
   /** Texture diffuse */
   private Texture textureDiffuse;
   /** Texture environment */
   private Texture textureSpheric;
   /** Transparency (0 <-> 1) */
   private float   transparency;
   /** Indicates if the material is two sided */
   private boolean twoSided;

   /**
    * Constructs the material
    * 
    * @param name
    *           Material name
    */
   public Material(String name)
   {
      if(name == null)
      {
         throw new NullPointerException("name musn't be null");
      }
      name = name.trim();
      if(name.length() < 1)
      {
         throw new IllegalArgumentException("name musn't be empty");
      }
      this.name = name;
      this.colorAmbiant = Color4f.BLACK.getCopy();
      this.colorDiffuse = Color4f.GRAY.getCopy();
      this.colorEmissive = Color4f.DARK_GRAY.getCopy();
      this.colorSpecular = Color4f.LIGHT_GRAY.getCopy();
      this.specularLevel = 0.1f;
      this.shininess = 12;
      this.transparency = 1f;
      this.twoSided = false;
      this.sphericRate = 1f;
      this.cubeMapRate = 1f;
      Material.registerMaterial(this);
   }

   /**
    * Render the material for a 3D object
    * 
    * @param gl
    *           OpenGL context
    * @param glu
    *           Open GL utilities
    * @param object3D
    *           Object to render
    */
   void renderMaterial(final GL2 gl, final GLU glu, final Object3D object3D)
   {
      this.prepareMaterial(gl);
      //
      if(this.textureDiffuse != null)
      {
         gl.glEnable(GL2.GL_TEXTURE_2D);
         this.textureDiffuse.bind(gl);
         object3D.drawObject(gl, glu);
         gl.glDisable(GL2.GL_TEXTURE_2D);
      }
      else
      {
         object3D.drawObject(gl, glu);
      }

      if(this.textureSpheric != null)
      {
         final float transparency = this.transparency;
         this.transparency *= this.sphericRate;
         //
         this.prepareMaterial(gl);
         gl.glDepthFunc(GL2.GL_LEQUAL);
         //
         gl.glEnable(GL2.GL_TEXTURE_2D);
         gl.glEnable(GL2.GL_TEXTURE_GEN_S);
         gl.glEnable(GL2.GL_TEXTURE_GEN_T);
         //
         gl.glTexGeni(GL2.GL_S, GL2.GL_TEXTURE_GEN_MODE, GL2.GL_SPHERE_MAP);
         gl.glTexGeni(GL2.GL_T, GL2.GL_TEXTURE_GEN_MODE, GL2.GL_SPHERE_MAP);
         //
         this.textureSpheric.bind(gl);
         object3D.drawObject(gl, glu);
         //
         gl.glDisable(GL2.GL_TEXTURE_GEN_T);
         gl.glDisable(GL2.GL_TEXTURE_GEN_S);
         gl.glDisable(GL2.GL_TEXTURE_2D);
         //
         gl.glDepthFunc(GL2.GL_LESS);
         //
         this.transparency = transparency;
      }

      if(this.cubeMap != null)
      {
         final float transparency = this.transparency;
         this.transparency *= this.cubeMapRate;

         this.prepareMaterial(gl);
         gl.glDepthFunc(GL2.GL_LEQUAL);

         this.cubeMap.bind(gl);
         object3D.drawObject(gl, glu);
         this.cubeMap.endCubeMap(gl);

         gl.glDepthFunc(GL2.GL_LESS);
         this.transparency = transparency;
      }

      if(object3D.isShowWire())
      {
         gl.glDisable(GL2.GL_LIGHTING);
         object3D.getWireColor().glColor4f(gl);
         gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
         gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_REPLACE);
         object3D.drawObject(gl, glu);
         gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_MODULATE);
         gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
         gl.glEnable(GL2.GL_LIGHTING);
      }
   }

   /**
    * Indicates if an Object is the same as the material
    * 
    * @param object
    *           Object to compare
    * @return {@code true} if an Object is the same as the material
    * @see Object#equals(Object)
    */
   @Override
   public boolean equals(final Object object)
   {
      if(object == null)
      {
         return false;
      }
      if(super.equals(object) == true)
      {
         return true;
      }
      if((object instanceof Material) == false)
      {
         return false;
      }
      final Material material = (Material) object;
      if(material.name.equals(this.name) == true)
      {
         return true;
      }
      if(material.colorAmbiant.equals(this.colorAmbiant) == false)
      {
         return false;
      }
      if(material.colorDiffuse.equals(this.colorDiffuse) == false)
      {
         return false;
      }
      if(material.colorEmissive.equals(this.colorEmissive) == false)
      {
         return false;
      }
      if(material.colorSpecular.equals(this.colorSpecular) == false)
      {
         return false;
      }
      if(material.shininess != this.shininess)
      {
         return false;
      }
      if(material.twoSided != this.twoSided)
      {
         return false;
      }
      if(Math3D.equal(material.specularLevel, this.specularLevel) == false)
      {
         return false;
      }
      if(Math3D.equal(material.sphericRate, this.sphericRate) == false)
      {
         return false;
      }
      if(Math3D.equal(material.transparency, this.transparency) == false)
      {
         return false;
      }
      if(((this.textureDiffuse == null) && (material.textureDiffuse != null)) || ((this.textureDiffuse != null) && (material.textureDiffuse == null)))
      {
         return false;
      }
      if((this.textureDiffuse != null) && (material.textureDiffuse != null) && (this.textureDiffuse.equals(material.textureDiffuse) == false))
      {
         return false;
      }
      if(((this.textureSpheric == null) && (material.textureSpheric != null)) || ((this.textureSpheric != null) && (material.textureSpheric == null)))
      {
         return false;
      }
      if((this.textureSpheric != null) && (material.textureSpheric != null) && (this.textureSpheric.equals(material.textureSpheric) == false))
      {
         return false;
      }
      return true;
   }

   /**
    * Ambiant color
    * 
    * @return Ambiant color
    */
   public Color4f getColorAmbiant()
   {
      return this.colorAmbiant;
   }

   /**
    * Diffuse color
    * 
    * @return Diffuse color
    */
   public Color4f getColorDiffuse()
   {
      return this.colorDiffuse;
   }

   /**
    * Emissive color
    * 
    * @return Emissive color
    */
   public Color4f getColorEmissive()
   {
      return this.colorEmissive;
   }

   /**
    * Specular color
    * 
    * @return Specular color
    */
   public Color4f getColorSpecular()
   {
      return this.colorSpecular;
   }

   /**
    * Return cubeMap
    * 
    * @return cubeMap
    */
   public CubeMap getCubeMap()
   {
      return this.cubeMap;
   }

   /**
    * Return cubeMapRate
    * 
    * @return cubeMapRate
    */
   public float getCubeMapRate()
   {
      return this.cubeMapRate;
   }

   /**
    * Material name
    * 
    * @return Material name
    */
   public String getName()
   {
      return this.name;
   }

   /**
    * Shininess
    * 
    * @return Shininess
    */
   public int getShininess()
   {
      return this.shininess;
   }

   /**
    * Specular level
    * 
    * @return Specular level
    */
   public float getSpecularLevel()
   {
      return this.specularLevel;
   }

   /**
    * Environment rate
    * 
    * @return Environment rate
    */
   public float getSphericRate()
   {
      return this.sphericRate;
   }

   /**
    * Diffuse texture
    * 
    * @return Diffuse texture
    */
   public Texture getTextureDiffuse()
   {
      return this.textureDiffuse;
   }

   /**
    * Environment texture
    * 
    * @return Environment texture
    */
   public Texture getTextureSpheric()
   {
      return this.textureSpheric;
   }

   /**
    * Transparency
    * 
    * @return Transparency
    */
   public float getTransparency()
   {
      return this.transparency;
   }

   /**
    * Indicates if the material is two sided
    * 
    * @return {@code true} if the material is two sided
    */
   public boolean isTwoSided()
   {
      return this.twoSided;
   }

   /**
    * Reset all settings to put as default
    */
   public void originalSettings()
   {
      this.colorAmbiant = Color4f.BLACK.getCopy();
      this.colorDiffuse = Color4f.GRAY.getCopy();
      this.colorEmissive = Color4f.DARK_GRAY.getCopy();
      this.colorSpecular = Color4f.LIGHT_GRAY.getCopy();
      this.specularLevel = 0.1f;
      this.shininess = 12;
      this.transparency = 1f;
      this.twoSided = false;
      this.sphericRate = 1f;
      this.cubeMapRate = 1f;
   }

   /**
    * Pepare material for OpenGL render.<br>
    * Use by the renderer, don't call it directly
    * 
    * @param gl
    *           OpenGL context
    */
   public void prepareMaterial(final GL2 gl)
   {
      if(this.twoSided)
      {
         gl.glDisable(GL2.GL_CULL_FACE);
      }
      else
      {
         gl.glEnable(GL2.GL_CULL_FACE);
      }
      gl.glDisable(GL2.GL_TEXTURE_2D);
      final float alpha = this.colorDiffuse.getAlpha();
      this.colorDiffuse.setAlpha(this.transparency);
      this.colorDiffuse.glColor4f(gl);
      this.colorDiffuse.setAlpha(alpha);
      //
      gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_DIFFUSE, this.colorDiffuse.putInFloatBuffer());
      //
      gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_EMISSION, this.colorEmissive.putInFloatBuffer());
      //
      gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, this.colorSpecular.putInFloatBuffer(this.specularLevel));
      //
      gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT, this.colorAmbiant.putInFloatBuffer());
      //
      gl.glMateriali(GL2.GL_FRONT_AND_BACK, GL2.GL_SHININESS, this.shininess);
   }

   /**
    * Serialize the mateirl in XML markup
    * 
    * @return Markup represents the material
    */
   public MarkupXML saveToXML()
   {
      final MarkupXML markupXML = new MarkupXML(ConstantsXML.MARKUP_MATERIAL);

      markupXML.addParameter(ConstantsXML.MARKUP_MATERIAL_colorAmbiant, this.colorAmbiant.serialize());
      markupXML.addParameter(ConstantsXML.MARKUP_MATERIAL_colorDiffuse, this.colorDiffuse.serialize());
      markupXML.addParameter(ConstantsXML.MARKUP_MATERIAL_colorEmissive, this.colorEmissive.serialize());
      markupXML.addParameter(ConstantsXML.MARKUP_MATERIAL_colorSpecular, this.colorSpecular.serialize());
      markupXML.addParameter(ConstantsXML.MARKUP_MATERIAL_name, this.name);
      markupXML.addParameter(ConstantsXML.MARKUP_MATERIAL_shininess, this.shininess);
      markupXML.addParameter(ConstantsXML.MARKUP_MATERIAL_specularLevel, this.specularLevel);
      markupXML.addParameter(ConstantsXML.MARKUP_MATERIAL_sphericRate, this.sphericRate);

      if(this.textureDiffuse != null)
      {
         markupXML.addParameter(ConstantsXML.MARKUP_MATERIAL_textureDiffuse, this.textureDiffuse.getTextureName());
      }

      markupXML.addParameter(ConstantsXML.MARKUP_MATERIAL_transparency, this.transparency);

      if(this.textureSpheric != null)
      {
         markupXML.addParameter(ConstantsXML.MARKUP_MATERIAL_textureSpheric, this.textureSpheric.getTextureName());
      }

      markupXML.addParameter(ConstantsXML.MARKUP_MATERIAL_twoSided, this.twoSided);

      return markupXML;
   }

   /**
    * Change ambiant color
    * 
    * @param colorAmbiant
    *           New ambiant color
    */
   public void setColorAmbiant(Color4f colorAmbiant)
   {
      if(colorAmbiant == null)
      {
         throw new NullPointerException("The colorAmbiant couldn't be null");
      }
      if(colorAmbiant.isDefaultColor())
      {
         colorAmbiant = colorAmbiant.getCopy();
      }
      this.colorAmbiant = colorAmbiant;
   }

   /**
    * Change diffuse color
    * 
    * @param colorDiffuse
    *           New diffuse color
    */
   public void setColorDiffuse(Color4f colorDiffuse)
   {
      if(colorDiffuse == null)
      {
         throw new NullPointerException("The colorDiffuse couldn't be null");
      }
      if(colorDiffuse.isDefaultColor())
      {
         colorDiffuse = colorDiffuse.getCopy();
      }
      this.colorDiffuse = colorDiffuse;
   }

   /**
    * Change emissive color
    * 
    * @param colorEmissive
    *           New emissive color
    */
   public void setColorEmissive(Color4f colorEmissive)
   {
      if(colorEmissive == null)
      {
         throw new NullPointerException("The colorEmissive couldn't be null");
      }
      if(colorEmissive.isDefaultColor())
      {
         colorEmissive = colorEmissive.getCopy();
      }
      this.colorEmissive = colorEmissive;
   }

   /**
    * Change specular color
    * 
    * @param colorSpecular
    *           New specular color
    */
   public void setColorSpecular(Color4f colorSpecular)
   {
      if(colorSpecular == null)
      {
         throw new NullPointerException("The colorSpecular couldn't be null");
      }
      if(colorSpecular.isDefaultColor())
      {
         colorSpecular = colorSpecular.getCopy();
      }
      this.colorSpecular = colorSpecular;
   }

   /**
    * Modify cubeMap
    * 
    * @param cubeMap
    *           New cubeMap value
    */
   public void setCubeMap(final CubeMap cubeMap)
   {
      this.cubeMap = cubeMap;
   }

   /**
    * Modify cubeMapRate
    * 
    * @param cubeMapRate
    *           New cubeMapRate value
    */
   public void setCubeMapRate(final float cubeMapRate)
   {
      this.cubeMapRate = cubeMapRate;
   }

   /**
    * Change shininess (0 <-> 128)
    * 
    * @param shininess
    *           New shininess (0 <-> 128)
    */
   public void setShininess(final int shininess)
   {
      if((shininess < 0) || (shininess > 128))
      {
         throw new IllegalArgumentException("The shininess must be on [0, 128], not : " + shininess);
      }
      this.shininess = shininess;
   }

   /**
    * Change specular level
    * 
    * @param specularLevel
    *           New specular level
    */
   public void setSpecularLevel(final float specularLevel)
   {
      this.specularLevel = specularLevel;
   }

   /**
    * Change environment rate
    * 
    * @param sphericRate
    *           New environment rate
    */
   public void setSphericRate(final float sphericRate)
   {
      this.sphericRate = sphericRate;
   }

   /**
    * Change diffuse texture<br>
    * Use {@code null} to remove diffuse texture
    * 
    * @param textureDiffuse
    *           New diffuse texture
    */
   public void setTextureDiffuse(final Texture textureDiffuse)
   {
      this.textureDiffuse = textureDiffuse;
   }

   /**
    * Change environment texture<br>
    * Use {@code null} to remove environment texture
    * 
    * @param textureSpheric
    *           New environment texture
    */
   public void setTextureSpheric(final Texture textureSpheric)
   {
      this.textureSpheric = textureSpheric;
   }

   /**
    * Do settings for 2D
    */
   public void settingAsFor2D()
   {
      this.colorEmissive.set(1f);
      this.specularLevel = 1f;
      this.shininess = 128;
      this.colorDiffuse.set(1f);
      this.colorSpecular.set();
      this.colorAmbiant.set(1f);
      this.twoSided = true;
   }

   /**
    * Change transparency
    * 
    * @param transparency
    *           New transparency
    */
   public void setTransparency(final float transparency)
   {
      this.transparency = transparency;
   }

   /**
    * Change two sided state
    * 
    * @param twoSided
    *           New two sided state
    */
   public void setTwoSided(final boolean twoSided)
   {
      this.twoSided = twoSided;
   }
}