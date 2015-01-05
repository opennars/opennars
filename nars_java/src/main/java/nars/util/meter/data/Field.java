/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.util.meter.data;

/**
 * Standard field names.
 */
public class Field {
    public static final String HITS = "hits";
    public static final String FIRST_HIT_STAMP = "firstHitStamp";
    public static final String LAST_HIT_STAMP = "lastHitStamp";
    public static final String COMMITS = "commits";
    public static final String FIRST = "first";
    public static final String LAST = "last";
    public static final String MIN = "min";
    public static final String MAX = "max";
    public static final String SUM = "sum";
    //public static final String STDEV = "stdDev";

    /**
     * DefaultField values for standard field names.
     */
    public static class DefaultField {

        public static final long HITS = 0L;
        public static final long FIRST_HIT_STAMP = -1L;
        public static final long LAST_HIT_STAMP = -1L;
        public static final long COMMITS = 0L;
        public static final double FIRST = Double.NaN;
        public static final double LAST = Double.NaN;
        public static final double MIN = Double.NaN;
        public static final double MAX = Double.NaN;
        public static final double SUM = 0D;
        //public static final Double STDEV = 0D;
    }
    
}
