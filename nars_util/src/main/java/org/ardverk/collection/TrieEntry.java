package org.ardverk.collection;

/**
 *  A {@link Trie} is a set of {@link TrieEntry} nodes
 */
public class TrieEntry<K,V> extends AbstractTrie.BasicEntry<K, V> {

  private static final long serialVersionUID = 4596023148184140013L;

  /** The index this entry is comparing. */
  protected int bitIndex;

  /** The parent of this entry. */
  protected TrieEntry<K,V> parent;

  /** The left child of this entry. */
  protected TrieEntry<K,V> left;

  /** The right child of this entry. */
  protected TrieEntry<K,V> right;

  /** The entry who uplinks to this entry. */
  protected TrieEntry<K,V> predecessor;

  public TrieEntry(K key, V value, int bitIndex) {
    super(key, value);

    this.bitIndex = bitIndex;

    this.parent = null;
    this.left = this;
    this.right = null;
    this.predecessor = this;
  }

  /**
   * Whether or not the entry is storing a key.
   * Only the root can potentially be empty, all other
   * nodes must have a key.
   */
  public boolean isEmpty() {
    return key == null;
  }

  /**
   * Neither the left nor right child is a loopback
   */
  public boolean isInternalNode() {
    return left != this && right != this;
  }

  /**
   * Either the left or right child is a loopback
   */
  public boolean isExternalNode() {
    return !isInternalNode();
  }

  @Override
  public String toString() {
    StringBuilder buffer = new StringBuilder();

    if (bitIndex == -1) {
      buffer.append("RootEntry(");
    } else {
      buffer.append("Entry(");
    }

    buffer.append("key=").append(getKey()).append(" [").append(bitIndex).append("], ");
    buffer.append("value=").append(getValue()).append(", ");
    //buffer.append("bitIndex=").append(bitIndex).append(", ");

    if (parent != null) {
      if (parent.bitIndex == -1) {
        buffer.append("parent=").append("ROOT");
      } else {
        buffer.append("parent=").append(parent.getKey()).append(" [").append(parent.bitIndex).append(']');
      }
    } else {
      buffer.append("parent=").append("null");
    }
    buffer.append(", ");

    if (left != null) {
      if (left.bitIndex == -1) {
        buffer.append("left=").append("ROOT");
      } else {
        buffer.append("left=").append(left.getKey()).append(" [").append(left.bitIndex).append(']');
      }
    } else {
      buffer.append("left=").append("null");
    }
    buffer.append(", ");

    if (right != null) {
      if (right.bitIndex == -1) {
        buffer.append("right=").append("ROOT");
      } else {
        buffer.append("right=").append(right.getKey()).append(" [").append(right.bitIndex).append(']');
      }
    } else {
      buffer.append("right=").append("null");
    }
    buffer.append(", ");

    if (predecessor != null) {
      if(predecessor.bitIndex == -1) {
        buffer.append("predecessor=").append("ROOT");
      } else {
        buffer.append("predecessor=").append(predecessor.getKey()).append(" [").append(predecessor.bitIndex).append(']');
      }
    }

    buffer.append(')');
    return buffer.toString();
  }
}
