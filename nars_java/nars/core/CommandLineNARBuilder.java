package nars.core;

/** Interprets command-line arguments as configuration parameters to build NAR's */
public class CommandLineNARBuilder extends DefaultNARBuilder {
    private final Param param;

    @Override public Param newParam() {        
        return param;
    }
    
    public CommandLineNARBuilder(String[] args) {
        super();
        
        param = super.newParam();
        
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ("--silence".equals(arg)) {
                arg = args[++i];
                int sl = Integer.parseInt(arg);                
                param.noiseLevel.set(100-sl);
            }
            if ("--noise".equals(arg)) {
                arg = args[++i];
                int sl = Integer.parseInt(arg);                
                param.noiseLevel.set(sl);
            }            
        }        
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
