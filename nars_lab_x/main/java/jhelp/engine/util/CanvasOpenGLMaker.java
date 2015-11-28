/**
 */
package jhelp.engine.util;


import com.jogamp.nativewindow.CapabilitiesImmutable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLCapabilitiesChooser;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import jhelp.util.debug.Debug;
import jhelp.util.debug.DebugLevel;

import java.util.List;

/**
 * Constructs a canvas for 3D<br>
 * <br>
 * 
 * @author JHelp
 */
public class CanvasOpenGLMaker
      implements GLCapabilitiesChooser
{
   /** The singleton */
   public static CanvasOpenGLMaker CANVAS_OPENGL_MAKER = new CanvasOpenGLMaker();
   /** Last capabilities choose */
   private GLCapabilities capabilities;

   /**
    * Constructs the singleton
    */
   private CanvasOpenGLMaker()
   {
      LibraryInstaller.install();
   }

   /**
    * Compute point for capabilities
    * 
    * @param tested
    *           Capabilities tested
    * @param desired
    *           Desired capabilities
    * @return Capabilities point
    */
   private int computePoint(final GLCapabilities tested, final GLCapabilities desired)
   {
      if(tested == null)
      {
         return -1;
      }
      int point = 0;
      if(tested.getDoubleBuffered() == desired.getDoubleBuffered())
      {
         point += 10;
      }
      if(tested.getHardwareAccelerated() == desired.getHardwareAccelerated())
      {
         point += 10;
      }
      if(tested.getSampleBuffers() == desired.getSampleBuffers())
      {
         point += 10;
      }
      if(tested.getSampleBuffers() == true)
      {
         point += 100 - (Math.abs(tested.getNumSamples() - desired.getNumSamples()) * 10);
      }
      point += 100 - (Math.abs(tested.getDepthBits() - desired.getDepthBits()) * 10);

      point += tested.getAlphaBits();
      point += tested.getRedBits();
      point += tested.getGreenBits();
      point += tested.getBlueBits();
      point += tested.getStencilBits();

      //
      Debug.println(DebugLevel.VERBOSE, "Test : ");
      Debug.println(DebugLevel.VERBOSE, tested);
      Debug.println(DebugLevel.VERBOSE, "With " + point + " points");
      return point;
   }

   @Override
   public int chooseCapabilities(CapabilitiesImmutable desired, List<? extends CapabilitiesImmutable> availableList, int windowSystemRecommendedChoice) {
      GLCapabilities[] available = availableList.toArray(new GLCapabilities[availableList.size()]);

      int chosen = -1;
      int actualPointChoice = 0;
      final int length = available.length;
      //
      if((windowSystemRecommendedChoice >= 0) && (windowSystemRecommendedChoice < length))
      {
         chosen = windowSystemRecommendedChoice;
         actualPointChoice = this.computePoint(available[chosen], (GLCapabilities) desired) + 1;
         Debug.printMark(DebugLevel.VERBOSE, "windowSystemRecommendedChoice=" + windowSystemRecommendedChoice + " | " + actualPointChoice);
      }
      //
      for(int i = 0; i < length; i++)
      {
         final int point = this.computePoint(available[i], (GLCapabilities) desired);
         if(point > actualPointChoice)
         {
            chosen = i;
            actualPointChoice = point;
         }
      }
      //
      Debug.printMark(DebugLevel.VERBOSE, "Choosen capabilitie");
      Debug.println(DebugLevel.VERBOSE, available[chosen]);
      Debug.println(DebugLevel.VERBOSE, "With " + actualPointChoice + " points");
      //
      this.capabilities = available[chosen];
      //
      return chosen;
   }

   /**
    * Constructs a canvas for 3D
    * 
    * @return Canvas constructed
    */
   public GLCanvas newGLCanvas()
   {
      if(this.capabilities != null)
      {
         return new GLCanvas(this.capabilities);
      }

      GLCapabilities capabilities = new GLCapabilities(GLProfile.getDefault());
      capabilities.setHardwareAccelerated(true);
      capabilities.setAlphaBits(8);
      capabilities.setNumSamples(1);

//      final GLCapabilities capabilities = new GLCapabilities(GLProfile.getMaximum(true));
//      capabilities.setDoubleBuffered(true);
//      capabilities.setHardwareAccelerated(true);
//      capabilities.setSampleBuffers(true);
//      //capabilities.setNumSamples(4);
//      //capabilities.setDepthBits(32);
      return this.newGLCanvas(capabilities);
   }

   /**
    * Constructs a canvas for 3D
    * 
    * @param capabilities
    *           Capabilities to use
    * @return Canvas constructed
    */
   public GLCanvas newGLCanvas(final GLCapabilities capabilities)
   {
      //return new GLCanvas(capabilities);
      return new GLCanvas(capabilities);
   }
}