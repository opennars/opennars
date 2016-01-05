package nars.util.io;

/**
 * Created by me on 9/12/15.
 */
public enum ANSI {
    ;

    public static final String COLOR_RESET = "\u001b[0m";

    public static final String RED = "\u001b[1;31m";
    public static final String COLOR_WARNING = "\u001b[1;33m";
    public static final String COLOR_INFO = "\u001b[0;32m";
    public static final String COLOR_CONFIG = "\u001b[1;34m";
    public static final String COLOR_FINE = "\u001b[0;36m";
    public static final String COLOR_FINER = "\u001b[0;35m";
    public static final String COLOR_FINEST = "\u001b[1;30m";
}
