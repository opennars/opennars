/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import nars.core.NAR;
import nars.io.condition.OutputCondition;

/**
 * Access to library of examples/unit tests
 */
public class ExampleFileInput extends TextInput {

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
    
    private final String content;

    protected ExampleFileInput(String input) throws FileNotFoundException {
        super(input);
        this.content = input;
    }
    
    public static ExampleFileInput getExample(String id) throws Exception {
        return new ExampleFileInput(load("./nal/" + id +".nal"));
    }
    
    public List<OutputCondition> getConditions(NAR n, int similarResultsToSave) {
        return OutputCondition.getConditions(n, content, similarResultsToSave);
    }
    
    public static Map<String,Object> getUnitTests() {
        Map<String,Object> l = new TreeMap();
        
        final String[] directories = new String[] { "nal/test", "nal/DecisionMaking", "nal/ClassicalConditioning" };
        
        for (String dir : directories ) {

            File folder = new File(dir);
        
            for (final File file : folder.listFiles()) {
                if (file.getName().equals("README.txt") || file.getName().contains(".png"))
                    continue;
                if(!("extra".equals(file.getName()))) {
                    l.put(file.getName(), new Object[] { file.getAbsolutePath() } );
                }
            }
            
        }
        return l;
    }
    
}
