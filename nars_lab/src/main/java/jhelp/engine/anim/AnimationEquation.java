package jhelp.engine.anim;

import com.jogamp.opengl.GL;
import jhelp.engine.Animation;
import jhelp.engine.Node;
import jhelp.util.math.formal.Function;
import jhelp.util.math.formal.Variable;

/**
 * Animation based on 3D equation.<br>
 * The linked node, will follow the equation.<br>
 * Equation is x=X(t), y=Y(t) and z=Z(t). Each equation use only the {@link #T t} variable
 * 
 * @author JHelp
 */
public class AnimationEquation
      implements Animation
{
   /** Variable <b>t</b> that {@link #functionX X(t)}, {@link #functionY Y(t)} and {@link #functionZ Z(t)} must only depends */
   public static final Variable T = new Variable("t");
   /** X(t) */
   private final Function       functionX;
   /** Y(t) */
   private final Function       functionY;
   /** Z(t) */
   private final Function       functionZ;
   /** Node moved */
   private final Node           nodeMoved;
   /** Number of frame for make {@link #T t} go from {@link #tMin} to {@link #tMax} */
   private final float          numberOFrame;
   /** Frame where animation started */
   private float                startAbsoluteFrame;
   /** {@link #T t} max value (end) */
   private final float          tMax;
   /** {@link #T t} min value (start) */
   private final float          tMin;

   /**
    * Create a new instance of AnimationEquation
    * 
    * @param functionX
    *           X(t)
    * @param functionY
    *           Y(t)
    * @param functionZ
    *           Z(t)
    * @param tMin
    *           {@link #T t} min value (start)
    * @param tMax
    *           {@link #T t} max value (end)
    * @param numberOfFrame
    *           Number of frame for t to go from min to max
    * @param nodeMoved
    *           Node to move
    */
   public AnimationEquation(final Function functionX, final Function functionY, final Function functionZ, final float tMin, final float tMax, final int numberOfFrame, final Node nodeMoved)
   {
      if(functionX == null)
      {
         throw new NullPointerException("functionX musn't be null");
      }

      if(functionY == null)
      {
         throw new NullPointerException("functionY musn't be null");
      }

      if(functionZ == null)
      {
         throw new NullPointerException("functionZ musn't be null");
      }

      if(nodeMoved == null)
      {
         throw new NullPointerException("nodeMoved musn't be null");
      }

      this.functionX = functionX;
      this.functionY = functionY;
      this.functionZ = functionZ;
      this.tMin = tMin;
      this.tMax = tMax;
      this.numberOFrame = Math.max(numberOfFrame, 1);
      this.nodeMoved = nodeMoved;
   }

   /**
    * Called when animation playing <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param gl
    *           Open GL context
    * @param absoluteFrame
    *           Absolute frame
    * @return {@code true} if animation must continue
    * @see jhelp.engine.Animation#animate(javax.media.opengl.GL, float)
    */
   @Override
   public boolean animate(final GL gl, final float absoluteFrame)
   {
      final float frame = absoluteFrame - this.startAbsoluteFrame;
      if(frame >= this.numberOFrame)
      {
         final float x = (float) this.functionX.replace(AnimationEquation.T, this.tMax).simplifyMaximum().obtainRealValueNumber();
         final float y = (float) this.functionY.replace(AnimationEquation.T, this.tMax).simplifyMaximum().obtainRealValueNumber();
         final float z = (float) this.functionZ.replace(AnimationEquation.T, this.tMax).simplifyMaximum().obtainRealValueNumber();

         this.nodeMoved.setPosition(x, y, z);

         return false;
      }

      final float t = this.tMin + (((this.tMax - this.tMin) * frame) / this.numberOFrame);

      final float x = (float) this.functionX.replace(AnimationEquation.T, t).simplifyMaximum().obtainRealValueNumber();
      final float y = (float) this.functionY.replace(AnimationEquation.T, t).simplifyMaximum().obtainRealValueNumber();
      final float z = (float) this.functionZ.replace(AnimationEquation.T, t).simplifyMaximum().obtainRealValueNumber();

      this.nodeMoved.setPosition(x, y, z);

      return true;
   }

   /**
    * Initialize aniamtion <br>
    * <br>
    * <b>Parent documentation:</b><br>
    * {@inheritDoc}
    * 
    * @param startAbsoluteFrame
    *           Start absolute frame
    * @see jhelp.engine.Animation#setStartAbsoluteFrame(float)
    */
   @Override
   public void setStartAbsoluteFrame(final float startAbsoluteFrame)
   {
      this.startAbsoluteFrame = startAbsoluteFrame;

      final float x = (float) this.functionX.replace(AnimationEquation.T, this.tMin).simplifyMaximum().obtainRealValueNumber();
      final float y = (float) this.functionY.replace(AnimationEquation.T, this.tMin).simplifyMaximum().obtainRealValueNumber();
      final float z = (float) this.functionZ.replace(AnimationEquation.T, this.tMin).simplifyMaximum().obtainRealValueNumber();

      this.nodeMoved.setPosition(x, y, z);
   }
}