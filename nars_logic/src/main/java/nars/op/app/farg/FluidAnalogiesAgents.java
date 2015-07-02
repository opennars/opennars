/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */

package nars.op.app.farg;

import nars.Memory;
import nars.NAR;
import nars.bag.impl.LevelBag;
import nars.task.Task;
import nars.nal.nal8.Operation;
import nars.nal.nal8.operator.SynchOperator;
import nars.term.Term;

import java.util.List;

/**
 *
 * @author tc
 */
public class FluidAnalogiesAgents extends SynchOperator {
    public int max_codelets=100;
    public int codelet_level=100;
    Workspace ws;
    final LevelBag<Codelet,Term> coderack = new LevelBag(codelet_level,max_codelets);

    @Override
    public boolean setEnabled(NAR n, boolean enabled) {
        if(enabled) {
            if (coderack!=null)
                coderack.clear();
            ws=new Workspace(this,n);
        }
        return true;
    }

    @Override
    protected List<Task> execute(Operation operation, Memory memory) {
        return null;
    }

}
