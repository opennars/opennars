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
package objenome.solver.evolve;

import objenome.op.Node;
import objenome.op.Variable;
import objenome.solver.evolve.GPContainer.GPKey;

import java.util.Map;
import java.util.Objects;

/**
 * An TypedOrganism is a candidate solution which uses a strongly
 * typed tree representation to represent a computer program. This class
 * provides several convenient methods for obtaining information about the
 * program tree (such as {@link #size()} and {@link #depth()}), but more
 * information is available directly from the tree. Use {@link #getRoot()} to
 * get access to the tree.
 *
 * <p>
 * Note: this class has a natural ordering that may be inconsistent with
 * <code>equals</code>.
 *
 * @since 2.0
 */
public class TypedOrganism<X extends Node,Y> extends AbstractOrganism {


    /**
     * The key for setting and retrieving the set of nodes that individuals are
     * constructed from
     */
    public static final GPKey<Node[]> SYNTAX = new GPKey<>();

    /**
     * The key for setting and retrieving the required data-type for the root
     * node
     */
    public static final GPKey<Class<?>> RETURN_TYPE = new GPKey<>();

    /**
     * The key for setting and retrieving the maximum depth setting for program
     * trees
     */
    public static final GPKey<Integer> MAXIMUM_DEPTH = new GPKey<>();

    // The root node of the program tree
    private Node<X,Y> root;
    
    private transient Class dataType; //caches data type
    private Map<String, ? extends Variable> vars = null;

    /**
     * Constructs an individual represented by a strongly typed tree, with a
     * <code>null</code> root node
     */
    public TypedOrganism() {
        this(null);
    }

    /**
     * Constructs an individual represented by a strongly typed tree, where
     * <code>root</code> is the root node of the tree
     *
     * @param root the <code>Node</code> to set as the root
     */
    public TypedOrganism(Node root) {
        this.root = root;
    }

    /**
     * Evaluates the strongly typed program tree this individual represents and
     * returns the value returned from the root. If no root node has been set
     * then an exception will be thrown.
     *
     * @return the result of evaluating the program tree
     */
    public Y evaluate() {
        return root.evaluate();
    }

    public double eval() {
        return root.asDouble();
    }

    /**
     * Returns the <code>Node</code> that is set as the root of the program tree
     *
     * @return the root node of the program tree.
     */
    public Node<X,Y> getRoot() {
        return root;
    }

    /**
     * Replaces the <code>Node</code> that is set as the root of the program
     * tree
     *
     * @param root the <code>Node</code> to set as the root
     */
    public void setRoot(Node<X,Y> root) {
        dataType = null;        
        this.root = root;
    }

    /**
     * Returns the <i>n</i>th node in the program tree. The tree is traversed in
     * pre-order (depth-first), indexed from 0 so that the root node is at index
     * 0.
     *
     * @param index index of the node to return
     * @return the node at the specified index
     * @throws IndexOutOfBoundsException if the index is out of range (index < 0
     *         || index >= getLength())
     */
    public Node getNode(int index) {
        if (index >= 0) {
            return root.getNode(index);
        } else {
            throw new IndexOutOfBoundsException("attempt to get node at negative index");
        }
    }

    /**
     * Replaces the node at the specified position in the program tree with the
     * specified node.
     *
     * @param index index of the node to be replaced
     * @param node node to be set at the specified position
     * @return the node previously at this position
     * @throws IndexOutOfBoundsException if the index is out of range (index < 0
     *         || index >= getLength())
     */
    public Node setNode(int index, Node node) {
        dataType = null;

        if (index > 0) {
            return root.setNode(index, node);
        } else if (index == 0) {
            Node old = getRoot();
            setRoot(node);
            return old;
        } else {
            // We rely on Node to throw exception if index >= length. It's too
            // expensive to check here.
            throw new IndexOutOfBoundsException("attempt to set node at negative index");
        }
    }

    @Override
    public void normalize() {
        Node nn = getRoot().normalize();
        if (nn!=getRoot()) {
            System.err.println(getRoot() + " normalized to " + nn);
        }
        setRoot(nn);
    }

    /**
     * Returns the maximum depth of the program tree. The depth of a tree is
     * defined as the length of the path from the root to the deepest node in
     * the tree. For a tree with just one node (the root), the depth is 0.
     *
     * @return the maximum depth of the program tree
     */
    public int depth() {
        return getRoot().depth();
    }

    /**
     * Returns the total number of nodes in the program tree
     *
     * @return the number of nodes in the program tree.
     */
    public int size() {
        return getRoot().length();
    }

    /**
     * Returns the data-type of the values returned by the program tree
     *
     * @return the object <code>Class</code> of the values returned
     */
    public Class<?> dataType() {
        if (dataType==null)
            dataType = getRoot().dataType();
        return dataType;
    }

    /**
     * Creates and returns a copy of this program. The copied individual has a
     * deep clone of the program tree. The clone is not assigned this
     * individual's fitness.
     *
     * @return a clone of this <code>STGPIndividual</code> instance
     */
    @Override
    public TypedOrganism<X,Y> clone() {
        TypedOrganism clone = (TypedOrganism) super.clone();

        // Deep copy node tree
        clone.root = root == null ? null : root.clone();

        return clone;
    }

    /**
     * Returns a string representation of this individual. The string
     * representation is the Epox source code of the program tree.
     *
     * @return a string representation of this individual
     */
    @Override
    public String toString() {
        return root == null ? null : getFitness() + " " + root;
    }

    /**
     * Compares the given object to this instance for equality. Equivalence is
     * defined as them both being instances of <code>STGPIndividual</code> and
     * having equal program trees according to
     * <code>getRoot().equals(obj)</code> (or if both root nodes are
     * <code>null</code>).
     *
     * @param obj an object to be compared for equivalence.
     * @return true if this individual is equivalent to the specified object and
     * false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        boolean equal = false;
        if ((obj instanceof TypedOrganism)) {
            TypedOrganism p = (TypedOrganism) obj;
            if (Objects.equals(root, p.root)) {
                equal = true;
            }
        }

        return equal;
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value for this object
     */
    @Override
    public int hashCode() {
        int hash = 1;
        hash = hash * 13 + (root == null ? 0 : root.hashCode());

        return hash;
    }

    /**
     * Compares this individual to another based on their fitness. It returns a
     * negative integer, zero, or a positive integer as this instance represents
     * the quality of an individual that is less fit, equally fit, or more fit
     * than the specified object. The individuals do not need to be of the same
     * object type, but must have non-null, comparable <code>Fitness</code>
     * instances.
     *
     * @param other an individual to compare against
     * @return a negative integer, zero, or a positive integer as this object is
     * less fit than, equally fit as, or fitter than the specified object
     */
    @Override
    public int compareTo(Organism other) {
        return getFitness().compareTo(other.getFitness());
    }

    public Variable var(String id) {
        if (vars == null) {
            vars = getRoot().newVariableMap();
        }

        Variable x = vars.get(id);
        if (x == null) {
            //create a dummy variable so the user wont throw NPE's even though the variable is not used
            return Variable.make(id, Object.class);
        }
        return x;
    }

}
