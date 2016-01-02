/**
 * Project : JHelpSceneGraph<br>
 * Package : jhelp.engine.util<br>
 * Class : Tool3D<br>
 * Date : 17 janv. 2009<br>
 * By JHelp
 */
package jhelp.engine.util;

import jhelp.engine.*;
import jhelp.engine.geom.*;
import jhelp.engine.io.ConstantsXML;
import jhelp.engine.twoD.GUI2D;
import jhelp.engine.twoD.Line2D;
import jhelp.engine.twoD.Object2D;
import jhelp.engine.twoD.Path;
import jhelp.util.debug.Debug;
import jhelp.util.text.UtilText;
import jhelp.xml.MarkupXML;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;
import java.util.StringTokenizer;

/**
 * Tools for 3D manipulation <br>
 * <br>
 * Last modification : 25 janv. 2009<br>
 * Version 0.0.1<br>
 * 
 * @author JHelp
 */
public class Tool3D
{
   /**
    * Add a Color4f parameter to XML markup
    * 
    * @param markupXML
    *           Markup where add
    * @param parameterName
    *           Parameter name
    * @param color4f
    *           Color to store
    */
   public static void addColor4fParameter(final MarkupXML markupXML, final String parameterName, final Color4f color4f)
   {
      StringBuffer stringBuffer = new StringBuffer();
      stringBuffer.append(color4f.getAlpha());
      stringBuffer.append(' ');
      stringBuffer.append(color4f.getRed());
      stringBuffer.append(' ');
      stringBuffer.append(color4f.getGreen());
      stringBuffer.append(' ');
      stringBuffer.append(color4f.getBlue());
      markupXML.addParameter(parameterName, stringBuffer.toString());
      stringBuffer = null;
   }

   /**
    * Add a Point3D parameter to XML markup
    * 
    * @param markupXML
    *           Markup where add
    * @param parameterName
    *           Parameter name
    * @param point
    *           Point to add
    */
   public static void addPoint3DParameter(final MarkupXML markupXML, final String parameterName, final Point3D point)
   {
      StringBuffer stringBuffer = new StringBuffer();
      stringBuffer.append(point.getX());
      stringBuffer.append(' ');
      stringBuffer.append(point.getY());
      stringBuffer.append(' ');
      stringBuffer.append(point.getZ());
      markupXML.addParameter(parameterName, stringBuffer.toString());
      stringBuffer = null;
   }

   /**
    * Collect all used material in a scene
    * 
    * @param scene
    *           Scene to explore
    * @return Materials collected list
    */
   public static ArrayList<Material> collectAllUsedMaterial(final Scene scene)
   {
      final ArrayList<Material> arrayList = new ArrayList<Material>();
      Node node = scene.getRoot();
      final Stack<Node> stack = new Stack<Node>();
      stack.push(node);
      NodeWithMaterial nodeWithMaterial;
      Material material;

      while(stack.isEmpty() == false)
      {
         node = stack.pop();

         if(node instanceof NodeWithMaterial)
         {
            nodeWithMaterial = (NodeWithMaterial) node;
            material = nodeWithMaterial.getMaterial();

            if(arrayList.contains(material) == false)
            {
               arrayList.add(material);
            }

            material = nodeWithMaterial.getMaterialForSelection();

            if((material != null) && (arrayList.contains(material) == false))
            {
               arrayList.add(material);
            }
         }

         final Iterator<Node> children = node.getChildren();

         while(children.hasNext() == true)
         {
            stack.push(children.next());
         }
      }
      return arrayList;
   }

   /**
    * Collect all texture used in a scene renderer
    * 
    * @param helpSceneRenderer
    *           Renderer to explore
    * @return Textures collected list
    */
   public static ArrayList<Texture> collectAllUsedTexture(final JHelpSceneRenderer helpSceneRenderer)
   {
      final Scene scene = helpSceneRenderer.getScene();
      final GUI2D gui2d = helpSceneRenderer.getGui2d();
      final ArrayList<Texture> arrayList = new ArrayList<Texture>();
      Texture texture;
      Material material;
      NodeWithMaterial nodeWithMaterial;
      Node node = scene.getRoot();
      final Stack<Node> stack = new Stack<Node>();
      stack.push(node);

      while(stack.isEmpty() == false)
      {
         node = stack.pop();

         if(node instanceof NodeWithMaterial)
         {
            nodeWithMaterial = (NodeWithMaterial) node;
            material = nodeWithMaterial.getMaterial();
            texture = material.getTextureDiffuse();

            if((texture != null) && (arrayList.contains(texture) == false))
            {
               arrayList.add(texture);
            }

            texture = material.getTextureSpheric();

            if((texture != null) && (arrayList.contains(texture) == false))
            {
               arrayList.add(texture);
            }

            material = nodeWithMaterial.getMaterialForSelection();

            if(material != null)
            {
               texture = material.getTextureDiffuse();

               if((texture != null) && (arrayList.contains(texture) == false))
               {
                  arrayList.add(texture);
               }

               texture = material.getTextureSpheric();

               if((texture != null) && (arrayList.contains(texture) == false))
               {
                  arrayList.add(texture);
               }
            }
         }

         texture = node.getTextureHotspot();

         if((texture != null) && (arrayList.contains(texture) == false))
         {
            arrayList.add(texture);
         }

         final Iterator<Node> children = node.getChildren();

         while(children.hasNext() == true)
         {
            stack.push(children.next());
         }
      }

      Object2D object2D;
      boolean visible;
      Iterator<Object2D> iterator = gui2d.getIteratorOver3D();

      while(iterator.hasNext() == true)
      {
         object2D = iterator.next();
         visible = object2D.isVisible();
         object2D.setVisible(true);
         texture = object2D.getTexture();
         object2D.setVisible(visible);

         if((texture != null) && (arrayList.contains(texture) == false))
         {
            arrayList.add(texture);
         }
      }

      iterator = gui2d.getIteratorUnder3D();

      while(iterator.hasNext() == true)
      {
         object2D = iterator.next();
         visible = object2D.isVisible();
         object2D.setVisible(true);
         texture = object2D.getTexture();
         object2D.setVisible(visible);

         if((texture != null) && (arrayList.contains(texture) == false))
         {
            arrayList.add(texture);
         }
      }

      object2D = null;

      return arrayList;
   }

   /**
    * Create a clone and clone also the all hierarchy
    * 
    * @param node
    *           Node to clone
    * @return Clone
    */
   public static Node createCloneHierarchy(final Node node)
   {
      return Tool3D.createCloneHierarchy(node, "");
   }

   /**
    * Create a clone and clone also the all hierarchy, and additional suffix is given to cloned node names
    * 
    * @param node
    *           Node to clone
    * @param suffix
    *           Suffix to add to clones name
    * @return Cloned hierarchy
    */
   public static Node createCloneHierarchy(final Node node, final String suffix)
   {
      Node clone = new Node();

      if(node instanceof Object3D)
      {
         clone = new ObjectClone((Object3D) node);
         ((NodeWithMaterial) clone).setTwoSidedState(((NodeWithMaterial) node).getTwoSidedState());
      }
      else if(node instanceof ObjectClone)
      {
         clone = new ObjectClone(((ObjectClone) node).getReference());
         ((NodeWithMaterial) clone).setTwoSidedState(((NodeWithMaterial) node).getTwoSidedState());
      }

      clone.nodeName = node.nodeName + suffix;
      clone.setPosition(node.getX(), node.getY(), node.getZ());
      clone.setScale(node.getScaleX(), node.getScaleY(), node.getScaleZ());
      clone.setAngleX(node.getAngleX());
      clone.setAngleY(node.getAngleY());
      clone.setAngleZ(node.getAngleZ());
      clone.setVisible(node.isVisible());
      clone.setCanBePick(node.isCanBePick());
      clone.setShowWire(node.isShowWire());
      clone.setWireColor(node.getWireColor());
      clone.setAdditionalInformation(node.getAdditionalInformation());

      if(node.isXLimited() == true)
      {
         clone.limitX(node.getXMin(), node.getXMax());
      }

      if(node.isYLimited() == true)
      {
         clone.limitY(node.getYMin(), node.getYMax());
      }

      if(node.isZLimited() == true)
      {
         clone.limitZ(node.getZMin(), node.getZMax());
      }

      if(node.isXAngleLimited() == true)
      {
         clone.limitAngleX(node.getXAngleMin(), node.getXAngleMax());
      }

      if(node.isYAngleLimited() == true)
      {
         clone.limitAngleY(node.getYAngleMin(), node.getYAngleMax());
      }

      if(node.isZAngleLimited() == true)
      {
         clone.limitAngleZ(node.getZAngleMin(), node.getZAngleMax());
      }

      final int nb = node.childCount();

      for(int i = 0; i < nb; i++)
      {
         clone.addChild(Tool3D.createCloneHierarchy(node.getChild(i), suffix));
      }

      return clone;
   }

   /**
    * Create a force joined mesh with to path.<br>
    * The path for V walk throw the path for U
    * 
    * @param pathU
    *           Path for U
    * @param precisionU
    *           Path for U precision
    * @param pathV
    *           Path for V
    * @param precisionV
    *           Path for V precision
    * @param multU
    *           multiplier
    * @param linearize
    *           Indicates if we want try to linearize UV
    * @param reverseNormals
    *           Indicates if normals must be reversed
    * @return Constructed mesh
    */
   public static Mesh createJoinedMesh(final Path pathU, final int precisionU, final Path pathV, final int precisionV, final float multU,
         final boolean linearize, final boolean reverseNormals)
   {
      final float mult = reverseNormals == true
            ? -1
            : 1;
      // Initialization
      final Mesh mesh = new Mesh();

      final ArrayList<Line2D> linesU = pathU.computePath(precisionU);
      linesU.add(linesU.get(0));
      final ArrayList<Line2D> linesV = pathV.computePath(precisionV);

      float x00 = 0, y00 = 0, z00 = 0, u00 = 0, v00 = 0, nx00 = 0, ny00 = 0, nz00 = 0;
      float x10 = 0, y10 = 0, z10 = 0, u10 = 0, v10 = 0, nx10 = 0, ny10 = 0, nz10 = 0;
      float x01 = 0, y01 = 0, z01 = 0, u01 = 0, v01 = 0, nx01 = 0, ny01 = 0, nz01 = 0;
      float x11 = 0, y11 = 0, z11 = 0, u11 = 0, v11 = 0, nx11 = 0, ny11 = 0, nz11 = 0;

      ArrayList<Vertex> temp = null;
      ArrayList<Vertex> old = null;

      float x, y, a0, b0, a1, b1, xp0, yp0, xp1, yp1, xx, yy;

      Vertex dir0, dir1, p0, p1;
      float a00, b00, c00, vx00, vy00, vz00;
      float a10, b10, c10, vx10, vy10, vz10;
      float a01, b01, c01, vx01, vy01, vz01;
      float a11, b11, c11, vx11, vy11, vz11;
      float mx0, my0, mz0, mx1, my1, mz1;

      float oldU0, oldU1;
      oldU0 = oldU1 = 0;
      float l0, l1;

      float length;
      boolean first;

      int index;
      first = true;

      // For each step in U path
      for(final Line2D lineU : linesU)
      {
         // U step goes (a0, b0) to (a1, b1), the direction vector is (x, y)
         a0 = lineU.pointStart.getX();
         b0 = lineU.pointStart.getY();
         a1 = lineU.pointEnd.getX();
         b1 = lineU.pointEnd.getY();
         x = a1 - a0;
         y = b1 - b0;

         // Normalize(x, y)
         length = (float) Math.sqrt((x * x) + (y * y));
         if(Math3D.nul(length) == false)
         {
            x /= length;
            y /= length;
         }

         // Compute U values of each face at this step
         u00 = u01 = lineU.start;
         u10 = u11 = lineU.end;

         // If we try to linearize, base on old values
         if(linearize == true)
         {
            u00 = u01 = oldU0;
            u10 = u11 = oldU1;
         }

         index = 0;
         temp = new ArrayList<Vertex>();

         // For each step in V path
         for(final Line2D lineV : linesV)
         {
            // V step goes (xp0, yp0) to (xp1, yp1), the direction vector is
            // (xx, yy)
            xp0 = lineV.pointStart.getX();
            yp0 = lineV.pointStart.getY();
            xp1 = lineV.pointEnd.getX();
            yp1 = lineV.pointEnd.getY();
            xx = xp1 - xp0;
            yy = yp1 - yp0;

            // Normalize (xx, yy)
            length = (float) Math.sqrt((xx * xx) + (yy * yy));
            if(Math3D.nul(length) == false)
            {
               xx /= length;
               yy /= length;
            }

            // Compute V for actual face
            v00 = v10 = lineV.start;
            v01 = v11 = lineV.end;

            // Up left position and normal
            x00 = a0 - (y * xp0);
            y00 = b0 + (x * xp0);
            z00 = yp0;

            nx00 = (mult * (x + (a0 - (y * xx)))) / 2f;
            ny00 = (mult * (y + (b0 + (x * xx)))) / 2f;
            nz00 = (mult * yy) / 2f;

            // Up right position and normal
            x10 = a1 - (y * xp0);
            y10 = b1 + (x * xp0);
            z10 = yp0;

            nx10 = (mult * (x + (a1 - (y * xx)))) / 2f;
            ny10 = (mult * (y + (b1 + (x * xx)))) / 2f;
            nz10 = (mult * yy) / 2f;

            // Down left position and normal
            x01 = a0 - (y * xp1);
            y01 = b0 + (x * xp1);
            z01 = yp1;

            nx01 = (mult * (x + (a0 - (y * xx)))) / 2f;
            ny01 = (mult * (y + (b0 + (x * xx)))) / 2f;
            nz01 = (mult * yy) / 2f;

            // Down right position and normal
            x11 = a1 - (y * xp1);
            y11 = b1 + (x * xp1);
            z11 = yp1;

            nx11 = (mult * (x + (a1 - (y * xx)))) / 2f;
            ny11 = (mult * (y + (b1 + (x * xx)))) / 2f;
            nz11 = (mult * yy) / 2f;

            // If it is not the first face time we goes on V path, join with old
            // face
            if(first == false)
            {
               // Get old face information to make the join join
               dir0 = old.get(index++);
               dir1 = old.get(index++);
               p0 = old.get(index++);
               p1 = old.get(index++);

               // Get first old point and direction
               a00 = p0.getPosition().getX();
               b00 = p0.getPosition().getY();
               c00 = p0.getPosition().getZ();
               vx00 = dir0.getPosition().getX();
               vy00 = dir0.getPosition().getY();
               vz00 = dir0.getPosition().getZ();
               // If direction is not zero vector
               length = (float) Math.sqrt((vx00 * vx00) + (vy00 * vy00) + (vz00 * vz00));
               if(Math3D.nul(length) == false)
               {
                  // Normalize direction
                  vx00 /= length;
                  vy00 /= length;
                  vz00 /= length;

                  // Get first new point and direction
                  a10 = x00;
                  b10 = y00;
                  c10 = z00;
                  vx10 = x10 - x00;
                  vy10 = y10 - y00;
                  vz10 = z10 - z00;

                  // If the direction is not zero vector
                  length = (float) Math.sqrt((vx10 * vx10) + (vy10 * vy10) + (vz10 * vz10));

                  if(Math3D.nul(length) == false)
                  {
                     // Normalize the vector
                     vx10 /= length;
                     vy10 /= length;
                     vz10 /= length;

                     // If the two direction are not colinear, then a corner
                     // join is need
                     if(Math3D.equal(Math.abs((vx00 * vx10) + (vy00 * vy10) + (vz00 * vz10)), 1f) == false)
                     {
                        // Get second old position and direction
                        a01 = p1.getPosition().getX();
                        b01 = p1.getPosition().getY();
                        c01 = p1.getPosition().getZ();
                        vx01 = dir1.getPosition().getX();
                        vy01 = dir1.getPosition().getY();
                        vz01 = dir1.getPosition().getZ();

                        // Normalize direction
                        length = (float) Math.sqrt((vx01 * vx01) + (vy01 * vy01) + (vz01 * vz01));
                        vx01 /= length;
                        vy01 /= length;
                        vz01 /= length;

                        // Get second new point and direction
                        a11 = x01;
                        b11 = y01;
                        c11 = z01;
                        vx11 = x11 - x01;
                        vy11 = y11 - y01;
                        vz11 = z11 - z01;

                        // Normalize direction
                        length = (float) Math.sqrt((vx11 * vx11) + (vy11 * vy11) + (vz11 * vz11));
                        vx11 /= length;
                        vy11 /= length;
                        vz11 /= length;

                        l0 = l1 = 0f;

                        // Compute intersection between first old and first new
                        if(Math3D.nul(vx00) == true)
                        {
                           if(Math3D.nul(vx10) == false)
                           {
                              l0 = (a00 - a10) / vx10;
                           }
                           else if(Math3D.nul(vy00) == true)
                           {
                              l0 = (b00 - b10) / vy10;
                           }
                           else if(Math3D.nul(vz00) == true)
                           {
                              l0 = (c00 - c10) / vz10;
                           }
                           else
                           {
                              l0 = (((b00 * vz00) - (c00 * vy00) - (b10 * vz00)) + (c10 * vy00)) / ((vy10 * vz00) - (vz10 * vy00));
                           }
                        }
                        else if(Math3D.nul(vy00) == true)
                        {
                           if(Math3D.nul(vy10) == false)
                           {
                              l0 = (b00 - b10) / vy10;
                           }
                           else if(Math3D.nul(vz00) == true)
                           {
                              l0 = (c00 - c10) / vz10;
                           }
                           else
                           {
                              l0 = (((a00 * vz00) - (c00 * vx00) - (a10 * vz00)) + (c10 * vx00)) / ((vx10 * vz00) - (vz10 * vx00));
                           }
                        }
                        else if((Math3D.nul(vz00) == true) && (Math3D.nul(vz10) == false))
                        {
                           l0 = (c00 - c10) / vz10;
                        }
                        else
                        {
                           l0 = (((a00 * vy00) - (b00 * vx00) - (a10 * vy00)) + (b10 * vx00)) / ((vx10 * vy00) - (vy10 * vx00));
                        }
                        mx0 = a10 + (vx10 * l0);
                        my0 = b10 + (vy10 * l0);
                        mz0 = c10 + (vz10 * l0);

                        // Compute intersection between second old and second
                        // new
                        if(Math3D.nul(vx01) == true)
                        {
                           if(Math3D.nul(vx11) == false)
                           {
                              l1 = (a01 - a11) / vx11;
                           }
                           else if(Math3D.nul(vy01) == true)
                           {
                              l1 = (b01 - b11) / vy11;
                           }
                           else if(Math3D.nul(vz01) == true)
                           {
                              l1 = (c01 - c11) / vz11;
                           }
                           else
                           {
                              l1 = (((b01 * vz01) - (c01 * vy01) - (b11 * vz01)) + (c11 * vy01)) / ((vy11 * vz01) - (vz11 * vy01));
                           }
                        }
                        else if(Math3D.nul(vy01) == true)
                        {
                           if(Math3D.nul(vy11) == false)
                           {
                              l1 = (b01 - b11) / vy11;
                           }
                           else if(Math3D.nul(vz01) == true)
                           {
                              l1 = (c01 - c11) / vz11;
                           }
                           else
                           {
                              l1 = (((a01 * vz01) - (c01 * vx01) - (a11 * vz01)) + (c11 * vx01)) / ((vx11 * vz01) - (vz11 * vx01));
                           }
                        }
                        else if((Math3D.nul(vz01) == true) && (Math3D.nul(vz11) == false))
                        {
                           l1 = (c01 - c11) / vz11;
                        }
                        else
                        {
                           l1 = (((a01 * vy01) - (b01 * vx01) - (a11 * vy01)) + (b11 * vx01)) / ((vx11 * vy01) - (vy11 * vx01));
                        }
                        mx1 = a11 + (vx11 * l1);
                        my1 = b11 + (vy11 * l1);
                        mz1 = c11 + (vz11 * l1);

                        // If we decide to linearize, linearize U
                        if(linearize == true)
                        {
                           u00 = p0.getUv().getX();
                           u01 = p1.getUv().getX();

                           u00 += (float) Math.sqrt(Math3D.square(mx0 - p0.getPosition().getX()) + Math3D.square(my0 - p0.getPosition().getY())
                                 + Math3D.square(mz0 - p0.getPosition().getZ()));
                           u01 += (float) Math.sqrt(Math3D.square(mx1 - p1.getPosition().getX()) + Math3D.square(my1 - p1.getPosition().getY())
                                 + Math3D.square(mz1 - p1.getPosition().getZ()));
                        }

                        // Create first part of the corner
                        mesh.addVertexToTheActualFace(p0);
                        mesh.addVertexToTheActualFace(p1);
                        mesh.addVertexToTheActualFace(new Vertex(mx1, my1, mz1, u01, v01, nx01, ny01, nz01));
                        mesh.addVertexToTheActualFace(new Vertex(mx0, my0, mz0, u00, v00, nx00, ny00, nz00));
                        mesh.endFace();

                        // We consider the end of the first part like the end of
                        // the old face
                        p0.set(mx0, my0, mz0);
                        p1.set(mx1, my1, mz1);

                        if(linearize == true)
                        {
                           p0.getUv().set(u00, p0.getUv().getY());
                           p1.getUv().set(u01, p1.getUv().getY());
                        }
                     }
                  }
               }

               // Link old face to new one.
               // Remember that it could be the end of the corner. In this case
               // its create the end of the corner
               if(linearize == true)
               {
                  u00 = p0.getUv().getX();
                  u01 = p1.getUv().getX();

                  u00 += (float) Math.sqrt(Math3D.square(x00 - p0.getPosition().getX()) + Math3D.square(y00 - p0.getPosition().getY())
                        + Math3D.square(z00 - p0.getPosition().getZ()));
                  u01 += (float) Math.sqrt(Math3D.square(x01 - p1.getPosition().getX()) + Math3D.square(y01 - p1.getPosition().getY())
                        + Math3D.square(z01 - p1.getPosition().getZ()));
               }

               mesh.addVertexToTheActualFace(p0);
               mesh.addVertexToTheActualFace(p1);
               mesh.addVertexToTheActualFace(new Vertex(x01, y01, z01, u01, v01, nx01, ny01, nz01));
               mesh.addVertexToTheActualFace(new Vertex(x00, y00, z00, u00, v00, nx00, ny00, nz00));
               mesh.endFace();
            }

            // Draw actual face
            if(linearize == true)
            {
               u10 = u00 + (float) Math.sqrt(Math3D.square(x00 - x10) + Math3D.square(y00 - y10) + Math3D.square(z00 - z10));
               u11 = u01 + (float) Math.sqrt(Math3D.square(x01 - x11) + Math3D.square(y01 - y11) + Math3D.square(z01 - z11));
            }

            mesh.addVertexToTheActualFace(new Vertex(x00, y00, z00, u00, v00, nx00, ny00, nz00));
            mesh.addVertexToTheActualFace(new Vertex(x01, y01, z01, u01, v01, nx01, ny01, nz01));
            mesh.addVertexToTheActualFace(new Vertex(x11, y11, z11, u11, v11, nx11, ny11, nz11));
            mesh.addVertexToTheActualFace(new Vertex(x10, y10, z10, u10, v10, nx10, ny10, nz10));
            mesh.endFace();

            // Memorize informations for the next join
            if(linearize == true)
            {
               u00 = u01 = oldU0;
               u10 = u11 = oldU1;
            }

            temp.add(new Vertex(x10 - x00, y10 - y00, z10 - z00, u10, v10, nx10, ny10, nz10));
            temp.add(new Vertex(x11 - x01, y11 - y01, z11 - z01, u11, v11, nx11, ny11, nz11));
            temp.add(new Vertex(x10, y10, z10, u10, v10, nx10, ny10, nz10));
            temp.add(new Vertex(x11, y11, z11, u11, v11, nx11, ny11, nz11));
         }

         if(linearize == true)
         {
            oldU0 = u00 + (float) Math.sqrt(Math3D.square(x00 - x10) + Math3D.square(y00 - y10) + Math3D.square(z00 - z10));
            oldU1 = u01 + (float) Math.sqrt(Math3D.square(x01 - x11) + Math3D.square(y01 - y11) + Math3D.square(z01 - z11));
         }

         first = false;
         old = temp;
      }

      // On linearize mode, try to make valid U
      if(linearize == true)
      {
         mesh.multUV(multU / Math.max(oldU0, oldU1), 1);
      }
      else
      {
         mesh.multUV(multU, 1);
      }

      return mesh;
   }

   /**
    * Create a mesh with to path.<br>
    * The path for V walk throw the path for U
    * 
    * @param pathU
    *           Path for U
    * @param precisionU
    *           Path for U precision
    * @param pathV
    *           Path for V
    * @param precisionV
    *           Path for V precision
    * @return Constructed mesh
    */
   public static Mesh createMesh(final Path pathU, final int precisionU, final Path pathV, final int precisionV)
   {
      // Initialization
      final Mesh mesh = new Mesh();

      final ArrayList<Line2D> linesU = pathU.computePath(precisionU);
      final ArrayList<Line2D> linesV = pathV.computePath(precisionV);

      float x00, y00, z00, u00, v00, nx00, ny00, nz00;
      float x10, y10, z10, u10, v10, nx10, ny10, nz10;
      float x01, y01, z01, u01, v01, nx01, ny01, nz01;
      float x11, y11, z11, u11, v11, nx11, ny11, nz11;

      float x, y, a0, b0, a1, b1, xp0, yp0, xp1, yp1, xx, yy;

      float length;

      // For each step in U path
      for(final Line2D lineU : linesU)
      {
         // U step goes (a0, b0) to (a1, b1), the direction vector is (x, y)
         a0 = lineU.pointStart.getX();
         b0 = lineU.pointStart.getY();
         a1 = lineU.pointEnd.getX();
         b1 = lineU.pointEnd.getY();
         x = a1 - a0;
         y = b1 - b0;
         // Normalize (x,y)
         length = (float) Math.sqrt((x * x) + (y * y));
         if(Math3D.nul(length) == false)
         {
            x /= length;
            y /= length;
         }

         // Compute U values of each face at this step
         u00 = u01 = lineU.start;
         u10 = u11 = lineU.end;

         // For each step on V path
         for(final Line2D lineV : linesV)
         {
            // V step goes (xp0, yp0) to (xp1, yp1), the direction vector is
            // (xx, yy)
            xp0 = lineV.pointStart.getX();
            yp0 = lineV.pointStart.getY();
            xp1 = lineV.pointEnd.getX();
            yp1 = lineV.pointEnd.getY();
            xx = xp1 - xp0;
            yy = yp1 - yp0;
            // Normalize(xx, yy)
            length = (float) Math.sqrt((xx * xx) + (yy * yy));
            if(Math3D.nul(length) == false)
            {
               xx /= length;
               yy /= length;
            }
            // Compute V for the actual face
            v00 = v10 = lineV.start;
            v01 = v11 = lineV.end;

            // Up left position and normal
            x00 = a0 - (y * xp0);
            y00 = b0 + (x * xp0);
            z00 = yp0;

            nx00 = (x + (a0 - (y * xx))) / 2f;
            ny00 = (y + (b0 + (x * xx))) / 2f;
            nz00 = yy / 2f;

            // Up right position and normal
            x10 = a1 - (y * xp0);
            y10 = b1 + (x * xp0);
            z10 = yp0;

            nx10 = (x + (a1 - (y * xx))) / 2f;
            ny10 = (y + (b1 + (x * xx))) / 2f;
            nz10 = yy / 2f;

            // Down left position and normal
            x01 = a0 - (y * xp1);
            y01 = b0 + (x * xp1);
            z01 = yp1;

            nx01 = (x + (a0 - (y * xx))) / 2f;
            ny01 = (y + (b0 + (x * xx))) / 2f;
            nz01 = yy / 2f;

            // Down right position and normal
            x11 = a1 - (y * xp1);
            y11 = b1 + (x * xp1);
            z11 = yp1;

            nx11 = (x + (a1 - (y * xx))) / 2f;
            ny11 = (y + (b1 + (x * xx))) / 2f;
            nz11 = yy / 2f;

            // Create the face
            mesh.addVertexToTheActualFace(new Vertex(x00, y00, z00, u00, v00, nx00, ny00, nz00));
            mesh.addVertexToTheActualFace(new Vertex(x01, y01, z01, u01, v01, nx01, ny01, nz01));
            mesh.addVertexToTheActualFace(new Vertex(x11, y11, z11, u11, v11, nx11, ny11, nz11));
            mesh.addVertexToTheActualFace(new Vertex(x10, y10, z10, u10, v10, nx10, ny10, nz10));
            mesh.endFace();
         }
      }

      return mesh;
   }

   /**
    * Create a node on parsing XML
    * 
    * @param markupXML
    *           Markup to parse
    * @return Created node
    * @throws Exception
    *            On creation problem
    */
   public static Node createNode(final MarkupXML markupXML) throws Exception
   {
      if(markupXML.isParameter(ConstantsXML.MARKUP_NODE_type) == false)
      {
         throw new IllegalArgumentException(UtilText.concatenate("Missing mendatory parameter ", ConstantsXML.MARKUP_NODE_type, " in ", markupXML.getName()));
      }

      try
      {
         final NodeType nodeType = NodeType.valueOf(markupXML.obtainParameter(ConstantsXML.MARKUP_NODE_type, ""));
         Node node = null;

         switch(nodeType)
         {
            case BOX:
               node = new Box();
            break;
            case CLONE:
               node = new ObjectClone((Object3D) null);
            break;
            case NODE:
               node = new Node();
            break;
            case OBJECT3D:
               node = new Object3D();
            break;
            case PATH_GEOM:
               node = new PathGeom();
            break;
            case PLANE:
               node = new Plane();
            break;
            case REVOLUTION:
               node = new Revolution();
            break;
            case SPHERE:
               node = new Sphere();
            break;
            case EQUATION:
               // {@todo} TODO Implements createNode in jhelp.engine.util [JHelpEngine]
               Debug.printTodo("Implements createNode in jhelp.engine.util [JHelpEngine] Eqaution case");
               node = new Node();
            break;
            default:
               // {@todo} TODO Implements createNode in jhelp.engine.util [JHelpEngine]
               Debug.printTodo("Implements createNode in jhelp.engine.util [JHelpEngine] Missing case");
               node = new Node();
            break;
         }

         node.loadFromXML(markupXML);

         return node;
      }
      catch(final Exception exception)
      {
         throw new Exception("Problem on constructs the node", exception);
      }
   }

   /**
    * Compute the volume of intersection that may append when given nodes will translates of given vectors
    * 
    * @param node1
    *           First node
    * @param vector1
    *           Translation that will apply to first node
    * @param node2
    *           Second node
    * @param vector2
    *           Translation that will apply to second node
    * @return Volume of future intersection. 0 means they will not have any intersection
    */
   public static float futureIntersectionVolume(final Node node1, final Point3D vector1, final Node node2, final Point3D vector2)
   {
      VirtualBox virtualBox = node1.computeProjectedTotalBox();
      virtualBox.translate(vector1);

      if(virtualBox.isEmpty() == true)
      {
         return 0;
      }

      final float xmin1 = virtualBox.getMinX();
      final float ymin1 = virtualBox.getMinY();
      final float zmin1 = virtualBox.getMinZ();
      final float xmax1 = virtualBox.getMaxX();
      final float ymax1 = virtualBox.getMaxY();
      final float zmax1 = virtualBox.getMaxZ();

      virtualBox = node2.computeProjectedTotalBox();
      virtualBox.translate(vector2);

      if(virtualBox.isEmpty() == true)
      {
         return 0;
      }

      final float xmin2 = virtualBox.getMinX();
      final float ymin2 = virtualBox.getMinY();
      final float zmin2 = virtualBox.getMinZ();
      final float xmax2 = virtualBox.getMaxX();
      final float ymax2 = virtualBox.getMaxY();
      final float zmax2 = virtualBox.getMaxZ();

      if((xmin1 > xmax2) || (ymin1 > ymax2) || (zmin1 > zmax2) || (xmin2 > xmax1) || (ymin2 > ymax1) || (zmin2 > zmax1))
      {
         return 0;
      }

      final float xmin = Math.max(xmin1, xmin2);
      final float xmax = Math.min(xmax1, xmax2);

      if(xmin >= xmax)
      {
         return 0;
      }

      final float ymin = Math.max(ymin1, ymin2);
      final float ymax = Math.min(ymax1, ymax2);

      if(ymin >= ymax)
      {
         return 0;
      }

      final float zmin = Math.max(zmin1, zmin2);
      final float zmax = Math.min(zmax1, zmax2);

      if(zmin >= zmax)
      {
         return 0;
      }

      return (xmax - xmin) * (ymax - ymin) * (zmax - zmin);
   }

   /**
    * Get a Color4f parameter from XML markup
    * 
    * @param markupXML
    *           XML markup where extract
    * @param parameterName
    *           Parameter name
    * @return The color
    */
   public static Color4f getColor4fParameter(final MarkupXML markupXML, final String parameterName)
   {
      final Color4f color4f = new Color4f();
      try
      {
         StringTokenizer stringTokenizer = new StringTokenizer(markupXML.obtainParameter(parameterName, "1 1 1 1"));
         color4f.setAlpha(Float.parseFloat(stringTokenizer.nextToken()));
         color4f.setRed(Float.parseFloat(stringTokenizer.nextToken()));
         color4f.setGreen(Float.parseFloat(stringTokenizer.nextToken()));
         color4f.setBlue(Float.parseFloat(stringTokenizer.nextToken()));
         stringTokenizer = null;
      }
      catch(final Exception exception)
      {
      }
      return color4f;
   }

   /**
    * Retrieve a Point3D parameter from XML markup
    * 
    * @param markupXML
    *           XML markup where extract
    * @param parameterName
    *           Parameter name
    * @return Point read
    */
   public static Point3D getPoint3DParameter(final MarkupXML markupXML, final String parameterName)
   {
      final Point3D point = new Point3D();
      try
      {
         StringTokenizer stringTokenizer = new StringTokenizer(markupXML.obtainParameter(parameterName, "0 0 0"));
         final float x = Float.parseFloat(stringTokenizer.nextToken());
         final float y = Float.parseFloat(stringTokenizer.nextToken());
         final float z = Float.parseFloat(stringTokenizer.nextToken());
         point.set(x, y, z);
         stringTokenizer = null;
      }
      catch(final Exception exception)
      {
      }
      return point;
   }

   /**
    * Create a bump texture
    * 
    * @param color
    *           Unify color
    * @param bump
    *           Texture with bump informations
    * @param contrast
    *           Contrast to apply
    * @param dark
    *           Dark level
    * @param shiftX
    *           X shifting
    * @param shiftY
    *           Y shifting
    * @return Bump texture
    */
   public static Texture obtainBumpTexture(final Color color, final Texture bump, final float contrast, final float dark, final int shiftX, final int shiftY)
   {
      return Tool3D.obtainBumpTexture(new Texture("Unify", bump.getWidth(), bump.getHeight(), color), bump, contrast, dark, shiftX, shiftY);
   }

   /**
    * Create a bump texture with 2 textures.<br>
    * The 2 textures MUST have same dimensions
    * 
    * @param original
    *           Texture where add bump
    * @param bump
    *           Texture with bump information
    * @param contrast
    *           Contrast
    * @param dark
    *           Dark level
    * @param shiftX
    *           X shift
    * @param shiftY
    *           Y shift
    * @return Bump texture
    */
   public static Texture obtainBumpTexture(final Texture original, final Texture bump, float contrast, final float dark, final int shiftX, final int shiftY)
   {
      if((original.getWidth() != bump.getWidth()) || (original.getHeight() != bump.getHeight()))
      {
         throw new IllegalArgumentException("Original and bump textures must have same size");
      }

      final String name = UtilText.concatenate(original.getTextureName(), "_BUMP_", bump.getTextureName());

      final int width = original.getWidth();
      final int height = original.getHeight();

      if(contrast <= 0.5)
      {
         contrast *= 2;
      }
      else
      {
         contrast *= 18;
         contrast -= 8;
      }

      Texture bumped = Texture.obtainTexture(name);
      if(bumped == null)
      {
         bumped = new Texture(name, width, height);
      }

      bumped.setPixels(bump);
      bumped.toGray();
      bumped.contrast(contrast);

      Texture temp = new Texture("temp", width, height);

      temp.setPixels(bumped);
      temp.multTexture(original);
      temp.darker(dark);

      bumped.invert();
      bumped.multTexture(original);
      bumped.darker(dark);
      bumped.shift(shiftX, shiftY);
      bumped.addTexture(temp);

      temp.destroy();
      temp = null;

      bumped.flush();

      return bumped;
   }

   /**
    * Indicates if given nodes will collide after a translation
    * 
    * @param node1
    *           First node
    * @param vector1
    *           Translation that will apply to first node
    * @param node2
    *           Second node
    * @param vector2
    *           Translation that will apply to second node
    * @return {@code ture} if collision will append
    */
   public static boolean willCollide(final Node node1, final Point3D vector1, final Node node2, final Point3D vector2)
   {
      return Tool3D.futureIntersectionVolume(node1, vector1, node2, vector2) > 0;
   }
}