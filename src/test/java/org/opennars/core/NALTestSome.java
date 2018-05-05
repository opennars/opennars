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
package org.opennars.core;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;


/**
 * runs a subset of the test cases, selected by the boolean include(filename) function
 */
@RunWith(Parameterized.class)
public class NALTestSome extends NALTest {

    static {
        showOutput = true;
        showSuccess = showOutput;
    }
    
   public static boolean include(String filename) {
       //return true; //filename.startsWith("nal6.8.nal");
       return filename.startsWith("nal1.0.nal");
   }

   
    @Parameterized.Parameters
    public static Collection params() {
        List l = new LinkedList();

        File folder = null;
        try {
            folder = new File(NALTestSome.class.getResource("/nal/single_step").toURI());
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Could not parse URI to nal test files.", e);
        }

        for (final File file : folder.listFiles()) {
            if (file.getName().equals("README.txt") || file.getName().contains(".png"))
                continue;
            if (include(file.getName()))
                l.add(new Object[] { file.getAbsolutePath() } );
        }
                  
        return l;
    }
   
   public static void main(String[] args) {
        org.junit.runner.JUnitCore.runClasses(NALTestSome.class);
   }    

   public NALTestSome(String scriptPath) {
       super(scriptPath);//, true);

   }

}
