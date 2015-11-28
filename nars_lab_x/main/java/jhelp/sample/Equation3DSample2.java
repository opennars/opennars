package jhelp.sample;

import jhelp.engine.*;
import jhelp.engine.NodeWithMaterial.TwoSidedState;
import jhelp.engine.geom.Equation3D;
import jhelp.engine.gui.JHelpFrame3D;
import jhelp.engine.twoD.Path;
import jhelp.engine.util.Math3D;
import jhelp.util.debug.Debug;
import jhelp.util.gui.UtilGUI;
import jhelp.util.resources.Resources;

import java.io.IOException;

public class Equation3DSample2
{
   /** Resources access */
   private static final Resources RESOURCES = new Resources(Equation3DSample2.class);

   /**
    * TODO Explains what does the method main in jhelp.engine.samples.equation3D [JHelpEngine]
    * 
    * @param args
    */
   public static void main(final String[] args)
   {
      UtilGUI.initializeGUI();

      // Create a frame 3D with default size and show it
      final JHelpFrame3D frame3d = new JHelpFrame3D(true, "Sample : Equation 3D");
      frame3d.setSize(600,600);
      frame3d.setVisible(true);


      // Get the scene renderer
      final JHelpSceneRenderer sceneRenderer = frame3d.getSceneRenderer();
      // Get the scene to modify
      final Scene scene = sceneRenderer.getScene();

      // Create an equation
      final Path path = new Path();
      path.appendQuad(new Point2D(-0.5f, 0.5f), new Point2D(0, 0.75f), new Point2D(0.5f, 0.5f));
      path.appendQuad(new Point2D(0.5f, 0.5f), new Point2D(0.75f, 0f), new Point2D(0.5f, -0.5f));
      path.appendQuad(new Point2D(0.5f, -0.5f), new Point2D(0, -0.75f), new Point2D(-0.5f, -0.5f));
      path.appendQuad(new Point2D(-0.5f, -0.5f), new Point2D(-0.75f, 0), new Point2D(-0.5f, 0.5f));
      final Object3D knot = new Equation3D(path, 16, -Math3D.PI, Math3D.PI, Math3D.PI / 64f, "2*(sin(t)+2*sin(2*t))", "2*(cos(t)-2*cos(2*t))", "2*(-sin(3*t))");
      knot.setTwoSidedState(TwoSidedState.FORCE_TWO_SIDE);

      // Add texture material to knot
      final Material material = new Material("MaterialEquation");
      try
      {
         Texture texture = new Texture("TextureDiffuse", Texture.REFERENCE_RESOURCES, Equation3DSample2.RESOURCES.obtainResourceStream("floor068.jpg"));
         material.setTextureDiffuse(texture);

         texture = new Texture("TextureSpherique", Texture.REFERENCE_RESOURCES, Equation3DSample2.RESOURCES.obtainResourceStream("emerald_bk.jpg"));
         material.setTextureSpheric(texture);
         material.setSphericRate(0.5f);
      }
      catch(final IOException exception)
      {
         Debug.printException(exception);
      }

      knot.setMaterial(material);

      // Add the knot in the scene
      scene.add(knot);

      // (0.0f, 0.0f, -20.279984f) | AngleX=183.0f | AngleY=13.0f | AngleZ=0.0f
      // Put the scene for being visible
      scene.setPosition(0, 0, -20.279984f);
      // Rotate a little for see its 3D
      scene.setAngleX(183.0f);
      scene.setAngleY(13.0f);
      scene.setAngleZ(0f);

      // Show last modifications
      scene.flush();
   }
}