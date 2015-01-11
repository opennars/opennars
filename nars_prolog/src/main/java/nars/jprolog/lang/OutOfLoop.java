package nars.jprolog.lang;
import java.io.Serializable;
/**
 * A trail entry for out-of-loop flag.<br>
 * This <code>OutOfLoop</code> class is used in 
 * subclasses of <code>BlockPredicate</code>.<br>
 * <font color="red">This document is under construction.</font>
 *
 * @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
 * @author Naoyuki Tamura (tamura@kobe-u.ac.jp)
 * @version 1.0
 */
public class OutOfLoop implements Serializable,Undoable {
    BlockPredicate p;

    public OutOfLoop(BlockPredicate _p) {
	p = _p;
    }

    public void undo() {
	p.outOfLoop = true;
    }
}

