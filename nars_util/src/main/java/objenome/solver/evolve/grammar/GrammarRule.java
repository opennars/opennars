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

import java.util.ArrayList;
import java.util.List;

/**
 * A GrammarRule is a component of a grammar parse tree that matches a rule of a
 * BNF language grammar. Each rule should have one or more productions which are
 * the possible valid mappings that this rule may resolve to.
 */
public class GrammarRule implements GrammarNode, Cloneable {

    // The available options this rule may resolve to.
    private final List<GrammarProduction> productions;

    // The name of this rule without angle brackets.
    private String name;

    // Whether the rule is self-referencing.
    private boolean recursive;

    // The minimum depth required for this rule to be fully resolved.
    private int minDepth;

    /**
     * Constructs a <code>GrammarRule</code> with the specified name label and
     * the production choices.
     *
     * @param name the label that identifies this non-terminal rule.
     * @param productions a list of all the <code>GrammarProductions</code> that
     * are possible mappings for this rule.
     */
    public GrammarRule(String name, List<GrammarProduction> productions) {
        this.name = name;
        this.productions = productions;
    }

    /**
     * Constructs a <code>GrammarRule</code> with the specified name label and
     * an empty list of <code>GrammarProductions</code>. Productions can be
     * added after construction using the <code>addProduction</code> method.
     *
     * @param name the label that identifies this non-terminal rule.
     */
    public GrammarRule(String name) {
        this(name, new ArrayList<>());
    }

    /**
     * Constructs a <code>GrammarRule</code> with no specified name and an empty
     * list of <code>GrammarProductions</code>.
     */
    public GrammarRule() {
        this(null);
    }

    /**
     * Append the given production to the list of <code>GrammarProduction</code>
     * options.
     *
     * @param production the <code>GrammarProduction</code> instance to be
     * appended to this rule's list of productions.
     */
    public void addProduction(GrammarProduction production) {
        productions.add(production);
    }

    /**
     * Inserts the given production at the specified position in the list of
     * this rule's productions. The production currently at that position (if
     * any) and any subsequent productions will be shifted to the right.
     *
     * @param index the position at which the specified production is to be
     * inserted.
     * @param production the <code>GrammarProduction</code> instance to be
     * inserted.
     */
    public void addProduction(int index, GrammarProduction production) {
        productions.add(index, production);
    }

    /**
     * Replaces the <code>GrammarProduction</code> at the specified position in
     * the list of this rule's productions.
     *
     * @param index the position of the <code>GrammarProduction</code> to
     * replace.
     * @param production the <code>GrammarProduction</code> instance to be
     * stored at the specified position.
     */
    public void setProduction(int index, GrammarProduction production) {
        productions.set(index, production);
    }

    /**
     * Returns the <code>GrammarProduction</code> at the specified position in
     * this rule's list of productions.
     *
     * @param index the index of the <code>GrammarProduction</code> to return.
     * @return the <code>GrammarProduction</code> that is at the specified index
     * in the list of productions.
     */
    public GrammarProduction getProduction(int index) {
        return productions.get(index);
    }

    /**
     * Returns a list of this rule's productions.
     *
     * @return a list of this rule's productions.
     */
    public List<GrammarProduction> getProductions() {
        return productions;
    }

    /**
     * Returns the quantity of productions in this rule.
     *
     * @return the number of productions in this rule.
     */
    public int getNoProductions() {
        return productions.size();
    }

    /**
     * Returns the name of this rule, without the angle brackets.
     *
     * @return the name that references this rule.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns whether this grammar rule refers to itself directly or
     * indirectly.
     *
     * <p>
     * A rule is defined as recursive if any of the following are true:
     * <ul>
     * <li>The right hand side of the production rule contains the non-terminal
     * on the left hand side.</li>
     * <li>The right hand side of the rule contains a non-terminal which points
     * to a rule that is recursive due to any of the other two reasons.</li>
     * <li>The right hand side of the rule may contain a non-terminal that leads
     * back to the same production rule.</li>
     * </ul>
     *
     * @return true if this grammar rule is recursive, false otherwise.
     */
    public boolean isRecursive() {
        return recursive;
    }

    /**
     * Specifies whether this rule recursively refers to itself either directly
     * or indirectly.
     *
     * @param recursive whether this grammar rule recursively refers to itself.
     */
    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    /**
     * Gets the minimum depth required to resolve this rule fully to literals.
     *
     * @return the minimum depth required to resolve to terminal symbols.
     */
    public int getMinDepth() {
        return minDepth;
    }

    /**
     * Sets the minimum depth required for this rule to resolve fully to all
     * literals.
     *
     * @param minDepth the minimum depth required to get to all terminals.
     */
    public void setMinDepth(int minDepth) {
        this.minDepth = minDepth;
    }

    /**
     * Makes and returns a copy of this <code>GrammarRule</code> instance.
     */
    @Override
    public GrammarRule clone() {
        GrammarRule clone = null;
        try {
            clone = (GrammarRule) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        clone.name = name;
        clone.recursive = recursive;
        clone.minDepth = minDepth;

        // Clone the grammar productions (but this will not clone their rules).
        for (GrammarProduction p : productions) {
            clone.productions.add(p.clone());
        }

        return clone;
    }

    /**
     * Returns a string representation of this rule.
     *
     * @return a string representation of this rule.
     */
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append('<');
        buffer.append(name);
        buffer.append('>');
        buffer.append(" ::= ");
        for (int i = 0; i < productions.size(); i++) {
            if (i > 0) {
                buffer.append(" | ");
            }
            buffer.append(productions.get(i));
        }
        return buffer.toString();
    }
}
