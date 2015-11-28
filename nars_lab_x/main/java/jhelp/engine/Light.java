/**
 * Project : JHelpEngine<br>
 * Package : jhelp.engine<br>
 * Class : Light<br>
 * Date : 2 déc. 2010<br>
 * By JHelp
 */
package jhelp.engine;

import com.jogamp.opengl.GL2;
import jhelp.engine.util.BufferUtils;

/**
 * A OpenGL Light<br>
 * <br>
 * Last modification : 2 déc. 2010<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public final class Light
{
   /** Light 0 name */
   public static final String LIGHT_O = "LIGHT_0";
   /** Ambient color */
   private Color4f            ambiant;
   /** Indicates that a change as just append */
   private boolean            asChanged;
   /** Constant attenuation */
   private float              constantAttenuation;
   /** Diffuse color */
   private Color4f            diffuse;
   /** Indicates if light is on */
   private boolean            enable;

   /** OpenGL light id */
   private final int          id;
   /** Linear attenuation */
   private float              linearAttenuation;
   /** Light name */
   private final String       name;

   /** Indicates if we need refresh the light */
   private boolean            needRefresh;
   /** Quadric attenuation */
   private float              quadricAttenuation;
   /** Specular color */
   private Color4f            specular;
   /** Spot cut off */
   private int                spotCutOff;

   /** Spot direction */
   private Point3D            spotDirection;
   /** Spot exponent */
   private int                spotExponent;
   /** Position/Direction W */
   private float              w;

   /** Position/Direction X */
   private float              x;
   /** Position/Direction Y */
   private float              y;
   /** Position/Direction Z */
   private float              z;

   /**
    * Constructs Light
    * 
    * @param name
    *           Light name
    * @param id
    *           Light index
    */
   Light(final String name, final int id)
   {
      this.name = name;
      this.id = GL2.GL_LIGHT0 + id;

      this.reset();
   }

   /**
    * Reset the light to default settings of light 0
    */
   private void reset0()
   {
      this.enable = true;

      if(this.diffuse == null)
      {
         this.diffuse = Color4f.WHITE.getCopy();
      }
      else
      {
         this.diffuse.set(1, 1, 1, 1);
      }

      if(this.specular == null)
      {
         this.specular = Color4f.WHITE.getCopy();
      }
      else
      {
         this.specular.set(1, 1, 1, 1);
      }
   }

   /**
    * Reset the light to the default setting of all lights except the light 0
    */
   private void resetOthers()
   {
      this.enable = false;

      if(this.diffuse == null)
      {
         this.diffuse = Color4f.BLACK.getCopy();
      }
      else
      {
         this.diffuse.set(0, 0, 0, 1);
      }

      if(this.specular == null)
      {
         this.specular = Color4f.BLACK.getCopy();
      }
      else
      {
         this.specular.set(0, 0, 0, 1);
      }
   }

   /**
    * Return ambiant
    * 
    * @return ambiant
    */
   public Color4f getAmbiant()
   {
      return this.ambiant.getCopy();
   }

   /**
    * Return constantAttenuation
    * 
    * @return constantAttenuation
    */
   public float getConstantAttenuation()
   {
      return this.constantAttenuation;
   }

   /**
    * Return diffuse
    * 
    * @return diffuse
    */
   public Color4f getDiffuse()
   {
      return this.diffuse.getCopy();
   }

   /**
    * Return linearAttenuation
    * 
    * @return linearAttenuation
    */
   public float getLinearAttenuation()
   {
      return this.linearAttenuation;
   }

   /**
    * Light name
    * 
    * @return Light name
    */
   public String getName()
   {
      return this.name;
   }

   /**
    * Return quadricAttenuation
    * 
    * @return quadricAttenuation
    */
   public float getQuadricAttenuation()
   {
      return this.quadricAttenuation;
   }

   /**
    * Return specular
    * 
    * @return specular
    */
   public Color4f getSpecular()
   {
      return this.specular.getCopy();
   }

   /**
    * Return spotCutOff
    * 
    * @return spotCutOff
    */
   public int getSpotCutOff()
   {
      return this.spotCutOff;
   }

   /**
    * Return spotDirection
    * 
    * @return spotDirection
    */
   public Point3D getSpotDirection()
   {
      return new Point3D(this.spotDirection);
   }

   /**
    * Return spotExponent
    * 
    * @return spotExponent
    */
   public int getSpotExponent()
   {
      return this.spotExponent;
   }

   /**
    * Return w
    * 
    * @return w
    */
   public float getW()
   {
      return this.w;
   }

   /**
    * Return x
    * 
    * @return x
    */
   public float getX()
   {
      return this.x;
   }

   /**
    * Return y
    * 
    * @return y
    */
   public float getY()
   {
      return this.y;
   }

   /**
    * Return z
    * 
    * @return z
    */
   public float getZ()
   {
      return this.z;
   }

   /**
    * Return enable
    * 
    * @return enable
    */
   public boolean isEnable()
   {
      return this.enable;
   }

   /**
    * Set the light to be a directional light
    * 
    * @param direction
    *           Direction
    */
   public void makeDirectional(final Point3D direction)
   {
      this.x = direction.x;
      this.y = direction.y;
      this.z = direction.z;
      this.w = 0;

      this.spotDirection.set(0, 0, -1);

      this.spotExponent = 0;
      this.spotCutOff = 180;

      this.constantAttenuation = 1;
      this.linearAttenuation = 0;
      this.quadricAttenuation = 0;

      this.asChanged = this.needRefresh = true;
   }

   /**
    * Set the light to be a ponctual light
    * 
    * @param position
    *           Position
    * @param exponent
    *           Exponent attenuation
    * @param constantAttenuation
    *           Constant attenuation
    * @param linearAttenuation
    *           Linear attenuation
    * @param quadricAttenuation
    *           Quadric attenuation
    */
   public void makePonctualLight(final Point3D position, final int exponent, final float constantAttenuation, final float linearAttenuation, final float quadricAttenuation)
   {
      if((exponent < 0) || (exponent > 128))
      {
         throw new IllegalArgumentException("exponent must be in [0, 128], not " + exponent);
      }

      if(constantAttenuation < 0)
      {
         throw new IllegalArgumentException("No negative value");
      }

      if(linearAttenuation < 0)
      {
         throw new IllegalArgumentException("No negative value");
      }

      if(quadricAttenuation < 0)
      {
         throw new IllegalArgumentException("No negative value");
      }

      this.x = position.x;
      this.y = position.y;
      this.z = position.z;
      this.w = 1;

      this.spotDirection.set(0, 0, -1);

      this.spotExponent = exponent;
      this.spotCutOff = 180;

      this.constantAttenuation = constantAttenuation;
      this.linearAttenuation = linearAttenuation;
      this.quadricAttenuation = quadricAttenuation;

      this.asChanged = this.needRefresh = true;
   }

   /**
    * Set the light to be a spot
    * 
    * @param position
    *           Position
    * @param direction
    *           Light direction
    * @param exponent
    *           Exponent attenuation
    * @param cutOff
    *           Cut off
    * @param constantAttenuation
    *           Constant attenuation
    * @param linearAttenuation
    *           Linear attenuation
    * @param quadricAttenuation
    *           Quadric attenuation
    */
   public void makeSpot(final Point3D position, final Point3D direction, final int exponent, final int cutOff, final float constantAttenuation, final float linearAttenuation, final float quadricAttenuation)
   {
      if((exponent < 0) || (exponent > 128))
      {
         throw new IllegalArgumentException("exponent must be in [0, 128], not " + exponent);
      }

      if(constantAttenuation < 0)
      {
         throw new IllegalArgumentException("No negative value");
      }

      if(linearAttenuation < 0)
      {
         throw new IllegalArgumentException("No negative value");
      }

      if(quadricAttenuation < 0)
      {
         throw new IllegalArgumentException("No negative value");
      }

      if(((cutOff < 0) || (cutOff > 90)) && (cutOff != 180))
      {
         throw new IllegalArgumentException("cutOff must be in [0, 90] or the special 180, not " + cutOff);
      }

      this.x = position.x;
      this.y = position.y;
      this.z = position.z;
      this.w = 1;

      this.spotDirection.set(direction);

      this.spotExponent = exponent;
      this.spotCutOff = cutOff;

      this.constantAttenuation = constantAttenuation;
      this.linearAttenuation = linearAttenuation;
      this.quadricAttenuation = quadricAttenuation;

      this.asChanged = this.needRefresh = true;
   }

   /**
    * Render the light
    * 
    * @param gl
    *           OpenGL context
    */
   public void render(final GL2 gl)
   {
      if(this.asChanged == true)
      {
         this.asChanged = false;

         if(this.needRefresh == true)
         {
            this.needRefresh = false;

            gl.glLightfv(this.id, GL2.GL_AMBIENT, this.ambiant.putInFloatBuffer());
            gl.glLightfv(this.id, GL2.GL_DIFFUSE, this.diffuse.putInFloatBuffer());
            gl.glLightfv(this.id, GL2.GL_SPECULAR, this.specular.putInFloatBuffer());

            gl.glLightfv(this.id, GL2.GL_POSITION, BufferUtils.transferFloat(this.x, this.y, this.z, this.w));

            gl.glLightfv(this.id, GL2.GL_SPOT_DIRECTION, BufferUtils.transferFloat(this.spotDirection.x, this.spotDirection.y, this.spotDirection.z));
            gl.glLighti(this.id, GL2.GL_SPOT_EXPONENT, this.spotExponent);
            gl.glLighti(this.id, GL2.GL_SPOT_CUTOFF, this.spotCutOff);

            gl.glLightf(this.id, GL2.GL_CONSTANT_ATTENUATION, this.constantAttenuation);
            gl.glLightf(this.id, GL2.GL_LINEAR_ATTENUATION, this.linearAttenuation);
            gl.glLightf(this.id, GL2.GL_QUADRATIC_ATTENUATION, this.quadricAttenuation);
         }

         if(this.enable == true)
         {
            gl.glEnable(this.id);
         }
         else
         {
            gl.glDisable(this.id);
         }
      }
   }

   /**
    * Reset the light to default settings
    */
   public void reset()
   {
      if(this.ambiant == null)
      {
         this.ambiant = Color4f.BLACK.getCopy();
      }
      else
      {
         this.ambiant.set(0, 0, 0, 1);
      }

      this.x = 0;
      this.y = 0;
      this.z = 1;
      this.w = 0;

      if(this.spotDirection == null)
      {
         this.spotDirection = new Point3D(0, 0, -1);
      }
      else
      {
         this.spotDirection.set(0, 0, -1);
      }

      this.spotExponent = 0;
      this.spotCutOff = 180;

      this.constantAttenuation = 1;
      this.linearAttenuation = 0;
      this.quadricAttenuation = 0;

      if(this.id == GL2.GL_LIGHT0)
      {
         this.reset0();
      }
      else
      {
         this.resetOthers();
      }

      this.asChanged = this.needRefresh = true;
   }

   /**
    * Modify ambiant
    * 
    * @param ambiant
    *           New ambiant value
    */
   public void setAmbiant(final Color4f ambiant)
   {
      this.ambiant.set(ambiant);

      this.asChanged = this.needRefresh = true;
   }

   /**
    * Modify constantAttenuation
    * 
    * @param constantAttenuation
    *           New constantAttenuation value
    */
   public void setConstantAttenuation(final float constantAttenuation)
   {
      if(constantAttenuation < 0)
      {
         throw new IllegalArgumentException("No negative value");
      }

      this.constantAttenuation = constantAttenuation;

      this.asChanged = this.needRefresh = true;
   }

   /**
    * Modify diffuse
    * 
    * @param diffuse
    *           New diffuse value
    */
   public void setDiffuse(final Color4f diffuse)
   {
      this.diffuse.set(diffuse);

      this.asChanged = this.needRefresh = true;
   }

   /**
    * Modify enable
    * 
    * @param enable
    *           New enable value
    */
   public void setEnable(final boolean enable)
   {
      if(this.enable != enable)
      {
         this.enable = enable;

         this.asChanged = true;
      }
   }

   /**
    * Modify linearAttenuation
    * 
    * @param linearAttenuation
    *           New linearAttenuation value
    */
   public void setLinearAttenuation(final float linearAttenuation)
   {
      if(linearAttenuation < 0)
      {
         throw new IllegalArgumentException("No negative value");
      }

      this.linearAttenuation = linearAttenuation;

      this.asChanged = this.needRefresh = true;
   }

   /**
    * Change the position/direction
    * 
    * @param x
    *           X
    * @param y
    *           Y
    * @param z
    *           Z
    * @param w
    *           W
    */
   public void setPosition(final float x, final float y, final float z, final float w)
   {
      this.x = x;
      this.y = y;
      this.z = z;
      this.w = w;

      this.asChanged = this.needRefresh = true;
   }

   /**
    * Modify quadricAttenuation
    * 
    * @param quadricAttenuation
    *           New quadricAttenuation value
    */
   public void setQuadricAttenuation(final float quadricAttenuation)
   {
      if(quadricAttenuation < 0)
      {
         throw new IllegalArgumentException("No negative value");
      }

      this.quadricAttenuation = quadricAttenuation;

      this.asChanged = this.needRefresh = true;
   }

   /**
    * Modify specular
    * 
    * @param specular
    *           New specular value
    */
   public void setSpecular(final Color4f specular)
   {
      this.specular.set(specular);

      this.asChanged = this.needRefresh = true;
   }

   /**
    * Modify spotCutOff
    * 
    * @param spotCutOff
    *           New spotCutOff value
    */
   public void setSpotCutOff(final int spotCutOff)
   {
      if(((spotCutOff < 0) || (spotCutOff > 90)) && (spotCutOff != 180))
      {
         throw new IllegalArgumentException("Spot cut off must be in [0, 90] or the special 180, not " + spotCutOff);
      }

      if(this.spotCutOff != spotCutOff)
      {
         this.spotCutOff = spotCutOff;

         this.asChanged = this.needRefresh = true;
      }
   }

   /**
    * Modify spotDirection
    * 
    * @param spotDirection
    *           New spotDirection value
    */
   public void setSpotDirection(final Point3D spotDirection)
   {
      this.spotDirection.set(spotDirection);

      this.asChanged = this.needRefresh = true;
   }

   /**
    * Modify spotExponent
    * 
    * @param spotExponent
    *           New spotExponent value
    */
   public void setSpotExponent(final int spotExponent)
   {
      if((spotExponent < 0) || (spotExponent > 128))
      {
         throw new IllegalArgumentException("Spot exponent must be in [0, 128], not " + spotExponent);
      }

      if(this.spotExponent != spotExponent)
      {
         this.spotExponent = spotExponent;

         this.asChanged = this.needRefresh = true;
      }
   }
}