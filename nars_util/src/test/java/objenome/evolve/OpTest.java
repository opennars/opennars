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

package objenome.evolve;

import junit.framework.TestCase;
import objenome.op.*;
import objenome.op.math.*;
import org.junit.Test;

import static org.junit.Assert.assertNotEquals;

/**
 * Unit tests for {@link org.epochx.epox.EpoxParser}
 */
public class OpTest extends TestCase {

	public void testEmpty() {

	}

	public static Doubliteral l(double v) { return new Doubliteral(v); }

	public void eq(Node n, double v) {
        Node nn = n.normalize();

		if (Double.isFinite(v)) {


			assertEquals(Doubliteral.class, nn.getClass());

			double d = (Double) (nn.evaluate());
			assertEquals(d, v, 0.001);

		}
        else {
            assertNotEquals(Doubliteral.class, nn.getClass());
            assertNotEquals(Literal.class, nn.getClass());
        }
	}

	public static VariableNode v(String name) {
		return new VariableNode(
				Variable.make(name, Double.class)
		);
	}

	@Test public void testArithmeticReduction() {

		eq(new Subtract(l(5.0), l(5.0)), 0.0);
		eq(new Subtract(l(5.0), l(4.0)), 1.0);
		eq(new Subtract(v("X"), v("X")), 0.0);
		eq(new Add(l(1.0), l(2.0)), 3.0);

        eq(new Multiply(l(1.0), l(2.0)), 2.0);
        eq(new Multiply(l(0.0), v("x")), 0.0);

        eq(new Min2(l(0.0), l(1.0)), 0.0);
        eq(new Min2(l(1.0), l(0.0)), 0.0);
        eq(new Min2(l(1.0), v("x")), Double.NaN);

        eq(new DivisionProtected(l(1.0), l(2.0)), 0.5);
        eq(new DivisionProtected(l(1.0), v("x")), Double.NaN);

        eq(new Add(l(1.0), v("a")), Double.NaN);
        eq(new Subtract(l(1.0), v("a")), Double.NaN);
        eq(new Multiply(l(1.0), v("c")), Double.NaN);
	}
}