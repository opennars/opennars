/*
 * Copyright 2007-2013
 * Licensed under GNU Lesser General Public License
 * 
 * This file is part of EpochX
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
package nars.obj.evolve.event.stat;

import junit.framework.TestCase;
import objenome.solver.evolve.GPContainer;
import objenome.solver.evolve.event.GenerationEvent;
import objenome.solver.evolve.event.stat.AbstractStat;
import objenome.solver.evolve.event.stat.RunBestFitness;

/**
 * The <code>AbstractStatTest</code> class provides unit tests for methods of
 * the {@link AbstractStat} class.
 * 
 * @see AbstractStat
 */
public class AbstractStatTest extends TestCase {

	/**
	 * Test for the {@link AbstractStat#clear()} method.
	 */
	public void testReset() {
        GPContainer config = new GPContainer();        
        
        AbstractStat<GenerationEvent.EndGeneration> a, a2, b;
        
        assertTrue(!config.contains(RunBestFitness.class));
        
        a = config.stat(new RunBestFitness());
        assertNotNull(a);
        a2 = config.stat(new RunBestFitness());
        assertNotNull(a2);                

        assertTrue("singleton accessed twice is identical", a==a2);
        
        config.resetStats();       
                
        assertNotNull(b = config.stat(new RunBestFitness()));
        
        assertTrue("Different instances as a result of resetStats removing the first", a!=b);
    }
}
