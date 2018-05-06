/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.opennars.core.bag;

import org.junit.Test;
import org.opennars.storage.Distributor;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author me
 */


public class DistributorAnalyzer {

    @Test public void testDistributorProbabilities() {
        
        final int levels = 20;
        final Distributor d = Distributor.get(levels);
        final int[] count = new int[levels];
        
        double total = 0;
        for (final short x : d.order) {
            count[x]++;
            total++;
        }
        
        final List<Double> probability = new ArrayList(levels);
        for (int i = 0; i < levels; i++) {
            probability.add( count[i] / total);
        }
        
        final List<Double> probabilityActiveAdjusted = new ArrayList(levels);
        final double activeIncrease = 0.009;
        final double dormantDecrease = ((0.1 * levels) * activeIncrease) / ((1.0 - 0.1) * levels);
        for (int i = 0; i < levels; i++) {
            double p = count[i] / total;
            final double pd = i < ((1.0 - 0.1) * levels) ? -dormantDecrease : activeIncrease;
            
            p+=pd;
            
            probabilityActiveAdjusted.add( p );
            System.out.println((i/((double)levels)) + "\t" + p);
        }
        //System.out.println(probabilityActiveAdjusted);
        
    }

    
}
