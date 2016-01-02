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
package objenome.solver.evolve.grammar;

import java.util.*;

/**
 * One of potentially multiple options that a <code>GrammarRule</code> can
 * resolve to. It contains a sequence of one or more <code>GrammarNode</code>s
 * that together produce a valid mapping for the left-hand side of a grammar
 * rule as represented by <code>GrammarRule</code> instances.
 */
public class GrammarProduction implements Cloneable {

    // The grammar rules and literals in order that make up this production.
    private final List<GrammarNode> grammarNodes;

    // Any key/value attributes which exist upon this production.
    private final Map<String, Object> attributes;

    /**
     * Constructs a production around the specified sequence of
     * <code>GrammarNode</code>s.
     *
     * @param grammarNodes a <code>List</code> of <code>GrammarNode</code>s that
     * provides the mapping sequence for this production.
     */
    public GrammarProduction(List<GrammarNode> grammarNodes) {
        this.grammarNodes = grammarNodes;

        attributes = new HashMap<>();
    }

    /**
     * Constructs a production with no <code>GrammarNode</code>s. A valid
     * production should have one or more GrammarNodes so one or more should be
     * added to this <code>GrammarProduction</code> before use.
     *
     * @see GrammarProduction#addGrammarNode(GrammarNode)
     */
    public GrammarProduction() {
        this(new ArrayList<>());
    }

    /**
     * Appends the specified symbol to the list of <code>GrammarNodes</code> in
     * this production.
     *
     * @param node the rule or literal to be appended to this production.
     * @see GrammarProduction#setGrammarNode(int, GrammarNode)
     */
    public void addGrammarNode(GrammarNode node) {
        grammarNodes.add(node);
    }

    /**
     * Returns a list of the rules and literals that make up this production, in
     * the order that they resolve to.
     *
     * @return the sequence of <code>GrammarNode</code>s that make up this
     * production.
     */
    public List<GrammarNode> getGrammarNodes() {
        return grammarNodes;
    }

    /**
     * Returns the <code>GrammarNode</code> at the specified index in the
     * sequence of rules and literals in this production.
     *
     * @return the <code>GrammarNode</code> at the specified index in this
     * production.
     */
    public GrammarNode getGrammarNode(int index) {
        return grammarNodes.get(index);
    }

    /**
     * Set the <code>GrammarNode</code> at the specified index, overwriting the
     * current occupant.
     *
     * @param index the index of the current <code>GrammarNode</code> to be
     * replaced.
     * @param node the new <code>GrammarNode</code> to place at the specified
     * index in this production.
     * @see GrammarProduction#addGrammarNode(GrammarNode)
     */
    public void setGrammarNode(int index, GrammarNode node) {
        grammarNodes.set(index, node);
    }

    /**
     * Inserts the <code>GrammarNode</code> at the specified index. Shifts the
     * node currently at that position along one, with their indices incremented
     * by one.
     *
     * @param index the index of where to insert the <code>GrammarNode</code>.
     * @param node the <code>GrammarNode</code> to place at the specified index
     * in this production.
     */
    public void addGrammarNode(int index, GrammarNode node) {
        grammarNodes.add(index, node);
    }

    /**
     * Returns the quantity of <code>GrammarNode</code>s in this production.
     *
     * @return the number of <code>GrammarNode</code>s in this production.
     */
    public int getNoGrammarNodes() {
        return grammarNodes.size();
    }

    /**
     * Retrieves the value of the attribute with the given key. If no attribute
     * with that key exists then <code>null</code> is returned.
     *
     * Currently all values that were parsed from a String grammar will be
     * returned here as <code>String</code> objects. In future versions this may
     * change to parse the values into more suitable Object types.
     *
     * @param key the key that identifies the attribute value to return.
     * @return the Object value of the attribute with the given key, or
     * <code>null</code> if no attributes with that key exist on this
     * <code>GrammarProduction</code>.
     */
    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    /**
     * Sets the value of the attribute with the given key. If no attribute with
     * that key exists then a new one will be created, if one does exist then it
     * will be overwritten with the new value.
     *
     * @param key the unique key of the attribute to set.
     * @param value an value of any Object type to store as an attribute under
     * the given key.
     */
    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    /**
     * Retrieve the <code>Set</code> of attribute keys that have been set for
     * this production.
     *
     * @return a <code>Set</code> of all attribute keys that have been set for
     * this production. If no attributes have been set then an empty set will be
     * returned.
     */
    public Set<String> getAttributeKeys() {
        return attributes.keySet();
    }

    /**
     * Calculates and returns the minimum depth required to resolve this
     * production to all <code>GrammarLiteral</code> nodes. The minimum depth of
     * a production is the largest of all the minimum depths of its
     * <code>GrammarNodes</code>.
     *
     * @return the minimum depth required to resolve to all
     * <code>GrammarLiteral</code> nodes.
     */
    public int getMinDepth() {
        int max = 0;
        for (GrammarNode s : grammarNodes) {
            int d = 0;
            if (s instanceof GrammarRule) {
                d = ((GrammarRule) s).getMinDepth();
            }

            if (d > max) {
                max = d;
            }
        }

        return max;
    }

    /**
     * Determines whether this production is recursive. A production is
     * recursive if any of its nodes that are rules (rather than literals) are
     * recursive.
     *
     * @return true if this production is has a <code>GrammarRule</code> that is
     * recursive and false otherwise.
     */
    public boolean isRecursive() {
        boolean recursive = false;

        for (GrammarNode s : grammarNodes) {
            if (s instanceof GrammarRule) {
                recursive = ((GrammarRule) s).isRecursive();
            }

            if (recursive) {
                break;
            }
        }

        return recursive;
    }

    /**
     * Returns a copy of this production which is a new instance of
     * <code>GrammarProduction</code> with the same set of grammar nodes and
     * attributes.
     *
     * @return a <code>GrammarProduction</code> which is a copy of this
     * production.
     */
    @Override
    public GrammarProduction clone() {
        GrammarProduction clone = null;
        try {
            clone = (GrammarProduction) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        // Shallow copy the grammar nodes.
        clone.grammarNodes.addAll(grammarNodes);

        // Shallow copy the attributes.
        clone.attributes.putAll(attributes);

        return clone;
    }

    /**
     * Returns a string representation of this production and its grammar nodes.
     *
     * @return a string representation of this production.
     */
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();

        for (GrammarNode s : grammarNodes) {
            if (s instanceof GrammarLiteral) {
                buffer.append(s);
            }
            if (s instanceof GrammarRule) {
                buffer.append('<');
                buffer.append(((GrammarRule) s).getName());
                buffer.append('>');
            }
            buffer.append(' ');
        }

        // Append any attributes.
        if (!attributes.isEmpty()) {
            buffer.append("<?");
            Set<String> keys = attributes.keySet();
            int i = 0;
            for (Map.Entry<String, Object> stringObjectEntry : attributes.entrySet()) {
                if (i != 0) {
                    buffer.append(';');
                }
                buffer.append(stringObjectEntry.getKey());
                buffer.append('=');
                buffer.append(stringObjectEntry.getValue());
                i++;
            }
            buffer.append("?>");
        }

        return buffer.toString();
    }
}
