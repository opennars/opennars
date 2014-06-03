package nars.main_nogui;

/**
 * The parameters used when the system is invoked from command line
 */
public class CommandLineParameters {

    /**
     * Decode the silence level
     *
     * @param args Given arguments
     * @param r The corresponding reasoner
     */
    public static void decode(String[] args, ReasonerBatch r) {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ("--silence".equals(arg)) {
                arg = args[++i];
                r.getSilenceValue().set(Integer.parseInt(arg));
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
