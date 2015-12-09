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
 * A node which performs logical negation
 *
 * @since 2.0
 */
public class Not extends BooleanNode {

    public static final String IDENTIFIER = "NOT";

    /**
     * Constructs a <code>NotFunction</code> with one <code>null</code> child
     */
    public Not() {
        this(null);
    }

    /**
     * Constructs a <code>NotFunction</code> with one boolean child node
     *
     * @param child the first child node
     */
    public Not(Node child) {
        super(child);
    }

    /**
     * Evaluates this function. The child node is evaluated, the result of which
     * must be a <code>Boolean</code> instance. The result is negated and
     * returned as the result.
     *
     * @return <code>true</code> if the child evaluates to <code>false</code>,
     * otherwise <code>false</code>
     */
    @Override
    public Boolean evaluate() {
        return !(boolean)(getChild(0).evaluate());
    }

    /**
     * Returns the identifier of this function which is <code>NOT</code>
     *
     * @return this node's identifier
     */
    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    /**
     * Returns this function node's return type for the given child input types.
     * If there is one child with a return type of <code>Boolean</code>, then
     * the return type of this function will also be <code>Boolean</code>. In
     * all other cases this method will return <code>null</code> to indicate
     * that the inputs are invalid.
     *
     * @return the <code>Boolean</code> class or <code>null</code> if the input
     * type is invalid
     */
    @Override
    public Class dataType(Class... inputTypes) {
        return (inputTypes.length == 1) && TypeUtil.allEqual(inputTypes, Boolean.class) ? Boolean.class : null;
    }


    @Override
    public Node normalize() {
        Node c = getChild(0);
        if (c instanceof Not) {
            return c.getChild(0);
        }
        int n = getChildConstantValue(0);
        if (n == 0) return False;
        if (n == 1) return True;

        /*
        if (notExpr.equalsExpr(yep())) return nope();
        if (notExpr.equalsExpr(nope())) return yep();
        if (notExpr instanceof objenome.op.cas.Or && ((Operation) notExpr).getExprs().size() == 2
                && ((Operation) notExpr).getExpr(0) instanceof LessThan
                && ((Operation) notExpr).getExpr(1) instanceof Equals
                && ((Operation) ((Operation) notExpr).getExpr(0)).getExpr(0).equalsExpr(
                ((Operation) ((Operation) notExpr).getExpr(1)).getExpr(0))
                && ((Operation) ((Operation) notExpr).getExpr(0)).getExpr(1).equalsExpr(
                ((Operation) ((Operation) notExpr).getExpr(1)).getExpr(1)))
            return new GreaterThan(((Operation) ((Operation) notExpr).getExpr(0)).getExpr(0),
                    ((Operation) ((Operation) notExpr).getExpr(0)).getExpr(1));
        return Sum.make(Num.make(1), Product.negative(notExpr));
        */

        return this;
    }
}
