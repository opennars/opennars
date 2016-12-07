/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.io;

import nars.NAR;
import nars.config.Plugins;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author patrick.hammer
 */
public class CommandLineNARBuilder extends Plugins {
        
    List<String> filesToLoad = new ArrayList();

    public CommandLineNARBuilder(String[] args) {
        super();

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ("--silence".equals(arg)) {
                arg = args[++i];
                int sl = Integer.parseInt(arg);                
                param.noiseLevel.set(100-sl);
            }
            else if ("--noise".equals(arg)) {
                arg = args[++i];
                int sl = Integer.parseInt(arg);                
                param.noiseLevel.set(sl);
            }    
            else {
                filesToLoad.add(arg);
            }

        }        
    }

    @Override
    public NAR init(NAR n) {
        n = super.init(n); 

        for (String x : filesToLoad) {
            try {
                n.addInput( new TextInput(new File(x) ) );
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            n.run(1);
        }

        return n;
    }

    /**
     * Decode the silence level
     *
     * @param param Given argument
     * @return Whether the argument is not the silence level
     */
    public static boolean isReallyFile(String param) {
        return !"--silence".equals(param);
    }
}
