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
package objenome.op.trig;

import objenome.op.Node;
import objenome.op.Numeric1d;

/**
 * A node which performs the trigonometric function of cosine, called COS
 *
 * @since 2.0
 */
public class Cosine extends Numeric1d  {

    public Cosine() {
    }

    public Cosine(Node child) {
        super(child);
    }


    @Override
    public double value(double x) {
        return Math.cos(x);
    }
    
    
    public static final String IDENTIFIER = "COS";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

}
