
package nars;

public class CachedTheory {
    public enum EnumOrigin {
        FILE,
        URL
    }
    
    public CachedTheory(EnumOrigin origin, String content) {
        this.origin = origin;
        this.content = content;
    }
    
    public EnumOrigin origin;
    public String content;
}
