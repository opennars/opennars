/**
 * Project : JHelpSceneGraph<br>
 * Package : jhelp.engine.twoD<br>
 * Class : PathElement<br>
 * Date : 17 janv. 2009<br>
 * By JHelp
 */
package jhelp.engine.twoD;

import jhelp.engine.Point3D;
import jhelp.engine.io.ConstantsXML;
import jhelp.util.text.UtilText;
import jhelp.xml.MarkupXML;

import java.util.StringTokenizer;

/**
 * Element of path <br>
 * <br>
 * Last modification : 21 janv. 2009<br>
 * Version 0.0.1<br>
 * 
 * @author JHelp
 */
public class PathElement
{
   /** Path type */
   public PathType  pathType;
   /** Point witch construct the element */
   public Point3D[] points;

   /**
    * Constructs PathElement
    */
   PathElement()
   {
   }

   /**
    * Constructs PathElement<br>
    * 
    * @param pathType
    *           Path type
    * @param points
    *           Points for the path element
    */
   public PathElement(final PathType pathType, final Point3D... points)
   {
      this.pathType = pathType;
      this.points = points;
   }

   /**
    * Read parameters form XML
    * 
    * @param markupXML
    *           Markup to parse
    * @throws Exception
    *            On reading/parsing issue
    */
   public void loadFromXML(final MarkupXML markupXML) throws Exception
   {
      final String pathType = markupXML.obtainParameter(ConstantsXML.MARKUP_PATH_ELEMENT_pathType, "");
      if(pathType.length() < 1)
      {
         throw new IllegalArgumentException(UtilText.concatenate("Missing mandatory parameter ", ConstantsXML.MARKUP_PATH_ELEMENT_pathType, " in ", markupXML.getName()));
      }

      this.pathType = PathType.valueOf(pathType);
      int length = 0;
      switch(this.pathType)
      {
         case CUBIC:
            length = 4;
         break;
         case LINE:
            length = 2;
         break;
         case QUAD:
            length = 3;
         break;
      }

      if(length < 2)
      {
         throw new Exception("Invalid type : " + pathType);
      }

      this.points = new Point3D[length];
      final StringTokenizer stringTokenizer = new StringTokenizer(markupXML.getText());
      for(int i = 0; i < length; i++)
      {
         this.points[i] = new Point3D(Float.parseFloat(stringTokenizer.nextToken()), Float.parseFloat(stringTokenizer.nextToken()), Float.parseFloat(stringTokenizer.nextToken()));
      }
   }

   /**
    * XML representation
    * 
    * @return XML representation
    */
   public MarkupXML saveToXML()
   {
      final MarkupXML markupXML = new MarkupXML(ConstantsXML.MARKUP_PATH_ELEMENT);
      markupXML.addParameter(ConstantsXML.MARKUP_PATH_ELEMENT_pathType, this.pathType.name());
      final StringBuffer stringBuffer = new StringBuffer();
      for(final Point3D point3D : this.points)
      {
         stringBuffer.append(point3D.getX());
         stringBuffer.append(' ');
         stringBuffer.append(point3D.getY());
         stringBuffer.append(' ');
         stringBuffer.append(point3D.getZ());
         stringBuffer.append(' ');
      }
      markupXML.setText(stringBuffer.toString());
      return markupXML;
   }
}