package nars.util.version;

import nars.util.data.list.FasterList;

/** versioning context that holds versioned instances */
public class Versioning extends FasterList<Versionable> {

    private int now = 0;


    /** TODO stores a sorted list according to their version #, newest are last */
    //SortedList<Versioned> recent = new SortedList();



    public int now() {
        return now;
    }

    public final void onChanged(Versionable v, boolean advance) {
        add(v);
        if (advance)
            now++;
    }

    /** reverts/undo to previous state */
    public final void revert(int when) {
        now = when;
    }

    public void commit() {


    }
}
