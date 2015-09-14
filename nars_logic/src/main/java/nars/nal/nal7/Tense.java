package nars.nal.nal7;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum Tense {

    Eternal(":-:"),
    Past(":\\:"),
    Present(":|:"),
    Future(":/:");

    public static final Tense Unknown = null;

    public final String symbol;

    Tense(String string) {
        this.symbol = string;
    }

    @Override
    public String toString() {
        return symbol;
    }
    
    protected static final Map<String, Tense> stringToTense;
    
    static {
        Map<String, Tense> stt = new HashMap(Tense.values().length*2);
        for (final Tense t : Tense.values()) {
            stt.put(t.toString(), t);
        }
        stringToTense = Collections.unmodifiableMap( stt );
    }

    public static Tense tense(final String s) {
        return stringToTense.get(s);
    }

    public static String tenseRelative(long then, long now) {
        long dt = then - now;
        if (dt < 0) return "[" + dt + "]";
        else return "[+" + dt + "]";
    }
}
