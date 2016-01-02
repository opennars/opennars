/**
 * Project : JHelpEngine<br>
 * Package : jhelp.engine<br>
 * Class : Font3D<br>
 * Date : 9 juin 2009<br>
 * By JHelp
 */
package jhelp.engine;

import jhelp.engine.twoD.BorderIterator;
import jhelp.engine.util.Math3D;

import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.util.Hashtable;

/**
 * 3D font. It creates 3D object for character or String <br>
 * <br>
 * Last modification : 9 juin 2009<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public class Font3D
{
   /** Objects already created */
   private static Hashtable<String, NodeWithMaterial> alreadyCreated;
   /** Identity transformation */
   public static final AffineTransform                affineTransform   = new AffineTransform();
   /** Font renderer context */
   public static final FontRenderContext              fontRenderContext = new FontRenderContext(Font3D.affineTransform, true, true);

   /**
    * Create an object represents a character
    * 
    * @param fontFamily
    *           Font family name
    * @param character
    *           Character to represents
    * @param flatness
    *           The maximum distance that the line segments used to approximate the curved segments are allowed to deviate from
    *           any point on the original curve
    * @param multBorder
    *           U multiplier
    * @return Created object
    */
   public static NodeWithMaterial createCharacter(final String fontFamily, final char character, final float flatness, final float multBorder)
   {
      if(fontFamily == null)
      {
         throw new NullPointerException("fontFamily musn't be null");
      }

      StringBuffer stringBuffer = new StringBuffer("Auto_Create_Character_");
      stringBuffer.append(character);
      stringBuffer.append("_Font_");
      stringBuffer.append(fontFamily);
      stringBuffer.append("_");
      stringBuffer.append(flatness);
      stringBuffer.append("_");
      stringBuffer.append(multBorder);

      final String name = stringBuffer.toString();
      stringBuffer = null;
      if(Font3D.alreadyCreated == null)
      {
         Font3D.alreadyCreated = new Hashtable<String, NodeWithMaterial>();
      }

      final NodeWithMaterial nodeWithMaterial = Font3D.alreadyCreated.get(name);
      if(nodeWithMaterial != null)
      {
         final ObjectClone objectClone = new ObjectClone((Object3D) nodeWithMaterial);
         objectClone.nodeName = name;
         objectClone.setAngleX(180);

         return objectClone;
      }

      final Font font = new Font(fontFamily, Font.PLAIN, 1);

      final Object3D object3D = new Object3D();
      object3D.nodeName = name;

      final BorderIterator borderIterator = new BorderIterator(font.createGlyphVector(Font3D.fontRenderContext, new char[]
      {
         character
      }).getOutline(), flatness);

      final float z = font.getSize2D() / 8f;

      final float minX = (float) borderIterator.getMinX();
      final float minY = (float) borderIterator.getMinY();

      final float width = (float) Math.abs(borderIterator.getMinX() - borderIterator.getMaxX());
      final float height = (float) Math.abs(borderIterator.getMinY() - borderIterator.getMaxY());

      final float tx = (float) ((borderIterator.getMinX() - borderIterator.getMaxX()) / 2d);
      final float ty = (float) ((borderIterator.getMaxY() - borderIterator.getMinY()) / 2d);

      final float length = (float) borderIterator.getLength();
      float u = 0;

      float dx = 0, dy = 0, cx = 0, cy = 0, fx, fy, nx, ny, vz2;
      float[] temp = new float[2];
      while(borderIterator.isDone() == false)
      {
         switch(borderIterator.currentSegment(temp))
         {
            case PathIterator.SEG_MOVETO:
               dx = cx = fx = temp[0];
               dy = cy = fy = temp[1];
            break;
            case PathIterator.SEG_LINETO:
               fx = temp[0];
               fy = temp[1];

               vz2 = 2f * z;

               nx = (cy - fy) * vz2;
               ny = (fx - cx) * vz2;

               object3D.add(new Vertex(tx + cx, ty + cy, -z, (u * multBorder) / length, 0, nx, ny, 0));
               object3D.add(new Vertex(tx + cx, ty + cy, z, (u * multBorder) / length, 1, nx, ny, 0));

               u += (float) Math.sqrt(Math3D.square(cx - fx) + Math3D.square(cy - fy));

               object3D.add(new Vertex(tx + fx, ty + fy, z, (u * multBorder) / length, 1, nx, ny, 0));
               object3D.add(new Vertex(tx + fx, ty + fy, -z, (u * multBorder) / length, 0, nx, ny, 0));
               object3D.nextFace();

               cx = fx;
               cy = fy;
            break;
            case PathIterator.SEG_CLOSE:
               vz2 = 2f * z;

               nx = (cy - dy) * vz2;
               ny = (dx - cx) * vz2;

               object3D.add(new Vertex(tx + cx, ty + cy, -z, (u * multBorder) / length, 0, nx, ny, 0));
               object3D.add(new Vertex(tx + cx, ty + cy, z, (u * multBorder) / length, 1, nx, ny, 0));

               u += (float) Math.sqrt(Math3D.square(cx - dx) + Math3D.square(cy - dy));

               object3D.add(new Vertex(tx + dx, ty + dy, z, (u * multBorder) / length, 1, nx, ny, 0));
               object3D.add(new Vertex(tx + dx, ty + dy, -z, (u * multBorder) / length, 0, nx, ny, 0));
               object3D.nextFace();

               cx = dx;
               cy = dy;
            break;
         }

         borderIterator.next();
      }

      borderIterator.reset();
      dx = dy = cx = cy = fx = fy = 0;
      Polygon3D polygon3D = new Polygon3D(borderIterator.getWindingRule());
      while(borderIterator.isDone() == false)
      {
         switch(borderIterator.currentSegment(temp))
         {
            case PathIterator.SEG_MOVETO:
               dx = cx = fx = temp[0];
               dy = cy = fy = temp[1];

               polygon3D.add(new Vertex(tx + cx, ty + cy, -z, (cx + minX) / width, (cy + minY) / height, 0, 0, 1));
            break;
            case PathIterator.SEG_LINETO:
               fx = temp[0];
               fy = temp[1];

               polygon3D.add(new Vertex(tx + fx, ty + fy, -z, (fx + minX) / width, (fy + minY) / height, 0, 0, 1));

               cx = fx;
               cy = fy;
            break;
            case PathIterator.SEG_CLOSE:
               polygon3D.add(new Vertex(tx + dx, ty + dy, -z, (dx + minX) / width, (dy + minY) / height, 0, 0, 1));

               cx = dx;
               cy = dy;
            break;
         }

         borderIterator.next();
      }
      object3D.addPolygon(polygon3D);

      borderIterator.reset();
      dx = dy = cx = cy = fx = fy = 0;
      polygon3D = new Polygon3D(borderIterator.getWindingRule());
      while(borderIterator.isDone() == false)
      {
         switch(borderIterator.currentSegment(temp))
         {
            case PathIterator.SEG_MOVETO:
               dx = cx = fx = temp[0];
               dy = cy = fy = temp[1];

               polygon3D.push(new Vertex(tx + cx, ty + cy, z, (cx + minX) / width, (cy + minY) / height, 0, 0, -1));
            break;
            case PathIterator.SEG_LINETO:
               fx = temp[0];
               fy = temp[1];

               polygon3D.push(new Vertex(tx + fx, ty + fy, z, (fx + minX) / width, (fy + minY) / height, 0, 0, -1));

               cx = fx;
               cy = fy;
            break;
            case PathIterator.SEG_CLOSE:
               polygon3D.push(new Vertex(tx + dx, ty + dy, z, (dx + minX) / width, (dy + minY) / height, 0, 0, -1));

               cx = dx;
               cy = dy;
            break;
         }

         borderIterator.next();
      }
      object3D.addPolygon(polygon3D);

      temp = null;
      borderIterator.destroy();

      object3D.setAngleX(180);

      Font3D.alreadyCreated.put(name, object3D);

      return object3D;
   }

   /**
    * Create a String object
    * 
    * @param fontFamily
    *           Font family name
    * @param string
    *           String to represents
    * @param flatness
    *           The maximum distance that the line segments used to approximate the curved segments are allowed to deviate from
    *           any point on the original curve
    * @param multBorder
    *           U multiplier
    * @param space
    *           Space between each letters
    * @return Created object
    */
   public static Node createString(final String fontFamily, final String string, final float flatness, final float multBorder, final float space)
   {
      final char[] characters = string.toCharArray();
      final int length = characters.length;
      final NodeWithMaterial[] letters = new NodeWithMaterial[length];
      final VirtualBox virtualBox = new VirtualBox();
      VirtualBox box;
      float x = 0;

      for(int i = 0; i < length; i++)
      {
         letters[i] = Font3D.createCharacter(fontFamily, characters[i], flatness, multBorder);
         box = letters[i].getBox();
         virtualBox.add(box.getMinX() + x, box.getMinY(), box.getMinZ());
         virtualBox.add(box.getMaxX() + x, box.getMaxY(), box.getMaxZ());

         x += Math.abs(box.getMaxX() - box.getMinX()) + space;
      }

      final Point3D center = virtualBox.getCenter();
      x = virtualBox.getMinX() - center.x;

      final Node node = new Node();
      node.nodeName = ((new StringBuffer("Auto_Generate_String_").append(string).append("_Font_").append(fontFamily).append('_').append(flatness).append('_').append(multBorder).append('_').append(space))).toString();

      for(int i = 0; i < length; i++)
      {
         letters[i].translate(x, 0, 0);
         node.addChild(letters[i]);
         box = letters[i].getBox();
         x += Math.abs(box.getMaxX() - box.getMinX()) + space;
      }

      return node;
   }
}