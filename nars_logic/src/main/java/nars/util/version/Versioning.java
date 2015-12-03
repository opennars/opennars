package nars.util.version;

import nars.util.data.list.FasterList;

/** versioning context that holds versioned instances */
public class Versioning extends FasterList<Versioned> {

    private int now = 0;

    /** serial id's assigned to each Versioned */
    private int nextID = 1;


    @Override
    public String toString() {
        return now + ":" + super.toString();
    }

    public int now() {
        return now;
    }

    /** start a new version with a commit, returns current version  */
    public final int newChange(Versioned v) {
        int c = commit();
        add(v);
        return c;
    }

    /** track change on current commit, returns current version */
    public final int continueChange(Versioned v) {
        add(v);
        return now;
    }

    public final int commit() {
        return ++now;
    }


    /** reverts to previous state */
    public final void revert() {
        revert(this.now-1);
    }

    /** reverts/undo to previous state */
    public final void revert(int when) {
        final int was = this.now;
        if (was == when) return; //nothing
        else if (was < when)
            throw new RuntimeException("reverting to future time");
        now = when;

        int s = size()-1;
        if (s == -1) return; //empty

        while (get(s).revert(when)) {
            --s;
            if (s <= 0) break;
        }

        popTo(s);


    }

    /** assigns a new serial ID to a versioned item for its use as a hashcode */
    public final int track(Versioned v) {
        return nextID++;
    }
}
