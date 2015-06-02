package nars.nal.nal3;

import nars.nal.term.Compound1;
import nars.nal.term.Term;
import nars.util.data.id.UTF8Identifier;

/**
 * Created by me on 6/2/15.
 */
abstract public class AbstractSet1<T extends Term> extends Compound1<T> implements SetTensional {

    public AbstractSet1(T the) {
        super(the);
    }

    public UTF8Identifier newName() {
        return new SetTensional.SetUTF8Identifier(this);
    }

}
