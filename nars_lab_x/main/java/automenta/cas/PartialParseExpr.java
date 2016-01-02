package objenome.op.cas;

import java.util.HashMap;

public class PartialParseExpr {
    
    public HashMap<String, Object> hash = new HashMap<>();
    
    public PartialParseExpr() {
    }
    
    public PartialParseExpr(String name) {
        hash.put("name", name);
    }
    
    public Object put(String key, Object value) {
        return hash.put(key, value);
    }
    
    public Object get(String key) {
        return hash.get(key);
    }
    
    public String name() {
        return (String) hash.get("name");
    }
    
    public String toString() {
        return name();
    }
    
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof PartialParseExpr)) return false;
        
        return name().equals(((PartialParseExpr) obj).name());
    }
    
}
