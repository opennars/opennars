package nars.tuprolog.gui.spyframe;

/**Defines the construction of a tree.
 * @param <T> the type of some thing to make a tree out of.
 * @author franz.beslmeisl at googlemail.com
 */
public interface ToTree<T>{
  /**Construct a tree out of some thing.
   * @param thing to make the tree out of.
   * @return root of the tree.
   */
  Node makeTreeFrom(T thing);
}
