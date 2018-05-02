package nars.language;

import java.util.HashMap;
import java.util.Map;

public enum Tense {
    
    
    Past(":\\:"),
    Present(":|:"),
    Future(":/:");
    
    
    public final String symbol;

    public static final Tense Eternal = null;
    
    private Tense(String string) {
        this.symbol = string;
    }

    @Override
    public String toString() {
        return symbol;
    }
    
    protected static final Map<String, Tense> stringToTense = new HashMap(Tense.values().length * 2);
    
    static {
        for (final Tense t : Tense.values()) {
            stringToTense.put(t.toString(), t);
        }
    }

    public static Tense tense(final String s) {
        return stringToTense.get(s);
    }
    
}
