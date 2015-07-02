package jhelp.engine.geom;

import jhelp.engine.Object3D;
import jhelp.engine.Point2D;
import jhelp.engine.Point3D;
import jhelp.engine.Vertex;
import jhelp.engine.event.Ribbon3DListener;
import jhelp.engine.util.Math3D;
import jhelp.util.math.UtilMath;
import jhelp.util.thread.ThreadManager;
import jhelp.util.thread.ThreadedVerySimpleTask;

/**
 * Represents a ribbon.<br>
 * Its a band if a number of rotation on himself before loop.<br>
 * The number of rotation can be negative or positive, negative are mirror of positive.<br>
 * 0 rotation, is just a tube.<br>
 * 1 or -1 its Möbius strip.<br>
 * Impair values will have only one side, pair value 2 sides.
 * 
 * @author JHelp
 */
public class Ribbon3D
      extends Object3D
{
   /**
    * Threaded task for create the ribbon
    * 
    * @author JHelp
    */
   class CreateRibbon
         extends ThreadedVerySimpleTask
   {
      /**
       * Create a new instance of CreateRibbon
       */
      CreateRibbon()
      {
      }

      /**
       * Create ribbon task <br>
       * <br>
       * <b>Parent documentation:</b><br>
       * {@inheritDoc}
       * 
       * @see jhelp.util.thread.ThreadedVerySimpleTask#doVerySimpleAction()
       */
      @Override
      protected void doVerySimpleAction()
      {
         Ribbon3D.this.createRibbon();
      }
   }

   /** Create ribbon task */
   private final CreateRibbon     createRibbon;
   /** U multiplier to repeat texture on ribbon loop */
   private final float            multU;
   /** V multiplier to repeat texture on ribbon "height" */
   private final float            multV;
   /** Drawing precision */
   private final int              precision;
   /** Listener of creation finish */
   private final Ribbon3DListener ribbon3dListener;
   /** Number of rotation */
   private final int              torsioCount;

   /**
    * Create a new instance of Ribbon3D
    * 
    * @param torsioCount
    *           Number of rotation
    */
   public Ribbon3D(final int torsioCount)
   {
      this(torsioCount, 64, 1, 1, null);
   }

   /**
    * Create a new instance of Ribbon3D
    * 
    * @param torsioCount
    *           Number of rotation
    * @param multU
    *           U multiplier to repeat texture along the loop
    * @param multV
    *           V multiplier to repeat texture along the "height"
    */
   public Ribbon3D(final int torsioCount, final float multU, final float multV)
   {
      this(torsioCount, 64, multU, multV, null);
   }

   /**
    * Create a new instance of Ribbon3D
    * 
    * @param torsioCount
    *           Number of rotation
    * @param multU
    *           U multiplier to repeat texture along the loop
    * @param multV
    *           V multiplier to repeat texture along the "height"
    * @param ribbon3dListener
    *           Listener to alert when ribbon finished to create
    */
   public Ribbon3D(final int torsioCount, final float multU, final float multV, final Ribbon3DListener ribbon3dListener)
   {
      this(torsioCount, 64, multU, multV, ribbon3dListener);
   }

   /**
    * Create a new instance of Ribbon3D
    * 
    * @param torsioCount
    *           Number of rotation
    * @param precision
    *           Drawing precision
    */
   public Ribbon3D(final int torsioCount, final int precision)
   {
      this(torsioCount, precision, 1, 1, null);
   }

   /**
    * Create a new instance of Ribbon3D
    * 
    * @param torsioCount
    *           Number of rotation
    * @param precision
    *           Drawing precision
    * @param multU
    *           U multiplier to repeat texture along the loop
    * @param multV
    *           V multiplier to repeat texture along the "height"
    */
   public Ribbon3D(final int torsioCount, final int precision, final float multU, final float multV)
   {
      this(torsioCount, precision, multU, multV, null);
   }

   /**
    * Create a new instance of Ribbon3D
    * 
    * @param torsioCount
    *           Number of rotation
    * @param precision
    *           Drawing precision
    * @param multU
    *           U multiplier to repeat texture along the loop
    * @param multV
    *           V multiplier to repeat texture along the "height"
    * @param ribbon3dListener
    *           Listener to alert when ribbon finished to create
    */
   public Ribbon3D(final int torsioCount, final int precision, final float multU, final float multV, final Ribbon3DListener ribbon3dListener)
   {
      this.createRibbon = new CreateRibbon();
      this.torsioCount = torsioCount;
      this.precision = Math.max(64, precision);
      this.multU = multU;
      this.multV = multV;
      this.ribbon3dListener = ribbon3dListener;
      this.setTwoSidedState(TwoSidedState.FORCE_TWO_SIDE);

      ThreadManager.THREAD_MANAGER.doThread(this.createRibbon, null);
   }

   /**
    * Create a new instance of Ribbon3D
    * 
    * @param torsioCount
    *           Number of rotation
    * @param precision
    *           Drawing precision
    * @param ribbon3dListener
    *           Listener to alert when ribbon finished to create
    */
   public Ribbon3D(final int torsioCount, final int precision, final Ribbon3DListener ribbon3dListener)
   {
      this(torsioCount, precision, 1, 1, ribbon3dListener);
   }

   /**
    * Create a new instance of Ribbon3D
    * 
    * @param torsioCount
    *           Number of rotation
    * @param ribbon3dListener
    *           Listener to alert when ribbon finished to create
    */
   public Ribbon3D(final int torsioCount, final Ribbon3DListener ribbon3dListener)
   {
      this(torsioCount, 64, ribbon3dListener);
   }

   /**
    * Create the ribbon
    */
   void createRibbon()
   {
      final Point3D[] firstSegment = new Point3D[this.precision];
      final Point3D[] segment1 = new Point3D[this.precision];
      final Point3D[] segment2 = new Point3D[this.precision];
      final float step = 1f / this.precision;
      final float rotation = Math3D.PI * this.torsioCount;
      final float rotStep = rotation / this.precision;
      float anRot = 0;
      final float zTranslationFactor = step * (this.torsioCount % 2) * UtilMath.sign(this.torsioCount);
      final float size = 1f + (this.torsioCount * 0.5f);

      for(int i = 0; i < this.precision; i++)
      {
         firstSegment[i] = new Point3D(size, 0, step * i);
      }

      System.arraycopy(firstSegment, 0, segment1, 0, this.precision);

      final float angleStep = Math3D.TWO_PI / this.precision;
      float angle = 0;
      Vertex v1, v2, v3, v4;
      final float uv = 1f / this.precision;
      float z, vx, vy, vz, cos, sin, u1, u2;
      Point3D normal = new Point3D(0, 1, 0);
      Point3D normal2 = new Point3D(0, 1, 0);

      for(int i = 1; i <= this.precision; i++)
      {
         anRot += rotStep;
         angle += angleStep;
         vx = (float) (size * Math.cos(angle));
         vy = (float) (size * Math.sin(angle));
         vz = i * zTranslationFactor;
         z = 0;
         cos = (float) Math.cos(anRot);
         sin = (float) Math.sin(anRot);

         for(int j = 0; j < this.precision; j++)
         {
            segment2[j] = new Point3D(vx - (z * sin), vy, vz + (z * cos));
            z += step;
         }

         u1 = (i - 1) * uv * this.multU;
         u2 = i * uv * this.multU;
         normal2 = new Point3D(-(float) Math.sin(anRot), (float) Math.cos(anRot), 0);
         v1 = new Vertex(segment1[0], new Point2D(u1, 0), normal);
         v2 = new Vertex(segment2[0], new Point2D(u2, 0), normal2);

         for(int j = 1; j < this.precision; j++)
         {
            v3 = new Vertex(segment1[j], new Point2D(u1, j * uv * this.multV), normal);
            v4 = new Vertex(segment2[j], new Point2D(u2, j * uv * this.multV), normal2);

            this.mesh.addVertexToTheActualFace(v1);
            this.mesh.addVertexToTheActualFace(v2);
            this.mesh.addVertexToTheActualFace(v4);
            this.mesh.addVertexToTheActualFace(v3);

            this.mesh.endFace();
            this.flush();

            v1 = v3;
            v2 = v4;
         }

         normal = normal2;
         System.arraycopy(segment2, 0, segment1, 0, this.precision);
      }

      u1 = this.multU * (1 - uv);
      u2 = this.multU;
      normal2 = new Point3D(-(float) Math.sin(anRot), (float) Math.cos(anRot), 0);
      v1 = new Vertex(segment2[0], new Point2D(u1, 0), normal);
      v2 = new Vertex(firstSegment[0], new Point2D(u2, 0), normal2);

      for(int j = 1; j < this.precision; j++)
      {
         v3 = new Vertex(segment2[j], new Point2D(u1, j * uv * this.multV), normal);
         v4 = new Vertex(firstSegment[j], new Point2D(u2, j * uv * this.multV), normal2);

         this.mesh.addVertexToTheActualFace(v1);
         this.mesh.addVertexToTheActualFace(v2);
         this.mesh.addVertexToTheActualFace(v4);
         this.mesh.addVertexToTheActualFace(v3);

         this.mesh.endFace();
         this.flush();

         v1 = v3;
         v2 = v4;
      }

      if(this.ribbon3dListener != null)
      {
         this.ribbon3dListener.ribbonReady(this);
      }
   }
}