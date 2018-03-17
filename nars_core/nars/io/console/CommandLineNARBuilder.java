package nars.io.console;

import java.util.ArrayList;
import java.util.List;
import nars.NAR;
import nars.config.Plugins;

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
                System.out.println("loading files is not part of NARS core anymore, just use a pipe");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return n;
    }

    /**
     * Decode the silence level
     *
     * @param param Given argument
     * @return Whether the argument is not the silence level
     */
    public static boolean notSilenceLevel(String param) {
        return !"--silence".equals(param);
    }
}

