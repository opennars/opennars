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
package objenome.op.bool;

import objenome.op.Node;
import objenome.util.TypeUtil;

/**
 * A node which performs logical conjunction or the boolean function of
 * <code>AND</code>
 *
 * @since 2.0
 */
public class And extends BooleanNode {

    public static final String IDENTIFIER = "AND";

    /**
     * Constructs an <code>AndFunction</code> with two <code>null</code>
     * children
     */
    public And() {
        this(null, null);
    }

    /**
     * Constructs an <code>AndFunction</code> with two <code>Boolean</code>
     * child nodes
     *
     * @param child1 the first child node
     * @param child2 the second child node
     */
    public And(Node child1, Node child2) {
        super(child1, child2);
    }

    /**
     * Evaluates this function lazily. The first child node is evaluated, the
     * result of which must be a <code>Boolean</code> instance. If the result is
     * a <code>true</code> value then the second child is also evaluated, the
     * result of which becomes the result of this <code>AND</code> function. If
     * the first child evaluated to a <code>false</code> value then the second
     * child is not evaluated at all and a <code>false</code> value is returned.
     *
     * @return <code>true</code> if both children evaluate to <code>true</code>
     * and <code>false</code> otherwise
     */
    @Override
    public Boolean evaluate() {
        boolean result = (boolean)(getChild(0).evaluate());

        if (result) {
            result = (boolean)(getChild(1).evaluate());
        }

        return result;
    }

    /**
     * Returns the identifier of this function which is <code>AND</code>
     *
     * @return this node's identifier
     */
    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    /**
     * Returns this function node's return type for the given child input types.
     * If there are two children, both of which have a return type of
     * <code>Boolean</code>, then the return type of this function will also be
     * <code>Boolean</code>. In all other cases this method will return
     * <code>null</code> to indicate that the inputs are invalid.
     *
     * @return the <code>Boolean</code> class or <code>null</code> if the input
     * type is invalid
     */
    @Override
    public Class dataType(Class... inputTypes) {
        return (inputTypes.length == 2) && TypeUtil.allEqual(inputTypes, Boolean.class) ? Boolean.class : null;
    }

    @Override
    public Node normalize() {
        Node a = getChild(0);
        Node b = getChild(1);
        if (a.equals(b)) return a;

        int an = getChildConstantValue(0);
        int bn = getChildConstantValue(1);

        if (an == 0) return False;
        if (bn == 0) return False;

        if (an == 1) return b;
        if (bn == 1) return a;

        /*
        while (exprs.remove(yep())) {}
        if (exprs.isEmpty()) return yep();
        if (exprs.size() == 1) return exprs.get(0);

        ArrayList<Expr> ors = new ArrayList<Expr>();
        for (Expr expr : exprs) {
            ors.add(objenome.op.cas.Not.make(expr));
        }
        return objenome.op.cas.Not.make(Or.make(ors));
        */

        //TODO other minifications, eX; demorgans

        return this;
    }
}
