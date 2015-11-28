/**
 * Project : JHelpEngine<br>
 * Package : jhelp.engine<br>
 * Class : CubeMap<br>
 * Date : 24 mai 2009<br>
 * By JHelp
 */
package jhelp.engine;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import jhelp.engine.util.BufferUtils;

/**
 * Cube map.<br>
 * A cube map is compose on six textures, place on each face of a cube.<br>
 * It use for having reflection, or simulate "mirror environment" in objects <br>
 * <br>
 * Last modification : 24 mai 2009<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public class CubeMap
{
   /** For place a texture in the "back" face of the cube */
   public static final int BACK   = GL2.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z;
   /** For place a texture in the "bottom" face of the cube */
   public static final int BOTTOM = GL2.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y;
   /** For place a texture in the "face" face of the cube */
   public static final int FACE   = GL2.GL_TEXTURE_CUBE_MAP_POSITIVE_Z;
   /** For place a texture in the "left" face of the cube */
   public static final int LEFT   = GL2.GL_TEXTURE_CUBE_MAP_NEGATIVE_X;
   /** For place a texture in the "right" face of the cube */
   public static final int RIGHT  = GL2.GL_TEXTURE_CUBE_MAP_POSITIVE_X;
   /** For place a texture in the "top" face of the cube */
   public static final int TOP    = GL2.GL_TEXTURE_CUBE_MAP_POSITIVE_Y;

   /** Cross texture */
   private Texture         crossTexture;
   /** Indicate if cube map need to be refresh */
   private boolean         neeedRefresh;
   /** Video memory ID */
   private int             videoMemoryID;
   /** Left face */
   private Texture         xNegative;
   /** Right face */
   private Texture         xPositive;
   /** Bottom face */
   private Texture         yNegative;
   /** Top face */
   private Texture         yPositive;
   /** Back face */
   private Texture         zNegative;
   /** Face face */
   private Texture         zPositive;

   /**
    * Constructs CubeMap
    */
   public CubeMap()
   {
      this.videoMemoryID = -1;
      this.neeedRefresh = true;
   }

   /**
    * Apply the cube map.<br>
    * If the cube map is not complete, nothing is done
    * 
    * @param gl
    *           OpenGL context
    */
   public void bind(final GL2 gl)
   {
      if(this.isComplete() == false)
      {
         // Not complete, so quit
         return;
      }

      if(this.videoMemoryID < 0)
      {
         // Not in video memory, so put it in
         BufferUtils.TEMPORARY_INT_BUFFER.rewind();
         BufferUtils.TEMPORARY_INT_BUFFER.put(1);
         BufferUtils.TEMPORARY_INT_BUFFER.rewind();

         gl.glGenTextures(1, BufferUtils.TEMPORARY_INT_BUFFER);
         BufferUtils.TEMPORARY_INT_BUFFER.rewind();

         this.videoMemoryID = BufferUtils.TEMPORARY_INT_BUFFER.get();
      }

      if(this.neeedRefresh == true)
      {
         // If the cube map need to be refresh, refresh it
         gl.glBindTexture(GL2.GL_TEXTURE_CUBE_MAP, this.videoMemoryID);

         gl.glTexImage2D(GL2.GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0, GL2.GL_RGBA, this.xPositive.width, this.xPositive.height, 0, GL2.GL_RGBA, GL2.GL_UNSIGNED_BYTE, BufferUtils.transferByte(this.xPositive.pixels));
         gl.glTexImage2D(GL2.GL_TEXTURE_CUBE_MAP_NEGATIVE_X, 0, GL2.GL_RGBA, this.xNegative.width, this.xNegative.height, 0, GL2.GL_RGBA, GL2.GL_UNSIGNED_BYTE, BufferUtils.transferByte(this.xNegative.pixels));

         gl.glTexImage2D(GL2.GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0, GL2.GL_RGBA, this.yPositive.width, this.yPositive.height, 0, GL2.GL_RGBA, GL2.GL_UNSIGNED_BYTE, BufferUtils.transferByte(this.yPositive.pixels));
         gl.glTexImage2D(GL2.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0, GL2.GL_RGBA, this.yNegative.width, this.yNegative.height, 0, GL2.GL_RGBA, GL2.GL_UNSIGNED_BYTE, BufferUtils.transferByte(this.yNegative.pixels));

         gl.glTexImage2D(GL2.GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0, GL2.GL_RGBA, this.zPositive.width, this.zPositive.height, 0, GL2.GL_RGBA, GL2.GL_UNSIGNED_BYTE, BufferUtils.transferByte(this.zPositive.pixels));
         gl.glTexImage2D(GL2.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0, GL2.GL_RGBA, this.zNegative.width, this.zNegative.height, 0, GL2.GL_RGBA, GL2.GL_UNSIGNED_BYTE, BufferUtils.transferByte(this.zNegative.pixels));

         gl.glTexParameteri(GL2.GL_TEXTURE_CUBE_MAP, GL2.GL_TEXTURE_WRAP_S, GL2.GL_REPEAT);
         gl.glTexParameteri(GL2.GL_TEXTURE_CUBE_MAP, GL2.GL_TEXTURE_WRAP_T, GL2.GL_REPEAT);
         gl.glTexParameteri(GL2.GL_TEXTURE_CUBE_MAP, GL2.GL_TEXTURE_WRAP_R, GL2.GL_REPEAT);
         gl.glTexParameteri(GL2.GL_TEXTURE_CUBE_MAP, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_NEAREST);
         gl.glTexParameteri(GL2.GL_TEXTURE_CUBE_MAP, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_NEAREST);

         // Cube map has been refresh
         this.neeedRefresh = false;
      }

      // Activate cube map
      gl.glBindTexture(GL2.GL_TEXTURE_CUBE_MAP, this.videoMemoryID);
      gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_MODULATE);
      gl.glEnable(GL2.GL_TEXTURE_CUBE_MAP);
      gl.glEnable(GL2.GL_TEXTURE_GEN_S);
      gl.glEnable(GL2.GL_TEXTURE_GEN_T);
      gl.glEnable(GL2.GL_TEXTURE_GEN_R);
      gl.glTexGeni(GL2.GL_S, GL2.GL_TEXTURE_GEN_MODE, GL2.GL_REFLECTION_MAP);
      gl.glTexGeni(GL2.GL_T, GL2.GL_TEXTURE_GEN_MODE, GL2.GL_REFLECTION_MAP);
      gl.glTexGeni(GL2.GL_R, GL2.GL_TEXTURE_GEN_MODE, GL2.GL_REFLECTION_MAP);
      gl.glEnable(GL2.GL_TEXTURE_2D);
   }

   /**
    * Cut a cross texture for fill the cube map.<br>
    * Cross suppose be like : <code><br>
    * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;+X<br>
    * &nbsp;+Y&nbsp;&nbsp;+Z&nbsp;&nbsp;-Y<br>
    * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;-X<br>
    * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;-Z<br>
    * </code>
    * 
    * @param texture
    *           Texture to cut
    */
   public void crossTexture(final Texture texture)
   {
      this.crossTexture = texture;

      if(texture == null)
      {
         this.xPositive = null;
         this.xNegative = null;
         this.yPositive = null;
         this.yNegative = null;
         this.zPositive = null;
         this.zNegative = null;

         return;
      }

      final int width = texture.width / 3;
      final int height = texture.height >> 2;

      // X positive
      this.xPositive = Texture.obtainTexture(texture.getTextureName() + "_CUBE_MAP_X_POSITIVE");
      if(this.xPositive == null)
      {
         this.xPositive = texture.obtainParcel(width, 0, width, height, "_CUBE_MAP_X_POSITIVE");
      }

      // X negative
      this.xNegative = Texture.obtainTexture(texture.getTextureName() + "_CUBE_MAP_X_NEGATIVE");
      if(this.xNegative == null)
      {
         this.xNegative = texture.obtainParcel(width, height << 1, width, height, "_CUBE_MAP_X_NEGATIVE");
      }

      // Y positive
      this.yPositive = Texture.obtainTexture(texture.getTextureName() + "_CUBE_MAP_Y_POSITIVE");
      if(this.yPositive == null)
      {
         this.yPositive = texture.obtainParcel(0, height, width, height, "_CUBE_MAP_Y_POSITIVE");
      }

      // Y negative
      this.yNegative = Texture.obtainTexture(texture.getTextureName() + "_CUBE_MAP_Y_NEGATIVE");
      if(this.yNegative == null)
      {
         this.yNegative = texture.obtainParcel(width << 1, height, width, height, "_CUBE_MAP_Y_NEGATIVE");
      }

      // Z positive
      this.zPositive = Texture.obtainTexture(texture.getTextureName() + "_CUBE_MAP_Z_POSITIVE");
      if(this.zPositive == null)
      {
         this.zPositive = texture.obtainParcel(width, height, width, height, "_CUBE_MAP_Z_POSITIVE");
      }

      // Z negative
      this.zNegative = Texture.obtainTexture(texture.getTextureName() + "_CUBE_MAP_Z_NEGATIVE");
      if(this.zNegative == null)
      {
         this.zNegative = texture.obtainParcel(width, height * 3, width, height, "_CUBE_MAP_Z_NEGATIVE");
      }

      this.neeedRefresh = true;
   }

   /**
    * End application of cube map
    * 
    * @param gl
    *           OpenGL context
    */
   public void endCubeMap(final GL gl)
   {
      gl.glDisable(GL2.GL_TEXTURE_CUBE_MAP);
      gl.glDisable(GL2.GL_TEXTURE_GEN_S);
      gl.glDisable(GL2.GL_TEXTURE_GEN_T);
      gl.glDisable(GL2.GL_TEXTURE_GEN_R);
      gl.glDisable(GL2.GL_TEXTURE_2D);
   }

   /**
    * Flush last changes.<br>
    * Use it if you have modified one of its texture pixels
    */
   public void flush()
   {
      this.neeedRefresh = true;
   }

   /**
    * Original cross texture (Last Texture use in {@link #crossTexture(Texture)})
    * 
    * @return Original cross texture
    */
   public Texture getCrossTexture()
   {
      return this.crossTexture;
   }

   /**
    * -X texture
    * 
    * @return -X texture
    */
   public Texture getxNegative()
   {
      return this.xNegative;
   }

   /**
    * +X texture
    * 
    * @return +X texture
    */
   public Texture getxPositive()
   {
      return this.xPositive;
   }

   /**
    * -Y texture
    * 
    * @return -Y texture
    */
   public Texture getyNegative()
   {
      return this.yNegative;
   }

   /**
    * +Y texture
    * 
    * @return +Y texture
    */
   public Texture getyPositive()
   {
      return this.yPositive;
   }

   /**
    * -Z texture
    * 
    * @return -Z texture
    */
   public Texture getzNegative()
   {
      return this.zNegative;
   }

   /**
    * +Z texture
    * 
    * @return +Z texture
    */
   public Texture getzPositive()
   {
      return this.zPositive;
   }

   /**
    * Indicates if the cube map is complete defines (If it is not, it can't be used)
    * 
    * @return {@code true} if the cube map is complete defines
    */
   public boolean isComplete()
   {
      return (this.xNegative != null) && (this.xPositive != null) && (this.yNegative != null) && (this.yPositive != null) && (this.zNegative != null) && (this.zPositive != null);
   }

   /**
    * Change a face of the cube map.<br>
    * The face parameter must be one of the following : {@link #RIGHT}, {@link #LEFT}, {@link #TOP}, {@link #BOTTOM},
    * {@link #FACE} or {@link #BACK}
    * 
    * @param face
    *           Face to change
    * @param texture
    *           Texture apply
    */
   public void setFace(final int face, final Texture texture)
   {
      if(texture == null)
      {
         throw new NullPointerException("texture musn't be null");
      }

      switch(face)
      {
         case CubeMap.RIGHT:
            this.xPositive = texture;
         break;
         case CubeMap.LEFT:
            this.xNegative = texture;
         break;
         case CubeMap.TOP:
            this.yPositive = texture;
         break;
         case CubeMap.BOTTOM:
            this.yNegative = texture;
         break;
         case CubeMap.FACE:
            this.zPositive = texture;
         break;
         case CubeMap.BACK:
            this.zNegative = texture;
         break;
         default:
            throw new IllegalArgumentException("face must be RIGHT, LEFT, TOP, BOTTOM, FACE or BACK");
      }

      this.neeedRefresh = true;
   }

   /**
    * Change define the -X texture (Remeber that the result will be better and more croos computer if you use same sizes for
    * each part and sizes are power of 2).<br>
    * Can Use {@code null} to remove (Rember that cube map be be show only if all textures are sets)
    * 
    * @param xNegative
    *           New -X texture
    */
   public void setxNegative(final Texture xNegative)
   {
      this.xNegative = xNegative;
   }

   /**
    * Change define the +X texture (Remeber that the result will be better and more croos computer if you use same sizes for
    * each part and sizes are power of 2).<br>
    * Can Use {@code null} to remove (Rember that cube map be be show only if all textures are sets)
    * 
    * @param xPositive
    *           New +X texture
    */
   public void setxPositive(final Texture xPositive)
   {
      this.xPositive = xPositive;
   }

   /**
    * Change define the -Y texture (Remeber that the result will be better and more croos computer if you use same sizes for
    * each part and sizes are power of 2).<br>
    * Can Use {@code null} to remove (Rember that cube map be be show only if all textures are sets)
    * 
    * @param yNegative
    *           New -Y texture
    */
   public void setyNegative(final Texture yNegative)
   {
      this.yNegative = yNegative;
   }

   /**
    * Change define the +Y texture (Remeber that the result will be better and more croos computer if you use same sizes for
    * each part and sizes are power of 2).<br>
    * Can Use {@code null} to remove (Rember that cube map be be show only if all textures are sets)
    * 
    * @param yPositive
    *           New +Y texture
    */
   public void setyPositive(final Texture yPositive)
   {
      this.yPositive = yPositive;
   }

   /**
    * Change define the -Z texture (Remeber that the result will be better and more croos computer if you use same sizes for
    * each part and sizes are power of 2).<br>
    * Can Use {@code null} to remove (Rember that cube map be be show only if all textures are sets)
    * 
    * @param zNegative
    *           New -Z texture
    */
   public void setzNegative(final Texture zNegative)
   {
      this.zNegative = zNegative;
   }

   /**
    * Change define the +Z texture (Remeber that the result will be better and more croos computer if you use same sizes for
    * each part and sizes are power of 2).<br>
    * Can Use {@code null} to remove (Rember that cube map be be show only if all textures are sets)
    * 
    * @param zPositive
    *           New +Z texture
    */
   public void setzPositive(final Texture zPositive)
   {
      this.zPositive = zPositive;
   }
}