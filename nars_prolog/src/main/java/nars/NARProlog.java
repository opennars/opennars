package nars;

import nars.core.NAR;
import nars.jwam.WAM;
import nars.jwam.WAMProlog;


/**
 * Wraps a Prolog instance loaded with nal.pl with some utility methods
 */
public class NARProlog extends WAMProlog  {
    
    public final NAR nar;
    
    public NARProlog(NAR n)  {
        this(n, WAM.newMedium());        
    }
    
    public NARProlog(NAR n, WAM w)  {
        super(w);
        this.nar = n;                     
    }

 
    
//    public static void main(String[] args) throws Exception {
//        NAR nar = new DefaultNARBuilder().build();
//        new TextOutput(nar, System.out);
//        
//        Prolog prolog = new NARProlog(nar);
//        prolog.solve("revision([inheritance(bird, swimmer), [1, 0.8]], [inheritance(bird, swimmer), [0, 0.5]], R).");
//        prolog.solve("inference([inheritance(swan, bird), [0.9, 0.8]], [inheritance(bird, swan), T]).");
//        
//    }

    
}
