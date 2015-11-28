/**
 * Project : JHelpEngine<br>
 * Package : jhelp.engine.geom<br>
 * Class : Equation3D<br>
 * Date : 5 avr. 2010<br>
 * By JHelp
 */
package jhelp.engine.geom;

import jhelp.engine.NodeType;
import jhelp.engine.Object3D;
import jhelp.engine.Vertex;
import jhelp.engine.event.Equation3DListener;
import jhelp.engine.twoD.Line2D;
import jhelp.engine.twoD.Path;
import jhelp.engine.util.Math3D;
import jhelp.math.Rotf;
import jhelp.math.Vec3f;
import jhelp.util.math.formal.Function;
import jhelp.util.math.formal.Variable;
import jhelp.util.thread.ThreadManager;
import jhelp.util.thread.ThreadedVerySimpleTask;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

/**
 * Equation 3D.<br>
 * It it a easy way to represents a equation 3D<br>
 * <br>
 * Last modification : 5 avr. 2010<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public class Equation3D
      extends Object3D
{
   /**
    * Creator of equation <br>
    * <br>
    * Last modification : 2 déc. 2010<br>
    * Version 0.0.0<br>
    * 
    * @author JHelp
    */
   private class Creator
         extends ThreadedVerySimpleTask
   {
      /** Path border */
      Path     border;
      /** Precision to use */
      int      borderPrecision;
      /** Function of X : x = X(t) */
      Function fonctionX;
      /** Function of Y : y = Y(t) */
      Function fonctionY;
      /** Function of Z : z = Z(t) */
      Function fonctionZ;
      /** t end value */
      float    tEnd;
      /** t start value */
      float    tStart;
      /** t step to increment */
      float    tStep;

      /**
       * Constructs Creator
       * 
       * @param border
       *           Path border
       * @param borderPrecision
       *           Precision to use
       * @param start
       *           t start
       * @param end
       *           t end
       * @param step
       *           step to increment t
       * @param fonctionX
       *           Function of X : x = X(t)
       * @param fonctionY
       *           Function of Y : y = Y(t)
       * @param fonctionZ
       *           Function of Z : z = Z(t)
       */
      Creator(final Path border, final int borderPrecision, final float start, final float end, final float step, final Function fonctionX, final Function fonctionY, final Function fonctionZ)
      {
         this.border = border;
         this.borderPrecision = borderPrecision;
         this.tStart = start;
         this.tEnd = end;
         this.tStep = step;
         this.fonctionX = fonctionX;
         this.fonctionY = fonctionY;
         this.fonctionZ = fonctionZ;
      }

      /**
       * Compute the equation
       * 
       * @see Runnable#run()
       */
      @Override
      protected void doVerySimpleAction()
      {
         this.fonctionX = this.fonctionX.simplifyMaximum();
         this.fonctionY = this.fonctionY.simplifyMaximum();
         this.fonctionZ = this.fonctionZ.simplifyMaximum();

         final Variable varT = new Variable("t");
         final Function deriveX = this.fonctionX.derive(varT).simplifyMaximum();
         final Function deriveY = this.fonctionY.derive(varT).simplifyMaximum();
         final Function deriveZ = this.fonctionZ.derive(varT).simplifyMaximum();

         final ArrayList<Line2D> lines = this.border.computePath(this.borderPrecision);
         Line2D line;
         final int size = lines.size();
         float dx, dy, dz, l, x, y, z, xStart, xEnd, yStart, yEnd, dx2, dy2, dz2, x2, y2, z2;
         Vertex vertex00, vertex01, vertex10, vertex11;
         final Vec3f axisZ = new Vec3f(0, 0, 1);
         Vec3f normal, point;
         Rotf rot, rot2;
         final Rectangle2D limit = this.border.computeBorder();
         final float minU = (float) limit.getX();
         final float minV = (float) limit.getY();
         final float multU = 1f / (float) limit.getWidth();
         final float multV = 1f / (float) limit.getHeight();

         for(float t = this.tStart; t < this.tEnd; t += this.tStep)
         {
            dx = (float) deriveX.replace(varT, t).simplifyMaximum().obtainRealValueNumber();
            dy = (float) deriveY.replace(varT, t).simplifyMaximum().obtainRealValueNumber();
            dz = (float) deriveZ.replace(varT, t).simplifyMaximum().obtainRealValueNumber();

            l = (float) Math.sqrt((dx * dx) + (dy * dy) + (dz * dz));
            if(Math3D.nul(l) == false)
            {
               dx /= l;
               dy /= l;
               dz /= l;
            }

            normal = new Vec3f(dx, dy, dz);
            rot = new Rotf(axisZ, normal);

            x = (float) this.fonctionX.replace(varT, t).simplifyMaximum().obtainRealValueNumber();
            y = (float) this.fonctionY.replace(varT, t).simplifyMaximum().obtainRealValueNumber();
            z = (float) this.fonctionZ.replace(varT, t).simplifyMaximum().obtainRealValueNumber();

            //

            dx2 = (float) deriveX.replace(varT, t + this.tStep).simplifyMaximum().obtainRealValueNumber();
            dy2 = (float) deriveY.replace(varT, t + this.tStep).simplifyMaximum().obtainRealValueNumber();
            dz2 = (float) deriveZ.replace(varT, t + this.tStep).simplifyMaximum().obtainRealValueNumber();

            l = (float) Math.sqrt((dx2 * dx2) + (dy2 * dy2) + (dz2 * dz2));
            if(Math3D.nul(l) == false)
            {
               dx2 /= l;
               dy2 /= l;
               dz2 /= l;
            }

            normal = new Vec3f(dx2, dy2, dz2);
            rot2 = new Rotf(axisZ, normal);

            x2 = (float) this.fonctionX.replace(varT, t + this.tStep).simplifyMaximum().obtainRealValueNumber();
            y2 = (float) this.fonctionY.replace(varT, t + this.tStep).simplifyMaximum().obtainRealValueNumber();
            z2 = (float) this.fonctionZ.replace(varT, t + this.tStep).simplifyMaximum().obtainRealValueNumber();

            for(int lig = 0; lig < size; lig++)
            {
               line = lines.get(lig);

               // Start
               xStart = line.pointStart.getX();
               yStart = line.pointStart.getY();

               point = new Vec3f(xStart, yStart, 0);
               point = rot.rotateVector(point);

               vertex00 = new Vertex(point.x() + x, point.y() + y, point.z() + z,//
                     (xStart - minU) * multU, (yStart - minV) * multV,//
                     -point.x(), -point.y(), -point.z());

               // End
               xEnd = line.pointEnd.getX();
               yEnd = line.pointEnd.getY();

               point = new Vec3f(xEnd, yEnd, 0);
               point = rot.rotateVector(point);

               vertex01 = new Vertex(point.x() + x, point.y() + y, point.z() + z,//
                     (xEnd - minU) * multU, (yEnd - minV) * multV,//
                     -point.x(), -point.y(), -point.z());

               // ---*---

               // Start
               point = new Vec3f(xStart, yStart, 0);
               point = rot2.rotateVector(point);

               vertex10 = new Vertex(point.x() + x2, point.y() + y2, point.z() + z2,//
                     (xStart - minU) * multU, (yStart - minV) * multV,//
                     -point.x(), -point.y(), -point.z());

               // End
               point = new Vec3f(xEnd, yEnd, 0);
               point = rot2.rotateVector(point);

               vertex11 = new Vertex(point.x() + x2, point.y() + y2, point.z() + z2,//
                     (xEnd - minU) * multU, (yEnd - minV) * multV,//
                     -point.x(), -point.y(), -point.z());

               // ---*---

               Equation3D.this.mesh.addVertexToTheActualFace(vertex10);
               Equation3D.this.mesh.addVertexToTheActualFace(vertex11);
               Equation3D.this.mesh.addVertexToTheActualFace(vertex01);
               Equation3D.this.mesh.addVertexToTheActualFace(vertex00);

               Equation3D.this.mesh.endFace();
               Equation3D.this.flush();
            }
         }

         Equation3D.this.flush();
         Equation3D.this.computeUVspherical(1, 1);

         if(Equation3D.this.equation3DListener != null)
         {
            Equation3D.this.equation3DListener.equation3Dready(Equation3D.this);
         }
      }
   }

   /** Border repeated path */
   private final Path     border;
   /** Precision used for border */
   private final int      borderPrecision;
   /** Function X(t) */
   private final Function fonctionX;
   /** Function Y(t) */
   private final Function fonctionY;
   /** Function Z(t) */
   private final Function fonctionZ;
   /** t value at end */
   private final float    tEnd;
   /** t value at start */
   private final float    tStart;
   /** t step size to use */
   private final float    tStep;
   /** Listener of equation computing */
   Equation3DListener     equation3DListener;

   /**
    * Constructs Equation3D
    * 
    * @param border
    *           Path border
    * @param borderPrecision
    *           Border precision
    * @param tStart
    *           t start
    * @param tEnd
    *           t end
    * @param tStep
    *           t step for increment
    * @param fonctionX
    *           Function X : x = X(t)
    * @param fonctionY
    *           Function Y : y = Y(t)
    * @param fonctionZ
    *           Function Z : z = Z(t)
    */
   public Equation3D(final Path border, final int borderPrecision, final float tStart, final float tEnd, final float tStep, final Function fonctionX, final Function fonctionY, final Function fonctionZ)
   {
      this(border, borderPrecision, tStart, tEnd, tStep, fonctionX, fonctionY, fonctionZ, null);
   }

   /**
    * Constructs Equation3D
    * 
    * @param border
    *           Path border
    * @param borderPrecision
    *           Border precision
    * @param tStart
    *           t start
    * @param tEnd
    *           t end
    * @param tStep
    *           t step for increment
    * @param fonctionX
    *           Function X : x = X(t)
    * @param fonctionY
    *           Function Y : y = Y(t)
    * @param fonctionZ
    *           Function Z : z = Z(t)
    * @param equation3DListener
    *           Listener for know when ready
    */
   public Equation3D(final Path border, final int borderPrecision, final float tStart, final float tEnd, final float tStep, final Function fonctionX, final Function fonctionY, final Function fonctionZ,
         final Equation3DListener equation3DListener)
   {
      this.nodeType = NodeType.EQUATION;
      this.equation3DListener = equation3DListener;

      this.border = border;
      this.borderPrecision = borderPrecision;
      this.tStart = tStart;
      this.tEnd = tEnd;
      this.tStep = tStep;
      this.fonctionX = fonctionX.simplifyMaximum();
      this.fonctionY = fonctionY.simplifyMaximum();
      this.fonctionZ = fonctionZ.simplifyMaximum();

      ThreadManager.THREAD_MANAGER.doThread(new Creator(border, borderPrecision, tStart, tEnd, tStep, fonctionX.simplifyMaximum(), fonctionY.simplifyMaximum(), fonctionZ.simplifyMaximum()), null);
   }

   /**
    * Constructs Equation3D
    * 
    * @param border
    *           Path border
    * @param borderPrecision
    *           Border precision
    * @param tStart
    *           t start
    * @param tEnd
    *           t end
    * @param tStep
    *           t step for increment
    * @param fonctionX
    *           Function X : x = X(t)
    * @param fonctionY
    *           Function Y : y = Y(t)
    * @param fonctionZ
    *           Function Z : z = Z(t)
    */
   public Equation3D(final Path border, final int borderPrecision, final float tStart, final float tEnd, final float tStep, final String fonctionX, final String fonctionY, final String fonctionZ)
   {
      this(border, borderPrecision, tStart, tEnd, tStep, Function.parse(fonctionX), Function.parse(fonctionY), Function.parse(fonctionZ), null);
   }

   /**
    * Constructs Equation3D
    * 
    * @param border
    *           Path border
    * @param borderPrecision
    *           Border precision
    * @param tStart
    *           t start
    * @param tEnd
    *           t end
    * @param tStep
    *           t step for increment
    * @param fonctionX
    *           Function X : x = X(t)
    * @param fonctionY
    *           Function Y : y = Y(t)
    * @param fonctionZ
    *           Function Z : z = Z(t)
    * @param equation3DListener
    *           Listener for know when ready
    */
   public Equation3D(final Path border, final int borderPrecision, final float tStart, final float tEnd, final float tStep, final String fonctionX, final String fonctionY, final String fonctionZ,
         final Equation3DListener equation3DListener)
   {
      this(border, borderPrecision, tStart, tEnd, tStep, Function.parse(fonctionX), Function.parse(fonctionY), Function.parse(fonctionZ), equation3DListener);
   }

   /**
    * Border path (Path repeat along the equation)
    * 
    * @return Border path
    */
   public Path getBorder()
   {
      return this.border;
   }

   /**
    * Border precision
    * 
    * @return Border precision
    */
   public int getBorderPrecision()
   {
      return this.borderPrecision;
   }

   /**
    * X(t)
    * 
    * @return X(t)
    */
   public Function getFonctionX()
   {
      return this.fonctionX;
   }

   /**
    * Y(t)
    * 
    * @return Y(t)
    */
   public Function getFonctionY()
   {
      return this.fonctionY;
   }

   /**
    * Z(t)
    * 
    * @return Z(t)
    */
   public Function getFonctionZ()
   {
      return this.fonctionZ;
   }

   /**
    * t value at end
    * 
    * @return t value at end
    */
   public float getTEnd()
   {
      return this.tEnd;
   }

   /**
    * t value at start
    * 
    * @return t value at start
    */
   public float getTStart()
   {
      return this.tStart;
   }

   /**
    * t step size
    * 
    * @return t step size
    */
   public float getTStep()
   {
      return this.tStep;
   }
}