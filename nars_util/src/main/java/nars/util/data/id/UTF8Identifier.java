package nars.util.data.id;

import nars.util.utf8.Byted;

/**
 * Created by me on 6/4/15.
 */
public abstract class UTF8Identifier extends Identifier implements Byted {

    @Override
    public abstract char[] chars(boolean pretty);

    /** estimated # of characters necessary to represent this.
     *  in UTF16 this means 2 bytes per char
     */
    @Override public abstract int charsEstimated();

    @Override
    public abstract byte[] bytes();
}
