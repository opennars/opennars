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
package objenome.op.lang;

import objenome.op.Node;

/**
 * A node which chains two nodes together in sequence, called <code>SEQ2</code>
 *
 * @since 2.0
 */
public class Seq2 extends SeqN {

    public static final String IDENTIFIER = "SEQ2";

    /**
     * Constructs a <code>Seq2Function</code> with two <code>null</code>
     * children
     */
    public Seq2() {
        this(null, null);
    }

    /**
     * Constructs a <code>Seq2Function</code> with two child nodes
     *
     * @param child1 the first child node
     * @param child2 the second child node
     */
    public Seq2(Node child1, Node child2) {
        super(child1, child2);
    }

    /**
     * Returns the identifier of this function which is <code>SEQ2</code>
     *
     * @return this node's identifier
     */
    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }
}
