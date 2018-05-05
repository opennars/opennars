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
/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package org.opennars.util.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.opennars.main.NAR;
import org.opennars.util.test.OutputCondition;

/**
 * Access to library of examples/unit tests
 */
public class ExampleFileInput {

    public static String load(String path) throws FileNotFoundException, IOException {
        StringBuilder  sb  = new StringBuilder();
        String line;
        File fp = new File(path);
        BufferedReader br = new BufferedReader(new FileReader(fp));
        while ((line = br.readLine())!=null) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }
    
    /** narsese source code, one instruction per line */
    private final String source;

    protected ExampleFileInput(String input) throws FileNotFoundException {
        this.source = input;
    }
    
    public static ExampleFileInput get(String id) throws Exception {
        return new ExampleFileInput(load("./nal/" + id +".nal"));
    }
    
    public List<OutputCondition> enableConditions(NAR n, int similarResultsToSave) {
        return OutputCondition.getConditions(n, source, similarResultsToSave);
    }
    
    public static Map<String,Object> getUnitTests() {
        Map<String,Object> l = new TreeMap();
        
        final String[] directories = new String[] { "/nal/single_step/", "/nal/multi_step/", "/nal/application/"  };
        
        for (String dir : directories ) {

            File folder = null;
            try {
                folder = new File(ExampleFileInput.class.getResource(dir).toURI());
            } catch (URISyntaxException e) {
                throw new IllegalStateException("Could not resolve path to nal tests in reosources.", e);
            }

            if( folder.listFiles() != null ) {
                for (final File file : folder.listFiles()) {
                    if (file.getName().equals("README.txt") || file.getName().contains(".png"))
                        continue;
                    if (!("extra".equals(file.getName()))) {
                        l.put(file.getName(), new Object[]{file.getAbsolutePath()});
                    }
                }
            }
            
        }
        return l;
    }

    public String getSource() {
        return source;
    }
    
}
