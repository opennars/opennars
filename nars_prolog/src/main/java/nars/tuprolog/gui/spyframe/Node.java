package nars.tuprolog.gui.spyframe;

import java.awt.*;

/**Nodes are the constituing element of a tree.
 * @author franz.beslmeisl at googlemail.com
 */
public class Node{
  /**An always empty array. Needed for convenience and speed.*/
  public static final Node[] empty=new Node[0];
  /** The children of this node. No children may be an empty array or <code>null</code>.*/
  public Node[] kids;
  /** Coordinates of the node's upper left edge (including border).*/
  public float x, y;
  /** The node's width including the border.*/
  public float w;
  /** The displayed text of the node.*/
  public String text;
  public Color textcolor;
  public Color bgcolor;
  public Color bordercolor;

  /**Creates a node with the specified text. Default colors are
   * black for the text, white for the background and red for the border.
   * @param text the text to be displayed.
   */
  public Node(String text){
    if(text==null) text="";
    this.text=text;
    textcolor=Color.BLACK;
    bgcolor=Color.WHITE;
    bordercolor=Color.RED;
    kids=empty;
  }

  /**Creates a node with empty text.*/
  public Node(){this(null);}

  /** Shifts horizontally the subtree the root of which is this.
   * @param dx the amount to shift this to the right.
   */
  protected void xShift(float dx){
    x+=dx;
    for(Node n: kids) n.xShift(dx);
  }
}