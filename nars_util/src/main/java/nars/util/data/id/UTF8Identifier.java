package nars.util.data.id;

/**
 * Created by me on 6/4/15.
 */
public abstract class UTF8Identifier extends Identifier {

    @Override
    public abstract char[] chars(boolean pretty);

    /** estimated # of characters necessary to represent this.
     *  in UTF16 this means 2 bytes per char
     */
    @Override public abstract int charsEstimated();

    public abstract byte[] bytes();
}
