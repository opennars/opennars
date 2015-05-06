/*
 * Copyright 2007-2013
 * Licensed under GNU Lesser General Public License
 * 
 * This file is part of EpochX: genetic programming software for research
 * 
 * EpochX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * EpochX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with EpochX. If not, see <http://www.gnu.org/licenses/>.
 * 
 * The latest version is available from: http://www.epochx.org
 */
package objenome.op;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import objenome.util.TypeUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;

/**
 * A <code>Node</code> is a vertex in a tree structure which represents a
 * program. A node can be thought of as an expression in a computer programming
 * language. Evaluating a node will involve evaluating any children and
 * optionally returning a value.
 *
 * Subclasses of <code>Node</code> should ensure they call the superclass
 * constructor with all child nodes so information such as the arity of the node
 * can be maintained. Concrete subclasses must also implement
 * <code>evaluate()</code> to evaluate the expression represented by the tree.
 * Nodes which support mixed type arguments, or terminal nodes with no arguments
 * must also override the <code>getReturnType(Class&lt;?&gt;)</code> method to
 * indicate their return type. The <code>clone</code> and
 * <code>newInstance</code> methods are also heavily used, so implementations
 * should ensure this classes implementations are sufficient or override as
 * necessary.
 *
 * @since 2.0
 */
public abstract class Node<X extends Node, Y extends Object> implements Cloneable {

    // TODO Consider renaming to EpoxNode
    protected X[] children;

    protected Node parent;

    /**
     * Constructs a new <code>Node</code> with the given child nodes. The arity
     * of the node will be the number of child nodes provided. The child nodes
     * may be initially set here as <code>null</code> and replaced before
     * evaluation. Terminal nodes are simply nodes with no children.
     *
     * @param children child nodes to this node
     */
    public Node(X... children) {
        setChildren(children);
    }

    /**
     * Subclasses should implement this method to perform some operation with
     * respect to its children and return a result. If there is no result (for
     * example, because this node has a <code>Void</code> data-type), then
     * <code>null</code> should be returned.
     *
     * @return the result of evaluating the node tree rooted at this node
     */
    public abstract Y evaluate();

    /**
     * Returns a specific child by index
     *
     * @param index the index of the child to be returned, valid indexes run
     * from <code>0</code> to <code>getArity()-1</code>
     * @return the child node at the specified index
     */
    public X getChild(final int index) {
        return children[index];
    }

    /**
     * Returns the parent of this node or <code>null</code> if it is the root
     * node
     *
     * @return the node that this node is a child of
     */
    public Node getParent() {
        return parent;
    }

    /**
     * Returns an array of this node's children. Modifying this array will not
     * change the set of children, but modifying the nodes will alter the nodes
     * of the tree.
     *
     * @return an array of this node's children
     */
    public X[] getChildren() {
        return ArrayUtils.clone(children);
    }

    /**
     * Sets the child nodes of this node. Modifications to this array after
     * being set will not modify the set of child nodes. The number of children
     * set here does not need to match the current arity.
     *
     * @param children the nodes to set as children in order
     */
    public void setChildren(X... children) {
        // Must be careful to maintain the integrity of parent
        this.children = Arrays.copyOf(children, children.length);
    }

    /**
     * Returns the element at the specified position in the node tree, where
     * this node is considered to be the root - that is the 0th node. The tree
     * is traversed in pre-order (depth first).
     *
     * @param n the index of the node to be returned
     * @return the node at the specified position in this node tree
     * @throws IndexOutOfBoundsException if <code>n</code> is out of range
     */
    public Node getNode(int n) {
        if (n >= 0) {
            return getNode(n, 0);
        } else {
            throw new IndexOutOfBoundsException("attempt to get node at negative index");
        }
    }

    /*
     * Recursive helper for the public getNode(int)
     */
    private Node getNode(int n, int current) {
        if (n == current) {
            return this;
        }

        Node node = null;
        for (Node child : children) {
            int childLength = child.length();

            // Only look at the subtree if it contains the right range of nodes.
            if (n <= childLength + current) {
                node = child.getNode(n, current + 1);
                if (node != null) {
                    break;
                }
            }

            current += childLength;
        }

        // If node is null now then the index did not exist within any children.
        if (node == null) {
            throw new IndexOutOfBoundsException("attempt to get node at index >= length");
        }

        return node;
    }

    /**
     * Replaces the node at the specified position in this node tree, where this
     * node is considered to be the root node - that is, the 0th node. The tree
     * is traversed in pre-order (depth first). It is not possible to set the
     * 0th node, since it does not make sense for an object to be able to
     * replace itself.
     *
     * @param n the index of the node to replace
     * @param newNode the node to be stored at the specified position
     * @throws IndexOutOfBoundsException if <code>n</code> is out of range
     */
    public Node setNode(int n, Node newNode) {
        if (n > 0) {
            return setNode(n, newNode, 0);
        } else if (n == 0) {
            throw new IndexOutOfBoundsException("attempt to set node at index 0, cannot replace self");
        } else {
            throw new IndexOutOfBoundsException("attempt to set node at negative index");
        }
    }

    /*
     * Recursive helper for the public setNode(int, Node)
     */
    private Node setNode(int n, Node newNode, int current) {
        int arity = getArity();
        for (int i = 0; i < arity; i++) {
            if (current + 1 == n) {
                Node old = getChild(i);
                setChild(i, (X)newNode);
                return old;
            }

            Node child = getChild(i);
            int childLength = child.length();

            // Only look at the subtree if it contains the right range of nodes
            if (n <= childLength + current) {
                return child.setNode(n, newNode, current + 1);
            }

            current += childLength;
        }

        throw new IndexOutOfBoundsException("attempt to set node at index >= length");
    }

    /**
     * Returns the index of the nth non-terminal node, where this node is
     * considered to be the root - that is the 0th node. The tree's nodes are
     * counted in pre-order (depth first) to locate the nth function, and return
     * its index within all nodes. Will throw an exception if the index is out
     * of bounds, which will be the case for all indexes when called on a
     * terminal node.
     *
     * @param n the non-terminal to find the index of
     * @return the index of the nth non-terminal node
     * @throws IndexOutOfBoundsException if <code>n</code> is out of range
     */
    public int nthNonTerminalIndex(int n) {
        int index = nthNonTerminalIndex(n, 0, 0, this);

        if (index < 0) {
            throw new IndexOutOfBoundsException("attempt to get function node index at index out of range");
        }

        return index;
    }

    /*
     * Recursive helper function for nthNonTerminalIndex
     */
    private int nthNonTerminalIndex(int n, int functionCount, int nodeCount, Node current) {
        if (current.isNonTerminal() && (n == functionCount)) {
            return nodeCount;
        }

        int result = -1;
        for (Node child : current.children) {
            int noNodes = child.length();
            int noFunctions = child.countNonTerminals();

            // Only look at the subtree if it contains the right range of nodes
            if (n <= noFunctions + functionCount) {
                int childResult = nthNonTerminalIndex(n, functionCount + 1, nodeCount + 1, child);
                if (childResult != -1) {
                    return childResult;
                }
            }

            // Skip the correct number of nodes from the subtree
            functionCount += noFunctions;
            nodeCount += noNodes;
        }

        return result;
    }

    /**
     * Returns the index of the nth terminal node, where this node is considered
     * to be the root - that is the 0th node. The tree's nodes are counted in
     * pre-order (depth first) to locate the nth terminal, and return its index
     * within all nodes.
     *
     * @param n the terminal to find the index of
     * @return the index of the nth terminal node
     * @throws IllegalArgumentException if <code>n</code> is out of bounds
     */
    public int nthTerminalIndex(int n) {
        int index = nthTerminalIndex(n, 0, 0, this);

        if (index < 0) {
            throw new IndexOutOfBoundsException("attempt to get terminal node index at index out of range");
        }

        return index;
    }

    /*
     * Recursive helper function for nthTerminalIndex
     */
    private int nthTerminalIndex(int n, int terminalCount, int nodeCount, Node current) {
        if (current.getArity() == 0) {
            if (n == terminalCount++) {
                return nodeCount;
            }
        }

        int result = -1;
        for (Node child : children) {
            int noNodes = child.length();
            int noTerminals = child.countTerminals();

            // Only look at the subtree if it contains the right range of nodes
            if (n <= noTerminals + terminalCount) {
                int childResult = nthTerminalIndex(n, terminalCount, nodeCount + 1, child);
                if (childResult != -1) {
                    return childResult;
                }
            }

            // Skip the correct number of nodes from the subtree
            terminalCount += noTerminals;
            nodeCount += noNodes;
        }

        return result;
    }

    /**
     * Retrieves all the nodes in the node tree at a specified depth from this
     * current node. This node is considered to be at depth zero.
     *
     * @param depth the specified depth of the nodes to return
     * @return a <code>List</code> of all the nodes at the specified depth
     */
    public List<Node> nodesAtDepth(int depth) {
        List<Node> nodes = new ArrayList<>((depth + 1) * 3);
        if (depth >= 0) {
            nodesAtDepth(nodes, depth, 0);
        } else {
            throw new IndexOutOfBoundsException("attempt to get nodes at negative depth");
        }

        if (nodes.isEmpty()) {
            throw new IndexOutOfBoundsException("attempt to get nodes at depth greater than maximum depth");
        }

        return nodes;
    }

    /*
     * A helper function for nodesAtDepth(int), to recurse down the node
     * tree and populate the nodes array when at the correct depth.
     */
    private void nodesAtDepth(List<Node> nodes, int d, int current) {
        if (d == current) {
            nodes.add(this);
        } else {
            for (Node child : children) {
                // Get the nodes at the right depth down each branch
                child.nodesAtDepth(nodes, d, current + 1);
            }
        }
    }

    /**
     * Replaces the child node at the specified index with the given node
     *
     * @param index the index of the child to replace, from <code>0</code> to
     * <code>getArity()-1</code>
     * @param child the child node to be stored at the specified position
     */
    public void setChild(int index, X child) {
        children[index] = child;

        if (child != null) {
            child.parent = this;
        }
    }

    /**
     * Returns the number of immediate children this <code>Node</code> has. This
     * is effectively the number of inputs the node has. A node with arity zero,
     * is considered to be a terminal node.
     *
     * @return the number of children required by this node
     */
    public int getArity() {
        
        return children.length;
    }

    /**
     * Returns a count of the terminal nodes in this node tree
     *
     * @return the number of terminal nodes in this node tree
     */
    public int countTerminals() {
        if (isTerminal()) {
            return 1;
        } else {
            int result = 0;
            for (int i = 0; i < getArity(); i++) {
                result += getChild(i).countTerminals();
            }
            return result;
        }
    }

    /**
     * Returns a count of the unique terminal nodes in the node tree below this
     * node
     *
     * @return the number of unique terminal nodes in this node tree
     */
    public int countDistinctTerminals() {
        List<Node> terminals = listTerminals();

        // Remove duplicates.
        Set<Node> terminalHash = new HashSet<>(terminals);

        return terminalHash.size();
    }

    /**
     * Returns a list of all the terminal nodes in this node tree
     *
     * @return a <code>List</code> of all the terminal nodes in this node tree
     */
    public List<Node> listTerminals() {
        List<Node> terminals = new ArrayList<>();

        int arity = getArity();
        if (isTerminal()) {
            terminals.add(this);
        } else {
            for (int i = 0; i < arity; i++) {
                terminals.addAll(getChild(i).listTerminals());
            }
        }
        return terminals;
    }

    /**
     * Returns a count of the non-terminal nodes in this node tree
     *
     * @return the number of non-terminal nodes in this node tree
     */
    public int countNonTerminals() {
        if (isTerminal()) {
            return 0;
        } else {
            int result = 1;
            for (int i = 0; i < getArity(); i++) {
                result += getChild(i).countNonTerminals();
            }
            return result;
        }
    }

    /**
     * Returns a count of the unique non-terminal nodes in this node tree
     *
     * @return the number of unique non-terminal nodes in this node tree
     */
    public int countDistinctNonTerminals() {
        List<Node> nonTerminals = listNonTerminals();

        // Remove duplicates. Cannot use equals because that compares children
        List<String> identifiers = new ArrayList<>();
        for (Node f : nonTerminals) {
            String name = f.getIdentifier();
            if (!identifiers.contains(name)) {
                identifiers.add(name);
            }
        }

        return identifiers.size();
    }

    /**
     * Returns a list of all the non-terminal nodes in this node tree
     *
     * @return a <code>List</code> of all the non-terminal nodes in this node
     * tree
     */
    public List<Node> listNonTerminals() {
        List<Node> nonTerminals = new ArrayList<>();

        if (isNonTerminal()) {
            // Add this node as a function and search its child nodes.
            nonTerminals.add(this);

            for (int i = 0; i < getArity(); i++) {
                nonTerminals.addAll(getChild(i).listNonTerminals());
            }
        }

        return nonTerminals;
    }

    /**
     * Returns the depth of deepest node in the node tree, given that this node
     * is at depth zero
     *
     * @return the depth of the deepest node in the node tree
     */
    public int depth() {
        return depth(this, 0, 0);
    }

    /*
     * A private helper function for depth() which recurses down the node
     * tree to determine the deepest node's depth
     */
    private int depth(Node rootNode, int currentDepth, int depth) {
        if (currentDepth > depth) {
            depth = currentDepth;
        }

        int arity = rootNode.getArity();
        if (arity > 0) {
            for (int i = 0; i < arity; i++) {
                Node childNode = rootNode.getChild(i);
                depth = depth(childNode, (currentDepth + 1), depth);
            }
        }
        return depth;
    }

    /**
     * Returns the number of nodes in the node tree
     *
     * @return the number of nodes in the node tree
     */
    public int length() {
        return length(this, 0);
    }

    /*
     * A private recursive helper function for length() which traverses the
     * the node tree counting the number of nodes
     */
    private int length(Node rootNode, int length) {
        length++;

        int arity = rootNode.getArity();
        if (arity > 0) {
            for (int i = 0; i < arity; i++) {
                Node childNode = rootNode.getChild(i);
                length = length(childNode, length);
            }
        }
        return length;
    }

    /**
     * Should be implemented to return an indentifier for this node. For
     * functions, where this is effectively the function name, this would
     * normally be unique within the given problem.
     *
     * @return a <code>String</code> identifier for this node.
     */
    public abstract String getIdentifier();

    /**
     * Returns the data-type of this node based on the child nodes that are
     * currently set. If any of this node's child nodes are currently
     * <code>null</code>, or their data-types are invalid, then the return type
     * will also be <code>null</code>.
     *
     * @return the return type of this node or <code>null</code> if any of its
     * children remain unset or are of an invalid data-type
     */
    public final Class<?> dataType() {
        Class<?>[] argTypes = new Class<?>[getArity()];
        final int arity = getArity();
        for (int i = 0; i < arity; i++) {
            Node child = getChild(i);
            if (child != null) {
                argTypes[i] = child.dataType();
            } else {
                return null;
            }
        }
        return dataType(argTypes);
    }

    /**
     * Returns this node's return type given the provided input data-types. The
     * default implementation for a non-terminal is that the node will support
     * the closure requirement - the return type will be the widest of the input
     * types, or <code>null</code> if they are not compatible. The default
     * return value for a terminal is <code>Void</code>. Mixed type non-terminal
     * nodes and most terminal nodes should override this method. If the input
     * types are invalid then <code>null</code> should be returned.
     *
     * @param inputTypes the set of input data-types for which to get the return
     * type.
     * @return the return type of this node given the provided input types, or
     * null if the set of input types is invalid.
     */
    public Class dataType(Class... inputTypes) {
        if (isTerminal()) {
            return Void.class;
        } else {
            // Either the widest type or null if not valid
            return TypeUtil.getSuper(inputTypes);
        }
    }

    /**
     * Returns <code>true</code> if this node has an arity of greater than
     * <code>0</code>.
     *
     * @return <code>true</code> if this node is a non-terminal, and
     * <code>false</code> otherwise.
     */
    public boolean isNonTerminal() {
        return (getArity() > 0);
    }

    /**
     * Returns <code>true</code> if this node has an arity of <code>0</code>
     *
     * @return <code>true</code> if this node is a terminal, and
     * <code>false</code> otherwise
     */
    public boolean isTerminal() {
        return (getArity() == 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = getIdentifier().hashCode();
        for (final Node child : children) {
            if (child != null) {
                result = 37 * result + child.hashCode();
            }
        }
        return result;
    }

    /**
     * Creates a deep copy of this node and its node tree. Each child node will
     * be cloned. Use this method for copying a whole node tree. Some
     * implementations of this class may need to override this method.
     *
     * @return a copy of this <code>Node</code> and its children
     */
    @Override
    public Node clone() {
        try {
            Node clone = (Node) super.clone();

            clone.children = children.clone();
            for (int i = 0; i < children.length; i++) {
                clone.children[i] = children[i]; // TODO Don't think we need
                // this line.
                if (clone.children[i] != null) {
                    clone.children[i] = clone.children[i].clone();
                }
            }

            return clone;
        } catch (final CloneNotSupportedException e) {
            assert false;
            // This shouldn't ever happen - if it does then everythings going to
            // blow up anyway.
        }
        return null;
    }

    /**
     * Constructs a new instance of this node-type. Rather than copying the node
     * tree, this node is copied without children. In the case of many terminal
     * nodes the bahaviour of this method will be the same as the clone method.
     * By default this method will simply return a new instance of the same type
     * in the same manner as clone, but with all children removed. Note that
     * there is no requirement for an implementation to return a different
     * instance.
     *
     * @return a copy of this <code>Node</code> with all children removed
     */
    public Node newInstance() {
        try {
            Node n = (Node) super.clone();
            n.children = new Node[children.length];
            return n;
        } catch (final CloneNotSupportedException e) {
            assert false;
        }

        return null;
    }

    /**
     * Compares an this node to another object for equality. Two nodes may be
     * considered equal if they have equal arity, equal identifiers, and their
     * children are also equal (and in the same order). Some nodes may wish to
     * enforce a stricter contract.
     *
     * @param obj {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        boolean equal = true;

        if (obj instanceof Node) {
            Node n = (Node) obj;

            if (n.getArity() != getArity()) {
                equal = false;
            } else if (!getIdentifier().equals(n.getIdentifier())) {
                equal = false;
            } else {
                for (int i = 0; (i < n.getArity()) && equal; i++) {
                    Node thatChild = n.getChild(i);
                    Node thisChild = getChild(i);

                    equal = ObjectUtils.equals(thisChild, thatChild);
                }
            }
        } else {
            equal = false;
        }
        return equal;
    }

    /**
     * Returns a string representation of this node. The default implementation
     * is output of the form:
     *
     * <pre>
     * identifier(children)
     * </pre>
     *
     * where <code>identifier</code> is the node's identifier as returned by
     * <code>getIdentifier</code>, and <code>children</code> is a space
     * separated list of child nodes, according to their <code>toString</code>
     * representation.
     *
     * @return a string representation of this node and its children
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(getIdentifier());
        builder.append('(');
        for (int i = 0, n = children.length; i < n; i++) {
            Node c = children[i];
            if (i != 0) {
                builder.append(' ');
            }

            if (c == null) {
                builder.append('X');
            } else {
                builder.append(c.toString());
            }
        }
        builder.append(')');
        return builder.toString();
    }

    public boolean allConstDescendants() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
