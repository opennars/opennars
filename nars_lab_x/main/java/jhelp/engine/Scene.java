/**
 */
package jhelp.engine;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;
import jhelp.engine.io.ConstantsXML;
import jhelp.engine.util.NodeComparatorZorder;
import jhelp.engine.util.Tool3D;
import jhelp.xml.MarkupXML;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Stack;

/**
 * Scene 3D <br>
 * <br>
 * Last modification : 25 janv. 2009<br>
 * Version 0.0.1<br>
 * 
 * @author JHelp
 */
public class Scene
{
   /** Background color'd blue */
   private float      blueBackground;
   /** Actual camera */
   private Camera     camera;
   /** Background color'd green */
   private float      greenBackground;
   /** List of nodes */
   private Node[]     nodeList;
   /** Background color'd red */
   private float      redBackground;
   /** Root node */
   private final Node root;

   /**
    * Constructs empty Scene
    */
   public Scene()
   {
      this.camera = new Camera();
      this.redBackground = this.greenBackground = this.blueBackground = 1f;
      this.root = new Node();
      this.root.nodeName = "ROOT";
   }

   /**
    * Draw the background
    * 
    * @param gl
    *           OpenGL context
    */
   void drawBackground(final GL gl)
   {
      gl.glClearColor(this.redBackground, this.greenBackground, this.blueBackground, 1f);
   }

   /**
    * Render a node in picking mode
    * 
    * @param node
    *           Node to render
    * @param gl
    *           OpenGL context
    * @param glu
    *           OpenGL utilities
    */
   void renderPickingUV(final Node node, final GL2 gl, final GLU glu)
   {
      this.root.renderPickingUV(node, gl, glu);
   }

   /**
    * Render the scene
    * 
    * @param gl
    *           OpenGL context
    * @param glu
    *           GLU context
    * @param sceneRenderer
    *           Scene renderer
    */
   synchronized void renderTheScene(final GL2 gl, final GLU glu, final JHelpSceneRenderer sceneRenderer)
   {
      Node node;
      // Get the node's list
      if(this.nodeList == null)
      {
         final Stack<Node> stack = new Stack<Node>();
         stack.push(this.root);
         final ArrayList<Node> nodes = new ArrayList<Node>();
         while(stack.isEmpty() == false)
         {
            node = stack.pop();
            nodes.add(node);
            for(final Node child : node.children)
            {
               stack.push(child);
            }
         }
         this.nodeList = new Node[nodes.size()];
         this.nodeList = nodes.toArray(this.nodeList);
      }

      // Compute Z-Orders
      int length = this.nodeList.length;
      for(int i = 0; i < length; i++)
      {
         node = this.nodeList[i];
         node.zOrder = node.getUnProjection(node.getCenter()).getZ();
      }
      length--;

      // Sort nodes
      Arrays.sort(this.nodeList, NodeComparatorZorder.NODE_COMPARATOR_Z_ORDER);

      // Draw nodes
      for(; length >= 0; length--)
      {
         node = this.nodeList[length];
         if(node.visible == true)
         {
            gl.glPushMatrix();
            node.matrixRootToMe(gl);
            node.renderSpecific(gl, glu);
            if(node.textureHotspot != null)
            {
               sceneRenderer.showHotspot(gl, glu, node);
            }
            gl.glPopMatrix();
         }
      }
   }

   /**
    * Render the scene in picking mode
    * 
    * @param gl
    *           OpenGL context
    * @param glu
    *           GLU context
    * @param sceneRenderer
    *           Scene renderer
    */
   void renderTheScenePicking(final GL2 gl, final GLU glu, final JHelpSceneRenderer sceneRenderer)
   {
      this.root.renderTheNodePicking(gl, glu, sceneRenderer);
   }

   /**
    * Add node to the scene
    * 
    * @param node
    *           Node to add
    */
   public void add(final Node node)
   {
      this.root.addChild(node);
   }

   /**
    * Flush the scene.<br>
    * Call it when you have add/remove nodes and don't plan to do again it very soon
    */
   public synchronized void flush()
   {
      this.nodeList = null;
   }

   /**
    * Obtain all child node hierarchical with the given name
    * 
    * @param nodeName
    *           Name search
    * @return List of matches nodes
    */
   public ArrayList<Node> getAllNodes(final String nodeName)
   {
      return this.root.getAllNodes(nodeName);
   }

   /**
    * Actual camera
    * 
    * @return Actual camera
    */
   public Camera getCamera()
   {
      return this.camera;
   }

   /**
    * Search throw child hierarchical and return the first node with the given name
    * 
    * @param nodeName
    *           Name search
    * @return Find node
    */
   public Node getFirstNode(final String nodeName)
   {
      return this.root.getFirstNode(nodeName);
   }

   /**
    * A child
    * 
    * @param index
    *           Child index
    * @return The child
    */
   public Node getNode(final int index)
   {
      return this.root.getChild(index);
   }

   /**
    * Children list
    * 
    * @return Children list
    */
   public Iterator<Node> getNodes()
   {
      return this.root.getChildren();
   }

   /**
    * Search the node with a specific picking color
    * 
    * @param color
    *           Picking color
    * @return The node
    */
   public Node getPickingNode(final Color4f color)
   {
      return this.root.getPickingNode(color);
   }

   /**
    * Scene root
    * 
    * @return Scene root
    */
   public Node getRoot()
   {
      return this.root;
   }

   /**
    * Load scene parameter form XML
    * 
    * @param markupXML
    *           Markup to parse
    * @throws Exception
    *            On parsing problem
    */
   public void loadFromXML(final MarkupXML markupXML) throws Exception
   {
      Color4f color4f = Tool3D.getColor4fParameter(markupXML, ConstantsXML.MARKUP_SCENE_background);
      this.redBackground = color4f.getRed();
      this.greenBackground = color4f.getGreen();
      this.blueBackground = color4f.getBlue();
      color4f = null;
      MarkupXML markup = markupXML.obtainChildren(ConstantsXML.MARKUP_NODE).getNextElement();
      this.root.loadFromXML(markup);
      markup = markupXML.obtainChildren(ConstantsXML.MARKUP_CAMERA).getNextElement();
      this.camera.loadFromXML(markup);
      markup = null;
      if(this.camera.lookName != null)
      {
         this.camera.lookAt(this.root.getFirstNode(this.camera.lookName));
         this.camera.lookName = null;
      }
      this.flush();
   }

   /**
    * Change mouse state
    * 
    * @param leftButton
    *           Indicates if left button is down
    * @param rightButton
    *           Indicates if right button is down
    * @param over
    *           Node that mouse id over
    */
   public void mouseState(final boolean leftButton, final boolean rightButton, final Node over)
   {
      Node node = this.root;
      final Stack<Node> stackNodes = new Stack<Node>();
      stackNodes.push(node);
      while(stackNodes.isEmpty() == false)
      {
         node = stackNodes.pop();
         node.mouseState(leftButton, rightButton, node == over);
         for(final Node child : node.children)
         {
            stackNodes.push(child);
         }
      }
   }

   /**
    * Remove node from the scene
    * 
    * @param node
    *           Node to remove
    */
   public void remove(final Node node)
   {
      this.root.removeChild(node);
   }

   /**
    * Remove all nodes from scene
    */
   public void removeAllNodes()
   {
      this.root.removeAllChildren();
   }

   /**
    * Rotate around X axis
    * 
    * @param angleX
    *           Rotation angle
    */
   public void rotateAngleX(final float angleX)
   {
      this.root.rotateAngleX(angleX);
   }

   /**
    * Rotate around Y axis
    * 
    * @param angleY
    *           Rotation angle
    */
   public void rotateAngleY(final float angleY)
   {
      this.root.rotateAngleY(angleY);
   }

   /**
    * Rotate around Z axis
    * 
    * @param angleZ
    *           Rotation angle
    */
   public void rotateAngleZ(final float angleZ)
   {
      this.root.rotateAngleZ(angleZ);
   }

   /**
    * Save scen in XML
    * 
    * @return XML representation
    */
   public MarkupXML saveToXML()
   {
      final MarkupXML markupXML = new MarkupXML(ConstantsXML.MARKUP_SCENE);
      Tool3D.addColor4fParameter(markupXML, ConstantsXML.MARKUP_SCENE_background, new Color4f(this.redBackground, this.greenBackground, this.blueBackground));
      markupXML.addChild(this.root.saveToXML());
      markupXML.addChild(this.camera.saveToXML());
      return markupXML;
   }

   /**
    * Change the rotation on X axis
    * 
    * @param angleX
    *           Rotation angle
    */
   public void setAngleX(final float angleX)
   {
      this.root.setAngleX(angleX);
   }

   /**
    * Change the rotation on Y axis
    * 
    * @param angleY
    *           Rotation angle
    */
   public void setAngleY(final float angleY)
   {
      this.root.setAngleY(angleY);
   }

   /**
    * Change the rotation on Z axis
    * 
    * @param angleZ
    *           Rotation angle
    */
   public void setAngleZ(final float angleZ)
   {
      this.root.setAngleZ(angleZ);
   }

   /**
    * Change background color
    * 
    * @param background
    *           New background color
    */
   public void setBackground(final Color background)
   {
      this.redBackground = background.getRed() / 255f;
      this.greenBackground = background.getGreen() / 255f;
      this.blueBackground = background.getBlue() / 255f;
   }

   /**
    * Change background color
    * 
    * @param red
    *           Red
    * @param green
    *           Green
    * @param blue
    *           Blue
    */
   public void setBackground(final float red, final float green, final float blue)
   {
      this.redBackground = red;
      this.greenBackground = green;
      this.blueBackground = blue;
   }

   /**
    * Change camera
    * 
    * @param camera
    *           New camera
    */
   public void setCamera(final Camera camera)
   {
      if(camera == null)
      {
         throw new NullPointerException("The camera couldn't be null");
      }
      this.camera = camera;
   }

   /**
    * Change scene position
    * 
    * @param x
    *           X
    * @param y
    *           Y
    * @param z
    *           Z
    */
   public void setPosition(final float x, final float y, final float z)
   {
      this.root.setPosition(x, y, z);
   }

   /**
    * Translate the scene
    * 
    * @param x
    *           X
    * @param y
    *           Y
    * @param z
    *           Z
    */
   public void translate(final float x, final float y, final float z)
   {
      this.root.translate(x, y, z);
   }
}