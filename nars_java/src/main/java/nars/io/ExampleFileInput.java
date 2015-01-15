/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.io;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import nars.core.NAR;
import nars.io.condition.OutputCondition;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Access to library of examples/unit tests
 * TODO use getClass().getResource ?
 */
public class ExampleFileInput extends TextInput {

    public static String load(String path) throws IOException {
        StringBuilder  sb  = new StringBuilder();
        String line;
        File fp = new File(path);
        BufferedReader br = new BufferedReader(new FileReader(fp));
        while ((line = br.readLine())!=null) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }

    public static Iterable<String> getUnitTestPaths() {
        Collection<Object> v = getUnitTests().values();
        return Iterables.transform(v, new Function<Object, String>() {

            @Override
            public String apply(Object f) {
                return (String)((Object[])f)[0];
            }
            
        });
    }
    
    /** narsese source code, one instruction per line */
    private final String source;

    protected ExampleFileInput(String input)  {
        super(input);
        this.source = input;
    }
    
    public static ExampleFileInput get(String id) throws Exception {
        if (!id.endsWith(".nal"))
            id = id + ".nal";

        String path = getExamplePath(id);
                
        return new ExampleFileInput(load(path));
    }

    final static String cwd;

    static {
        Path currentRelativePath = Paths.get("");
        cwd = currentRelativePath.toAbsolutePath().toString();
    }

    public static String getExamplePath(String path) {
        if (path.startsWith("/")) return path; //dont modify, it's already absolute
        if (cwd.endsWith("nars_java"))
            return "../nal/" + path;
        else
            return "nal/" + path;
    }
    
    public List<OutputCondition> enableConditions(NAR n, int similarResultsToSave) {
        return OutputCondition.getConditions(n, source, similarResultsToSave);
    }
    
    public static Map<String,Object> getUnitTests() {
        Map<String,Object> l = new TreeMap();
                
        final String[] directories = new String[] { "test", "Examples/DecisionMaking", "Examples/ClassicalConditioning" };
        
        for (String dir : directories ) {

            File folder = new File(getExamplePath(dir));
            File[] files = folder.listFiles();
            if (files == null) {
                System.err.println(folder.getAbsoluteFile() + " is not a directory or does not exist");
                break;
            }

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

    public String getSource() {
        return source;
    }
    
}
