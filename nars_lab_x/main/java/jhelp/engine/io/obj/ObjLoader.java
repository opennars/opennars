/**
 * Project : JHelpEngine<br>
 * Package : jhelp.io.obj<br>
 * Class : ObjLoader<br>
 * Date : 24 mai 2009<br>
 * By JHelp
 */
package jhelp.engine.io.obj;

import jhelp.engine.Mesh;
import jhelp.engine.Object3D;
import jhelp.engine.Point2D;
import jhelp.engine.Point3D;
import jhelp.util.debug.Debug;
import jhelp.util.debug.DebugLevel;
import jhelp.util.list.ArrayInt;
import jhelp.util.text.StringCutter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Loader for OBJ format <br>
 * <br>
 * Last modification : 24 mai 2009<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public class ObjLoader
{
   /**
    * Load object from stream
    * 
    * @param object3D
    *           Object to fill, if {@code null} a new instance is created
    * @param inputStream
    *           Stream to parse
    * @return Object created
    * @throws IOException
    *            On reading issue
    */
   public static Object3D loadObj(final Object3D object3D, final InputStream inputStream) throws IOException
   {
      return ObjLoader.loadObj(object3D, inputStream, false);
   }

   /**
    * Load a "obj" stream.<br>
    * Its possible to modify an object or create a new one.<br>
    * If there more than one object inside stream a complete hierarchy is created
    * 
    * @param object3D
    *           Object to reuse. Can be {@code null} to create a new object
    * @param inputStream
    *           Stream to read. The stream is not closed, it up to caller to do it
    * @param reveseNormals
    *           Indicates if normal have to be reversed
    * @return Created/modified object
    * @throws IOException
    *            On stream read issue
    */
   public static Object3D loadObj(Object3D object3D, final InputStream inputStream, final boolean reveseNormals) throws IOException
   {
      if(object3D == null)
      {
         object3D = new Object3D();
      }

      object3D.reset();

      final ArrayList<Point3D> points = new ArrayList<Point3D>();
      final ArrayList<Point3D> normals = new ArrayList<Point3D>();
      final ArrayList<Point2D> uv = new ArrayList<Point2D>();

      final ArrayInt pointFace = new ArrayInt();
      final ArrayInt normalFace = new ArrayInt();
      final ArrayInt uvFace = new ArrayInt();

      Object3D edit = object3D;

      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

      StringTokenizer stringTokenizer;
      StringCutter stringCuter;
      String word;

      int startPoint = 1;
      int startUV = 1;
      int startNormal = 1;
      float multiplier = 1;

      if(reveseNormals == true)
      {
         multiplier = -1;
      }

      String line = bufferedReader.readLine();
      while(line != null)
      {
         line = line.trim();

         if((line.length() > 0) && (line.charAt(0) != '#'))
         {
            stringTokenizer = new StringTokenizer(line, " \n\t\f\r", false);
            word = stringTokenizer.nextToken();

            if((word.equals("g") == true) || (word.equals("o") == true))
            {
               if(pointFace.getSize() > 0)
               {
                  Mesh.fillObjectOBJ(edit, points, normals, uv, pointFace, normalFace, uvFace, startPoint, startUV, startNormal);

                  startPoint += points.size();
                  startUV += uv.size();
                  startNormal += normals.size();

                  points.clear();
                  normals.clear();
                  uv.clear();
                  pointFace.clear();
                  normalFace.clear();
                  uvFace.clear();
               }

               edit = new Object3D();
               object3D.addChild(edit);

               if(stringTokenizer.hasMoreTokens() == true)
               {
                  edit.nodeName = stringTokenizer.nextToken();
               }

               Debug.println(DebugLevel.VERBOSE, "Found object : ", edit.nodeName);
            }
            else if(word.equals("v") == true)
            {
               points.add(new Point3D(Float.parseFloat(stringTokenizer.nextToken()), Float.parseFloat(stringTokenizer.nextToken()),
                     Float.parseFloat(stringTokenizer.nextToken())));
            }
            else if(word.equals("vt") == true)
            {
               uv.add(new Point2D(Float.parseFloat(stringTokenizer.nextToken()), 1 - Float.parseFloat(stringTokenizer.nextToken())));
            }
            else if(word.equals("vn") == true)
            {
               normals.add(new Point3D(multiplier * Float.parseFloat(stringTokenizer.nextToken()), multiplier * Float.parseFloat(stringTokenizer.nextToken()),
                     multiplier * Float.parseFloat(stringTokenizer.nextToken())));
            }
            else if((word.equals("f") == true) || (word.equals("fo") == true))
            {
               while(stringTokenizer.hasMoreTokens() == true)
               {
                  stringCuter = new StringCutter(stringTokenizer.nextToken(), '/');

                  pointFace.add(Integer.parseInt(stringCuter.next()));

                  word = stringCuter.next();
                  if(word != null)
                  {
                     if(word.length() > 0)
                     {
                        uvFace.add(Integer.parseInt(word));
                     }

                     word = stringCuter.next();
                     if(word != null)
                     {
                        if(word.length() > 0)
                        {
                           normalFace.add(Integer.parseInt(word));
                        }
                     }
                  }
               }

               pointFace.add(-1);
            }
         }

         line = bufferedReader.readLine();
      }

      bufferedReader.close();
      bufferedReader = null;

      if(pointFace.getSize() > 0)
      {
         Mesh.fillObjectOBJ(edit, points, normals, uv, pointFace, normalFace, uvFace, startPoint, startUV, startNormal);
      }

      object3D.flush();

      return object3D;
   }
}