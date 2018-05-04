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

import java.util.ArrayList;
import java.util.List;
import org.opennars.storage.Distributor;
import org.junit.Test;

/**
 *
 * @author me
 */


public class DistributorAnalyzer {

    @Test public void testDistributorProbabilities() {
        
        int levels = 20;
        Distributor d = Distributor.get(levels);
        int[] count = new int[levels];
        
        double total = 0;
        for (short x : d.order) {
            count[x]++;
            total++;
        }
        
        List<Double> probability = new ArrayList(levels);
        for (int i = 0; i < levels; i++) {
            probability.add( count[i] / total);
        }
        
        List<Double> probabilityActiveAdjusted = new ArrayList(levels);
        double activeIncrease = 0.009;
        double dormantDecrease = ((0.1 * levels) * activeIncrease) / ((1.0 - 0.1) * levels);
        for (int i = 0; i < levels; i++) {
            double p = count[i] / total;
            double pd = i < ((1.0 - 0.1) * levels) ? -dormantDecrease : activeIncrease;
            
            p+=pd;
            
            probabilityActiveAdjusted.add( p );
            System.out.println((i/((double)levels)) + "\t" + p);
        }
        //System.out.println(probabilityActiveAdjusted);
        
    }

    
}
