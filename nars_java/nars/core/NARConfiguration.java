package nars.core;


/** The architectural definition of a NAR.  
 *  Essentially, a read-only version of NARBuilder as seen by a NAR. */
public interface NARConfiguration {

    public int getConceptBagLevels();

    public int getConceptBagSize();

    public int getTaskLinkBagLevels();

    public int getTaskLinkBagSize();

    public int getTermLinkBagLevels();

    public int getTermLinkBagSize();
    
}

