/**
 * Project : JHelpEngine<br>
 * Package : jhelp.engine.util<br>
 * Class : NodeComparator<br>
 * Date : 27 janv. 2009<br>
 * By JHelp
 */
package jhelp.engine.util;

import jhelp.engine.Node;

import java.util.Comparator;

/**
 * Compare two nodes in Z Order <br>
 * <br>
 * Last modification : 27 janv. 2009<br>
 * Version 0.0.0<br>
 * 
 * @author JHelp
 */
public class NodeComparatorZorder
      implements Comparator<Node>
{
   /** Node comparator Z-order singleton */
   public static final NodeComparatorZorder NODE_COMPARATOR_Z_ORDER = new NodeComparatorZorder();

   /**
    * Constructs NodeComparator
    */
   private NodeComparatorZorder()
   {
   }

   /**
    * Compare two nodes with there Z-order
    * 
    * @param node1
    *           Node 1
    * @param node2
    *           Node 2
    * @return Result
    * @see Comparator#compare(Object, Object)
    */
   @Override
   public int compare(final Node node1, final Node node2)
   {
      if(Math3D.equal(node1.zOrder, node2.zOrder) == true)
      {
         return 0;
      }
      if(node1.zOrder < node2.zOrder)
      {
         return -1;
      }
      return 1;
   }

}
