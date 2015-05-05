package nars.tuprolog.gui.spyframe;


import javax.swing.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

/** Representation of a tree with spacing. Every node is centered above its
 * children nodes. To assure this the spacing can only be changed by remeasuring
 * the whole tree.
 *
 * @param <T> type of the structures out of which trees shall be made.
 * @author franz.beslmeisl at googlemail.com
 */
public class Tree<T> extends JComponent{

	private static final long serialVersionUID = 1L;
public static final Font defaultfont=new Font(Font.SANS_SERIF, Font.PLAIN, 12);
  /**The totree to consult for constructing the tree.*/
  protected ToTree<T> totree;
  /** The font to be used for rendering the tree nodes.*/
  protected Font font;
  /** The root of the tree.*/
  protected Node root;
  /** Horizontal space between neighboring nodes.*/
  protected float xspace;
  /** Vertical space between neighboring nodes.*/
  protected float yspace;
  /** Space between text and border of a node.*/
  protected float border;
  /** Height of text in a node.*/
  protected float textheight;
  /** Number of lines in the tree.*/
  protected int levels;

  //The following attributes are only for timing optimization
  /** Facts about font geometry (distortion forbidden).*/
  private FontRenderContext frc=new FontRenderContext(null, true, true);
  /** Height of most letters above the baseline.*/
  private int asc;
  /** One rectangle that will be reused for all drawing operations.*/
  private Rectangle2D.Float rect=new Rectangle2D.Float();
  /** One line that will be reused for all drawing operations.*/
  private Line2D.Float line=new Line2D.Float();

  /**Creates a component to display a tree.
   * @param totree to consult for constructing the tree.
   */
  public Tree(ToTree<T> treemaker){this.totree=treemaker;}

  /**Creates a component to display a tree.
   * @param totree to consult for constructing the tree.
   * @param str the structure to be displayed as a tree.
   */
  public Tree(ToTree<T> treemaker, T str){
    this(treemaker);
    setStructure(str);
  }

  /**Sets a new structure to be displayed by this component.
   * @param str the structure to be displayed as a tree.
   */
  public final void setStructure(T str){
    root=totree.makeTreeFrom(str);
    setViewParams(5, 5, 2, null);
  }

  /**Sets the horizontal spacing and remeasures the tree nodes. Changes will
   * be made to the attributes @tlink{xspace}, @tlink{border}
   * and eventually @tlink{font} and @tlink{textheight}.
   * 
   * @param xspace the new horizontal space between two closest possible nodes.
   * @param yspace the new vertical space between two closest possible nodes.
   * @param border the new distance between text and border inside the nodes.
   * @param f the new font. If <code>null</code> is passed the font will
   *   not be changed.
   */
  public final void setViewParams(float xspace, float yspace, float border, Font f){
    this.xspace=xspace;
    this.yspace=yspace;
    this.border=border;
    if(f!=null) font=f;
    remeasure();
  }

  /**Remeasures the tree nodes.
   * @return width of the whole tree of term nodes.
   */
  public float remeasure(){
    if(font==null) font=defaultfont;
    textheight=(float)font.getStringBounds("Ij", frc).getHeight();
    levels=0;
    if(root==null) return 0;
    float width=pose(root, -xspace, 0)-xspace;
    setPreferredSize(new Dimension(1+(int)width, 1+(int)height()));
    invalidate();
    repaint();
    return width;
  }

  /**The height of the whole tree touching the outer bounds of the component.
   * @return the total height.
   */
  public float height(){
    return levels==0?0:levels*(yspace+2*border+textheight)-yspace;
  }

  /** Measures recursively a subtree the root of which is this and centers the
   * this above the children.
   * @param node the node to be posed.
   * @param xpos rightmost position of the neigbor to the left.
   * @param level logical depth of this node in the whole tree. (Zero-based.)
   * @return width of this subtree including one @tlink{xspacing} left of each
   * node. Two cases are considered: The children are together wider than the
   * node itself or they are not.
   */
  protected float pose(Node node, float xpos, int level){
    if(node==null) return 0;
    if(level>=levels) levels=level+1;
    node.x=xpos+xspace;
    node.y=level*(textheight+2*border+yspace);
    node.w=2*border+(float)font.getStringBounds(node.text, frc).getWidth();
    float nodew=xspace+node.w;
    float kidsw=0;
    for(Node n: node.kids)
      kidsw+=pose(n, xpos+kidsw, level+1);
    if(kidsw>=nodew){//move this to the right by half the difference
      node.x+=(kidsw-nodew)/2;
      return kidsw;
    } else{//move the kids to the right by half the difference
      for(Node n: node.kids) n.xShift((nodew-kidsw)/2);
      return nodew;
    }
  }

  @Override
  public void paintComponent(Graphics g){
    if(root!=null){
      Graphics2D g2=(Graphics2D)g;
      g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
          RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
          RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setFont(font);
      asc=g2.getFontMetrics().getAscent();
      rect.height=2*border+textheight;
      paintTree(g2, root);
    }
  }

  /**Displays recursively the subtree rooted at the specified node.
   * The connections to the children go from the center of the root to the upper
   * middle of the respective child node.
   * 
   * @param g2 the graphics to draw on.
   * @param node root of the tree to be drawn.
   */
  protected void paintTree(Graphics2D g2, Node node){
    for(Node kid: node.kids){
      paintTree(g2, kid);
      line.x1=node.x+node.w/2;
      line.y1=node.y+border+textheight/2;
      g2.setColor(Color.BLACK);
      g2.draw(line);
    }
    line.x2=node.x+node.w/2;
    line.y2=node.y;
    paintNode(g2, node);
  }

  /**Displays this node with colored border and text.
   * @param g2 the graphics to draw on.
   * @param node to be painted.
   */
  public void paintNode(Graphics2D g2, Node node){
    rect.setFrame(node.x, node.y, node.w, rect.height);
    g2.setColor(node.bgcolor);
    g2.fill(rect);
    g2.setColor(node.bordercolor);
    g2.draw(rect);
    g2.setColor(node.textcolor);
    g2.drawString(node.text, node.x+border, node.y+border+asc);
  }
}